package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.catalog.entities.CatalogEntityWorkerState;
import org.xbib.content.rdf.Resource;
import org.xbib.content.resource.IRI;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class Shelfmark extends CatalogEntity {

    private String prefix = "";

    public Shelfmark(Map<String, Object> params) {
        super(params);
        // override by "identifier"
        if (params.containsKey("identifier")) {
            this.prefix = params.get("identifier").toString();
        }
    }

    @Override
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        worker.append(worker.getWorkerState().getNextItemResource(), field, this);
        return null;
    }

    @Override
    public List<String> transform(CatalogEntityWorker worker,
                                  String predicate, Resource resource, String property, String value) {
        CatalogEntityWorkerState state = worker.getWorkerState();
        if ("Shelfmark".equals(predicate) && prefix != null && !prefix.isEmpty()) {
            resource.add("identifier", prefix);
            // create synthetic local record identifier
            state.setUID(IRI.builder()
                    .curie("uid:" + state.getRecordIdentifier() + "/" + state.getISIL() + "/" + value)
                    .build());
        }
        return Collections.singletonList(value);
    }
}
