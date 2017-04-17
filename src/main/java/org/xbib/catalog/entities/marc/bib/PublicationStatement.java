package org.xbib.catalog.entities.marc.bib;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.catalog.entities.matching.endeavor.AuthoredWorkKey;
import org.xbib.catalog.entities.matching.endeavor.PublishedJournalKey;
import org.xbib.content.rdf.Resource;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class PublicationStatement extends CatalogEntity {

    public PublicationStatement(Map<String, Object> params) {
        super(params);
    }

    @Override
    public List<String> transform(CatalogEntityWorker worker,
                                  String predicate, Resource resource, String property, String value) {
        if ("dateOfPublication".equals(property)) {
            AuthoredWorkKey authoredWorkKey = worker.getWorkerState().getAuthoredWorkKey();
            authoredWorkKey.year(value);
        }
        if ("publisherName".equals(property) || "placeOfPublication".equals(property)) {
            PublishedJournalKey publishedJournalKey = worker.getWorkerState().getJournalKey();
            publishedJournalKey.addPublishingEntity(value);
        }
        return Collections.singletonList(value);
    }
}
