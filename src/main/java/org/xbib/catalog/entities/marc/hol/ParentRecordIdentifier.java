package org.xbib.catalog.entities.marc.hol;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.content.rdf.Resource;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class ParentRecordIdentifier extends CatalogEntity {

    private String prefix = "";

    public ParentRecordIdentifier(Map<String, Object> params) {
        super(params);
        if (params.containsKey("_prefix")) {
            this.prefix = params.get("_prefix").toString();
        }
        if (params.containsKey("catalogid")) {
            this.prefix = "(" + params.get("catalogid").toString() + ")";
        }
    }

    @Override
    public List<String> transform(CatalogEntityWorker worker,
                                  String predicate, Resource resource, String property, String value) throws IOException {
        return Collections.singletonList(prefix + value);
    }
}
