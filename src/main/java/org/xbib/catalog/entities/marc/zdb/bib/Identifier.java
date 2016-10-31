package org.xbib.catalog.entities.marc.zdb.bib;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.content.rdf.Resource;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class Identifier extends CatalogEntity {

    private static final Logger logger = Logger.getLogger(Identifier.class.getName());

    public Identifier(Map<String, Object> params) {
        super(params);
    }

    @Override
    public String transform(CatalogEntityWorker worker,
                            String predicate, Resource resource, String property, String value) {
        if ("IdentifierZDB".equals(predicate)) {
            if ("value".equals(property)) {
                if (value.startsWith("(DE-599)")) {
                    resource.add("identifierEKI", value.substring(8));
                    return null;
                } else if (value.startsWith("(OCoLC)")) {
                    resource.add("identifierOCLC", value.substring(7).replaceAll("\\-", "").toLowerCase());
                    return null;
                } else {
                    //(NL-LiSWE) = Swets & Zeitlinger
                    /*int pos = value.indexOf(')');
                    String prefix = pos > 0 ? value.substring(1,pos).replaceAll("\\-", "").toUpperCase() : "";
                    value = pos > 0 ? value.substring(pos + 1) : value;
                    resource.add("identifier" + prefix, value.replaceAll("\\-", "").toLowerCase());*/
                    logger.log(Level.WARNING, "unprocessed identifier: {}", value);
                    return null;
                }
            }
        }
        return value;
    }
}
