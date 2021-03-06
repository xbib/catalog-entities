package org.xbib.catalog.entities.marc.bib;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.util.Map;

/**
 *
 */
public class RecordIdentifier extends CatalogEntity {

    private String prefix = "";

    public RecordIdentifier(Map<String, Object> params) {
        super(params);
        if (params.containsKey("_prefix")) {
            this.prefix = params.get("_prefix").toString();
        }
        if (params.containsKey("catalogid")) {
            this.prefix = "(" + params.get("catalogid").toString() + ")";
        }
    }

    @Override
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        String value = prefix + getValue(field).trim();
        worker.getWorkerState().setIdentifier(value);
        worker.getWorkerState().setRecordIdentifier(value);
        worker.getWorkerState().getResource().newResource("xbib").add("uid", value);
        return super.transform(worker, field);
    }
}
