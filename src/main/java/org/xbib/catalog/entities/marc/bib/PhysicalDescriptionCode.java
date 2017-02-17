package org.xbib.catalog.entities.marc.bib;

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
        this.codes = (Map<String, Object>) params.get("codes");
    }

    @SuppressWarnings("unchecked")
    @Override
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        Map<String, Object> subcodes = null;
        if (codes != null) {
            subcodes = (Map<String, Object>) codes.get("0");
        }
        if (subcodes != null) {
            String data = getValue(field);
            Map<String, Object> m = (Map<String, Object>) codes.get(data.substring(0, 1));
            if (m != null) {
                Resource resource = worker.getWorkerState().getResource().newResource(getClass().getSimpleName());
                for (int i = 1; i < data.length(); i++) {
                    Map<String, Object> q = (Map<String, Object>) m.get(Integer.toString(i));
                    if (q != null) {
                        String predicate = (String) q.get("_predicate");
                        String code = data.substring(i, i + 1);
                        if (!"|".equals(code)) {
                            String value = (String) q.get(code);
                            if (value == null) {
                                logger.warning(worker.getWorkerState().getRecordIdentifier() +
                                        ": unspecified code '" + code + "' for " + predicate);
                            }
                            resource.add(predicate, value);
                        }
                    }
                }
            }
        }
        return super.transform(worker, field);
    }
}
