package org.xbib.catalog.entities.marc.zdb.hol;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.content.rdf.Resource;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 */
public class PhysicalDescriptionCode extends CatalogEntity {

    private static final Logger logger = Logger.getLogger(PhysicalDescriptionCode.class.getName());

    private final Map<String, Object> codes;

    @SuppressWarnings("unchecked")
    public PhysicalDescriptionCode(Map<String, Object> params) {
        super(params);
        this.codes = (Map<String, Object>) getParams().get("codes");
    }

    @Override
    @SuppressWarnings("unchecked")
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        if (codes != null) {
            // position 0 is the selector
            Map<String, Object> subcodes = (Map<String, Object>) codes.get("0");
            String value = getValue(field);
            if (value != null) {
                Map<String, Object> m = (Map<String, Object>) subcodes.get(value.substring(0, 1));
                if (m != null) {
                    Resource resource = worker.getWorkerState().getResource().newResource(getClass().getSimpleName());
                    // transform all codes except position 0
                    for (int i = 1; i < value.length(); i++) {
                        String ch = value.substring(i, i + 1);
                        Map<String, Object> q = (Map<String, Object>) m.get(Integer.toString(i));
                        if (q != null) {
                            String predicate = (String) q.get("_predicate");
                            String code = (String) q.get(ch);
                            resource.add(predicate, code);
                        }
                    }
                }
            }
        }
        return super.transform(worker, field);
    }
}
