package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.content.rdf.Resource;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class RecordIdentifierParent extends CatalogEntity {

    private String prefix = "";

    public RecordIdentifierParent(Map<String, Object> params) {
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
        if ("RecordIdentifierParent".equals(predicate)) {
            // trim important for MAB 010 having an indicator which is not possible in ISO 2709
            resource.add("identifier", prefix + value.trim());
            return null;
        }
        return Collections.singletonList(value);
    }
}
