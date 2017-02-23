package org.xbib.marc.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.xbib.marc.MarcField;
import org.xbib.marc.MarcListener;
import org.xbib.marc.transformer.value.MarcValueTransformers;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import static com.fasterxml.jackson.core.JsonToken.END_ARRAY;
import static com.fasterxml.jackson.core.JsonToken.END_OBJECT;
import static com.fasterxml.jackson.core.JsonToken.FIELD_NAME;
import static com.fasterxml.jackson.core.JsonToken.START_ARRAY;
import static com.fasterxml.jackson.core.JsonToken.START_OBJECT;

/**
 * Read MARC from JSON lines format.
 */
public class MarcXchangeJSONLinesReader implements Closeable {

    private static final int DEFAULT_BUFFER_SIZE = 8192;

    private static final JsonFactory factory = new JsonFactory();

    private final Reader reader;

    private final MarcListener listener;

    private final int bufferSize;

    private JsonParser jsonParser;

    private MarcField.Builder marcFieldBuilder;

    private String format;

    private String type;

    private String leader;

    private MarcValueTransformers marcValueTransformers;

    public MarcXchangeJSONLinesReader(InputStream in) throws IOException {
        this(new InputStreamReader(in, StandardCharsets.UTF_8), DEFAULT_BUFFER_SIZE, null);
    }

    public MarcXchangeJSONLinesReader(InputStream in, MarcListener listener) throws IOException {
        this(new InputStreamReader(in, StandardCharsets.UTF_8), DEFAULT_BUFFER_SIZE, listener);
    }

    public MarcXchangeJSONLinesReader(InputStream in, int buffersize, MarcListener listener) throws IOException {
        this(new InputStreamReader(in, StandardCharsets.UTF_8), buffersize, listener);
    }

    public MarcXchangeJSONLinesReader(Reader reader, MarcListener listener) {
        this(reader, DEFAULT_BUFFER_SIZE, listener);
    }

    public MarcXchangeJSONLinesReader(Reader reader, int bufferSize, MarcListener listener) {
        this.reader = reader;
        this.listener = listener == null ? new EmptyListener() : listener;
        this.bufferSize = bufferSize;
    }

    public void setMarcValueTransformers(MarcValueTransformers marcValueTransformers) {
        this.marcValueTransformers = marcValueTransformers;
    }

    public void parse() throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(reader, bufferSize)) {
            listener.beginCollection();
            bufferedReader.lines().forEach(this::parse);
            listener.endCollection();
        }
    }

    private void parse(String line) {
        try {
            jsonParser = factory.createParser(line);
            jsonParser.nextToken();
            marcFieldBuilder = MarcField.builder();
            parseRecord();
            listener.endRecord();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void parseRecord() throws IOException {
        String tag = null;
        JsonToken currentToken = jsonParser.getCurrentToken();
        while (currentToken != null && currentToken != END_OBJECT) {
            if (FIELD_NAME.equals(currentToken)) {
                tag = jsonParser.getCurrentName();
                marcFieldBuilder = MarcField.builder().tag(tag);
            } else if (START_ARRAY.equals(currentToken)) {
                parseTags();
            } else if (tag != null && currentToken.isScalarValue()) {
                // special handling of format, type, leader
                String value = jsonParser.getText();
                switch (tag) {
                    case MarcJsonWriter.FORMAT_TAG:
                        format = value;
                        break;
                    case MarcJsonWriter.TYPE_TAG:
                        type = value;
                        break;
                    case MarcJsonWriter.LEADER_TAG:
                        leader = value;
                        break;
                    default:
                        break;
                }
                marcFieldBuilder.value(value);
                emit();
            }
            currentToken = jsonParser.nextToken();
        }
    }

    private void parseTags() throws IOException {
        JsonToken currentToken = jsonParser.getCurrentToken();
        if (currentToken == START_ARRAY) {
            // special case of values directly attached to a tag
            while (currentToken != null && currentToken != END_ARRAY) {
                if (FIELD_NAME.equals(currentToken)) {
                    marcFieldBuilder = MarcField.builder().tag(jsonParser.getCurrentName());
                } else if (START_OBJECT.equals(currentToken)) {
                    parseIndicators();
                } else if (currentToken.isScalarValue()) {
                    String value = jsonParser.getText();
                    marcFieldBuilder.value(value);
                    emit();
                }
                currentToken = jsonParser.nextToken();
            }
        } else if (currentToken == START_OBJECT) {
            while (currentToken != null && currentToken != END_OBJECT) {
                if (FIELD_NAME.equals(currentToken)) {
                    marcFieldBuilder = MarcField.builder().tag(jsonParser.getCurrentName());
                } else if (START_OBJECT.equals(currentToken)) {
                    parseIndicators();
                } else if (currentToken.isScalarValue()) {
                    String value = jsonParser.getText();
                    marcFieldBuilder.value(value);
                    emit();
                }
                currentToken = jsonParser.nextToken();
            }
        }
    }

    private void parseIndicators() throws IOException {
        JsonToken currentToken = jsonParser.getCurrentToken();
        while (currentToken != null && currentToken != END_ARRAY) {
            if (FIELD_NAME.equals(currentToken)) {
                String indicator = jsonParser.getCurrentName().replace('_', ' ');
                marcFieldBuilder.indicator(indicator);
            } else if (START_ARRAY.equals(currentToken)) {
                // save current MARC field to restore tag and indicator
                String tag = marcFieldBuilder.tag();
                String indicator = marcFieldBuilder.indicator();
                parseSubfields();
                emit();
                marcFieldBuilder = MarcField.builder().tag(tag).indicator(indicator);
            }
            currentToken = jsonParser.nextToken();
        }
    }

    private void parseSubfields() throws IOException {
        JsonToken currentToken = jsonParser.getCurrentToken();
        while (currentToken != null && currentToken != END_ARRAY) {
            if (FIELD_NAME.equals(currentToken)) {
                marcFieldBuilder.subfield(jsonParser.getCurrentName());
            } else if (currentToken.isScalarValue()) {
                String value = jsonParser.getText();
                if (marcFieldBuilder.hasSubfields()) {
                    marcFieldBuilder.subfieldValue(value);
                } else {
                    // non-control fields without subfields (custom fields)
                    marcFieldBuilder.value(value);
                }
            }
            currentToken = jsonParser.nextToken();
        }
    }

    private void emit() {
        if (format != null || type != null) {
            if (format != null && type != null) {
                listener.beginRecord(format, type);
                format = null;
                type = null;
            }
        } else if (leader != null) {
            listener.leader(leader);
            leader = null;
        } else {
            MarcField marcField = marcFieldBuilder.build();
            listener.field(marcValueTransformers != null ? marcValueTransformers.transformValue(marcField) : marcField);
        }
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    private class EmptyListener implements MarcListener {

        @Override
        public void beginCollection() {
            // do nothing
        }

        @Override
        public void beginRecord(String format, String type) {
            // do nothing
        }

        @Override
        public void leader(String label) {
            // do nothing
        }

        @Override
        public void field(MarcField field) {
            // do nothing
        }

        @Override
        public void endRecord() {
            // do nothing
        }

        @Override
        public void endCollection() {
            // do nothing
        }
    }
}
