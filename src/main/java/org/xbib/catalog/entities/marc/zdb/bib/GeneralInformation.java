package org.xbib.catalog.entities.marc.zdb.bib;

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

    private Map<String, Object> continuingresource;

    @SuppressWarnings("unchecked")
    public GeneralInformation(Map<String, Object> params) {
        super(params);
        this.codes = (Map<String, Object>) params.get("codes");
        this.continuingresource = (Map<String, Object>) params.get("continuingresource");
    }

    /**
     * Example
     * "991118d19612006xx z||p|r ||| 0||||0ger c"
     *
     * "091130||||||||||||||||ger|||||||"
     */
    @Override
    @SuppressWarnings("unchecked")
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        for (MarcField.Subfield subfield : field.getSubfields()) {
            String data = subfield.getValue();
            String date1 = data.length() > 11 ? data.substring(7, 11) : "0000";
            worker.getWorkerState().getResource().add("date1", check(date1));
            String date2 = data.length() > 15 ? data.substring(11, 15) : "0000";
            worker.getWorkerState().getResource().add("date2", check(date2));
            for (int i = 0; i < data.length(); i++) {
                String ch = data.substring(i, i + 1);
                if ("|".equals(ch) || " ".equals(ch)) {
                    continue;
                }
                if (codes != null) {
                    Map<String, Object> q = (Map<String, Object>) codes.get(Integer.toString(i));
                    if (q != null) {
                        String predicate = (String) q.get("_predicate");

                        String code = (String) q.get(ch);
                        if (code == null) {
                            logger.log(Level.WARNING,
                                    MessageFormat.format("unmapped code {0} in field {2} predicate {3}",
                                            ch, field, predicate));
                        }
                        worker.getWorkerState().getResource().add(predicate, code);
                    }
                }
                if (continuingresource != null) {
                    Map<String, Object> q = (Map<String, Object>) continuingresource.get(Integer.toString(i));
                    if (q != null) {
                        String predicate = (String) q.get("_predicate");
                        String code = (String) q.get(ch);
                        if (code == null) {
                            logger.log(Level.WARNING,
                                    MessageFormat.format("unmapped code {0} in field {1} predicate {2}",
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
            if (d < 1450) {
                if (d > 0) {
                    logger.log(Level.WARNING, MessageFormat.format("very early date ignored: {0}", d));
                }
                return null;
            }
            if (d == 9999) {
                return null;
            }
            return d;
        } catch (Exception e) {
            return null;
        }
    }
}
