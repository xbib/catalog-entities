package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class RecordLabel extends CatalogEntity {

    private static final Logger logger = Logger.getLogger(RecordLabel.class.getName());

    public RecordLabel(Map<String, Object> params) {
        super(params);
    }

    @Override
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        String value = getValue(field);
        worker.getWorkerState().setLabel(value.trim());
        if (value.length() == 24) {
            char satztyp = value.charAt(23);
            worker.getWorkerState().getResource().add("type", String.valueOf(satztyp));
            worker.getWorkerState().getResource().add("boost", satztyp == 'u' ? "0.1" : "1.0");
        } else {
            logger.log(Level.WARNING,
                    MessageFormat.format("the length of this record label is {0} characters and was skipped: {1}",
                            value.length(), value));
        }
        return null; // done
    }
}
