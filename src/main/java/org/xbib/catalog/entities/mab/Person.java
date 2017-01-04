package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.catalog.entities.IdentifierMapper;
import org.xbib.content.rdf.Resource;

import java.util.Map;

/**
 *
 */
public class Person extends CatalogEntity {

    public Person(Map<String, Object> params) {
        super(params);
    }

    @Override
    public String transform(CatalogEntityWorker worker,
                            String predicate, Resource resource, String property, String value) {
        if ("personIdentifier".equals(property)) {
            // add at first the unchanged identifier
            resource.add("personIdentifier", value);
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
            // returning null is very important for GND referencing
            return null;
        } else if ("source".equals(property)) {
            IdentifierMapper mapper = worker.getIdentifierMapper();
            if (mapper != null) {
                String isil = mapper.lookup(value);
                if (isil != null) {
                    resource.add("personSource", isil);
                    return null;
                }
            }
        }
        return value
                .replaceAll("<<(.*?)>>", "\u0098$1\u009C")
                .replaceAll("<(.*?)>", "[$1]")
                .replaceAll("¬(.*?)¬", "\u0098$1\u009C");

    }
}
