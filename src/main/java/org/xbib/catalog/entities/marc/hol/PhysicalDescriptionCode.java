package org.xbib.catalog.entities.marc.hol;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PhysicalDescriptionCode extends CatalogEntity {

    private static final Logger logger = Logger.getLogger(PhysicalDescriptionCode.class.getName());

    public PhysicalDescriptionCode(Map<String, Object> params) {
        super(params);
    }

    @SuppressWarnings("unchecked")
    @Override
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        String value = getValue(field);
        Map<String, Object> codes = (Map<String, Object>) getParams().get("codes");
        if (codes == null) {
            logger.log(Level.WARNING, "no 'codes' for " + value);
            return super.transform(worker, field);
        }
        // position 0 is the selector
        codes = (Map<String, Object>) codes.get("0");
        if (value != null) {
            check(worker, codes, value);
        }
        for (MarcField.Subfield subfield : field.getSubfields()) {
            value = subfield.getValue();
            check(worker, codes, value);
        }
        return super.transform(worker, field);
    }

    @SuppressWarnings("unchecked")
    private void check(CatalogEntityWorker worker,
                       Map<String, Object> codes, String data) throws IOException {
        Map<String, Object> m = (Map<String, Object>) codes.get(data.substring(0, 1));
        if (m == null) {
            return;
        }
        // transform all codes except position 0
        String predicate = (String) m.get("_predicate");
        for (int i = 1; i < data.length(); i++) {
            String ch = data.substring(i, i + 1);
            Map<String, Object> q = (Map<String, Object>) m.get(Integer.toString(i));
            if (q != null) {
                String code = (String) q.get(ch);
                worker.getWorkerState().getResource().add(predicate, code);
            }
        }
    }
}
