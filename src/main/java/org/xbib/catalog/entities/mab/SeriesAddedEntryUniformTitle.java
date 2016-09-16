package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.rdf.Resource;

import java.util.Map;

public class SeriesAddedEntryUniformTitle extends CatalogEntity {

    private String prefix = "";

    public SeriesAddedEntryUniformTitle(Map<String, Object> params) {
        super(params);
        if (params.containsKey("_prefix")) {
            this.prefix = params.get("_prefix").toString();
        }
        // override prefix by "catalogid" with braces
        if (params.containsKey("catalogid")) {
            this.prefix = "(" + params.get("catalogid").toString() + ")";
        }
    }

    @Override
    public String transform(CatalogEntityWorker worker,
                            String predicate, Resource resource, String property, String value) {
        if ("title".equals(property)) {
            resource.add("title", value
                    //.replace('\u0098', '\u00ac')
                    //.replace('\u009c', '\u00ac')
                    .replaceAll("<<(.*?)>>", "¬$1¬")
                    .replaceAll("<(.*?)>", "[$1]")
                    .replaceAll("¬(.*?)¬", "$1"));
            return null;
        }
        if ("designation".equals(property)) {
            resource.add("designation", prefix + value);
            return null;
        }
        return value;
    }

}
