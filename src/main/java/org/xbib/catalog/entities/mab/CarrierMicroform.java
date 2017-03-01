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
public class CarrierMicroform extends CatalogEntity {

    private static final String FACET_NAME = "dc.format";

    private final Map<String, Object> codes;

    private final Map<String, Object> facetcodes;

    public CarrierMicroform(Map<String, Object> params) {
        super(params);
        codes = getCodes();
        facetcodes = getFacetCodes();
    }

    @Override
    @SuppressWarnings("unchecked")
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        String value = getValue(field);
        if (codes != null) {
            Resource resource = worker.getWorkerState().getResource().newResource("Carrier");
            for (int i = 0; i < value.length(); i++) {
                Map<String, Object> q = (Map<String, Object>) codes.get(Integer.toString(i));
                if (q != null) {
                    String code = (String) q.get(value.substring(i, i + 1));
                    if (code == null && (i + 1 < value.length())) {
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
        return FACET_NAME;
    }
}
