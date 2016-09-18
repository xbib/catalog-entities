package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.util.Map;

/**
 *
 */
public class Delete extends CatalogEntity {

    public Delete(Map<String, Object> params) {
        super(params);
    }

    @Override
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        String value = getValue(field);
        // turn field-level delete record marker into record deleted state
        if ("Y".equals(value)) {
            worker.getWorkerState().getResource().setDeleted(true);
        }
        return null;
    }
}
