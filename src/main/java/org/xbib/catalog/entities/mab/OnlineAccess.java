package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.catalog.entities.CatalogEntityWorkerState;
import org.xbib.catalog.entities.Classifier;
import org.xbib.catalog.entities.ClassifierEntry;
import org.xbib.catalog.entities.IdentifierMapper;
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
public class OnlineAccess extends CatalogEntity {

    private static final String taxonomyFacet = "xbib.taxonomy";

    private static final String identifierFacet = "xbib.identifier";

    public OnlineAccess(Map<String, Object> params) {
        super(params);
    }

    @Override
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField fields) throws IOException {
        worker.append(worker.getWorkerState().getNextItemResource(), fields, this);
        return null;
    }

    @Override
    public List<String> transform(CatalogEntityWorker worker,
                                  String predicate, Resource resource, String property, String value) {
        CatalogEntityWorkerState state = worker.getWorkerState();
        String isil = value;
        if ("uri".equals(property)) {
            // create synthetic local record identifier as scheme specific part. We have no ISIL!
            state.setUID(IRI.builder().curie("uid:" + value).build());
        } else if ("identifier".equals(property)) {
            IdentifierMapper mapper = worker.getIdentifierMapper();
            if (mapper != null) {
                isil = mapper.lookup(value);
                state.setISIL(isil);
                state.getFacets().putIfAbsent(identifierFacet,
                        new TermFacet().setName(identifierFacet).setType(Literal.STRING));
                state.getFacets().get(identifierFacet).addValue(isil);
                IRI uid = state.getUID();
                if (uid != null) {
                    // update UID to correct value
                    state.setUID(IRI.builder().curie("uid:" + state.getISIL() + "/" +
                            uid.getSchemeSpecificPart()).build());
                }
            }
            resource.add("identifier", isil);
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
        } else if ("publicnote".equals(property) && "Frei zugänglich".equals(value)) {
            state.setUID(IRI.builder().curie("green").build());
        }
        return Collections.singletonList(isil);
    }
}
