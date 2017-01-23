package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.catalog.entities.matching.title.RAK;
import org.xbib.content.rdf.Resource;

import java.util.Collections;
import java.util.List;
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
    public List<String> transform(CatalogEntityWorker worker,
                                  String predicate, Resource resource, String property, String value) {
        if ("TitleSuperIdentifier".equals(predicate)) {
            resource.add("titleSuperIdentifier", prefix + value);
            return null;
        }
        return Collections.singletonList(RAK.clean(value));
    }

}
