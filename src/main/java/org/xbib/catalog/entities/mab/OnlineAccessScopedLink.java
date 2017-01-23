package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.catalog.entities.CatalogEntityWorkerState;
import org.xbib.catalog.entities.Classifier;
import org.xbib.catalog.entities.ClassifierEntry;
import org.xbib.catalog.entities.TermFacet;
import org.xbib.content.rdf.Literal;
import org.xbib.content.rdf.Resource;
import org.xbib.content.resource.IRI;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class OnlineAccessScopedLink extends OnlineAccess {

    private static final String taxonomyFacet = "xbib.taxonomy";

    private static final String identifierFacet = "xbib.identifier";

    private String catalogid = "";

    public OnlineAccessScopedLink(Map<String, Object> params) {
        super(params);
        // override by "catalogid"
        if (params.containsKey("catalogid")) {
            this.catalogid = params.get("catalogid").toString();
        }
    }

    @Override
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        worker.append(worker.getWorkerState().getNextItemResource(), field, this);
        return null;
    }

    @Override
    public List<String> transform(CatalogEntityWorker worker,
                                  String predicate, Resource resource, String property, String value) {
        CatalogEntityWorkerState state = worker.getWorkerState();
        if ("url".equals(property)) {
            // create synthetic local record identifier
            state.setUID(IRI.builder().curie("uid:" +
                    state.getRecordIdentifier() + "/" + state.getISIL() + "/" + value).build());
        } else if ("scope".equals(property) && catalogid != null && !catalogid.isEmpty()) {
            String isil = catalogid;
            resource.add("identifier", isil);
            state.getFacets().putIfAbsent(identifierFacet,
                    new TermFacet().setName(identifierFacet).setType(Literal.STRING));
            state.getFacets().get(identifierFacet).addValue(isil);

            Classifier classifier = worker.getClassifier();
            if (classifier != null) {
                String key = isil + "." + state.getRecordIdentifier() + ".";
                java.util.Collection<ClassifierEntry> entries = classifier.lookup(key);
                if (entries != null) {
                    for (ClassifierEntry classifierEntry : entries) {
                        String facet = taxonomyFacet + "." + isil + ".notation";
                        state.getFacets().putIfAbsent(facet, new TermFacet().setName(facet).setType(Literal.STRING));
                        state.getFacets().get(facet).addValue(classifierEntry.getCode());
                        facet = taxonomyFacet + "." + isil + ".text";
                        state.getFacets().putIfAbsent(facet, new TermFacet().setName(facet).setType(Literal.STRING));
                        state.getFacets().get(facet).addValue(classifierEntry.getText());
                    }
                }
            }
            return Collections.singletonList(isil);
        }
        return Collections.singletonList(value);
    }
}
