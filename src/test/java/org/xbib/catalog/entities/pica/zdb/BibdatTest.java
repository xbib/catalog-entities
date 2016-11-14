package org.xbib.catalog.entities.pica.zdb;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.catalog.entities.CatalogEntityBuilder;
import org.xbib.catalog.entities.CatalogEntityWorkerState;
import org.xbib.marc.Marc;
import org.xbib.marc.dialects.pica.PicaXMLContentHandler;
import org.xbib.marc.transformer.value.MarcValueTransformers;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URL;
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
    public void testPicaSetup() throws Exception {
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
        try (InputStream inputStream = getClass().getResource("zdb-oai-bib.xml").openStream();
             BibdatPicaBuilder builder = new BibdatPicaBuilder("org.xbib.catalog.entities.pica.zdb.bibdat",
                getClass().getResource("bibdat.json"))) {
            PicaXMLContentHandler contentHandler = new PicaXMLContentHandler();
            contentHandler.setFormat("Pica");
            contentHandler.setType("XML");
            contentHandler.setMarcListener(builder);
            Marc.builder()
                    .setInputStream(inputStream)
                    .setContentHandler(contentHandler)
                    .setMarcValueTransformers(marcValueTransformers)
                    .build()
                    .xmlReader().parse();
            builder.flush();
            logger.log(Level.INFO, MessageFormat.format("unmapped Bibdat OAI fields = {0}", builder.getUnmapped()));
            assertTrue(builder.getCounter().get() > 0);
        }
    }

    private class BibdatPicaBuilder extends CatalogEntityBuilder {

        BibdatPicaBuilder(String packageName, URL url) throws Exception {
            super(packageName, 1, url);
        }

        @Override
        public void afterFinishState(CatalogEntityWorkerState state) {
            counter.incrementAndGet();
        }
    }
}
