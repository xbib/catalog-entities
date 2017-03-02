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
public class Country extends CatalogEntity {

    private static final String FACET_NAME = "dc.coverage";

    public Country(Map<String, Object> params) {
        super(params);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        super.transform(worker, field);
        facetize(worker, field.getValue());
        for (MarcField.Subfield subfield : field.getSubfields()) {
            facetize(worker, subfield.getValue());
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void facetize(CatalogEntityWorker worker, String value) {
        CatalogEntityWorkerState state = worker.getWorkerState();
        state.getFacets().putIfAbsent(getFacetName(), new TermFacet().setName(getFacetName()).setType(Literal.STRING));
        TermFacet countryFacet = state.getFacets().get(getFacetName());
        Map<String, String> countries = (Map<String, String>) getParams().get("countryCode");
        if (countries == null) {
            return;
        }
        if (countries.containsKey(value)) {
            countryFacet.addValue(countries.get(value));
        }
    }

    @Override
    protected String getFacetName() {
        return FACET_NAME;
    }
}
