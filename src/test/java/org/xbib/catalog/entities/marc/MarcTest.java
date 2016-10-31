package org.xbib.catalog.entities.marc;

import static org.xbib.content.rdf.RdfXContentFactory.rdfXContentBuilder;

import org.junit.Assert;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class MarcTest extends Assert {

    private static final Logger logger = Logger.getLogger(MarcTest.class.getName());

    public void testMarcSetup() throws Exception {
        try (MyBuilder builder = new MyBuilder(getClass().getResource("bib.json"))) {
            assertEquals(116, builder.getEntitySpecification().getMap().size());
            assertEquals(32, builder.getEntitySpecification().getEntities().size());
        }
    }

    @Test
    public void testStbBonn() throws Exception {
        try (MyBuilder myBuilder = new MyBuilder(getClass().getResource("bib.json"))) {
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

    private class MyBuilder extends CatalogEntityBuilder {

        MyBuilder(URL url) throws Exception {
            super("org.xbib.catalog.entities.marc.bib", url);
        }

        @Override
        public void afterFinishState(CatalogEntityWorkerState state) {
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
