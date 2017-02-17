package org.xbib.catalog.entities.marc.zdb.bib;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.content.rdf.Resource;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Transform identifiers to their namespace.
 * Skipped because not in libraryland:
 * (NL-LiSWE) = Swets & Zeitlinger
 */
public class Identifier extends CatalogEntity {

    private static final Logger logger = Logger.getLogger(Identifier.class.getName());

    private final Set<String> unrecognized;

    public Identifier(Map<String, Object> params) {
        super(params);
        this.unrecognized = new HashSet<>();
    }

    @Override
    public List<String> transform(CatalogEntityWorker worker,
                                  String predicate, Resource resource, String property, String value) {
        if ("IdentifierZDB".equals(predicate) && "value".equals(property)) {
            if (value.startsWith("(DE-599)")) {
                resource.add("identifierEKI", value.substring(8));
                return null;
            } else if (value.startsWith("(OCoLC)")) {
                resource.add("identifierOCLC", value.substring(7).replaceAll("\\-", "").toLowerCase());
                return null;
            } else {
                if (!unrecognized.contains(value)) {
                    unrecognized.add(value);
                    logger.log(Level.WARNING, () -> MessageFormat.format("unprocessed identifier: {0}", value));
                }
                return null;
            }
        }
        return Collections.singletonList(value);
    }
}
