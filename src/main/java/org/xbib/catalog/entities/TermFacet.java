package org.xbib.catalog.entities;

import org.xbib.content.resource.IRI;

import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class TermFacet implements Facet<String> {

    private String name;

    private IRI type;

    private List<String> values = new LinkedList<>();

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
    public List<String> getValues() {
        return values;
    }

}
