package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.rdf.Resource;

import java.util.Map;

/**
 *
 */
public class TitleSuper extends Title {

    private String prefix = "";

    public TitleSuper(Map<String, Object> params) {
        super(params);
        if (params.containsKey("_prefix")) {
            this.prefix = params.get("_prefix").toString();
        }
        // override prefix by "catalogid" with braces
        if (params.containsKey("catalogid")) {
            this.prefix = "(" + params.get("catalogid").toString() + ")";
        }
    }

    @Override
    public String transform(CatalogEntityWorker worker,
                            String predicate, Resource resource, String property, String value) {
        if ("TitleSuperIdentifier".equals(predicate)) {
            resource.add("titleSuperIdentifier", prefix + value);
            return null;
        }
        return value;
    }

}
