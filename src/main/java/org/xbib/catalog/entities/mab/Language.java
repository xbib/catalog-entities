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
public class Language extends CatalogEntity {

    private static final String FACET_NAME = "dc.language";

    public Language(Map<String, Object> params) {
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

    @SuppressWarnings("unchecked")
    protected void facetize(CatalogEntityWorker worker, String value) {
        CatalogEntityWorkerState state = worker.getWorkerState();
        state.getFacets().putIfAbsent(getFacetName(), new TermFacet().setName(getFacetName()).setType(Literal.STRING));
        TermFacet languageFacet = state.getFacets().get(getFacetName());
        Map<String, String> languages = (Map<String, String>) getParams().get("language");
        if (languages == null) {
            return;
        }
        if (languages.containsKey(value)) {
            languageFacet.addValue(languages.get(value));
        }
    }

    @Override
    protected String getFacetName() {
        return FACET_NAME;
    }
}
