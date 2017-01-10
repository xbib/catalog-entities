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
public class SourceDate extends CatalogEntity {

    private String facet = "dc.date";

    public SourceDate(Map<String, Object> params) {
        super(params);
        if (params.containsKey("_facet")) {
            this.facet = params.get("_facet").toString();
        }
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
        state.getFacets().putIfAbsent(facet, new YearFacet().setName(facet).setType(Literal.GYEAR));
        state.getFacets().get(facet).addValue(value);
    }

}
