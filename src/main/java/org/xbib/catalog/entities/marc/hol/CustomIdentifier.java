package org.xbib.catalog.entities.marc.hol;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.content.rdf.Resource;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class CustomIdentifier extends CatalogEntity {

    public CustomIdentifier(Map<String, Object> params) {
        super(params);
    }

    /**
     * Construct ZDB-ID identifier from multiple subfields.
     *
     * Challenge: type flag (here: DE-600) is after the value (here: 13-9):
     *
     * tag=016 ind=7  subf=a data=13-9
     * tag=016 ind=7  subf=2 data=DE-600
     *
     * Idea: if type flag indicates ZDB, look up existing value, and add a property 'identifierZDB'.
     */

    @Override
    public List<String> transform(CatalogEntityWorker worker,
                                  String predicate, Resource resource, String property, String value) {
        if ("value".equals(property)) {
            worker.getWorkerState().getScratch().put("value", value);
            return null;
        } else {
            if ("type".equals(property)) {
                if ("IdentifierZDB".equals(value)) {
                    String v = (String) worker.getWorkerState().getScratch().get("value");
                    resource.add("identifierZDB", v.replaceAll("\\-", "").toLowerCase());
                } else if ("IdentifierDNB".equals(value)) {
                    String v = (String) worker.getWorkerState().getScratch().get("value");
                    resource.add("identifierDNB", v.replaceAll("\\-", "").toLowerCase());
                }
                return null;
            }
        }
        return Collections.singletonList(value);
    }
}
