package org.xbib.catalog.entities.marc.hol;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.content.rdf.Resource;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
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
        Resource info = worker.getWorkerState().getResource().newResource("GeneralInformation");
        examine(codes, info, value);
        return super.transform(worker, field);
    }

    @SuppressWarnings("unchecked")
    private void examine(Map<String, Object> codes, Resource info, String value)
            throws IOException {
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
                    info.add(pred, convertFromMarcDate(v));
                } else {
                    if (!"|".equals(v) && !"||".equals(v) && !"|||".equals(v) && !"|| ".equals(v)) {
                        info.add(pred, v);
                    }
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

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");

    // convert from yymmdd to ISO 8601 format
    private String convertFromMarcDate(String date) {
        if (date.indexOf('|') >= 0) {
            // invalid date / no date
            return null;
        }
        try {
            // yy maps to 2000-2099 which is wrong, cataloging era is somewhere around 1970-2070
            LocalDate localDatetime = LocalDate.parse(date, formatter);
            // adjust year. If in the future, subtract 100 to go back to 20th century cataloging.
            // one year tolerance, maybe "future cataloging"
            if (localDatetime.getYear() > Year.now().plusYears(1).getValue()) {
                localDatetime = localDatetime.minusYears(100);
            }
            return localDatetime.toString();
        } catch (Exception e) {
            logger.log(Level.WARNING, "unable to convert date: " + date, e);
            return date;
        }
    }
}
