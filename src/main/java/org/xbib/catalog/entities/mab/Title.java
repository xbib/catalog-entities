package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.rdf.Resource;

import java.util.Map;

/**
 *
 */
public class Title extends CatalogEntity {

    public Title(Map<String, Object> params) {
        super(params);
    }

    @Override
    public String transform(CatalogEntityWorker worker,
                            String predicate, Resource resource, String property, String value) {
        return value
                //.replace('\u0098', '\u00ac')
                //.replace('\u009c', '\u00ac')
                .replaceAll("<<(.*?)>>", "¬$1¬")
                .replaceAll("<(.*?)>", "[$1]")
                .replaceAll("¬(.*?)¬", "$1");
    }

}
