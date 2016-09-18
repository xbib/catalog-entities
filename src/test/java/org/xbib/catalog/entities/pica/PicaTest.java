package org.xbib.catalog.entities.pica;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.catalog.entities.CatalogEntityBuilder;
import org.xbib.catalog.entities.CatalogEntityWorkerState;
import org.xbib.iri.IRI;
import org.xbib.marc.Marc;
import org.xbib.marc.MarcField;
import org.xbib.marc.dialects.pica.PicaXMLContentHandler;

public class PicaTest extends Assert {

    private static final Logger logger = Logger.getLogger(PicaTest.class.getName());

    private final AtomicInteger counter = new AtomicInteger();

    @Test
    public void testPicaSetup() throws Exception {
        File file = File.createTempFile("marc-bib-entities.", ".json");
        file.deleteOnExit();
        try (MyBuilder builder = new MyBuilder("org.xbib.catalog.entities.pica.zdb.bibdat", getClass().getResource("bibdat.json"))) {
            FileWriter writer = new FileWriter(file);
            builder.getEntitySpecification().dump(writer);
            writer.close();
        }
    }

    @Test
    public void testPica() throws Exception {
        try (MyBuilder myBuilder = new MyBuilder("org.xbib.catalog.entities.pica.zdb.bibdat", getClass().getResource("bibdat.json"))) {
            Marc.builder()
                    .setInputStream(getClass().getResourceAsStream("zdb-oai-bib.xml"))
                    .setMarcRecordListener(myBuilder)
                    //.setStringTransformer(s -> Normalizer.normalize(s, Normalizer.Form.NFKC))
                    .build()
                    .xmlReader().parse();
            logger.log(Level.INFO, MessageFormat.format("unmapped ZDB OAI fields = {0}", myBuilder.getUnmapped()));
        }
    }

    @Test
    public void testNatLizPica() throws Exception {
        try (MyBuilder myBuilder = new MyBuilder("org.xbib.catalog.entities.pica.natliz.bib", getClass().getResource("bib.json"))) {
            PicaXMLContentHandler contentHandler = new PicaXMLContentHandler();
            contentHandler.setFormat("Pica");
            contentHandler.setType("XML");
            contentHandler.setMarcListener(myBuilder);
            Marc.builder()
                    .setInputStream(getClass().getResourceAsStream("natliz.xml"))
                    .setContentHandler(contentHandler)
                    .build()
                    .xmlReader().parse();
        }
    }

    private class MyBuilder extends CatalogEntityBuilder {

        MyBuilder(String packageName, URL url) throws Exception {
            super(packageName, url);
        }

        @Override
        public void beforeFinishState(CatalogEntityWorkerState context) {
            IRI iri = IRI.builder().scheme("http")
                    .host("xbib.org")
                    .query("bibdat")
                    .fragment(Long.toString(counter.getAndIncrement())).build();
            try {
                context.getResource().setId(iri);
            } catch (IOException e) {
                // ignore
            }
        }

        @Override
        public void unmapped(String id, MarcField marcField, String message) {
        }
    }
}
