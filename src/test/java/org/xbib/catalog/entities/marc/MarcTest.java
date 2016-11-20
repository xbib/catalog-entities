package org.xbib.catalog.entities.marc;

import static org.xbib.content.rdf.RdfXContentFactory.rdfXContentBuilder;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.catalog.entities.CatalogEntityBuilder;
import org.xbib.catalog.entities.CatalogEntityWorkerState;
import org.xbib.catalog.entities.WorkerPool;
import org.xbib.catalog.entities.WorkerPoolListener;
import org.xbib.content.rdf.RdfContentBuilder;
import org.xbib.content.rdf.RdfXContentParams;
import org.xbib.marc.Marc;
import org.xbib.marc.MarcRecord;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class MarcTest extends Assert {

    private static final Logger logger = Logger.getLogger(MarcTest.class.getName());

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
    public void testMarcSetup() throws Exception {
        // do nothing, just set up and finish
        try (MyBuilder builder = new MyBuilder("org.xbib.catalog.entities.marc.bib",
                getClass().getResource("bib.json"))) {
            assertEquals(116, builder.getEntitySpecification().getMap().size());
            assertEquals(32, builder.getEntitySpecification().getEntities().size());
        }
    }

    @Test
    public void testStbBonn() throws Exception {
        try (MyBuilder myBuilder = new MyBuilder("org.xbib.catalog.entities.marc.bib",
                getClass().getResource("bib.json"))) {
            Marc.builder()
                    .setInputStream(getClass().getResource("stb-bonn.mrc").openStream())
                    .setMarcListener(myBuilder)
                    .build()
                    .writeCollection();
            logger.log(Level.INFO, MessageFormat.format("mapped StB Bonn fields = {0}",
                    myBuilder.getMapped()));
            logger.log(Level.INFO, MessageFormat.format("unmapped StB Bonn fields = {0}",
                    myBuilder.getUnmapped()));
            logger.log(Level.INFO, MessageFormat.format("invalid StB Bonn fields = {0}",
                    myBuilder.getInvalid()));
        }
    }

    private static class MyBuilder extends CatalogEntityBuilder {

        MyBuilder(String packageName, URL url) throws Exception {
            super(packageName, url, listener);
        }

        @Override
        protected void afterFinishState(CatalogEntityWorkerState state) {
            try {
                RdfXContentParams params = new RdfXContentParams();
                RdfContentBuilder<RdfXContentParams> builder = rdfXContentBuilder(params);
                builder.receive(state.getResource());
                String result = params.getGenerator().get();
                //logger.info("rdf="+result);
            } catch (IOException e) {
                // ignore
            }
        }
    }
}
