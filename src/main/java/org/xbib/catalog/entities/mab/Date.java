package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.catalog.entities.CatalogEntityWorkerState;
import org.xbib.catalog.entities.YearFacet;
import org.xbib.content.rdf.Literal;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.util.Map;

/**
 *
 */
public class Date extends CatalogEntity {

    private static final String FACET_NAME = "dc.date";

    public Date(Map<String, Object> params) {
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
    public void facetize(CatalogEntityWorker worker, String value) {
        CatalogEntityWorkerState state = worker.getWorkerState();
        state.getFacets().putIfAbsent(getFacetName(), new YearFacet().setName(getFacetName()).setType(Literal.GYEAR));
        YearFacet dateFacet = (YearFacet) state.getFacets().get(getFacetName());
        dateFacet.addValue(value);
    }

    @Override
    public YearFacet getDefaultFacet() {
        YearFacet dateFacet = new YearFacet();
        dateFacet.setName(getFacetName()).setType(Literal.GYEAR);
        return dateFacet;
    }

    @Override
    protected String getFacetName() {
        return FACET_NAME;
    }
}
