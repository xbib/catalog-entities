package org.xbib.catalog.entities;

import java.io.IOException;

/**
 *
 */
public class MarcHolCatalogEntityBuilder extends CatalogEntityBuilder {

    public MarcHolCatalogEntityBuilder() throws IOException {
        super("org.xbib.catalog.entities.marc.hol",
                CatalogEntityBuilder.class.getResource("org/xbib/catalog/entities/marc/hol.json"));
    }

}
