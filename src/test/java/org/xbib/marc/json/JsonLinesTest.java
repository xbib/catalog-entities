package org.xbib.marc.json;

import static org.xbib.rdf.content.RdfXContentFactory.rdfXContentBuilder;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.catalog.entities.CatalogEntityBuilder;
import org.xbib.catalog.entities.CatalogEntityWorkerState;
import org.xbib.iri.IRI;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.Resource;
import org.xbib.rdf.content.RdfXContentParams;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonLinesTest extends Assert {

    private static final Logger logger = Logger.getLogger(JsonLinesTest.class.getName());

    @Test
    public void testJsonLines() throws Exception {
        MyBuilder mabCatalogEntityBuilder = new MyBuilder(getClass().getResource("/org/xbib/catalog/entities/mab/titel.json"));
        InputStream in = getClass().getResource("aleph-marcxchange-sample.jsonl").openStream();
        //InputStream in = new GZIPInputStream(new FileInputStream("/Users/joerg/DE-605-aleph-baseline-marcxchange-20160911.jsonl.gz"));
        try (MarcXchangeJSONLinesReader reader = new MarcXchangeJSONLinesReader(in, mabCatalogEntityBuilder)) {
            reader.parse();
            mabCatalogEntityBuilder.close();
            logger.log(Level.INFO, MessageFormat.format("counter = {0}",
                    mabCatalogEntityBuilder.getCounter()));
            logger.log(Level.INFO, MessageFormat.format("checksum = {0}",
                    mabCatalogEntityBuilder.getChecksum()));
            BufferedWriter writer = new BufferedWriter(new FileWriter("mapped.txt"));
            writer.write(mabCatalogEntityBuilder.getMapped().toString());
            writer.close();
            writer = new BufferedWriter(new FileWriter("unmapped.txt"));
            writer.write(mabCatalogEntityBuilder.getUnmapped().toString());
            writer.close();
            writer = new BufferedWriter(new FileWriter("invalid.txt"));
            writer.write(mabCatalogEntityBuilder.getInvalid().toString());
            writer.close();

            //logger.log(Level.INFO, MessageFormat.format("mapped = {0}",
            //        myBuilder.getMapped()));
            //logger.log(Level.INFO, MessageFormat.format("unmapped  = {0}",
            //        myBuilder.getUnmapped()));
            //logger.log(Level.INFO, MessageFormat.format("invalid = {0}",
            //        myBuilder.getInvalid()));
        }
    }

    private class MyBuilder extends CatalogEntityBuilder {

        MyBuilder(URL url) throws Exception {
            super("org.xbib.catalog.entities.mab", url);
            setEnableChecksum(true);
        }

        /**
         * After state finish, we have the facets generated.
         * @param state the state.
         */
        @Override
        public void afterFinishState(CatalogEntityWorkerState state) {
            IRI iri = IRI.builder().scheme("http")
                    .host("jsonlines")
                    .query("type")
                    .fragment(Long.toString(counter.get())).build();
            try {
                state.getResource().setId(iri);
                RdfXContentParams params = new RdfXContentParams();
                RdfContentBuilder builder; //rdfXContentBuilder(params);
               // builder.receive(state.getResource());
                //String result = params.getGenerator().get();
                //logger.info("title="+result);
                Iterator<Resource> it = state.getResourceIterator();
                while (it.hasNext()) {
                    builder = rdfXContentBuilder(params);
                    Resource resource = it.next();
                    builder.receive(resource);
                    String result = params.getGenerator().get();
                    //logger.info(result);
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }
}
