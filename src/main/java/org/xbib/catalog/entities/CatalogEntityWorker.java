package org.xbib.catalog.entities;

import org.xbib.iri.IRI;
import org.xbib.marc.MarcField;
import org.xbib.marc.MarcRecord;
import org.xbib.rdf.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
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

    @SuppressWarnings("unchecked")
    private CatalogEntityWorkerState newState() {
        return new CatalogEntityWorkerState(entityBuilder);
    }

    @Override
    public void execute(MarcRecord marcRecord) throws IOException {
        this.state = newState();
        build(marcRecord);
        entityBuilder.beforeFinishState(state);
        state.finish();
        entityBuilder.afterFinishState(state);
    }

    public CatalogEntityWorkerState getWorkerState() {
        return state;
    }

    public Classifier classifier() {
        return entityBuilder.getClassifier();
    }

    public IdentifierMapper identifierMapper() {
        return entityBuilder.getIdentifierMapper();
    }

    public StatusCodeMapper statusCodeMapper() {
        return entityBuilder.getStatusMapper();
    }

    public void build(MarcRecord marcRecord) throws IOException {
        if (entityBuilder.isEnableChecksum()) {
            crc32.reset();
        }
        for (MarcField marcField : marcRecord.getFields()) {
            if (!marcField.isTagValid()) {
                entityBuilder.invalid(getWorkerState().getRecordIdentifier(), marcField,
                        "field " + marcField + ": invalid tag");
                continue;
            }
            if (!marcField.isIndicatorValid()) {
                entityBuilder.invalid(getWorkerState().getRecordIdentifier(), marcField,
                        "field " + marcField + " invalid indicator");
                continue;
            }
            CatalogEntity entity = entityBuilder.getEntitySpecification().retrieve(marcField);
            if (entity != null) {
                entity.transform(this, marcField);
                entityBuilder.mapped(getWorkerState().getRecordIdentifier(), marcField);
            } else {
                entityBuilder.unmapped(getWorkerState().getRecordIdentifier(), marcField,
                        "field " + marcField + " tag definition missing in specification");
            }
        }
        if (entityBuilder.isEnableChecksum()) {
            crc32.update(marcRecord.getFields().toString().getBytes(StandardCharsets.UTF_8));
            getWorkerState().getResource().add(IRI.builder().curie("crc").build(), Long.toHexString(crc32.getValue()));
            entityBuilder.checksum(crc32);
        }
        entityBuilder.getCounter().incrementAndGet();
    }

    @SuppressWarnings("unchecked")
    public Resource append(Resource resource, MarcField field, CatalogEntity entity) throws IOException {
        Map<String, Object> defaultSubfields = (Map<String, Object>) entity.getParams().get("subfields");
        if (defaultSubfields == null) {
            return resource;
        }
        Map<MarcField, String> fieldNames = new HashMap<>();
        // create another anoymous resource, will be linked late if predicate is determined
        Resource newResource = resource.newResource(tempPredicate);
        // default predicate is the name of the class
        String predicate = entity.getClass().getSimpleName();
        // the _predicate field allows to select a field to name the resource by a coded value
        if (entity.getParams().containsKey("_predicate")) {
            predicate = (String) entity.getParams().get("_predicate");
        }
        if (field.isControl()) {
            // for control field, look into "subfields" { " " : ... }
            Map<String, Object> subfields = (Map<String, Object>) entity.getParams().get("subfields");
            newResource.add((String) subfields.get(" "), field.getValue());
        } else {
            boolean overridePredicate = false;
            // put all found fields with configured subfield names to this resource
            for (MarcField.Subfield subfield : field.getSubfields()) {
                Map<String, Object> subfields = defaultSubfields;
                if (entity.getParams().containsKey("tags")) {
                    Object o = entity.getParams().get("tags");
                    if (o instanceof Map) {
                        Map<String, Object> tags = (Map<String, Object>) o;
                        if (tags.containsKey(field.getTag())) {
                            if (!overridePredicate) {
                                predicate = (String) tags.get(field.getTag());
                            }
                            subfields = (Map<String, Object>) entity.getParams().get(predicate);
                            if (subfields == null) {
                                subfields = defaultSubfields;
                            }
                        }
                    }
                }
                // indicator-based predicate defined?
                if (entity.getParams().containsKey("indicators")) {
                    Map<String, Object> indicators = (Map<String, Object>) entity.getParams().get("indicators");
                    if (indicators.containsKey(field.getTag())) {
                        Map<String, Object> indicatorMap = (Map<String, Object>) indicators.get(field.getTag());
                        if (indicatorMap.containsKey(field.getIndicator())) {
                            if (!overridePredicate) {
                                predicate = (String) indicatorMap.get(field.getIndicator());
                                fieldNames.put(field, predicate);
                            }
                            subfields = (Map<String, Object>) entity.getParams().get(predicate);
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
                        Map<String, Object> vm = (Map<String, Object>) entity.getParams().get(fieldName);
                        if (vm == null) {
                            // fallback to "subfields"
                            vm = (Map<String, Object>) entity.getParams().get("subfields");
                        }
                        // is value containing a blank?
                        int pos = v.indexOf(' ');
                        // move after blank
                        String vv = pos > 0 ? v.substring(0, pos) : v;
                        // code table lookup
                        if (vm.containsKey(v)) {
                            newResource.add(me.getKey() + "Source", v);
                            v = (String) vm.get(v);
                        } else if (vm.containsKey(vv)) {
                            newResource.add(me.getKey() + "Source", v);
                            v = (String) vm.get(vv);
                        } else {
                            // relation by pattern?
                            List<Map<String, String>> patterns =
                                    (List<Map<String, String>>) entity.getParams().get(fieldName + "pattern");
                            if (patterns != null) {
                                for (Map<String, String> pattern : patterns) {
                                    Map.Entry<String, String> mme = pattern.entrySet().iterator().next();
                                    String p = mme.getKey();
                                    String rel = mme.getValue();
                                    Matcher m = Pattern.compile(p, Pattern.CASE_INSENSITIVE).matcher(v);
                                    if (m.matches()) {
                                        newResource.add(me.getKey() + "Source", v);
                                        v = rel;
                                        break;
                                    }
                                }
                            }
                        }
                    } else {
                        // default subfield map
                        String fieldName = me.getKey();
                        if (entity.getParams().containsKey(fieldName)) {
                            try {
                                Map<String, Object> vm = (Map<String, Object>) entity.getParams().get(fieldName);
                                int pos = v.indexOf(' ');
                                String vv = pos > 0 ? v.substring(0, pos) : v;
                                if (vm.containsKey(v)) {
                                    newResource.add(fieldName + "Source", v);
                                    v = (String) vm.get(v);
                                } else if (vm.containsKey(vv)) {
                                    newResource.add(fieldName + "Source", v);
                                    v = (String) vm.get(vv);
                                } else {
                                    // relation by pattern?
                                    List<Map<String, String>> patterns =
                                            (List<Map<String, String>>) entity.getParams().get(fieldName + "pattern");
                                    if (patterns != null) {
                                        for (Map<String, String> pattern : patterns) {
                                            Map.Entry<String, String> mme = pattern.entrySet().iterator().next();
                                            String p = mme.getKey();
                                            String rel = mme.getValue();
                                            Matcher m = Pattern.compile(p, Pattern.CASE_INSENSITIVE).matcher(v);
                                            if (m.matches()) {
                                                newResource.add(fieldName + "Source", v);
                                                v = rel;
                                                break;
                                            }
                                        }
                                    }
                                }
                            } catch (ClassCastException e) {
                                logger.log(Level.WARNING,
                                        MessageFormat.format("entity {0}: found {1} of class {2} in entity settings {3}"
                                                        + " for key {4} but must be a map",
                                                entity.getClass(),
                                                entity.getParams().get(fieldName),
                                                entity.getParams().get(fieldName).getClass(),
                                                entity.getParams(),
                                                fieldName));
                            }
                        }
                    }
                    // transform value v
                    if (v != null) {
                        v = entity.transform(this, predicate, newResource, me.getKey(), v);
                    }
                    // is this the predicate field or a value?
                    if (me.getKey().equals(predicate)) {
                        predicate = v;
                        overridePredicate = true;
                    } else {
                        newResource.add(me.getKey(), v);
                    }
                } else {
                    // no decoder, simple add field data
                    String property;
                    String subfieldId = subfield.getId();
                    if (subfieldId.isEmpty()) {
                        subfieldId = " "; // there are no empty subfield IDs except in MAB. Replace with space.
                    }
                    if (subfields.containsKey(subfieldId)) {
                        property = (String) subfields.get(subfieldId);
                        newResource.add(property, entity.transform(this, predicate, newResource, property,
                                subfield.getValue()));
                    } else {
                        entityBuilder.unmapped(getWorkerState().getRecordIdentifier(), field,
                                "field " + field + " missing definition for subfield '" + subfieldId + "'");
                    }
                }
            }
        }
        // rename, now that we know the predicate
        resource.rename(tempPredicate, IRI.builder().curie(predicate).build());
        return newResource;
    }

    @Override
    public void close() throws IOException {
        // nothing to do
    }
}
