package org.xbib.catalog.entities.marc.zdb.bib;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.marc.MarcField;

import java.util.Map;

public class Event extends CatalogEntity {

    public Event(Map<String, Object> params) {
        super(params);
    }

    @Override
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) {
        return null;
    }

}
