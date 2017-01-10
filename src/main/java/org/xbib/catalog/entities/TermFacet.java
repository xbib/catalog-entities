package org.xbib.catalog.entities;

import org.xbib.content.resource.IRI;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A term facet.
 */
public class TermFacet implements Facet<String> {

    private String name;

    private IRI type;

    private Set<String> values = new LinkedHashSet<>();

    @Override
    public String getName() {
        return name;
    }

    @Override
    public TermFacet setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public IRI getType() {
        return type;
    }

    @Override
    public TermFacet setType(IRI type) {
        this.type = type;
        return this;
    }

    @Override
    public TermFacet addValue(String value) {
        if (value != null && !value.isEmpty()) {
            values.add(value);
        }
        return this;
    }

    @Override
    public Collection<String> getValues() {
        return values;
    }


    @Override
    public String toString() {
        return "[type=" + type + ",name=" + name + ",values=" + values + "]";
    }
}
