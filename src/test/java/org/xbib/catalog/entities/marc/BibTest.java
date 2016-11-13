package org.xbib.catalog.entities.marc;

import static org.junit.Assert.assertTrue;
import static org.xbib.content.rdf.RdfXContentFactory.rdfXContentBuilder;
import static org.xbib.content.rdf.RdfXContentFactory.routeRdfXContentBuilder;

import org.junit.Test;
import org.xbib.catalog.entities.CatalogEntityBuilder;
import org.xbib.catalog.entities.CatalogEntityWorkerState;
import org.xbib.content.rdf.RdfContentBuilder;
import org.xbib.content.rdf.RdfXContentParams;
import org.xbib.content.rdf.RouteRdfXContentParams;
import org.xbib.content.resource.IRI;
import org.xbib.marc.Marc;
import org.xbib.marc.MarcXchangeConstants;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class BibTest {

    private static final Logger logger = Logger.getLogger(BibTest.class.getName());

    @Test
    public void testBib() throws Exception {
        try (MyBuilder myBuilder = new MyBuilder(getClass().getResource("bib.json"))) {
            Marc.builder()
                    .setInputStream(getClass().getResource("zdbtitutf8.mrc").openStream())
                    .setMarcRecordListener(myBuilder)
                    .build()
                    .writeRecords();
            logger.log(Level.INFO, MessageFormat.format("unmapped ZDB Bib fields = {0}", myBuilder.getUnmapped()));
        }
    }

    @Test
    public void testOAI() throws Exception {
        try (MyBuilder myBuilder = new MyBuilder(getClass().getResource("bib.json"))) {
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
    public void testBibToRoute() throws Exception {
        try (MyRouteBuilder myBuilder = new MyRouteBuilder(getClass().getResource("bib.json"))) {
            Marc.builder()
                    .setInputStream(getClass().getResource("zdbtitutf8.mrc").openStream())
                    .setMarcRecordListener(myBuilder)
                    .build()
                    .writeRecords();
            logger.log(Level.INFO, MessageFormat.format("unmapped ZDB Bib fields = {0}", myBuilder.getUnmapped()));
            assertTrue(myBuilder.getCount() > 0);
        }
    }

    private class MyBuilder extends CatalogEntityBuilder {

        MyBuilder(URL url) throws Exception {
            super("org.xbib.catalog.entities.marc.zdb.bib", url);
        }

        @Override
        public void afterFinishState(CatalogEntityWorkerState state) {
            try {
                IRI iri = IRI.builder().scheme("http")
                        .host("zdb")
                        .query("title")
                        .fragment(Long.toString(counter.getAndIncrement())).build();
                state.getResource().setId(iri);
                RdfXContentParams params = new RdfXContentParams();
                RdfContentBuilder<RdfXContentParams> builder = rdfXContentBuilder(params);
                builder.receive(state.getResource());
                String result = params.getGenerator().get();
                //logger.info("rdf=" + result);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private class MyRouteBuilder extends CatalogEntityBuilder {

        final AtomicInteger counter = new AtomicInteger();

        MyRouteBuilder(URL url) throws Exception {
            super("org.xbib.catalog.entities.marc.zdb.bib", url);
        }

        @Override
        public void afterFinishState(CatalogEntityWorkerState state) {
            try {
                IRI iri = IRI.builder().scheme("http")
                        .host("zdb")
                        .query("title")
                        .fragment(Long.toString(counter.getAndIncrement())).build();
                state.getResource().setId(iri);
                RouteRdfXContentParams params = new RouteRdfXContentParams();
                // testing the setHandler() method
                params.setHandler((content, i) -> {
                    logger.log(Level.INFO, content);
                    counter.incrementAndGet();
                });
                RdfContentBuilder<RouteRdfXContentParams> builder = routeRdfXContentBuilder(params);
                builder.receive(state.getResource());
            } catch (IOException e) {
                // ignore
            }
        }

        public int getCount() {
            return counter.get();
        }
    }
}
