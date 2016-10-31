package org.xbib.catalog.entities.marc;

import static org.xbib.content.rdf.RdfXContentFactory.rdfXContentBuilder;

import org.junit.Test;
import org.xbib.catalog.entities.CatalogEntityBuilder;
import org.xbib.catalog.entities.CatalogEntityWorkerState;
import org.xbib.content.rdf.RdfContentBuilder;
import org.xbib.content.rdf.RdfXContentParams;
import org.xbib.content.resource.IRI;
import org.xbib.marc.Marc;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class HolTest {

    private static final Logger logger = Logger.getLogger(BibTest.class.getName());

    @Test
    public void testHol() throws Exception {
        try (MyBuilder myBuilder = new MyBuilder(getClass().getResource("zdb/hol.json"))) {
            Marc.builder()
                    .setInputStream(getClass().getResource("zdblokutf8.mrc").openStream())
                    .setMarcRecordListener(myBuilder)
                    .build()
                    .writeRecords();
            logger.log(Level.INFO, MessageFormat.format("unmapped ZDB Hol fields = {0}", myBuilder.getUnmapped()));
        }
    }

    private class MyBuilder extends CatalogEntityBuilder {

        MyBuilder(URL url) throws Exception {
            super("org.xbib.catalog.entities.marc.zdb.hol", url);
        }

        @Override
        public void afterFinishState(CatalogEntityWorkerState state) {
            try {
                IRI iri = IRI.builder().scheme("http")
                        .host("zdb")
                        .query("holdings")
                        .fragment(Long.toString(counter.getAndIncrement())).build();
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
