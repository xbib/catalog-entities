package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.catalog.entities.CatalogEntityWorkerState;
import org.xbib.catalog.entities.YearFacet;
import org.xbib.marc.MarcField;
import org.xbib.rdf.Literal;

import java.util.Map;

/**
 *
 */
public class SourceDate extends CatalogEntity {

    private String facet = "dc.date";

    public SourceDate(Map<String, Object> params) {
        super(params);
        if (params.containsKey("_facet")) {
            this.facet = params.get("_facet").toString();
        }
    }

    @Override
    public void facetize(CatalogEntityWorker worker, MarcField.Subfield field) {
        CatalogEntityWorkerState state = worker.getWorkerState();
        state.getFacets().putIfAbsent(facet, new YearFacet().setName(facet).setType(Literal.GYEAR));
        state.getFacets().get(facet).addValue(field.getValue());
    }

}
