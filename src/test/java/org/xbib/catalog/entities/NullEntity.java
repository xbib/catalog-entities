package org.xbib.catalog.entities;

import java.util.HashMap;

class NullEntity extends CatalogEntity {

    NullEntity() {
        super(new HashMap<>());
    }

    public String toString() {
        return "<null>";
    }
}
