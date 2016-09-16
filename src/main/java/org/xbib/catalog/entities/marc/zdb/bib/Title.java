package org.xbib.catalog.entities.marc.zdb.bib;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.rdf.Resource;

import java.util.Map;

public class Title extends CatalogEntity {

    public Title(Map<String, Object> params) {
        super(params);
    }

    @Override
    public String transform(CatalogEntityWorker worker,
                            String predicate, Resource resource, String property, String value) {
        // let's make "sorting" marker characters visible again
        // 0098 = START OF STRING, 009c = END OF STRING
        // --> 00ac = negation sign
        return value.replace('\u0098', '\u00ac').replace('\u009c', '\u00ac');
    }
}
