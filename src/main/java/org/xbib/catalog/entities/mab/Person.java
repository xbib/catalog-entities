package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.catalog.entities.IdentifierMapper;
import org.xbib.catalog.entities.matching.title.RAK;
import org.xbib.content.rdf.Resource;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class Person extends CatalogEntity {

    public Person(Map<String, Object> params) {
        super(params);
    }

    @Override
    public List<String> transform(CatalogEntityWorker worker,
                                  String predicate, Resource resource, String property, String value) {
        if ("identifier".equals(property)) {
            if (value.startsWith("(DE-588)")) {
                // GND-ID: upper case, with hyphen
                resource.add("identifierGND", value.substring(8));
            } else if (value.startsWith("(DE-101)")) {
                // DNB-ID: upper case, with hyphen
                resource.add("identifierDNB", value.substring(8));
            } else if (value.startsWith("(DE-600)")) {
                // ZDB-ID does not matter at all
                resource.add("identifierZDB", value.substring(8).replaceAll("\\-", "").toLowerCase());
                return Collections.singletonList(value.replaceAll("\\-", "").toLowerCase());
            } else {
                // add at first the unchanged identifier
                resource.add("identifier", value);
            }
            // returning null is very important for GND referencing
            return null;
        } else if ("source".equals(property)) {
            IdentifierMapper mapper = worker.getIdentifierMapper();
            if (mapper != null) {
                String isil = mapper.lookup(value);
                if (isil != null) {
                    resource.add("personSource", isil);
                    return null;
                }
            }
        }
        return Collections.singletonList(RAK.clean(value));
    }
}
