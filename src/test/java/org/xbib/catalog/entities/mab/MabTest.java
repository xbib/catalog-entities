package org.xbib.catalog.entities.mab;

import static org.junit.Assert.assertEquals;
import static org.xbib.content.rdf.RdfXContentFactory.rdfXContentBuilder;

import org.junit.Test;
import org.xbib.catalog.entities.CatalogEntityBuilder;
import org.xbib.catalog.entities.CatalogEntityWorkerState;
import org.xbib.content.rdf.RdfContentBuilder;
import org.xbib.content.rdf.RdfXContentParams;
import org.xbib.marc.Marc;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class MabTest {

    private static final Logger logger = Logger.getLogger(MabTest.class.getName());

    @Test
    public void testSetupOfMABElements() throws Exception {
        try (MyBuilder builder = new MyBuilder("org.xbib.catalog.entities.mab",
                getClass().getResource("titel.json"))) {
            // update these values if you extend MAB specification
            assertEquals(558, builder.getEntitySpecification().getMap().size());
            assertEquals(95, builder.getEntitySpecification().getEntities().size());
        }
    }

    @Test
    public void testZDBMAB() throws Exception {
        try (MyBuilder myBuilder = new MyBuilder("org.xbib.catalog.entities.mab",
                getClass().getResource("titel.json"))) {
            Marc.builder()
                    .setInputStream(getClass().getResource("1217zdbtit.dat").openStream())
                    .setCharset(Charset.forName("x-MAB"))
                    .setRecordLabelFixer(recordLabel ->
                            org.xbib.marc.label.RecordLabel.builder().from(recordLabel).setSubfieldIdentifierLength(0).build())
                    .setMarcRecordListener(myBuilder)
                    .build()
                    .writeRecords();
            logger.log(Level.INFO, MessageFormat.format("count ZDB MAB fields = {0}",
                    myBuilder.getCounter()));
            logger.log(Level.INFO, MessageFormat.format("checksum ZDB MAB fields = {0}",
                    myBuilder.getChecksum()));
            logger.log(Level.INFO, MessageFormat.format("mapped ZDB MAB fields = {0}",
                    myBuilder.getMapped()));
            logger.log(Level.INFO, MessageFormat.format("unmapped ZDB MAB fields = {0}",
                    myBuilder.getUnmapped()));
            logger.log(Level.INFO, MessageFormat.format("invalid ZDB MAB fields = {0}",
                    myBuilder.getInvalid()));
        }
    }

    private static class MyBuilder extends CatalogEntityBuilder {

        MyBuilder(String packageName, URL url) throws Exception {
            super(packageName, url);
        }

        @Override
        protected void beforeFinishState(CatalogEntityWorkerState state) {
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
