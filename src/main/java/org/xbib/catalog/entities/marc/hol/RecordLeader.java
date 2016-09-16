package org.xbib.catalog.entities.marc.hol;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.util.Map;

public class RecordLeader extends CatalogEntity {

    private Map<String, Object> codes;

    private String predicate;

    @SuppressWarnings("unchecked")
    public RecordLeader(Map<String, Object> params) {
        super(params);
        this.codes = (Map<String, Object>) params.get("codes");
        this.predicate = params.containsKey("_predicate") ?
                (String) params.get("_predicate") : "leader";
    }

    @SuppressWarnings("unchecked")
    @Override
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        String value = getValue(field);
        worker.getWorkerState().setLabel(value);
        if (codes == null) {
            return super.transform(worker, field);
        }
        for (Map.Entry<String, Object> entry : codes.entrySet()) {
            String k = entry.getKey();
            int pos = Integer.parseInt(k);
            Map<String, String> v = (Map<String, String>) codes.get(k);
            String code = value.length() > pos ? value.substring(pos, pos + 1) : "";
            if (v.containsKey(code)) {
                worker.getWorkerState().getResource().add(predicate, v.get(code));
            }
        }
        return super.transform(worker, field);
    }
}
