package org.xbib.catalog.entities;

import org.xbib.iri.IRI;

import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class TermFacet implements Facet<String> {

    private String name;

    private IRI type;

    private List<String> values = new LinkedList<>();

    public String getName() {
        return name;
    }

    public TermFacet setName(String name) {
        this.name = name;
        return this;
    }

    public IRI getType() {
        return type;
    }

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

    public List<String> getValues() {
        return values;
    }

}
