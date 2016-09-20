package org.xbib.marc.json;

import static com.fasterxml.jackson.core.JsonToken.END_ARRAY;
import static com.fasterxml.jackson.core.JsonToken.END_OBJECT;
import static com.fasterxml.jackson.core.JsonToken.FIELD_NAME;
import static com.fasterxml.jackson.core.JsonToken.START_ARRAY;
import static com.fasterxml.jackson.core.JsonToken.START_OBJECT;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.xbib.marc.MarcField;
import org.xbib.marc.MarcListener;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Read MARC from JSON lines format.
 */
public class MarcXchangeJSONLinesReader implements Closeable {

    private static final Logger logger = Logger.getLogger(MarcXchangeJSONLinesReader.class.getName());

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

    public void parse() throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(reader, bufferSize)) {
            listener.beginCollection();
            bufferedReader.lines().forEach(this::parse);
            listener.endCollection();
        }
    }

    private void parse(String line) {
        try {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, line);
            }
            parseLine(line);
            listener.endRecord();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void parseLine(String line) throws IOException {
        jsonParser = factory.createParser(line);
        jsonParser.nextToken();
        marcFieldBuilder = MarcField.builder();
        parseRecord();
    }

    private void parseRecord() throws IOException {
        JsonToken currentToken = jsonParser.getCurrentToken();
        String tag = null;
        while (currentToken != null && currentToken != END_OBJECT) {
            if (FIELD_NAME.equals(currentToken)) {
                tag = jsonParser.getCurrentName();
                marcFieldBuilder.tag(tag);
            } else if (START_ARRAY.equals(currentToken)) {
                parseTag();
            } else if (tag != null && currentToken.isScalarValue()) {
                // format, type, leader
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
                }
                marcFieldBuilder.value(value);
                emit();
            }
            currentToken = jsonParser.nextToken();
        }
    }

    private void parseTag() throws IOException {
        JsonToken currentToken = jsonParser.getCurrentToken();
        while (currentToken != null && currentToken != END_ARRAY) {
            if (START_OBJECT.equals(currentToken)) {
                parseIndicator();
                emit();
            } else if (currentToken.isScalarValue()) {
                marcFieldBuilder.value(jsonParser.getText());
                emit();
            }
            currentToken = jsonParser.nextToken();
        }
    }

    private void parseIndicator() throws IOException {
        JsonToken currentToken = jsonParser.getCurrentToken();
        while (currentToken != null && currentToken != END_OBJECT) {
            if (FIELD_NAME.equals(currentToken)) {
                String indicator = jsonParser.getCurrentName().replace('_', ' ');
                marcFieldBuilder.indicator(indicator);
            } else if (START_ARRAY.equals(currentToken)) {
                parseSubfields();
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
                marcFieldBuilder.subfieldValue(jsonParser.getText());
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
            String tag = marcField.getTag();
            listener.field(marcField);
            marcFieldBuilder = MarcField.builder().tag(tag);
        }
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    private class EmptyListener implements MarcListener {

        @Override
        public void beginCollection() {

        }

        @Override
        public void beginRecord(String format, String type) {

        }

        @Override
        public void leader(String label) {

        }

        @Override
        public void field(MarcField field) {

        }

        @Override
        public void endRecord() {

        }

        @Override
        public void endCollection() {

        }
    }
}
