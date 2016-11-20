package org.xbib.catalog.entities.marc.bib;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class RecordIdentifier extends CatalogEntity {

    private static final Logger logger = Logger.getLogger(RecordIdentifier.class.getName());

    public RecordIdentifier(Map<String, Object> params) {
        super(params);
    }

    @Override
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        String value = getValue(field).trim();
        worker.getWorkerState().setIdentifier(value);
        worker.getWorkerState().setRecordIdentifier(value);
        worker.getWorkerState().getResource().newResource("xbib").add("uid", value);
        logger.log(Level.FINE, "id=" + value);
        return super.transform(worker, field);
    }
}
