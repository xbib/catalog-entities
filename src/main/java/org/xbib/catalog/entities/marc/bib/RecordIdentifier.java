package org.xbib.catalog.entities.marc.bib;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.util.Map;

public class RecordIdentifier extends CatalogEntity {

    public RecordIdentifier(Map<String, Object> params) {
        super(params);
    }

    @Override
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        String value = getValue(field);
        worker.getWorkerState().setIdentifier(value);
        worker.getWorkerState().setRecordIdentifier(value);
        return super.transform(worker, field);
    }
}
