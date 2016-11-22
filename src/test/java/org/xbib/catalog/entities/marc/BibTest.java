package org.xbib.catalog.entities.marc;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.xbib.content.rdf.RdfXContentFactory.rdfXContentBuilder;
import static org.xbib.content.rdf.RdfXContentFactory.routeRdfXContentBuilder;
import static org.xbib.helper.StreamMatcher.assertStream;

import org.junit.Test;
import org.xbib.catalog.entities.CatalogEntityBuilder;
import org.xbib.catalog.entities.CatalogEntityWorkerState;
import org.xbib.catalog.entities.WorkerPool;
import org.xbib.catalog.entities.WorkerPoolListener;
import org.xbib.content.rdf.RdfContentBuilder;
import org.xbib.content.rdf.RdfXContentParams;
import org.xbib.content.rdf.RouteRdfXContentParams;
import org.xbib.marc.Marc;
import org.xbib.marc.MarcRecord;
import org.xbib.marc.MarcXchangeConstants;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class BibTest {

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
    public void testBibFromMarc() throws Exception {
        try (MyBuilder myBuilder = new MyBuilder("org.xbib.catalog.entities.marc.zdb.bib",
                getClass().getResource("zdb/bib.json"), listener)) {
            Marc.builder()
                    .setInputStream(getClass().getResource("zdbtitutf8.mrc").openStream())
                    .setMarcRecordListener(myBuilder)
                    .build()
                    .writeRecords();
            logger.log(Level.INFO, MessageFormat.format("unmapped ZDB Bib fields = {0}", myBuilder.getUnmapped()));
        }
    }

    @Test
    public void testOAIFromXML() throws Exception {
        try (MyBuilder myBuilder = new MyBuilder("org.xbib.catalog.entities.marc.zdb.bib",
                getClass().getResource("zdb/bib.json"), listener)) {
            Marc.builder()
                    .setInputStream(getClass().getResource("zdb-oai-marc.xml").openStream())
                    .setFormat("MARC21")
                    .setMarcListener(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE, myBuilder)
                    .setMarcListener(MarcXchangeConstants.HOLDINGS_TYPE, myBuilder)
                    .build()
                    .xmlReader().parse();
            logger.log(Level.INFO, MessageFormat.format("unmapped ZDB OAI fields = {0}", myBuilder.getUnmapped()));
            assertTrue(myBuilder.getCounter().get() > 0);
        }
    }

    @Test
    public void testBibRecords() throws Exception {
        try (MyRouteBuilder myBuilder = new MyRouteBuilder("org.xbib.catalog.entities.marc.zdb.bib",
                getClass().getResource("zdb/bib.json"), listener)) {
            Marc.builder()
                    .setInputStream(getClass().getResource("zdbtitutf8.mrc").openStream())
                    .setMarcRecordListener(myBuilder)
                    .build()
                    .writeRecords();
            logger.log(Level.INFO, MessageFormat.format("unmapped ZDB Bib fields = {0}", myBuilder.getUnmapped()));
        }
    }

    private static class MyBuilder extends CatalogEntityBuilder {

        MyBuilder(String packageName, URL url, WorkerPoolListener<WorkerPool<MarcRecord>> listener) throws Exception {
            super(packageName, url, listener);
        }

        @Override
        protected void afterFinishState(CatalogEntityWorkerState state) {
            try {
                RdfXContentParams params = new RdfXContentParams();
                RdfContentBuilder<RdfXContentParams> builder = rdfXContentBuilder(params);
                builder.receive(state.getResource());
                String content = params.getGenerator().get();
                InputStream inputStream = getClass().getResource(state.getRecordIdentifier() + ".json").openStream();
                assertStream("" + state.getRecordIdentifier(),
                        inputStream,
                        new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
            } catch (IOException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    private static class MyRouteBuilder extends CatalogEntityBuilder {

        MyRouteBuilder(String packageName, URL url, WorkerPoolListener<WorkerPool<MarcRecord>> listener) throws Exception {
            super(packageName, url, listener);
        }

        @Override
        protected void afterFinishState(CatalogEntityWorkerState state) {
            try {
                RouteRdfXContentParams params = new RouteRdfXContentParams();
                params.setHandler((content, i) -> {
                    InputStream inputStream =
                     getClass().getResource(state.getRecordIdentifier() + ".route.json").openStream();
                    assertStream("" + state.getRecordIdentifier(),
                            inputStream,
                            new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
                });
                RdfContentBuilder<RouteRdfXContentParams> builder = routeRdfXContentBuilder(params);
                builder.receive(state.getResource());
            } catch (IOException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }
}
