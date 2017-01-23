package org.xbib.catalog.entities;

import static org.xbib.catalog.entities.CatalogEntitySpecification.LEADER;

import org.xbib.content.rdf.Resource;
import org.xbib.content.resource.IRI;
import org.xbib.marc.MarcField;
import org.xbib.marc.MarcRecord;
import org.xbib.marc.label.RecordLabel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

/**
 *
 */
public class CatalogEntityWorker implements Worker<MarcRecord> {

    private static final Logger logger = Logger.getLogger(CatalogEntityWorker.class.getName());

    private static final IRI tempPredicate = IRI.create("tmp");

    private final CatalogEntityBuilder entityBuilder;

    private final CRC32 crc32;

    private MarcRecord marcRecord;

    private CatalogEntityWorkerState state;

    public CatalogEntityWorker(CatalogEntityBuilder entityBuilder) {
        this.entityBuilder = entityBuilder;
        this.crc32 = new CRC32();
    }

    @SuppressWarnings("unchecked")
    private static Map.Entry<String, Object> subfieldDecoderMap(Map<String, Object> subfields, MarcField.Subfield field) {
        String k = null;
        Object v = field.getValue();
        Object subfieldDef = subfields.get(field.getId());
        if (subfieldDef instanceof Map) {
            // key/value mapping
            Map<String, Object> subfieldmap = (Map<String, Object>) subfieldDef;
            if (subfieldmap.containsKey(v.toString())) {
                Object o = subfieldmap.get(v.toString());
                if (o instanceof Map) {
                    Map.Entry<String, Object> me = (Map.Entry<String, Object>) ((Map) o).entrySet().iterator().next();
                    k = me.getKey();
                    v = me.getValue();
                } else {
                    v = o;
                }
            }
        } else {
            // new key (may be null to skip the value)
            k = (String) subfieldDef;
        }
        // create result map entry
        final String newKey = k;
        final Object newValue = v;
        return new Map.Entry<String, Object>() {
            @Override
            public String getKey() {
                return newKey;
            }

            @Override
            public Object getValue() {
                return newValue;
            }

            @Override
            public Object setValue(Object value) {
                return null;
            }
        };
    }

    @Override
    public void execute(MarcRecord marcRecord) throws IOException {
        this.marcRecord = marcRecord;
        this.state = newState();
        try {
            build(marcRecord);
        } finally {
            // always execute state finishing, even in case of an IOException
            entityBuilder.beforeFinishState(state);
            state.finish();
            entityBuilder.afterFinishState(state);
        }
    }

    @Override
    public MarcRecord getRequest() {
        return marcRecord;
    }

    @SuppressWarnings("unchecked")
    protected CatalogEntityWorkerState newState() {
        return new CatalogEntityWorkerState(entityBuilder);
    }

    public CatalogEntityWorkerState getWorkerState() {
        return state;
    }

    public Classifier getClassifier() {
        return entityBuilder.getClassifier();
    }

    public IdentifierMapper getIdentifierMapper() {
        return entityBuilder.getIdentifierMapper();
    }

    public ValueMapper getValueMapper() {
        return entityBuilder.getValueMapper();
    }

    protected void build(MarcRecord marcRecord) throws IOException {
        if (entityBuilder.isEnableChecksum()) {
            crc32.reset();
        }
        build(marcRecord.getRecordLabel());
        for (MarcField marcField : marcRecord.getFields()) {
            build(marcField);
        }
        if (entityBuilder.isEnableChecksum()) {
            crc32.update(marcRecord.getFields().toString().getBytes(StandardCharsets.UTF_8));
            getWorkerState().getResource().add(IRI.builder().curie("crc").build(), Long.toHexString(crc32.getValue()));
            entityBuilder.checksum(crc32);
        }
        entityBuilder.getCounter().incrementAndGet();
    }

    public void build(RecordLabel recordLabel) throws IOException {
        CatalogEntity entity = entityBuilder.getEntitySpecification().retrieve(recordLabel);
        if (entity != null) {
            entity.transform(this, MarcField.builder().tag(LEADER).value(recordLabel.toString()).build());
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "recordLabel=" + recordLabel + " entity=" + entity);
        }
    }

    /**
     * Catalog entities can decide to build a MARC field.
     * @param marcField the MARC field to build
     * @throws IOException if build fails
     */
    public void build(MarcField marcField) throws IOException {
        if (!marcField.isTagValid()) {
            entityBuilder.invalid(getWorkerState().getRecordIdentifier(), marcField,
                    "field " + marcField + ": invalid tag");
            return;
        }
        if (!marcField.isIndicatorValid()) {
            entityBuilder.invalid(getWorkerState().getRecordIdentifier(), marcField,
                    "field " + marcField + " invalid indicator");
            return;
        }
        CatalogEntity entity = entityBuilder.getEntitySpecification().retrieve(marcField);
        if (entity != null) {
            entity.transform(this, marcField);
            entityBuilder.mapped(getWorkerState().getRecordIdentifier(), marcField);
        } else {
            entityBuilder.unmapped(getWorkerState().getRecordIdentifier(), marcField,
                    "field " + marcField + " tag definition missing in specification");
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "field=" + marcField + " entity=" + entity);
        }
    }

    @SuppressWarnings("unchecked")
    public Resource append(Resource resource, MarcField field, CatalogEntity entity) throws IOException {
        Map<String, Object> params = entity.getParams();
        Map<String, Object> defaultSubfields = (Map<String, Object>) params.get("subfields");
        if (defaultSubfields == null) {
            return resource;
        }
        Map<MarcField, String> fieldNames = new HashMap<>();
        // create another anoymous resource, will be linked late if predicate is determined
        Resource newResource = resource.newResource(tempPredicate);
        // default predicate is the name of the class
        String predicate = entity.getClass().getSimpleName();
        // the _predicate field allows to select a field to name the resource by a coded value
        if (params.containsKey("_predicate")) {
            predicate = (String) params.get("_predicate");
        }
        boolean overridePredicate = false;
        // put all found fields with configured subfield names to this resource
        for (MarcField.Subfield subfield : field.getSubfields()) {
            Map<String, Object> subfields = defaultSubfields;
            if (params.containsKey("tags")) {
                Object o = params.get("tags");
                if (o instanceof Map) {
                    Map<String, Object> tags = (Map<String, Object>) o;
                    if (tags.containsKey(field.getTag())) {
                        if (!overridePredicate) {
                            predicate = (String) tags.get(field.getTag());
                        }
                        subfields = (Map<String, Object>) params.get(predicate);
                        if (subfields == null) {
                            subfields = defaultSubfields;
                        }
                    }
                }
            }
            // indicator-based predicate defined?
            if (params.containsKey("indicators")) {
                Map<String, Object> indicators = (Map<String, Object>) params.get("indicators");
                if (indicators.containsKey(field.getTag())) {
                    Map<String, Object> indicatorMap = (Map<String, Object>) indicators.get(field.getTag());
                    if (indicatorMap.containsKey(field.getIndicator())) {
                        if (!overridePredicate) {
                            predicate = (String) indicatorMap.get(field.getIndicator());
                            fieldNames.put(field, predicate);
                        }
                        subfields = (Map<String, Object>) params.get(predicate);
                        if (subfields == null) {
                            subfields = defaultSubfields;
                        }
                    }
                }
            }
            // is there a subfield value decoder?
            Map.Entry<String, Object> me = subfieldDecoderMap(subfields, subfield);
            if (me.getKey() != null && me.getValue() != null) {
                String v = me.getValue().toString();
                if (fieldNames.containsKey(field)) {
                    // field-specific subfield map
                    String fieldName = fieldNames.get(field);
                    List<Map<String, String>> patterns =
                            (List<Map<String, String>>) params.get(fieldName + "pattern");
                    if (patterns != null) {
                        for (Map<String, String> pattern : patterns) {
                            Map.Entry<String, String> mme = pattern.entrySet().iterator().next();
                            String p = mme.getKey();
                            String rel = mme.getValue();
                            Matcher m = Pattern.compile(p, Pattern.CASE_INSENSITIVE).matcher(v);
                            if (m.matches()) {
                                v = rel;
                                break;
                            }
                        }
                    } else {
                        if (params.containsKey(me.getKey())) {
                            Map<String, Object> vm = (Map<String, Object>) params.get(me.getKey());
                            v = vm.containsKey(v) ? vm.get(v).toString() : v;
                        }
                    }
                } else {
                    // default subfield map
                    String fieldName = me.getKey();
                    if (params.containsKey(fieldName)) {
                        Map<String, Object> vm = (Map<String, Object>) params.get(fieldName);
                        int pos = v.indexOf(' ');
                        String vv = pos > 0 ? v.substring(0, pos) : v;
                        if (vm.containsKey(v)) {
                            v = (String) vm.get(v);
                        } else if (vm.containsKey(vv)) {
                            v = (String) vm.get(vv);
                        } else {
                            // relation by pattern?
                            List<Map<String, String>> patterns =
                                    (List<Map<String, String>>) params.get(fieldName + "pattern");
                            if (patterns != null) {
                                for (Map<String, String> pattern : patterns) {
                                    Map.Entry<String, String> mme = pattern.entrySet().iterator().next();
                                    String p = mme.getKey();
                                    String rel = mme.getValue();
                                    Matcher m = Pattern.compile(p, Pattern.CASE_INSENSITIVE).matcher(v);
                                    if (m.matches()) {
                                        v = rel;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                // transform/split value v
                List<String> transformed = null;
                if (v != null) {
                    transformed = entity.transform(this, predicate, newResource, me.getKey(), v);
                }
                // is this the predicate field or a value?
                if (me.getKey().equals(predicate)) {
                    predicate = v;
                    overridePredicate = true;
                } else if (transformed != null) {
                    for (String t : transformed) {
                        newResource.add(me.getKey(), t);
                    }
                }
            } else {
                // no decoder, simple add field data
                String subfieldId = subfield.getId();
                if (subfieldId.isEmpty()) {
                    // "empty" subfield IDs exist in MAB. Replace with space. Maybe "_" or "|"?
                    subfieldId = " ";
                }
                if (subfields.containsKey(subfieldId)) {
                    String property = (String) subfields.get(subfieldId);
                    List<String> transformed = entity.transform(this, predicate, newResource, property,
                            subfield.getValue());
                    if (transformed != null) {
                        for (String t : transformed) {
                            newResource.add(property, t);
                        }
                    }
                } else {
                    entityBuilder.unmapped(getWorkerState().getRecordIdentifier(), field,
                            "field " + field + " missing definition for subfield '" + subfieldId
                                    + "' subfields=" + subfields);
                }
            }
        }
        // rename, now that we know the predicate
        if (predicate != null) {
            resource.rename(tempPredicate, IRI.builder().curie(predicate).build());
        }
        return newResource;
    }
}
