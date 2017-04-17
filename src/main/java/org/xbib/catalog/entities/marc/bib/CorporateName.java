package org.xbib.catalog.entities.marc.bib;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.catalog.entities.matching.endeavor.PublishedJournalKey;
import org.xbib.content.rdf.Resource;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class CorporateName extends CatalogEntity {

    public CorporateName(Map<String, Object> params) {
        super(params);
    }

    @Override
    public List<String> transform(CatalogEntityWorker worker,
                                  String predicate, Resource resource, String property, String value) {
        PublishedJournalKey publishedJournalKey = worker.getWorkerState().getJournalKey();
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
        if ("name".equals(property) || "unit".equals(property) || "place".equals(property)) {
            publishedJournalKey.addPublishingEntity(value);
        }
        return Collections.singletonList(value);
    }
}
