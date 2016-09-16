package org.xbib.catalog.entities.pica.zdb.bibdat;

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
        worker.getWorkerState().setRecordIdentifier(getValue(field));
        return super.transform(worker, field);
    }
}
