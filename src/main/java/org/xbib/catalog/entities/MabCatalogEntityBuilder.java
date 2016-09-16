package org.xbib.catalog.entities;

import java.io.IOException;

/**
 *
 */
public class MabCatalogEntityBuilder extends CatalogEntityBuilder {

    public MabCatalogEntityBuilder() throws IOException {
        super("org.xbib.catalog.entities.mab",
                CatalogEntityBuilder.class.getResource("/org/xbib/catalog/entities/mab/titel.json"));
    }

}
