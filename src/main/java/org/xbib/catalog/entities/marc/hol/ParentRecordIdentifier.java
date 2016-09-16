package org.xbib.catalog.entities.marc.hol;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.util.Map;

public class ParentRecordIdentifier extends org.xbib.catalog.entities.marc.bib.Identifier {

    public ParentRecordIdentifier(Map<String, Object> params) {
        super(params);
    }

    @Override
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        String predicate = getClass().getSimpleName();
        if (getParams().containsKey("_predicate")) {
            predicate = (String) getParams().get("_predicate");
        }
        worker.getWorkerState().getResource().add(predicate, getValue(field));
        return super.transform(worker, field);
    }
}
