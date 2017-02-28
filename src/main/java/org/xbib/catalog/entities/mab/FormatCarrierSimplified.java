package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.catalog.entities.TermFacet;
import org.xbib.content.rdf.Literal;
import org.xbib.content.rdf.Resource;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.util.Map;

/**
 *
 */
public class FormatCarrierSimplified extends CatalogEntity {

    public static final String FACET = "dc.format";

    public FormatCarrierSimplified(Map<String, Object> params) {
        super(params);
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
        Resource resource = worker.getWorkerState().getResource().newResource("FormatCarrierSimplified");
        for (MarcField.Subfield subfield : field.getSubfields()) {
            String value = subfield.getValue();
            for (int i = 0; i < value.length(); i++) {
                // mapped codes
                String code = (String) codes.get(value.substring(i, i + 1));
                if (code == null && (i + 1 < value.length())) {
                    // two letters?
                    code = (String) codes.get(value.substring(i, i + 2));
                }
                resource.add("value", code);
                // faceting
                code = (String) facetcodes.get(value.substring(i, i + 1));
                if (code == null && (i + 1 < value.length())) {
                    // two letters?
                    code = (String) facetcodes.get(value.substring(i, i + 2));
                }
                worker.getWorkerState().getFacets().putIfAbsent(FACET, new TermFacet().setName(FACET).setType(Literal.STRING));
                worker.getWorkerState().getFacets().get(FACET).addValue(code);
            }
        }
        return super.transform(worker, field); // done!
    }
}
