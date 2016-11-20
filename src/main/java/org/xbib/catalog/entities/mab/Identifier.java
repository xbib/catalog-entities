package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.content.rdf.Resource;

import java.util.Map;

/**
 *
 */
public class Identifier extends CatalogEntity {

    public Identifier(Map<String, Object> params) {
        super(params);
    }

    @Override
    public String transform(CatalogEntityWorker worker,
                            String predicate, Resource resource, String property, String string) {
        String value = string;
        if ("IdentifierZDB".equals(predicate) && "identifierZDB".equals(property)) {
            if (value.startsWith("(DE-599)")) {
                resource.add("identifierEKI", value.substring(8));
                return null;
            } else if (value.startsWith("(OCoLC)")) {
                resource.add("identifierOCLC", value.substring(7).replaceAll("\\-", "").toLowerCase());
                return null;
            } else {
                int pos = value.indexOf(')');
                String prefix = pos > 0 ? value.substring(1, pos).toUpperCase() : "ZDB";
                value = pos > 0 ? value.substring(pos + 1) : value;
                resource.add("identifier" + prefix, value.replaceAll("\\-", "").toLowerCase());
                return null;
            }
        }
        return value;
    }
}
