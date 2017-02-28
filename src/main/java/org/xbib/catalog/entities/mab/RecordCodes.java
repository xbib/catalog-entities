package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.content.rdf.Resource;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.util.Map;

/**
 *
 */
public class RecordCodes extends CatalogEntity {

    private final Map<String, Object> codes;

    public RecordCodes(Map<String, Object> params) {
        super(params);
        this.codes = getCodes();
    }

    @SuppressWarnings("unchecked")
    @Override
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        String value = getValue(field);
        if (codes != null) {
            Resource resource = worker.getWorkerState().getResource().newResource("RecordCodes");
            for (int i = 0; i < value.length(); i++) {
                Map<String, Object> q = (Map<String, Object>) codes.get(Integer.toString(i));
                if (q != null) {
                    String code = (String) q.get(value.substring(i, i + 1));
                    resource.add("value", code);
                }
            }
        }
        return null; // done!
    }
}
