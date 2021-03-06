package org.xbib.catalog.entities.marc.bib;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.catalog.entities.matching.endeavor.AuthoredWorkKey;
import org.xbib.content.rdf.Resource;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class PersonalName extends CatalogEntity {

    public PersonalName(Map<String, Object> params) {
        super(params);
    }

    @Override
    public List<String> transform(CatalogEntityWorker worker,
                                  String predicate, Resource resource, String property, String value) {
        if ("personalName".equals(property)) {
            AuthoredWorkKey authoredWorkKey = worker.getWorkerState().getAuthoredWorkKey();
            authoredWorkKey.authorName(value);
        }
        if ("identifier".equals(property)) {
            if (value.startsWith("(DE-588)")) {
                resource.add("identifierGND", value.substring(8).replaceAll("\\-", "").toLowerCase());
                return null;
            } else if (value.startsWith("(DE-600)")) {
                resource.add("identifierZDB", value.substring(8).replaceAll("\\-", "").toLowerCase());
                return null;
            } else if (value.startsWith("(DE-101)")) {
                resource.add("identifierDNB", value.substring(8).replaceAll("\\-", "").toLowerCase());
                return null;
            }
            return Collections.singletonList(value.replaceAll("\\-", "").toLowerCase());
        }
        return Collections.singletonList(value);
    }
}
