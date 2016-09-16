package org.xbib.marc.json;

import org.junit.Ignore;
import org.junit.Test;
import org.xbib.helper.StreamTester;
import org.xbib.marc.MarcField;
import org.xbib.marc.MarcListener;

import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class MarcXchangeJSONLinesReaderTest extends StreamTester {

    @Test
    @Ignore
    public void testZDBJSONLines() throws Exception {
        final StringBuilder sb = new StringBuilder();
        MarcListener marcListener = new MarcListener() {
            @Override
            public void beginCollection() {
            }

            @Override
            public void beginRecord(String format, String type) {
                sb.append("beginRecord").append("\n");
                sb.append("format=").append(format).append("\n");
                sb.append("type=").append(type).append("\n");
            }

            @Override
            public void leader(String label) {
                sb.append("leader=").append(label).append("\n");
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
            }
        };
        InputStream in = getClass().getResource("zdb-marc.json").openStream();
        try (MarcXchangeJSONLinesReader reader = new MarcXchangeJSONLinesReader(in, marcListener)) {
            reader.parse();
        }
        //System.err.println(sb.toString());
        //assertStream(getClass().getResource("zdb-marc-json.txt").openStream(),
        //        new ByteArrayInputStream(sb.toString().getBytes("UTF-8")));

    }

    @Test
    public void testHBZ() throws Exception {
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

        InputStream in = new GZIPInputStream(getClass().getResource("aleph-marcxchange-sample.jsonl.gz").openStream());
        try (MarcXchangeJSONLinesReader reader = new MarcXchangeJSONLinesReader(in, handler)) {
            reader.parse();
        }
        //System.err.println(sb.toString());
    }

}
