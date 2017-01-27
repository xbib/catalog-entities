package org.xbib.catalog.entities.marc.zdb.bib;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.content.rdf.Resource;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.text.MessageFormat;
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

    @SuppressWarnings("unchecked")
    public GeneralInformation(Map<String, Object> params) {
        super(params);
        this.codes = (Map<String, Object>) params.get("codes");
    }

    @SuppressWarnings("unchecked")
    @Override
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        String value = getValue(field);
        if (value.length() != 40) {
            logger.log(Level.WARNING,
                    "broken GeneralInformation field, length is not 40, but " + value.length() + " field=" + field);
        }
        Resource info = worker.getWorkerState().getResource().newResource("GeneralInformation");
        examine(codes, info, value);
        List<String> resourceTypes = worker.getWorkerState().getResourceType();
        if (resourceTypes != null && !resourceTypes.isEmpty()) {
            for (String resourceType : resourceTypes) {
                Map<String, Object> map = (Map<String, Object>) getParams().get(resourceType);
                examine(map, info, value);
            }
        }
        return super.transform(worker, field);
    }

    @SuppressWarnings("unchecked")
    private void examine(Map<String, Object> codes, Resource info, String value)
            throws IOException {
        if (codes == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : codes.entrySet()) {
            String key = entry.getKey();
            // from-to
            int pos = key.indexOf('-');
            String fromStr = pos > 0 ? key.substring(0, pos) : key;
            String toStr = pos > 0 ? key.substring(pos + 1) : key;
            int from = Integer.parseInt(fromStr);
            int to = fromStr.equals(toStr) ? from + 1 : Integer.parseInt(toStr) + 1;
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
                String v = value.length() >= to ? value.substring(from, to) : "|";
                String predicate = (String) values.get("_predicate");
                if (predicate != null && !"|".equals(v) && !"||".equals(v) && !"|||".equals(v) && !"|| ".equals(v)) {
                    if (values.containsKey(v)) {
                        info.add(predicate, (String) values.get(v));
                    } else {
                        logger.log(Level.WARNING, () ->
                                MessageFormat.format("undefined general information code {0}, key {1}, in field {2}",
                                        v, predicate, value));
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
            if (d < 1450) {
                logger.log(Level.WARNING, () -> MessageFormat.format("very early date ignored: {0}", d));
                return null;
            }
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
