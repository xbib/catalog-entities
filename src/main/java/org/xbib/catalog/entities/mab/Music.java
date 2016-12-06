package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;

import java.util.Map;

/**
 *  MAB 680, 681, 682, 683.
 *
 *  http://www.dnb.de/SharedDocs/Downloads/DE/DNB/standardisierung/mabTabelleDeutschEnglisch2006.pdf?__blob=publicationFile
 */
public class Music extends CatalogEntity {

    public Music(Map<String, Object> params) {
        super(params);
    }
}
