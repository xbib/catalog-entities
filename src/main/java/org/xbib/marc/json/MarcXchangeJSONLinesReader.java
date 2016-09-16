package org.xbib.marc.json;

import static com.fasterxml.jackson.core.JsonToken.END_ARRAY;
import static com.fasterxml.jackson.core.JsonToken.END_OBJECT;
import static com.fasterxml.jackson.core.JsonToken.FIELD_NAME;
import static com.fasterxml.jackson.core.JsonToken.START_ARRAY;
import static com.fasterxml.jackson.core.JsonToken.START_OBJECT;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
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

    private String tag;

    private String indicator;

    private String subfieldId;

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
        marcFieldBuilder = MarcField.builder();
        jsonParser.nextToken();
        parseObject(0);
    }

    private void parseObject(int level) throws IOException {
        while (jsonParser.nextToken() != null && jsonParser.getCurrentToken() != END_OBJECT) {
            if (FIELD_NAME.equals(jsonParser.getCurrentToken())) {
                jsonParser.nextToken();
                parseInner(jsonParser.getCurrentName(), level);
            } else {
                throw new JsonParseException(jsonParser, "expected field name, but got " + jsonParser.getCurrentToken(),
                        jsonParser.getCurrentLocation());
            }
        }
        emitFields();
    }

    private void parseArray(String subfieldId) throws IOException {
        while (jsonParser.nextToken() != null && jsonParser.getCurrentToken() != END_ARRAY) {
            marcFieldBuilder.subfield(subfieldId, jsonParser.getText());
        }
    }

    private void parseInner(String name, int level) throws IOException {
        JsonToken currentToken = jsonParser.getCurrentToken();
        if (START_OBJECT.equals(currentToken)) {
            switch (level) {
                case 0: {
                    tag = name;
                    break;
                }
                case 1: {
                    indicator = name.replace('_', ' ');
                    break;
                }
                default:
                    break;
            }
            parseObject(level + 1);
        } else if (START_ARRAY.equals(currentToken)) {
            parseArray(name);
        } else if (currentToken.isScalarValue()) {
            if (MarcJsonWriter.FORMAT_TAG.equals(name) || "__FORMAT".equals(name)) {
                format = jsonParser.getText();
            } else if (MarcJsonWriter.TYPE_TAG.equals(name) || "__TYPE".equals(name)) {
                type = jsonParser.getText();
            } else if (MarcJsonWriter.LEADER_TAG.equals(name) || "__LEADER".equals(name)) {
                leader = jsonParser.getText();
            } else {
                switch (level) {
                    case 0: {
                        tag = name;
                        break;
                    }
                    case 1: {
                        indicator = name.replace('_', ' ');
                        break;
                    }
                    case 2: {
                        subfieldId = name;
                        break;
                    }
                    default:
                        break;
                }
                marcFieldBuilder.tag(tag).indicator(indicator);
                if (subfieldId != null) {
                    marcFieldBuilder.subfield(subfieldId, jsonParser.getText());
                } else {
                    marcFieldBuilder.value(jsonParser.getText());
                    if (marcFieldBuilder.isControl()) {
                        logger.log(Level.FINE, "control field: emit " + marcFieldBuilder.build());
                    }
                    emitFields();
                }
            }
        }
    }

    private void emitFields() {
        if (format != null && type != null) {
            listener.beginRecord(format, type);
            format = null;
            type = null;
        }
        if (leader != null) {
            listener.leader(leader);
            leader = null;
        }
        MarcField marcField = marcFieldBuilder.build();
        if (!marcField.isEmpty()) {
            listener.field(marcField);
        }
        marcFieldBuilder = MarcField.builder();
        tag = null;
        indicator = null;
        subfieldId = null;
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
