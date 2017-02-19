package org.xbib.catalog.entities.marc.bib;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.content.rdf.Resource;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class CustomIdentifier extends CatalogEntity {

    public CustomIdentifier(Map<String, Object> params) {
        super(params);
    }

    @Override
    public List<String> transform(CatalogEntityWorker worker,
                                  String predicate, Resource resource, String property, String value) {
        if ("value".equals(property)) {
            if ("IdentifierZDB".equals(predicate)) {
                resource.add("identifierZDB", value.replaceAll("\\-", "").toLowerCase());
                return null;
            } else if ("IdentifierDNB".equals(predicate)) {
                resource.add("identifierDNB", value.replaceAll("\\-", "").toUpperCase());
                return null;
            }
        }
        return Collections.singletonList(value);
    }
}
