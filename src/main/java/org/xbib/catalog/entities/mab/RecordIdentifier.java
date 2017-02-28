package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.catalog.entities.CatalogEntityWorkerState;
import org.xbib.catalog.entities.Classifier;
import org.xbib.catalog.entities.ClassifierEntry;
import org.xbib.catalog.entities.TermFacet;
import org.xbib.content.rdf.Literal;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class RecordIdentifier extends CatalogEntity {

    private static final Logger logger = Logger.getLogger(RecordIdentifier.class.getName());

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
        if (state.getRecordIdentifier() == null) {
            state.setIdentifier(v);
            state.setRecordIdentifier(v);
            state.getResource().newResource("xbib").add("uid", v);
        } else {
            logger.log(Level.WARNING, "record identifier already set, skipping " + value);
        }
        // check for classifier
        Classifier classifier = worker.getClassifier();
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
