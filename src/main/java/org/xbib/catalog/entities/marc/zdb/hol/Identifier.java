package org.xbib.catalog.entities.marc.zdb.hol;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.content.rdf.Resource;

import java.util.Map;

/**
 *
 */
public class Identifier extends CatalogEntity {

    public Identifier(Map<String, Object> params) {
        super(params);
    }

    @Override
    public String transform(CatalogEntityWorker worker,
                            String predicate, Resource resource, String property, String value) {
        if ("IdentifierZDB".equals(predicate)) {
            if ("value".equals(property)) {
                if (value.startsWith("(DE-600)")) {
                    resource.add("identifierDNB", value.substring(8).replaceAll("\\-", "").toLowerCase());
                    return null;
                } else if (value.startsWith("(DE-601)")) {
                    resource.add("identifierGBV", value.substring(8).replaceAll("\\-", "").toLowerCase());
                    return null;
                } else if (value.startsWith("(DE-602)")) {
                    resource.add("identifierKOBV", value.substring(8).replaceAll("\\-", "").toLowerCase());
                    return null;
                } else if (value.startsWith("(DE-603)")) {
                    resource.add("identifierHEBIS", value.substring(8).replaceAll("\\-", "").toLowerCase());
                    return null;
                } else if (value.startsWith("(DE-604)")) {
                    resource.add("identifierBVB", value.substring(8).replaceAll("\\-", "").toLowerCase());
                    return null;
                } else if (value.startsWith("(DE-605)")) {
                    resource.add("identifierHBZ", value.substring(8).replaceAll("\\-", "").toLowerCase());
                    return null;
                } else if (value.startsWith("(DE-576)")) {
                    resource.add("identifierSWB", value.substring(8).replaceAll("\\-", "").toLowerCase());
                    return null;
                }
            }
        }
        return value;
    }
}
