package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.content.rdf.Resource;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class RecordLeader extends CatalogEntity {

    private static final Logger logger = Logger.getLogger(RecordLeader.class.getName());

    private final Map<String, Object> codes;

    @SuppressWarnings("unchecked")
    public RecordLeader(Map<String, Object> params) {
        super(params);
        this.codes = (Map<String, Object>) params.get("codes");
    }

    @SuppressWarnings("unchecked")
    @Override
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        if (codes == null) {
            return super.transform(worker, field);
        }
        String value = getValue(field);
        worker.getWorkerState().setRecordLabel(value.trim());
        if (value.length() != 24) {
            logger.log(Level.WARNING,
                    () -> MessageFormat.format("the length of this record label is {0} characters and was skipped: {1}",
                            value.length(), value));
        }
        Resource resource = worker.getWorkerState().getResource().newResource("RecordLeader");
        for (Map.Entry<String, Object> entry : codes.entrySet()) {
            String k = entry.getKey();
            int pos = Integer.parseInt(k);
            Map<String, String> v = (Map<String, String>) codes.get(k);
            String code = value.length() > pos ? value.substring(pos, pos + 1) : "";
            if (v.containsKey(code)) {
                resource.add(v.get("_predicate"), v.get(code));
            } else {
                logger.log(Level.WARNING, () -> MessageFormat.format("key={0} code not configured: {1}", k, code));
            }
        }
        return super.transform(worker, field);
    }
}
