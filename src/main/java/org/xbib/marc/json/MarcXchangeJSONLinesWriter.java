package org.xbib.marc.json;

import static org.xbib.common.xcontent.XContentService.jsonBuilder;

import org.xbib.common.xcontent.XContentBuilder;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Convert a MarcXchange stream to XContent builder.
 *
 */
public class MarcXchangeJSONLinesWriter implements MarcListener, MarcRecordListener, MarcXchangeConstants {

    private final OutputStream out;

    private Marc.Builder marcBuilder;

    private XContentBuilder builder;

    private String format;

    private String type;

    private Exception exception;

    private boolean fatalErrors = false;

    private final ReentrantLock lock = new ReentrantLock(true);

    public MarcXchangeJSONLinesWriter(OutputStream out) throws IOException {
        this.out = new BufferedOutputStream(out);
        this.builder = jsonBuilder(out);
        this.marcBuilder = Marc.builder();
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
        // empty
    }

    public void endDocument() throws IOException {
        builder.flush();
        out.flush();
    }

    @Override
    public void beginCollection() {
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
            out.write('\n');
        } catch (IOException e) {
            handleException(e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void endCollection() {
        //
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
        builder.startObject();
        for (Map.Entry<String, Object> tags : marcRecord.entrySet()) {
            String tag = tags.getKey();
            builder.field(tag);
            Object o = tags.getValue();
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
                        if (list2.size() > 1) {
                            builder.startArray();
                        }
                        for (Object value2 : list2) {
                            if (value2 instanceof Map) {
                                builder.startObject();
                                Map<String, Object> map = (Map<String, Object>) value2;
                                for (Map.Entry<String, Object> subfield : map.entrySet()) {
                                    builder.field(subfield.getKey());
                                    if (subfield.getValue() instanceof List) {
                                        builder.startArray();
                                        for (String s : (List<String>)subfield.getValue()) {
                                            builder.value(s);
                                        }
                                        builder.endArray();
                                    } else {
                                        builder.value(subfield.getValue().toString());
                                    }
                                }
                                builder.endObject();
                            } else {
                                builder.value(value2.toString());
                            }
                        }
                        if (list2.size() > 1) {
                            builder.endArray();
                        }
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
        builder.endObject();
    }

    private void handleException(IOException e) {
        exception = e;
        if (fatalErrors) {
            throw new UncheckedIOException(e);
        }
    }
}
