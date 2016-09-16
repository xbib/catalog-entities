package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.util.Map;

/**
 *
 */
public class TypeMicroform extends CatalogEntity {

    private final Map<String, Object> codes;
    private final Map<String, Object> facetcodes;
    private String facet = "dc.format";

    public TypeMicroform(Map<String, Object> params) {
        super(params);
        if (params.containsKey("_facet")) {
            this.facet = params.get("_facet").toString();
        }
        codes = getCodes();
        facetcodes = getFacetCodes();
    }

    @Override
    @SuppressWarnings("unchecked")
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        String value = getValue(field);
        if (codes == null) {
            throw new IllegalStateException("no codes section for " + field);
        }
        String predicate = (String) codes.get("_predicate");
        if (predicate == null) {
            predicate = this.getClass().getSimpleName();
        }
        for (int i = 0; i < value.length(); i++) {
            Map<String, Object> q = (Map<String, Object>) codes.get(Integer.toString(i));
            if (q != null) {
                String code = (String) q.get(value.substring(i, i + 1));
                if (code == null && (i + 1 < value.length())) {
                    // two letters?
                    code = (String) q.get(value.substring(i, i + 2));
                }
                worker.getWorkerState().getResource().add(predicate, code);
            }
        }
        if (facetcodes != null) {
            for (int i = 0; i < value.length(); i++) {
                Map<String, Object> q = (Map<String, Object>) facetcodes.get(Integer.toString(i));
                if (q != null) {
                    String code = (String) q.get(value.substring(i, i + 1));
                    if (code == null && (i + 1 < value.length())) {
                        // two letters?
                        code = (String) q.get(value.substring(i, i + 2));
                    }
                    if (code != null) {
                        facetize(worker, code);
                    }
                }
            }
        }
        return null; // done!
    }

    @Override
    protected String getFacetName() {
        return facet;
    }
}
