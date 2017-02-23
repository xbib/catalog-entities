package org.xbib.marc.json;

import org.xbib.content.XContentBuilder;
import org.xbib.marc.Marc;
import org.xbib.marc.MarcField;
import org.xbib.marc.MarcListener;
import org.xbib.marc.MarcRecord;
import org.xbib.marc.MarcRecordListener;
import org.xbib.marc.MarcXchangeConstants;
import org.xbib.marc.label.RecordLabel;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

import static org.xbib.content.json.JsonXContent.contentBuilder;

/**
 * Convert a MarcXchange stream to XContent builder.
 */
public class MarcXchangeJSONLinesWriter implements AutoCloseable, MarcListener, MarcRecordListener, MarcXchangeConstants {

    private static final int DEFAULT_BUFFER_SIZE = 65536;

    private final AtomicInteger recordCounter = new AtomicInteger();

    private final Lock lock;

    private XContentBuilder builder;

    private OutputStream out;

    private Marc.Builder marcBuilder;

    private String format;

    private String type;

    private boolean fatalErrors;

    private Style style;

    private Exception exception;

    private String fileNamePattern;

    private AtomicInteger fileNameCounter;

    private int splitlimit;

    private int bufferSize;

    private boolean compress;

    private String index;

    private String indexType;

    private boolean top;

    public MarcXchangeJSONLinesWriter(OutputStream out) throws IOException {
        this(out, Style.LINES);
    }

    public MarcXchangeJSONLinesWriter(OutputStream out, Style style) throws IOException {
        this(out, DEFAULT_BUFFER_SIZE, style);
    }

    public MarcXchangeJSONLinesWriter(OutputStream out, int bufferSize, Style style) throws IOException {
        this.out = new BufferedOutputStream(out, bufferSize);
        this.bufferSize = bufferSize;
        this.lock = new ReentrantLock();
        this.builder = contentBuilder(out);
        this.marcBuilder = Marc.builder();
        this.fatalErrors = false;
        this.style = style;
    }

    public MarcXchangeJSONLinesWriter(String fileNamePattern, int splitlimit) throws IOException {
        this(fileNamePattern, splitlimit, Style.LINES, DEFAULT_BUFFER_SIZE, false);
    }

    public MarcXchangeJSONLinesWriter(String fileNamePattern, int splitlimit, Style style) throws IOException {
        this(fileNamePattern, splitlimit, style, DEFAULT_BUFFER_SIZE, false);
    }

    public MarcXchangeJSONLinesWriter(String fileNamePattern, int splitlimit, Style style, int bufferSize, boolean compress)
            throws IOException {
        this.fileNameCounter = new AtomicInteger(0);
        this.fileNamePattern = fileNamePattern;
        this.splitlimit = splitlimit;
        this.bufferSize = bufferSize;
        this.lock = new ReentrantLock();
        this.marcBuilder = Marc.builder();
        this.top = true;
        this.style = style;
        this.compress = compress;
        this.builder = contentBuilder(newOutputStream(fileNamePattern, fileNameCounter, bufferSize, compress));
    }

    public MarcXchangeJSONLinesWriter setIndex(String index, String indexType) {
        this.index = index;
        this.indexType = indexType;
        return this;
    }

    public MarcXchangeJSONLinesWriter setFatalErrors(boolean fatalErrors) {
        this.fatalErrors = fatalErrors;
        return this;
    }

    public MarcXchangeJSONLinesWriter setFormat(String format) {
        this.format = format;
        return this;
    }

    public MarcXchangeJSONLinesWriter setType(String type) {
        this.type = type;
        return this;
    }

    public void startDocument() throws IOException {
        // nothing to do here
    }

    @Override
    public void beginCollection() {
        if (style == Style.ARRAY) {
            try {
                builder.startArray();
            } catch (IOException e) {
                handleException(e);
            }
        }
    }

    @Override
    public void record(MarcRecord marcRecord) {
        if (exception != null) {
            return;
        }
        lock.lock();
        try {
            toJson(marcRecord);
            builder.flush();
            recordCounter.incrementAndGet();
            out.write('\n');
        } catch (IOException e) {
            handleException(e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void endCollection() {
        try {
            if (style == Style.ARRAY) {
                builder.endArray();
            }
            builder.flush();
            if (style == Style.ELASTICSEARCH_BULK) {
                // finish with line-feed
                out.write('\n');
            }
            out.flush();
        } catch (IOException e) {
            handleException(e);
        }
    }

    public void endDocument() throws IOException {
        builder.flush();
        out.flush();
    }

    @Override
    public void beginRecord(String format, String type) {
        if (format != null) {
            this.format = format;
            marcBuilder.setFormat(format);
        }
        if (type != null) {
            this.type = type;
            marcBuilder.setType(type);
        }
    }

    @Override
    public void endRecord() {
        if (format != null) {
            marcBuilder.setFormat(format);
        }
        if (type != null) {
            marcBuilder.setType(type);
        }
        record(marcBuilder.buildRecord());
        marcBuilder = Marc.builder();
    }

    @Override
    public void leader(String label) {
        marcBuilder.recordLabel(RecordLabel.builder().from(label.toCharArray()).build());
    }

    @Override
    public void field(MarcField field) {
        marcBuilder.addField(field);
    }

    /**
     * Format MARC record as key-oriented JSON.
     */
    @SuppressWarnings("unchecked")
    private void toJson(MarcRecord marcRecord) throws IOException {
        if (marcRecord.isEmpty()) {
            return;
        }
        if (top) {
            top = false;
            if (style == Style.ELASTICSEARCH_BULK) {
                writeMetaDataLine(marcRecord);
            }
        } else {
            switch (style) {
                case ARRAY:
                    out.write(',');
                    break;
                case LINES:
                    out.write('\n');
                    break;
                case ELASTICSEARCH_BULK:
                    out.write('\n');
                    writeMetaDataLine(marcRecord);
                    break;
                default:
                    break;
            }
        }
        builder.startObject();
        for (Map.Entry<String, Object> tags : marcRecord.entrySet()) {
            String tag = tags.getKey();
            builder.field(tag);
            Object o = tags.getValue();
            if (o instanceof Map) {
                Map<String, Object> repeatMap = (Map<String, Object>) o;
                builder.startArray();
                for (Map.Entry<String, Object> repeats : repeatMap.entrySet()) {
                    o = repeats.getValue();
                    if (!(o instanceof List)) {
                        o = Collections.singletonList(o);
                    }
                    List<?> list = (List<?>) o;
                    if (list.size() > 1) {
                        builder.startArray();
                    }
                    for (Object value : list) {
                        if (value instanceof Map) {
                            builder.startObject();
                            for (Map.Entry<String, Object> indicators : ((Map<String, Object>) value).entrySet()) {
                                String indicator = indicators.getKey();
                                builder.field(indicator);
                                o = indicators.getValue();
                                if (!(o instanceof List)) {
                                    o = Collections.singletonList(o);
                                }
                                List<?> list2 = (List<?>) o;
                                builder.startArray();
                                for (Object value2 : list2) {
                                    if (value2 instanceof Map) {
                                        Map<String, Object> map = (Map<String, Object>) value2;
                                        for (Map.Entry<String, Object> subfield : map.entrySet()) {
                                            if (subfield.getValue() instanceof List) {
                                                for (String s : (List<String>) subfield.getValue()) {
                                                    builder.startObject();
                                                    builder.field(subfield.getKey(), s);
                                                    builder.endObject();
                                                }
                                            } else {
                                                builder.startObject()
                                                        .field(subfield.getKey(), subfield.getValue().toString())
                                                        .endObject();
                                            }
                                        }
                                    } else {
                                        builder.value(value2.toString());
                                    }
                                }
                                builder.endArray();
                            }
                            builder.endObject();
                        } else {
                            if (value == null) {
                                builder.nullValue();
                            } else {
                                builder.value(value.toString());
                            }
                        }
                    }
                    if (list.size() > 1) {
                        builder.endArray();
                    }
                }
                builder.endArray();
            } else {
                if (o == null) {
                    builder.nullValue();
                } else {
                    builder.value(o.toString());
                }
            }
        }
        builder.endObject();
    }

    private void handleException(IOException e) {
        exception = e;
        if (fatalErrors) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() {
        try {
            builder.close();
            out.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    public int getRecordCounter() {
        return recordCounter.get();
    }

    /**
     * Split records, if configured.
     */
    private void afterRecord() throws IOException {
        if (fileNamePattern != null && getRecordCounter() % splitlimit == 0) {
            endCollection();
            close();
            this.out = newOutputStream(fileNamePattern, fileNameCounter, bufferSize, compress);
            top = true;
            beginCollection();
        }
    }

    private OutputStream newOutputStream(String fileNamePattern, AtomicInteger fileNameCounter,
                           int bufferSize, boolean compress) throws IOException {
        String name = String.format(fileNamePattern, fileNameCounter.getAndIncrement());
        OutputStream outputStream = Files.newOutputStream(Paths.get(name), StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
        return compress ?
                new CompressedOutputStream(outputStream, bufferSize) :
                new BufferedOutputStream(outputStream, bufferSize);
    }


    private void writeMetaDataLine(MarcRecord marcRecord) throws IOException {
        Object object = marcRecord.get("001");
        // step down to indicator/subfield ID levels if possible, get first value, assuming single field/value in 001
        if (object instanceof Map) {
            object = ((Map) object).values().iterator().next();
        }
        if (object instanceof Map) {
            object = ((Map) object).values().iterator().next();
        }
        String id = object.toString();
        if (index != null && indexType != null && id != null) {
            builder.startObject()
                .startObject("index")
                .field("_index", index)
                .field("_type", type)
                .field("_id", id)
                .endObject()
                .endObject();
            builder.flush();
            out.write('\n');
        }
    }

    /**
     * A GZIP output stream, modified for best compression.
     */
    private static class CompressedOutputStream extends GZIPOutputStream {

        CompressedOutputStream(OutputStream out, int size) throws IOException {
            super(out, size, true);
            def.setLevel(Deflater.BEST_COMPRESSION);
        }
    }

    /**
     *
     */
    public enum Style {
        ARRAY, LINES, ELASTICSEARCH_BULK
    }
}
