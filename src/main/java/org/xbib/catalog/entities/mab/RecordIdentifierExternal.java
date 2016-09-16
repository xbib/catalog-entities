package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class RecordIdentifierExternal extends CatalogEntity {

    private static final Logger logger = Logger.getLogger(RecordIdentifierExternal.class.getName());

    private Map<String, Object> codes;

    private Set<String> unknown;

    public RecordIdentifierExternal(Map<String, Object> params) {
        super(params);
        this.codes = getCodes();
        this.unknown = Collections.synchronizedSet(new HashSet<>());
    }

    @Override
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        String prefix = "";
        String content = "";
        for (MarcField.Subfield subfield : field.getSubfields()) {
            if ("b".equals(subfield.getId())) {
                String s = subfield.getValue();
                if (codes.containsKey(s)) {
                    prefix = (String) codes.get(s);
                } else {
                    if (!unknown.contains(s)) {
                        unknown.add(s);
                        logger.log(Level.WARNING,
                                MessageFormat.format("no external source key configured for {0}", s));
                    }
                }
            } else {
                content = subfield.getValue();
            }
        }
        if (prefix != null && !prefix.isEmpty() && content != null && !content.isEmpty()) {
            worker.getWorkerState().getResource().newResource("RecordIdentifierExternal").add("identifier", prefix + content);
        }
        return null; // done!
    }

}
