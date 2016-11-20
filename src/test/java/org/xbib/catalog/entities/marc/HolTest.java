package org.xbib.catalog.entities.marc;

import static org.junit.Assert.fail;
import static org.xbib.content.rdf.RdfXContentFactory.rdfXContentBuilder;

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
public class HolTest {

    private static final Logger logger = Logger.getLogger(BibTest.class.getName());

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
    public void testHol() throws Exception {
        try (MyBuilder myBuilder = new MyBuilder("org.xbib.catalog.entities.marc.zdb.hol",
                getClass().getResource("zdb/hol.json"))) {
            Marc.builder()
                    .setInputStream(getClass().getResource("zdblokutf8.mrc").openStream())
                    .setMarcRecordListener(myBuilder)
                    .build()
                    .writeRecords();
            logger.log(Level.INFO, MessageFormat.format("unmapped ZDB Hol fields = {0}", myBuilder.getUnmapped()));
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
