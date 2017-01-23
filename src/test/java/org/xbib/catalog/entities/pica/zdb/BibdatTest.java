package org.xbib.catalog.entities.pica.zdb;

import static org.xbib.content.rdf.RdfXContentFactory.rdfXContentBuilder;
import static org.xbib.helper.StreamMatcher.assertStream;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.catalog.entities.CatalogEntityBuilder;
import org.xbib.catalog.entities.CatalogEntityWorkerState;
import org.xbib.catalog.entities.WorkerPool;
import org.xbib.catalog.entities.WorkerPoolListener;
import org.xbib.content.rdf.RdfContentBuilder;
import org.xbib.content.rdf.RdfXContentParams;
import org.xbib.content.settings.Settings;
import org.xbib.marc.Marc;
import org.xbib.marc.MarcRecord;
import org.xbib.marc.dialects.pica.PicaXMLContentHandler;
import org.xbib.marc.transformer.value.MarcValueTransformers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.text.Normalizer;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class BibdatTest extends Assert {

    private static final Logger logger = Logger.getLogger(BibdatTest.class.getName());

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
    public void testPicaBibdatSetup() throws Exception {
        File file = File.createTempFile("bibdat-entities.", ".json");
        file.deleteOnExit();
        Settings settings = Settings.settingsBuilder()
                .put("package", "org.xbib.catalog.entities.pica.zdb.bibdat")
                .put("elements", "/org/xbib/catalog/entities/pica/zdb/bibdat.json")
                .build();
        try (BibdatPicaBuilder builder = new BibdatPicaBuilder(settings)) {
            assertFalse(builder.getEntitySpecification().getEntities().isEmpty());
            Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            builder.getEntitySpecification().dump(writer);
            writer.close();
        }
    }

    @Test
    public void testPicaBibdat() throws Exception {
        MarcValueTransformers marcValueTransformers = new MarcValueTransformers();
        marcValueTransformers.setMarcValueTransformer(value ->
                Normalizer.normalize(value, Normalizer.Form.NFKC));
        CountingPicaXMLContentHandler contentHandler = new CountingPicaXMLContentHandler();
        Settings settings = Settings.settingsBuilder()
                .put("package", "org.xbib.catalog.entities.pica.zdb.bibdat")
                .put("elements", "/org/xbib/catalog/entities/pica/zdb/bibdat.json")
                .build();
        try (InputStream inputStream = getClass().getResource("zdb-oai-bib.xml").openStream();
             BibdatPicaBuilder builder = new BibdatPicaBuilder(settings)) {
            contentHandler.setFormat("Pica");
            contentHandler.setType("XML");
            contentHandler.setMarcListener(builder);
            Marc.builder()
                    .setInputStream(inputStream)
                    .setContentHandler(contentHandler)
                    .setMarcValueTransformers(marcValueTransformers)
                    .build()
                    .xmlReader().parse();
            logger.log(Level.INFO, MessageFormat.format("unmapped Bibdat OAI fields = {0}", builder.getUnmapped()));
        }
        assertTrue(contentHandler.getCounter() > 0);
    }

    /**
     * Class for counting records outside of NatLizPicaBuilder.
     */
    private static class CountingPicaXMLContentHandler extends PicaXMLContentHandler {
        private int counter = 0;

        @Override
        public void endRecord() {
            super.endRecord();
            counter++;
        }

        int getCounter() {
            return counter;
        }
    }

    private static class BibdatPicaBuilder extends CatalogEntityBuilder {

        BibdatPicaBuilder(Settings settings) throws IOException {
            super(settings, listener);
        }

        @Override
        protected void afterFinishState(CatalogEntityWorkerState state) throws IOException {
            RdfXContentParams params = new RdfXContentParams();
            RdfContentBuilder<RdfXContentParams> builder = rdfXContentBuilder(params);
            builder.receive(state.getResource());
            String result = params.getGenerator().get();
            URL url = getClass().getResource(state.getRecordIdentifier() + ".bibdat.json");
            if (url != null) {
                InputStream inputStream = url.openStream();
                assertStream("" + state.getRecordIdentifier(),
                        inputStream,
                        new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8)));
            } else {
                fail("resource not found: '" + state.getRecordIdentifier() + ".bibdat.json'");
            }
        }
    }
}
