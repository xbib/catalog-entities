package org.xbib.catalog.entities.marc.zdb.bib;

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
public class ClassificationNumber extends CatalogEntity {

    private final Map<String, Object> ddc;

    @SuppressWarnings("unchecked")
    public ClassificationNumber(Map<String, Object> params) {
        super(params);
        ddc = (Map<String, Object>) getParams().get("ddc");
    }

    @Override
    public List<String> transform(CatalogEntityWorker worker,
                                  String predicate, Resource resource, String property, String value) throws IOException {
        if (ddc != null && ddc.containsKey(value)) {
            resource.add(property + "Text", (String) ddc.get(value));
        }
        return Collections.singletonList(value);
    }
}
