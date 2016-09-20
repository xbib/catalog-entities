package org.xbib.marc.json;

import static org.junit.Assert.assertEquals;
import static org.xbib.helper.StreamTester.assertStream;
import static org.xbib.rdf.content.RdfXContentFactory.rdfXContentBuilder;

import org.junit.Test;
import org.xbib.catalog.entities.CatalogEntityBuilder;
import org.xbib.catalog.entities.CatalogEntityWorkerState;
import org.xbib.iri.IRI;
import org.xbib.marc.Marc;
import org.xbib.marc.MarcField;
import org.xbib.marc.MarcListener;
import org.xbib.marc.MarcRecordAdapter;
import org.xbib.marc.xml.MarcContentHandler;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.Resource;
import org.xbib.rdf.content.RdfXContentParams;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonLinesTest {

    private static final Logger logger = Logger.getLogger(JsonLinesTest.class.getName());


    @Test
    public void testHBZ() throws Exception {

        InputStream in = getClass().getResource("aleph-publish.xml").openStream();
        StringWriter sw = new StringWriter();

        try (MarcJsonWriter writer = new MarcJsonWriter(sw)) {
            MarcContentHandler contentHandler = new MarcContentHandler();
            contentHandler.addNamespace("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd");
            contentHandler.setFormat("MARC21");
            contentHandler.setType("Bibliographic");
            contentHandler.setMarcListener(new MarcRecordAdapter(writer));
            Marc.builder()
                    .setContentHandler(contentHandler)
                    .build()
                    .xmlReader().parse(new InputSource(in));
        }


        final StringBuilder sb = new StringBuilder();
        MarcListener handler = new MarcListener() {
            @Override
            public void beginCollection() {
                sb.append("beginCollection").append("\n");
            }

            @Override
            public void beginRecord(String format, String type) {
                sb.append("beginRecord").append("\n");
                sb.append("format=").append(format).append("\n");
                sb.append("type=").append(type).append("\n");
            }

            @Override
            public void leader(String label) {
                sb.append("leader").append("=").append(label).append("\n");
            }

            @Override
            public void field(MarcField field) {
                sb.append(field).append("\n");
            }

            @Override
            public void endRecord() {
                sb.append("endRecord").append("\n");
            }

            @Override
            public void endCollection() {
                sb.append("endCollection").append("\n");
            }
        };

        in = new ByteArrayInputStream(sw.toString().getBytes(StandardCharsets.UTF_8));
        try (MarcXchangeJSONLinesReader reader = new MarcXchangeJSONLinesReader(in, handler)) {
            reader.parse();
        }
        assertStream(getClass().getResource("aleph-publish.jsonl.txt").openStream(),
                new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testJsonLines() throws Exception {
        MyBuilder mabCatalogEntityBuilder = new MyBuilder(getClass().getResource("/org/xbib/catalog/entities/mab/titel.json"));
        InputStream in = getClass().getResource("aleph-publish.jsonl").openStream();
        //InputStream in = getClass().getResource("aleph-marcxchange-sample.jsonl").openStream();
        //InputStream in = new GZIPInputStream(new FileInputStream("/Users/joerg/DE-605-aleph-baseline-marcxchange-20160911.jsonl.gz"));
        try (MarcXchangeJSONLinesReader reader = new MarcXchangeJSONLinesReader(in, mabCatalogEntityBuilder)) {
            reader.parse();
            mabCatalogEntityBuilder.close();
            logger.log(Level.INFO, MessageFormat.format("counter = {0}",
                    mabCatalogEntityBuilder.getCounter()));
            assertEquals(4, mabCatalogEntityBuilder.getCounter().get());
            logger.log(Level.INFO, MessageFormat.format("checksum = {0}",
                    mabCatalogEntityBuilder.getChecksum()));
            assertEquals(991529170,  mabCatalogEntityBuilder.getChecksum());
        }
    }

    private class MyBuilder extends CatalogEntityBuilder {

        MyBuilder(URL url) throws Exception {
            super("org.xbib.catalog.entities.mab", 4, url); // set workers to 4,  we have only 4 records, avoid timeout
            setEnableChecksum(true);
        }

        @Override
        public void afterFinishState(CatalogEntityWorkerState state) {
            IRI iri = IRI.builder().scheme("http")
                    .host("jsonlines")
                    .query("type")
                    .fragment(Long.toString(counter.get())).build();
            try {
                state.getResource().setId(iri);
                RdfXContentParams params = new RdfXContentParams();
                RdfContentBuilder builder;
                Iterator<Resource> it = state.getResourceIterator();
                while (it.hasNext()) {
                    builder = rdfXContentBuilder(params);
                    Resource resource = it.next();
                    builder.receive(resource);
                    String result = params.getGenerator().get();
                    //logger.info(result);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
