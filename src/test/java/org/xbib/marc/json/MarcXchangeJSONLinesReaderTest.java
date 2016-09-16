package org.xbib.marc.json;

import org.junit.Test;
import org.xbib.helper.StreamTester;
import org.xbib.marc.MarcField;
import org.xbib.marc.MarcListener;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class MarcXchangeJSONLinesReaderTest extends StreamTester {

    @Test
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
        assertStream(getClass().getResource("zdb-marc.json.txt").openStream(),
                new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8)));
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

        InputStream in = getClass().getResource("aleph-marcxchange-sample.jsonl").openStream();
        try (MarcXchangeJSONLinesReader reader = new MarcXchangeJSONLinesReader(in, handler)) {
            reader.parse();
        }
        assertStream(getClass().getResource("aleph-marcxchange-sample.jsonl.txt").openStream(),
                new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8)));
    }

}
