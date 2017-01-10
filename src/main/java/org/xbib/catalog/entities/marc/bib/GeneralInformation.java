package org.xbib.catalog.entities.marc.bib;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class GeneralInformation extends CatalogEntity {

    private static final Logger logger = Logger.getLogger(GeneralInformation.class.getName());

    private Map<String, Object> codes;

    @SuppressWarnings("unchecked")
    public GeneralInformation(Map<String, Object> params) {
        super(params);
        this.codes = (Map<String, Object>) params.get("codes");
    }

    /**
     * Example "991118d19612006xx z||p|r ||| 0||||0ger c".
     */
    @SuppressWarnings("unchecked")
    @Override
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        String value = getValue(field);
        if (value.length() != 40) {
            logger.log(Level.WARNING, "broken GeneralInformation field, length is not 40");
        }
        String date1 = value.length() > 11 ? value.substring(7, 11) : "0000";
        worker.getWorkerState().getResource().add("date1", check(date1));
        String date2 = value.length() > 15 ? value.substring(11, 15) : "0000";
        worker.getWorkerState().getResource().add("date2", check(date2));
        for (int i = 0; i < value.length(); i++) {
            String ch = value.substring(i, i + 1);
            if ("|".equals(ch) || " ".equals(ch)) {
                continue;
            }
            if (codes != null) {
                Map<String, Object> q = (Map<String, Object>) codes.get(Integer.toString(i));
                if (q != null) {
                    String predicate = (String) q.get("_predicate");
                    if (predicate == null) {
                        logger.log(Level.WARNING, () -> MessageFormat.format("no predicate set, code {0}, field {1}",
                                ch, field));
                    } else {
                        String code = (String) q.get(ch);
                        if (code == null) {
                            logger.log(Level.WARNING,
                                    () -> MessageFormat.format("unmapped code {0} in field {1} predicate {2}",
                                            ch, field, predicate));
                        }
                        worker.getWorkerState().getResource().add(predicate, code);
                    }
                }
            }
        }
        return super.transform(worker, field);
    }

    // check for valid date, else return null
    private Integer check(String date) {
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
