package org.xbib.catalog.entities.marc.bib;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.content.rdf.Resource;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class ClassificationNumber extends CatalogEntity {

    private static final String SCRATCH_KEY = "classificationNumber";

    private final Map<String, String> ddcMap;

    private final Map<String, String> dnbMap;

    @SuppressWarnings("unchecked")
    public ClassificationNumber(Map<String, Object> params) {
        super(params);
        this.ddcMap = (Map<String, String>) params.get("ddc");
        this.dnbMap = (Map<String, String>) params.get("dnb");
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> transform(CatalogEntityWorker worker,
                                  String predicate, Resource resource, String property, String value) {
        Map<String, Object> scratch = worker.getWorkerState().getScratch();
        if ("classificationNumber".equals(property)) {
            scratch.putIfAbsent(SCRATCH_KEY, new LinkedList<>());
            List<String> values = (List<String>) scratch.get(SCRATCH_KEY);
            values.add(value);
            scratch.put("classificationNumber", values);
        }
        if ("type".equals(property)) {
            switch (value) {
                // DNB uses some sort of DDC
                case "DNB":
                    List<String> dnbValues = (List<String>) scratch.get(SCRATCH_KEY);
                    for (String dnbValue : dnbValues) {
                        resource.add("dnb", dnbMap != null ? dnbMap.get(dnbValue) : null);
                    }
                    scratch.remove(SCRATCH_KEY);
                    break;
                case "DDC":
                    List<String> ddcValues = (List<String>) scratch.get(SCRATCH_KEY);
                    for (String ddcValue : ddcValues) {
                        resource.add("ddc", ddcMap != null ? ddcMap.get(ddcValue) : null);
                    }
                    scratch.remove(SCRATCH_KEY);
                    break;
                default:
                    break;
            }
        }
        return Collections.singletonList(value);
    }
}
