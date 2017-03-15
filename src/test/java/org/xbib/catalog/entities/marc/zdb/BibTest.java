package org.xbib.catalog.entities.marc.zdb;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.xbib.content.rdf.RdfXContentFactory.rdfXContentBuilder;
import static org.xbib.content.rdf.RdfXContentFactory.routeRdfXContentBuilder;
import static org.xbib.helper.StreamMatcher.assertStream;

import org.junit.Ignore;
import org.junit.Test;
import org.xbib.catalog.entities.CatalogEntityBuilder;
import org.xbib.catalog.entities.CatalogEntityWorkerState;
import org.xbib.catalog.entities.WorkerPool;
import org.xbib.catalog.entities.WorkerPoolListener;
import org.xbib.content.rdf.RdfContentBuilder;
import org.xbib.content.rdf.RdfXContentParams;
import org.xbib.content.rdf.RouteRdfXContentParams;
import org.xbib.content.settings.Settings;
import org.xbib.marc.Marc;
import org.xbib.marc.MarcRecord;
import org.xbib.marc.MarcXchangeConstants;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

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
    public void testBibFromMarc() throws IOException {
        Settings settings = Settings.settingsBuilder()
                .put("package", "org.xbib.catalog.entities.marc.bib")
                .put("elements", "org/xbib/catalog/entities/marc/bib.json")
                .build();
        try (MyBuilder myBuilder = new MyBuilder(settings)) {
            Marc.builder()
                    .setInputStream(getClass().getResource("zdbtitutf8.mrc").openStream())
                    .setMarcRecordListener(myBuilder)
                    .build()
                    .writeRecords();
            logger.log(Level.INFO, MessageFormat.format("unmapped ZDB Bib fields = {0}", myBuilder.getUnmapped()));
        }
    }

    @Test
    public void testOAIFromXML() throws IOException {
        Settings settings = Settings.settingsBuilder()
                .put("package", "org.xbib.catalog.entities.marc.bib")
                .put("elements", "org/xbib/catalog/entities/marc/bib.json")
                .build();
        try (MyBuilder myBuilder = new MyBuilder(settings)) {
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
    public void testBibRecords() throws IOException {
        Map<String, String> facets = new HashMap<>();
        facets.put("facets.dc.type", "type");
        facets.put("facets.dc.date", "GeneralInformation.date1");
        facets.put("facets.dc.format", "GeneralInformation.formOfItem");
        facets.put("facets.dc.language", "GeneralInformation.language");
        facets.put("facets.dc.coverage", "GeneralInformation.place");
        Settings settings = Settings.settingsBuilder()
                .put("package", "org.xbib.catalog.entities.marc.bib")
                .put("elements", "org/xbib/catalog/entities/marc/bib.json")
                .put(facets)
                .build();
        try (MyRouteBuilder myBuilder = new MyRouteBuilder(settings)) {
            Marc.builder()
                    .setInputStream(getClass().getResource("zdbtitutf8.mrc").openStream())
                    .setMarcRecordListener(myBuilder)
                    .build()
                    .writeRecords();
            logger.log(Level.INFO, MessageFormat.format("unmapped ZDB Bib fields = {0}", myBuilder.getUnmapped()));
        }
    }

    @Test
    @Ignore
    public void testAllRecords() throws IOException {
        Settings settings = Settings.settingsBuilder()
                .put("package", "org.xbib.catalog.entities.marc.bib")
                .put("elements", "org/xbib/catalog/entities/marc/bib.json")
                .build();
        try (MySimpleBuilder myBuilder = new MySimpleBuilder(settings)) {
            Marc.builder()
                    .setInputStream(new GZIPInputStream(
                            Files.newInputStream(Paths.get("/data/zdb/1609zdbtitgesamtutf8.mrc.gz"))))
                    .setMarcRecordListener(myBuilder)
                    .build()
                    .writeRecords();
            logger.log(Level.INFO, MessageFormat.format("unmapped ZDB Bib fields = {0}", myBuilder.getUnmapped()));
        }
    }

    private static class MySimpleBuilder extends CatalogEntityBuilder {

        MySimpleBuilder(Settings settings) throws IOException {
            super(settings, listener);
        }

        @Override
        protected void afterFinishState(CatalogEntityWorkerState state) throws IOException {
            RdfXContentParams params = new RdfXContentParams();
            RdfContentBuilder<RdfXContentParams> builder = rdfXContentBuilder(params);
            builder.receive(state.getResource());
        }
    }

    private static class MyBuilder extends CatalogEntityBuilder {

        MyBuilder(Settings settings) throws IOException {
            super(settings, listener);
        }

        @Override
        protected void afterFinishState(CatalogEntityWorkerState state) throws IOException {
            RdfXContentParams params = new RdfXContentParams();
            RdfContentBuilder<RdfXContentParams> builder = rdfXContentBuilder(params);
            builder.receive(state.getResource());
            String content = params.getGenerator().get();
            /*try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(state.getRecordIdentifier() + ".json"))) {
                writer.write(content);
            }*/
            assertStream("" + state.getRecordIdentifier(),
                    getClass().getResource(state.getRecordIdentifier() + ".json").openStream(),
                    new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
        }
    }

    private static class MyRouteBuilder extends CatalogEntityBuilder {

        MyRouteBuilder(Settings settings) throws IOException {
            super(settings, listener);
        }

        @Override
        protected void afterFinishState(CatalogEntityWorkerState state) throws IOException {
            RouteRdfXContentParams params = new RouteRdfXContentParams();
            params.setHandler((content, i) -> {
                /*try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(state.getRecordIdentifier() + ".route.json"))) {
                    writer.write(content);
                }*/
                assertStream("" + state.getRecordIdentifier(),
                        getClass().getResource(state.getRecordIdentifier() + ".route.json").openStream(),
                        new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
            });
            RdfContentBuilder<RouteRdfXContentParams> builder = routeRdfXContentBuilder(params);
            builder.receive(state.getResource());
        }
    }
}
