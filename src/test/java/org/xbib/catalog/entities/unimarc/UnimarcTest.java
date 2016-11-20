package org.xbib.catalog.entities.unimarc;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xbib.catalog.entities.CatalogEntityBuilder;
import org.xbib.catalog.entities.WorkerPool;
import org.xbib.catalog.entities.WorkerPoolListener;
import org.xbib.marc.Marc;
import org.xbib.marc.MarcRecord;

import java.io.StringWriter;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class UnimarcTest extends Assert {

    private static final Logger logger = Logger.getLogger(UnimarcTest.class.getName());

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
    public void testUnimarcSetup() throws Exception {
        try (MyBuilder myBuilder = new MyBuilder("org.xbib.catalog.entities.unimarc.bib",
                getClass().getResource("bib.json"))) {
            StringWriter writer = new StringWriter();
            myBuilder.getEntitySpecification().dump(writer);
        }
    }

    @Test
    @Ignore
    public void testUnimarc() throws Exception {
        try (MyBuilder myBuilder = new MyBuilder("org.xbib.catalog.entities.unimarc.bib",
                getClass().getResource("bib.json"))) {
            Marc.builder()
                    .setInputStream(getClass().getResourceAsStream("serres.mrc"))
                    .setMarcListener(myBuilder)
                    .build()
                    .writeCollection();
            logger.log(Level.INFO, MessageFormat.format("unmapped Unimarc fields = {0}", myBuilder.getUnmapped()));
        }
    }

    private static class MyBuilder extends CatalogEntityBuilder {

        MyBuilder(String packageName, URL url) throws Exception {
            super(packageName, url, listener);
        }

    }
}
