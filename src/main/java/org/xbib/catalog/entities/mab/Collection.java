package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.TermFacet;
import org.xbib.content.rdf.Literal;

import java.util.Map;

/**
 *
 */
public class Collection extends CatalogEntity {

    public static final String FACET = "xbib.collection";

    private String prefix = "";

    public Collection(Map<String, Object> params) {
        super(params);
        if (params.containsKey("collection")) {
            this.prefix = params.get("collection").toString();
        }
    }

    @Override
    public TermFacet getDefaultFacet() {
        return prefix != null ? new TermFacet().setName(FACET).setType(Literal.STRING).addValue(prefix) : null;
    }

}
