package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.util.Map;

/**
 *
 */
public class TypeMonographSimplified extends CatalogEntity {

    private static final String FACET_NAME = "dc.type";

    private String predicate;

    private Map<String, Object> codes;

    private Map<String, Object> facetcodes;

    public TypeMonographSimplified(Map<String, Object> params) {
        super(params);
        this.predicate = this.getClass().getSimpleName();
        if (params.containsKey("_predicate")) {
            this.predicate = params.get("_predicate").toString();
        }
        this.codes = getCodes();
        this.facetcodes = getFacetCodes();
    }

    @Override
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        String value = getValue(field);
        if (codes != null) {
            for (int i = 0; i < value.length(); i++) {
                String code = (String) codes.get(value.substring(i, i + 1));
                if (code == null && (i + 1 < value.length())) {
                    // two letters?
                    code = (String) codes.get(value.substring(i, i + 2));
                }
                worker.getWorkerState().getResource().add(predicate, code);
            }
        }
        if (facetcodes != null) {
            for (int i = 0; i < value.length(); i++) {
                String code = (String) facetcodes.get(value.substring(i, i + 1));
                if (code == null && (i + 1 < value.length())) {
                    // two letters?
                    code = (String) facetcodes.get(value.substring(i, i + 2));
                }
                facetize(worker, code);
            }
        }
        return null; // done!
    }

    @Override
    protected String getFacetName() {
        return FACET_NAME;
    }


}
