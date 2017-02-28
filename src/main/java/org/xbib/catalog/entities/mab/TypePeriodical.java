package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.content.rdf.Resource;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.util.Map;

/**
 *
 */
public class TypePeriodical extends CatalogEntity {

    private String facet = "dc.type";

    private Map<String, Object> codes;

    private Map<String, Object> facetcodes;

    public TypePeriodical(Map<String, Object> params) {
        super(params);
        if (params.containsKey("_facet")) {
            this.facet = params.get("_facet").toString();
        }
        this.codes = getCodes();
        this.facetcodes = getFacetCodes();
    }

    @Override
    @SuppressWarnings("unchecked")
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        String value = getValue(field);
        if (codes != null) {
            Resource resource = worker.getWorkerState().getResource().newResource("TypeMonograph");
            for (int i = 0; i < value.length(); i++) {
                Map<String, Object> q = (Map<String, Object>) codes.get(Integer.toString(i));
                if (q != null) {
                    String code = (String) q.get(value.substring(i, i + 1));
                    if (code == null && i + 1 < value.length()) {
                        // two letters?
                        code = (String) q.get(value.substring(i, i + 2));
                    }
                    resource.add("value", code);
                }
            }
        }
        if (facetcodes != null) {
            for (int i = 0; i < value.length(); i++) {
                Map<String, Object> q = (Map<String, Object>) facetcodes.get(Integer.toString(i));
                if (q != null) {
                    String code = (String) q.get(value.substring(i, i + 1));
                    if (code == null && i + 1 < value.length()) {
                        // two letters?
                        code = (String) q.get(value.substring(i, i + 2));
                    }
                    if (code != null) {
                        facetize(worker, code);
                    }
                }
            }
        }
        return null; // done
    }

    @Override
    protected String getFacetName() {
        return facet;
    }
}

