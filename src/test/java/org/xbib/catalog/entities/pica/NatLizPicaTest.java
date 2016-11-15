package org.xbib.catalog.entities.pica;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.catalog.entities.CatalogEntityBuilder;
import org.xbib.marc.Marc;
import org.xbib.marc.dialects.pica.PicaXMLContentHandler;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class NatLizPicaTest extends Assert {

    private static final Logger logger = Logger.getLogger(NatLizPicaTest.class.getName());

    @Test
    public void testNatLizPicaSetup() throws Exception {
        File file = File.createTempFile("natliz-pica-bib-entities.", ".json");
        file.deleteOnExit();
        try (FileWriter writer = new FileWriter(file);
             NatLizPicaBuilder builder = new NatLizPicaBuilder("org.xbib.catalog.entities.pica.natliz.bib",
                getClass().getResource("bib.json"))) {
            assertFalse(builder.getEntitySpecification().getEntities().isEmpty());
            builder.getEntitySpecification().dump(writer);
        }
    }

    @Test
    public void testNatLizPica() throws Exception {
        CountingPicaXMLContentHandler contentHandler = new CountingPicaXMLContentHandler();
        try (InputStream inputStream = getClass().getResource("natliz.xml").openStream();
                NatLizPicaBuilder builder = new NatLizPicaBuilder("org.xbib.catalog.entities.pica.natliz.bib",
                getClass().getResource("bib.json"))) {
            contentHandler.addNamespace("info:srw/schema/5/picaXML-v1.0");
            contentHandler.setMarcListener(builder);
            Marc.builder()
                    .setInputStream(inputStream)
                    .setContentHandler(contentHandler)
                    .build()
                    .xmlReader().parse();
            logger.log(Level.INFO, MessageFormat.format("parser done. unmapped natliz fields = {0}", builder.getUnmapped()));
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

    private class NatLizPicaBuilder extends CatalogEntityBuilder {

        NatLizPicaBuilder(String packageName, URL url) throws Exception {
            super(packageName, 1, url);
        }
    }
}
