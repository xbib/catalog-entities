package org.xbib.catalog.entities.marc.bib;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.content.rdf.Resource;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class GeneralInformation extends CatalogEntity {

    private static final Logger logger = Logger.getLogger(GeneralInformation.class.getName());

    private final Map<String, Object> codes;

    private final Map<String, Object> undefinedCodes;

    @SuppressWarnings("unchecked")
    public GeneralInformation(Map<String, Object> params) {
        super(params);
        this.codes = (Map<String, Object>) params.get("codes");
        this.undefinedCodes = new HashMap<>();
    }

    /**
     * Example "991118d19612006xx z||p|r ||| 0||||0ger c".
     */
    @SuppressWarnings("unchecked")
    @Override
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        String value = getValue(field);
        Resource info = worker.getWorkerState().getResource().newResource("GeneralInformation");
        examine(codes, info, value);
        List<String> resourceTypes = worker.getWorkerState().getResourceType();
        if (resourceTypes != null && !resourceTypes.isEmpty()) {
            for (String resourceType : resourceTypes) {
                Map<String, Object> map = (Map<String, Object>) getParams().get(resourceType);
                if (map != null) {
                    examine(map, info, value);
                } else {
                    logger.warning("no codes for resource type '" + resourceType + "'");
                }
            }
        }
        return super.transform(worker, field);
    }

    @SuppressWarnings("unchecked")
    private void examine(Map<String, Object> codes, Resource info, String value) throws IOException {
        for (Map.Entry<String, Object> entry : codes.entrySet()) {
            String key = entry.getKey();
            // from-to
            int pos = key.indexOf('-');
            String fromStr = pos > 0 ? key.substring(0, pos) : key;
            String toStr = pos > 0 ? key.substring(pos + 1) : key;
            int from = Integer.parseInt(fromStr);
            int to = fromStr.equals(toStr) ? from + 1 : Integer.parseInt(toStr) + 1;
            if (to > value.length()) {
                continue;
            }
            if (entry.getValue() instanceof String) {
                String pred = entry.getValue().toString();
                String v = value.substring(from, to);
                if (pred.startsWith("date")) {
                    info.add(pred, checkDate(v));
                } else {
                    info.add(pred, v);
                }
            } else if (entry.getValue() instanceof Map) {
                Map<String, Object> values = (Map<String, Object>) entry.getValue();
                String v = value.substring(from, to);
                String predicate = (String) values.get("_predicate");
                if (predicate != null && !"|".equals(v) && !"||".equals(v) && !"|||".equals(v) && !"|| ".equals(v)) {
                    if (values.containsKey(v)) {
                        info.add(predicate, (String) values.get(v));
                    } else {
                        if (!undefinedCodes.containsKey(key + "_" + v)) {
                            undefinedCodes.put(key + "_" + v, true);
                            logger.log(Level.WARNING, () ->
                                    MessageFormat.format("undefined general information code {0}, key {1}, in field {2}",
                                            v, predicate, value));
                        }
                    }
                }
            }
        }
    }

    // check for valid date, else return null
    private Integer checkDate(String date) {
        if ("    ".equals(date)) {
            return null;
        }
        try {
            int d = Integer.parseInt(date);
            if (d == 9999) {
                return null;
            }
            return d;
        } catch (Exception e) {
            logger.log(Level.FINEST, e.getMessage(), e);
            return null;
        }
    }
}
