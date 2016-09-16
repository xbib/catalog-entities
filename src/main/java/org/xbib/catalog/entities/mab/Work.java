package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.rdf.Resource;

import java.util.Map;

/**
 *
 */
public class Work extends CatalogEntity {

    public Work(Map<String, Object> params) {
        super(params);
    }

    @Override
    public String transform(CatalogEntityWorker worker,
                            String predicate, Resource resource, String property, String value) {
        if ("identifier".equals(property)) {
            resource.add("identifier", value);
            if (value.startsWith("(DE-588)")) {
                // GND-ID: upper case, with hyphen
                resource.add("identifierGND", value.substring(8));
            } else if (value.startsWith("(DE-101)")) {
                // DNB-ID: upper case, with hyphen
                resource.add("identifierDNB", value.substring(8));
            } else if (value.startsWith("(DE-600)")) {
                // ZDB-ID does not matter at all
                resource.add("identifierZDB", value.substring(8).replaceAll("\\-", "").toLowerCase());
                return value.replaceAll("\\-", "").toLowerCase();
            }
            return null;
        }
        // u00ac = ¬
        return value
                //.replace('\u0098', '\u00ac')
                //.replace('\u009c', '\u00ac')
                .replaceAll("<<(.*?)>>", "\u00ac$1\u00ac")
                .replaceAll("<(.*?)>", "[$1]")
                .replaceAll("¬(.*?)¬", "$1");
    }
}
