package org.xbib.catalog.entities.marc.zdb.bib;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.rdf.Resource;

import java.util.Map;

public class CustomIdentifier extends CatalogEntity {

    public CustomIdentifier(Map<String, Object> params) {
        super(params);
    }

    @Override
    public String transform(CatalogEntityWorker worker,
                            String predicate, Resource resource, String property, String value) {
        if ("IdentifierZDB".equals(predicate)) {
            if ("value".equals(property)) {
                resource.add("identifierZDB", value.replaceAll("\\-", "").toLowerCase());
                return null;
            }
        } else if ("IdentifierDNB".equals(predicate)) {
            if ("value".equals(property)) {
                resource.add("identifierDNB", value.replaceAll("\\-", "").toLowerCase());
                return null;
            }
        }
        return value;
    }
}
