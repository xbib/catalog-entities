package org.xbib.catalog.entities.pica;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.catalog.entities.CatalogEntityBuilder;
import org.xbib.catalog.entities.WorkerPool;
import org.xbib.catalog.entities.WorkerPoolListener;
import org.xbib.marc.Marc;
import org.xbib.marc.MarcRecord;
import org.xbib.marc.dialects.pica.PicaXMLContentHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class NatLizPicaTest extends Assert {

    private static final Logger logger = Logger.getLogger(NatLizPicaTest.class.getName());

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
    public void testNatLizPicaSetup() throws Exception {
        File file = File.createTempFile("natliz-pica-bib-entities.", ".json");
        file.deleteOnExit();
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
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

    private static class NatLizPicaBuilder extends CatalogEntityBuilder {

        NatLizPicaBuilder(String packageName, URL url) throws Exception {
            super(packageName, 1, url, listener);
        }
    }
}
