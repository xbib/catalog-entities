package org.xbib.catalog.entities.marc.bib;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.catalog.entities.matching.endeavor.AuthoredWork;
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
            AuthoredWork authoredWork = worker.getWorkerState().getAuthoredWorkKey();
            authoredWork.authorName(value);
        }
        return Collections.singletonList(value);
    }
}
