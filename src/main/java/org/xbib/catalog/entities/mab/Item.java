package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.catalog.entities.CatalogEntityWorkerState;
import org.xbib.catalog.entities.Classifier;
import org.xbib.catalog.entities.ClassifierEntry;
import org.xbib.catalog.entities.Facet;
import org.xbib.catalog.entities.IdentifierMapper;
import org.xbib.catalog.entities.StatusCodeMapper;
import org.xbib.catalog.entities.TermFacet;
import org.xbib.content.rdf.Literal;
import org.xbib.content.rdf.Resource;
import org.xbib.content.resource.IRI;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class Item extends CatalogEntity {

    /*
            "a": "identifier",
            "b": "shelfmark",
            "c": "callnumber",
            "d": "collection",
            "e": "status"
     */

    private static final String taxonomyFacet = "xbib.taxonomy";
    private String identifierFacet = "xbib.identifier";

    public Item(Map<String, Object> params) {
        super(params);
        // facet name
        if (params.containsKey("_facet")) {
            this.identifierFacet = params.get("_facet").toString();
        }
    }

    @Override
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        worker.append(worker.getWorkerState().getNextItemResource(), field, this);
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public String transform(CatalogEntityWorker worker,
                            String predicate, Resource resource, String property, String value) {
        CatalogEntityWorkerState state = worker.getWorkerState();
        if ("identifier".equals(property)) {
            IdentifierMapper mapper = worker.identifierMapper();
            if (mapper != null) {
                String isil = mapper.lookup(value);
                if (isil != null) {
                    resource.add("identifier", isil);
                    state.setUID(IRI.builder().curie(isil).build());
                    state.setISIL(isil);
                    state.getFacets().putIfAbsent(identifierFacet,
                            new TermFacet().setName(identifierFacet).setType(Literal.STRING));
                    Facet<String> holderFacet = state.getFacets().get(identifierFacet);
                    holderFacet.addValue(isil);
                    // add "main ISIL" if not main ISIL (=two hyphens)
                    int pos = isil.lastIndexOf('-');
                    if (isil.indexOf('-') < pos) {
                        holderFacet.addValue(isil.substring(0, pos));
                    }
                    Classifier classifier = worker.classifier();
                    if (classifier != null) {
                        String doc = state.getRecordIdentifier();
                        java.util.Collection<ClassifierEntry> entries = classifier.lookup(isil, doc, value, null);
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
                    return isil;
                }
            }
        } else if ("callnumber".equals(property)) {
            // create synthetic local record identifier
            state.setUID(IRI.builder().curie(state.getISIL() + "/" + value).build());
            Classifier classifier = worker.classifier();
            if (classifier != null) {
                String isil = state.getISIL();
                String doc = state.getRecordIdentifier();
                java.util.Collection<ClassifierEntry> entries = classifier.lookup(isil, doc, value, null);
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
        } else if ("status".equals(property)) {
            StatusCodeMapper mapper = worker.statusCodeMapper();
            if (mapper != null && mapper.getMap().containsKey(value)) {
                List<String> codes = (List<String>) mapper.getMap().get(value);
                for (String code : codes) {
                    resource.add("interlibraryservice", code);
                }
            }
        }
        return value;
    }
}
