package org.xbib.catalog.entities.marc.zdb.hol;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.rdf.Resource;

import java.util.Map;

public class CustomIdentifier extends CatalogEntity {

    public CustomIdentifier(Map<String, Object> params) {
        super(params);
    }

    /**
     * Construct purified ZDB-ID for fast term search.
     *
     * Type flag DE-600 is after the value:
     *
     * tag=016 ind=7  subf=a data=13-9
     * tag=016 ind=7  subf=2 data=DE-600
     *
     * Idea: if type flag indicates ZDB, look up existing value, and add a property 'identifierZDB'.
     */

    @Override
    public String transform(CatalogEntityWorker worker,
                            String predicate, Resource resource, String property, String value) {
        if ("IdentifierZDB".equals(value) && "type".equals(property)) {
            String v = resource.objects("value").get(0).toString();
            resource.add("identifierZDB", v.replaceAll("\\-", "").toLowerCase());
            return value;
        }
        return value;
    }
}
