package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.catalog.entities.CatalogEntityWorkerState;
import org.xbib.catalog.entities.Classifier;
import org.xbib.catalog.entities.ClassifierEntry;
import org.xbib.catalog.entities.TermFacet;
import org.xbib.marc.MarcField;
import org.xbib.rdf.Literal;

import java.io.IOException;
import java.util.Map;

/**
 *
 */
public class RecordIdentifier extends CatalogEntity {

    private static final String taxonomyFacet = "xbib.taxonomy";

    private String prefix = "";

    private String catalogid = "";

    public RecordIdentifier(Map<String, Object> params) {
        super(params);
        if (params.containsKey("_prefix")) {
            this.prefix = params.get("_prefix").toString();
        }
        if (params.containsKey("catalogid")) {
            this.catalogid = params.get("catalogid").toString();
            this.prefix = "(" + this.catalogid + ")";
        }
    }

    @Override
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        String value = getValue(field);
        CatalogEntityWorkerState state = worker.getWorkerState();
        String v = prefix + value.trim();
        worker.getWorkerState().setIdentifier(v);
        worker.getWorkerState().setRecordIdentifier(v);
        worker.getWorkerState().getResource().newResource("xbib").add("uid", v);
        // check for classifier
        Classifier classifier = worker.classifier();
        if (classifier != null) {
            String isil = catalogid;
            String key = catalogid + "." + state.getRecordIdentifier() + ".";
            java.util.Collection<ClassifierEntry> entries = classifier.lookup(key);
            if (entries != null) {
                for (ClassifierEntry classifierEntry : entries) {
                    if (classifierEntry.getCode() != null && !classifierEntry.getCode().trim().isEmpty()) {
                        String facet = taxonomyFacet + "." + isil + ".notation";
                        state.getFacets().putIfAbsent(facet, new TermFacet().setName(facet).setType(Literal.STRING));
                        state.getFacets().get(facet).addValue(classifierEntry.getCode());
                    }
                    if (classifierEntry.getText() != null && !classifierEntry.getText().trim().isEmpty()) {
                        String facet = taxonomyFacet + "." + isil + ".text";
                        state.getFacets().putIfAbsent(facet, new TermFacet().setName(facet).setType(Literal.STRING));
                        state.getFacets().get(facet).addValue(classifierEntry.getText());
                    }
                }
            }
        }
        return super.transform(worker, field);
    }
}
