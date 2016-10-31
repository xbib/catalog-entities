package org.xbib.catalog.entities.mab;

import org.junit.Test;
import org.xbib.catalog.entities.CatalogEntityBuilder;
import org.xbib.catalog.entities.CatalogEntityWorkerState;
import org.xbib.content.resource.IRI;
import org.xbib.marc.Marc;
import org.xbib.content.rdf.RdfContentBuilder;
import org.xbib.content.rdf.RdfXContentParams;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.xbib.content.rdf.RdfXContentFactory.rdfXContentBuilder;

public class MabTest {

    private static final Logger logger = Logger.getLogger(MabTest.class.getName());

    @Test
    public void testSetupOfMABElements() throws Exception {
        try (MyBuilder builder = new MyBuilder(getClass().getResource("titel.json"))) {
            assertEquals(515, builder.getEntitySpecification().getMap().size());
            assertEquals(86, builder.getEntitySpecification().getEntities().size());
        }
    }

    @Test
    public void testZDBMAB() throws Exception {
        try (MyBuilder myBuilder = new MyBuilder(getClass().getResource("titel.json"))) {
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

    private class MyBuilder extends CatalogEntityBuilder {

        MyBuilder(URL url) throws Exception{
            super("org.xbib.catalog.entities.mab", url);
        }

        @Override
        public void beforeFinishState(CatalogEntityWorkerState state) {
            IRI iri = IRI.builder().scheme("http")
                    .host("dummy")
                    .query("dummy")
                    .fragment(Long.toString(counter.getAndIncrement())).build();
            try {
                state.getResource().setId(iri);
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
