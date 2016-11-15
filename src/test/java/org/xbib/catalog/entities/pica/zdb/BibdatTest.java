package org.xbib.catalog.entities.pica.zdb;

import static org.xbib.content.rdf.RdfXContentFactory.rdfXContentBuilder;
import static org.xbib.helper.StreamMatcher.assertStream;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.catalog.entities.CatalogEntityBuilder;
import org.xbib.catalog.entities.CatalogEntityWorkerState;
import org.xbib.content.rdf.RdfContentBuilder;
import org.xbib.content.rdf.RdfXContentParams;
import org.xbib.marc.Marc;
import org.xbib.marc.dialects.pica.PicaXMLContentHandler;
import org.xbib.marc.transformer.value.MarcValueTransformers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.text.Normalizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class BibdatTest extends Assert {

    private static final Logger logger = Logger.getLogger(BibdatTest.class.getName());

    @Test
    public void testPicaBibdatSetup() throws Exception {
        File file = File.createTempFile("bibdat-entities.", ".json");
        file.deleteOnExit();
        try (BibdatPicaBuilder builder = new BibdatPicaBuilder("org.xbib.catalog.entities.pica.zdb.bibdat",
                getClass().getResource("bibdat.json"))) {
            assertFalse(builder.getEntitySpecification().getEntities().isEmpty());
            FileWriter writer = new FileWriter(file);
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
        try (InputStream inputStream = getClass().getResource("zdb-oai-bib.xml").openStream();
             BibdatPicaBuilder builder = new BibdatPicaBuilder("org.xbib.catalog.entities.pica.zdb.bibdat",
                getClass().getResource("bibdat.json"))) {
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
    private class CountingPicaXMLContentHandler extends PicaXMLContentHandler {
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

    private class BibdatPicaBuilder extends CatalogEntityBuilder {

        BibdatPicaBuilder(String packageName, URL url) throws Exception {
            super(packageName, 1, url);
        }

        @Override
        public void afterFinishState(CatalogEntityWorkerState state) {
            counter.incrementAndGet();
            try {
                RdfXContentParams params = new RdfXContentParams();
                RdfContentBuilder<RdfXContentParams> builder = rdfXContentBuilder(params);
                builder.receive(state.getResource());
                String result = params.getGenerator().get();
                InputStream inputStream = getClass().getResource(state.getRecordIdentifier() + ".bibdat.json").openStream();
                assertStream("" + state.getRecordIdentifier(),
                        inputStream,
                        new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8)));
            } catch (IOException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }
}
