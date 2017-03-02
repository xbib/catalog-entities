package org.xbib.catalog.entities.mab;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.xbib.content.rdf.RdfXContentFactory.rdfXContentBuilder;
import static org.xbib.helper.StreamMatcher.assertStream;

import org.junit.Test;
import org.xbib.catalog.entities.CatalogEntityBuilder;
import org.xbib.catalog.entities.CatalogEntityWorkerState;
import org.xbib.catalog.entities.ErrorFields;
import org.xbib.catalog.entities.ISILTransformer;
import org.xbib.catalog.entities.SkipFields;
import org.xbib.catalog.entities.TransformFields;
import org.xbib.catalog.entities.WorkerPool;
import org.xbib.catalog.entities.WorkerPoolListener;
import org.xbib.content.rdf.RdfContentBuilder;
import org.xbib.content.rdf.RdfXContentParams;
import org.xbib.content.rdf.Resource;
import org.xbib.content.settings.Settings;
import org.xbib.marc.Marc;
import org.xbib.marc.MarcRecord;
import org.xbib.marc.MarcRecordAdapter;
import org.xbib.marc.json.MarcXchangeJSONLinesReader;
import org.xbib.marc.transformer.field.MarcFieldTransformers;
import org.xbib.marc.transformer.value.MarcValueTransformers;
import org.xbib.marc.xml.MarcContentHandler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class HbzMabTest {

    private static final Logger logger = Logger.getLogger(HbzMabTest.class.getName());

    private static WorkerPoolListener<WorkerPool<MarcRecord>> listener =
            new WorkerPoolListener<WorkerPool<MarcRecord>>() {
                @Override
                public void success(WorkerPool<MarcRecord> workerPool) {
                    logger.log(Level.INFO, "success of " + workerPool + " (" + workerPool.getCounter() + " records)");
                }

                @Override
                public void failure(WorkerPool<MarcRecord> workerPool, Map<Runnable, Throwable> exceptions) {
                    logger.log(Level.SEVERE, "failure of " + workerPool + " reason " + exceptions.toString());
                    fail();
                }
            };

    @Test
    public void testSetupOfMABElements() throws IOException {
        Settings settings = Settings.settingsBuilder()
                .put("package", "org.xbib.catalog.entities.mab")
                .put("elements", "org/xbib/catalog/entities/mab/titel.json")
                .put("facets", "org/xbib/catalog/entities/mab/facets.json")
                .build();
        try (MyHbzBuilder builder = new MyHbzBuilder(settings)) {
            // update these values if you extend MAB specification
            assertEquals(774, builder.getEntitySpecification().getMap().size());
            assertEquals(96, builder.getEntitySpecification().getEntities().size());
        }
    }

    @Test
    public void testHbzPublishing() throws IOException {
        Settings settings = Settings.settingsBuilder()
                .put("package", "org.xbib.catalog.entities.mab")
                .put("elements", "org/xbib/catalog/entities/mab/titel.json")
                .put("facets", "org/xbib/catalog/entities/mab/facets.json")
                .put("transform2isil", "org/xbib/catalog/entities/mab/transform2isil.json")
                .put("error_fields", "org/xbib/catalog/entities/mab/error_fields.json")
                .put("skip_fields", "org/xbib/catalog/entities/mab/skip_fields.json")
                .put("skip_subfields", "org/xbib/catalog/entities/mab/skip_subfields.json")
                .put("transform_fields", "org/xbib/catalog/entities/mab/transform_fields.json")
                .put("transform_subfields", "org/xbib/catalog/entities/mab/transform_subfields.json")
                .put("transform_subfields_tail", "org/xbib/catalog/entities/mab/transform_subfields_tail.json")
                .put("field_mapping_source", "org/xbib/catalog/entities/mab/rak2rda.json")
                .put("field_mapping_target", "org/xbib/catalog/entities/mab/imd.json")
                .putArray("field_mapping_target_keys", Arrays.asList("rda.carrier", "rda.content", "rda.media"))
                .build();

        try (MyHbzBuilder myBuilder = new MyHbzBuilder(settings);
              BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                getClass().getResource( "hbz/sysids.txt").openStream(), StandardCharsets.UTF_8))) {
            // hbz Publishing is not configured correctly, this custom handler adapts to wrong namespace
            MarcContentHandler contentHandler = new MarcContentHandler();
            contentHandler.addNamespace("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd");
            contentHandler.setFormat("AlephXML");
            contentHandler.setType("Bibliographic");
            contentHandler.setMarcListener(new MarcRecordAdapter(myBuilder));

            // set up field transformers
            final MarcFieldTransformers marcFieldTransformers = new MarcFieldTransformers();
            ErrorFields errorFields = new ErrorFields(settings);
            errorFields.createFieldTransformers(marcFieldTransformers);
            SkipFields skipFields = new SkipFields(settings);
            skipFields.createFieldTransformers(marcFieldTransformers);
            TransformFields transformFields = new TransformFields(settings);
            transformFields.createTransformerFields(marcFieldTransformers);
            transformFields.createTransformerSubfields(marcFieldTransformers);
            transformFields.createTransformerSubfieldsTail(marcFieldTransformers);

            // set up field value transformers
            final MarcValueTransformers marcValueTransformers = new MarcValueTransformers();
            ISILTransformer isilTransformer = new ISILTransformer(settings);
            isilTransformer.createTransformer(settings, marcValueTransformers);

            bufferedReader.lines().forEach( line -> {
                    try (InputStream inputStream = getClass().getResource("hbz/" + line + ".xml").openStream()) {
                        Marc.builder()
                                .setInputStream(inputStream)
                                .setContentHandler(contentHandler)
                                .setMarcFieldTransformers(marcFieldTransformers)
                                .setMarcValueTransformers(marcValueTransformers)
                                .build()
                                .xmlReader().parse();
                    } catch (IOException e)  {
                        logger.log(Level.SEVERE, e.getMessage(), e);
                    }
            });
            logger.log(Level.INFO, MessageFormat.format("count fields = {0}",
                    myBuilder.getCounter()));
            logger.log(Level.INFO, MessageFormat.format("checksum fields = {0}",
                    myBuilder.getChecksum()));
            logger.log(Level.INFO, MessageFormat.format("mapped fields = {0}",
                    myBuilder.getMapped()));
            logger.log(Level.INFO, MessageFormat.format("unmapped fields = {0}",
                    myBuilder.getUnmapped()));
            logger.log(Level.INFO, MessageFormat.format("invalid fields = {0}",
                    myBuilder.getInvalid()));
        }
    }

    @Test
    public void testHbzJsonLines() throws IOException {
        Settings settings = Settings.settingsBuilder()
                .put("package", "org.xbib.catalog.entities.mab")
                .put("elements", "org/xbib/catalog/entities/mab/titel.json")
                .put("facets", "org/xbib/catalog/entities/mab/facets.json")
                .build();
        try (MyHbzBuilder myBuilder = new MyHbzBuilder(settings);
             InputStream inputStream = getClass().getResource( "hbz/DE-605-aleph.jsonl").openStream();
             MarcXchangeJSONLinesReader reader = new MarcXchangeJSONLinesReader(inputStream, myBuilder)) {
            reader.parse();
            logger.log(Level.INFO, MessageFormat.format("count fields = {0}",
                    myBuilder.getCounter()));
            logger.log(Level.INFO, MessageFormat.format("checksum fields = {0}",
                    myBuilder.getChecksum()));
            logger.log(Level.INFO, MessageFormat.format("mapped fields = {0}",
                    myBuilder.getMapped()));
            logger.log(Level.INFO, MessageFormat.format("unmapped fields = {0}",
                    myBuilder.getUnmapped()));
            logger.log(Level.INFO, MessageFormat.format("invalid fields = {0}",
                    myBuilder.getInvalid()));
        }
    }

    private static class MyHbzBuilder extends CatalogEntityBuilder {

        MyHbzBuilder(Settings settings) throws IOException {
            super(settings, listener);
        }

        @Override
        protected void afterFinishState(CatalogEntityWorkerState state) throws IOException {
            RdfXContentParams params = new RdfXContentParams();
            RdfContentBuilder<RdfXContentParams> builder = rdfXContentBuilder(params);
            builder.receive(state.getResource());
            String string = params.getGenerator().get();

            /*try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(state.getRecordIdentifier() + ".json"))) {
                writer.write(string);
            }*/
            assertStream(state.getRecordIdentifier(),
                    getClass().getResource("hbz/" + state.getRecordIdentifier() + ".json").openStream(),
                    new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)));
            Iterator<Resource> it = state.getResourceIterator();
            while (it.hasNext()) {
                Resource resource = it.next();
                if (resource.equals(state.getResource())) {
                    // skip main resource
                    continue;
                }
                builder = rdfXContentBuilder(params);
                resource.newResource("xbib").add("uid", state.getIdentifier());
                resource.newResource("xbib").add("uid", state.getRecordIdentifier());
                builder.receive(resource);
                string = params.getGenerator().get();
                String name = URLEncoder.encode(state.getRecordIdentifier() + resource.id(), "UTF-8");
                /*
                try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(name + ".hol.json"))) {
                    writer.write(string);
                }*/
                assertStream(state.getRecordIdentifier(),
                        getClass().getResource("hbz/" + name + ".hol.json").openStream(),
                        new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)));
            }
        }
    }
}
