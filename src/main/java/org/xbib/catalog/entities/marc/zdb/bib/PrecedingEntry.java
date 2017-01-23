package org.xbib.catalog.entities.marc.zdb.bib;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.content.rdf.Resource;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class PrecedingEntry extends CatalogEntity {

    public PrecedingEntry(Map<String, Object> params) {
        super(params);
    }

    @Override
    public List<String> transform(CatalogEntityWorker worker,
                                  String predicate, Resource resource, String property, String value) {
        if ("id".equals(property)) {
            if (value.startsWith("(DE-600)")) {
                resource.add("identifierZDB", value.substring(8).replaceAll("\\-", "").toLowerCase());
                return null;
            } else if (value.startsWith("(DE-101)")) {
                resource.add("identifierDNB", value.substring(8).replaceAll("\\-", "").toLowerCase());
                return null;
            }
            return Collections.singletonList(value.replaceAll("\\-", "").toLowerCase());
        } else if ("title".equals(property)) {
            return Collections.singletonList(value);
        }
        return Collections.singletonList(value);
    }
}
