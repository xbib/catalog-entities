package org.xbib.catalog.entities.marc.bib;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.catalog.entities.matching.endeavor.AuthoredWork;
import org.xbib.content.rdf.Resource;

import java.util.Map;

/**
 *
 */
public class PublicationStatement extends CatalogEntity {

    public PublicationStatement(Map<String, Object> params) {
        super(params);
    }

    @Override
    public String transform(CatalogEntityWorker worker,
                            String predicate, Resource resource, String property, String value) {
        if ("dateOfPublication".equals(property)) {
            AuthoredWork authoredWork = worker.getWorkerState().getAuthoredWorkKey();
            authoredWork.year(value);
        }
        return value;
    }
}
