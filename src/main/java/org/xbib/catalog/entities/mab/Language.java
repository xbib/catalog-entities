package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.catalog.entities.CatalogEntityWorkerState;
import org.xbib.catalog.entities.Facet;
import org.xbib.catalog.entities.TermFacet;
import org.xbib.marc.MarcField;
import org.xbib.rdf.Literal;

import java.util.Map;

public class Language extends CatalogEntity {

    private static final String FACET_NAME = "dc.language";

    public Language(Map<String, Object> params) {
        super(params);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void facetize(CatalogEntityWorker worker, MarcField.Subfield field) {
        CatalogEntityWorkerState state = worker.getWorkerState();
        state.getFacets().putIfAbsent(getFacetName(), new TermFacet().setName(getFacetName()).setType(Literal.STRING));
        Facet<String> languageFacet = state.getFacets().get(getFacetName());
        String s = field.getValue();
        Map<String, String> languages = (Map<String, String>) getParams().get("language");
        if (languages == null) {
            return;
        }
        if (languages.containsKey(s)) {
            languageFacet.addValue(languages.get(s));
        }
    }

    @Override
    protected String getFacetName() {
        return FACET_NAME;
    }
}
