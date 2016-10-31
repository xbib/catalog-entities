package org.xbib.catalog.entities.unimarc;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xbib.catalog.entities.CatalogEntityWorkerState;
import org.xbib.catalog.entities.CatalogEntityBuilder;
import org.xbib.content.resource.IRI;
import org.xbib.marc.Marc;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UnimarcTest extends Assert {

    private static final Logger logger = Logger.getLogger(UnimarcTest.class.getName());

    @Test
    public void testUnimarcSetup() throws Exception {
        try (MyBuilder myBuilder = new MyBuilder(getClass().getResource("bib.json"))) {
            StringWriter writer = new StringWriter();
            myBuilder.getEntitySpecification().dump(writer);
        }
    }

    @Test
    @Ignore
    public void testUnimarc() throws Exception {
        try (MyBuilder myBuilder = new MyBuilder(getClass().getResource("bib.json"))) {
            Marc.builder()
                    .setInputStream(getClass().getResourceAsStream("serres.mrc"))
                    .setMarcListener(myBuilder)
                    //.setMarcDataTransformer(s -> Normalizer.normalize(s, Normalizer.Form.NFKC))
                    .build()
                    .writeCollection();
            logger.log(Level.INFO, MessageFormat.format("unmapped Unimarc fields = {0}", myBuilder.getUnmapped()));
        }
    }

    private class MyBuilder extends CatalogEntityBuilder {

        MyBuilder(URL url) throws Exception{
            super("org.xbib.catalog.entities.unimarc.bib", url);
        }

        @Override
        public void beforeFinishState(CatalogEntityWorkerState state) {
            try {
                IRI iri = IRI.builder().scheme("http")
                        .host("dummy")
                        .query("dummy")
                        .fragment(Long.toString(counter.getAndIncrement())).build();
                state.getResource().setId(iri);
            } catch (IOException e) {
                //
            }
        }
    }
}
