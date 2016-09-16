package org.xbib.catalog.entities;

import java.io.IOException;

/**
 *
 */
public class MarcBibCatalogEntityBuilder extends CatalogEntityBuilder {

    public MarcBibCatalogEntityBuilder() throws IOException {
        super("org.xbib.catalog.entities.marc.bib",
                CatalogEntityBuilder.class.getResource("org/xbib/catalog/entities/marc/bib.json"));
    }

}
