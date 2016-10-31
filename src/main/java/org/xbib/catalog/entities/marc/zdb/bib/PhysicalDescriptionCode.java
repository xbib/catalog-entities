package org.xbib.catalog.entities.marc.zdb.bib;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.util.Map;

/**
 *
 */
public class PhysicalDescriptionCode extends CatalogEntity {

    public PhysicalDescriptionCode(Map<String, Object> params) {
        super(params);
    }

    @SuppressWarnings("unchecked")
    @Override
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        Map<String, Object> codes = (Map<String, Object>) getParams().get("codes");
        if (codes != null) {
            // position 0 is the selector
            codes = (Map<String, Object>) codes.get("0");
        }
        if (codes == null) {
            return super.transform(worker, field);
        }
        for (MarcField.Subfield subfield : field.getSubfields()) {
            String data = subfield.getValue();
            Map<String, Object> m = (Map<String, Object>) codes.get(data.substring(0, 1));
            if (m == null) {
                continue;
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
        return super.transform(worker, field);
    }

}
