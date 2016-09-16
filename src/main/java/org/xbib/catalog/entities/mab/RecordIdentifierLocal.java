package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.rdf.Resource;

import java.util.Map;

/**
 *
 */
public class RecordIdentifierLocal extends CatalogEntity {

    private String prefix = "";

    public RecordIdentifierLocal(Map<String, Object> params) {
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
        if (value == null) {
            return null;
        }
        if ("RecordIdentifierLocal".equals(predicate)) {
            // trim important for MAB 010 having an indicator which is not possible in ISO 2709
            resource.add("recordIdentifierLocal", prefix + value.trim());
            return null;
        }
        return value;
    }
}
