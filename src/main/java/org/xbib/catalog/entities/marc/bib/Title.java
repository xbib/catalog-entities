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
public class Title extends CatalogEntity {

    public Title(Map<String, Object> params) {
        super(params);
    }

    @Override
    public List<String> transform(CatalogEntityWorker worker,
                                  String predicate, Resource resource, String property, String value) {
        // the "core title". Can be a translated title of an original work. Tht is ok, because
        // we do not fold translations into one work.
        if ("TitleStatement".equals(predicate)
                || "FormerTitle".equals(predicate)
                || "VaryingTitle".equals(predicate)) {
            AuthoredWorkKey authoredWorkKey = worker.getWorkerState().getAuthoredWorkKey();
            PublishedJournalKey publishedJournalKey = worker.getWorkerState().getJournalKey();
            if ("title".equals(property)) {
                authoredWorkKey.workName(filter(value));
                publishedJournalKey.addTitle(filter(value));
            }
            if ("remainder".equals(property)
                    || "medium".equals(property)
                    || "partName".equals(property)
                    || "partNumber".equals(property)
                    ) {
                publishedJournalKey.addTitle(filter(value));
            }
        }
        // let's make "sorting" marker characters visible again
        // 0098 = START OF STRING, 009c = END OF STRING
        // --> 00ac = negation sign
        //return Collections.singletonList(value.replace('\u0098', '\u00ac').replace('\u009c', '\u00ac'));
        return Collections.singletonList(value);
    }

    // filter out forbidden titles
    private String filter(String s) {
        return "Elektronische Ressource".equals(s) ? null : s;
    }
}
