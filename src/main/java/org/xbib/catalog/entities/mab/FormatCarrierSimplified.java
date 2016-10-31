package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.catalog.entities.CatalogEntityWorkerState;
import org.xbib.catalog.entities.TermFacet;
import org.xbib.content.rdf.Literal;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.util.Map;

/**
 *
 */
public class FormatCarrierSimplified extends CatalogEntity {

    public static final String FACET = "dc.format";

    private String predicate;

    public FormatCarrierSimplified(Map<String, Object> params) {
        super(params);
        this.predicate = getClass().getSimpleName();
        if (params.containsKey("_predicate")) {
            this.predicate = params.get("_predicate").toString();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        Map<String, Object> codes = (Map<String, Object>) getParams().get("codes");
        if (codes == null) {
            return super.transform(worker, field);
        }
        Map<String, Object> facetcodes = (Map<String, Object>) getParams().get("facetcodes");
        if (facetcodes == null) {
            return super.transform(worker, field);
        }
        for (MarcField.Subfield subfield : field.getSubfields()) {
            String value = subfield.getValue();
            for (int i = 0; i < value.length(); i++) {
                // mapped codes
                String code = (String) codes.get(value.substring(i, i + 1));
                if (code == null && (i + 1 < value.length())) {
                    // two letters?
                    code = (String) codes.get(value.substring(i, i + 2));
                }
                worker.getWorkerState().getResource().add(predicate, code);
                // faceting
                code = (String) facetcodes.get(value.substring(i, i + 1));
                if (code == null && (i + 1 < value.length())) {
                    // two letters?
                    code = (String) facetcodes.get(value.substring(i, i + 2));
                }
                facetize(worker.getWorkerState(), code);
            }
        }
        return super.transform(worker, field); // done!
    }

    private void facetize(CatalogEntityWorkerState state, String value) {
        state.getFacets().putIfAbsent(FACET, new TermFacet().setName(FACET).setType(Literal.STRING));
        state.getFacets().get(FACET).addValue(value);
    }

}
