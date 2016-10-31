package org.xbib.catalog.entities.marc.hol;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.util.Map;

/**
 *
 */
public class RecordIdentifier extends CatalogEntity {

    public RecordIdentifier(Map<String, Object> params) {
        super(params);
    }

    @Override
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        String value = getValue(field);
        String predicate = getClass().getSimpleName();
        if (getParams().containsKey("_predicate")) {
            predicate = (String) getParams().get("_predicate");
        }
        worker.getWorkerState().setRecordIdentifier(value);
        worker.getWorkerState().getResource().add(predicate, value);
        return super.transform(worker, field);
    }

}
