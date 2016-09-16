package org.xbib.catalog.entities.marc.zdb.bib;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.rdf.Resource;

import java.io.IOException;
import java.util.Map;

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
    public String transform(CatalogEntityWorker worker,
                            String predicate, Resource resource, String property, String value) throws IOException {
        if (value == null || value.isEmpty()) {
            return value;
        }
        String v = prefix + value.trim();
        worker.getWorkerState().setRecordIdentifier(v);
        try {
            worker.getWorkerState().getResource().newResource("xbib").add("uid", v);
        } catch (IOException e) {
            // ignore
        }
        return v;
    }
}
