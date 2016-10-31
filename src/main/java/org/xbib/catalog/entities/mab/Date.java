package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.catalog.entities.CatalogEntityWorkerState;
import org.xbib.catalog.entities.YearFacet;
import org.xbib.content.rdf.Literal;
import org.xbib.marc.MarcField;

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
    public void facetize(CatalogEntityWorker worker, MarcField.Subfield field) {
        CatalogEntityWorkerState state = worker.getWorkerState();
        state.getFacets().putIfAbsent(getFacetName(), new YearFacet().setName(getFacetName()).setType(Literal.GYEAR));
        YearFacet dateFacet = (YearFacet) state.getFacets().get(getFacetName());
        dateFacet.addValue(field.getValue());
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
