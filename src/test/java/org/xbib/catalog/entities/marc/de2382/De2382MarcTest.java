package org.xbib.catalog.entities.marc.de2382;

import static org.junit.Assert.fail;
import static org.xbib.content.rdf.RdfXContentFactory.rdfXContentBuilder;
import static org.xbib.helper.StreamMatcher.assertStream;

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

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class De2382MarcTest {

    private static final Logger logger = Logger.getLogger(De2382MarcTest.class.getName());

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
    public void testCatpfile() throws IOException {
        Settings settings = Settings.settingsBuilder()
                .put("package", "org.xbib.catalog.entities.marc.bib")
                .put("elements", "org/xbib/catalog/entities/marc/bib.json")
                .build();
        try (MyBuilder myBuilder = new MyBuilder(settings)) {
            Marc.builder()
                    .setInputStream(getClass().getResource("test-catpfile.book.marc21").openStream())
                    .setMarcListener(myBuilder)
                    .build()
                    .writeCollection();
            logger.log(Level.INFO, MessageFormat.format("mapped fields = {0}",
                    myBuilder.getMapped()));
            logger.log(Level.INFO, MessageFormat.format("unmapped fields = {0}",
                    myBuilder.getUnmapped()));
            logger.log(Level.INFO, MessageFormat.format("invalid fields = {0}",
                    myBuilder.getInvalid()));
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
            if (content != null) {
                /*try (BufferedWriter writer = Files.newBufferedWriter( Paths.get(state.getRecordIdentifier() + ".json"))) {
                    writer.write(content);
                }*/
                assertStream("" + state.getRecordIdentifier(),
                        getClass().getResource(state.getRecordIdentifier() + ".json").openStream(),
                        new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
            }
        }
    }
}
