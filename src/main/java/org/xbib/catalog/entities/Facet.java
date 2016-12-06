package org.xbib.catalog.entities;

import org.xbib.content.resource.IRI;

import java.util.Collection;

/**
 * @param <O> the facet type paramter
 */
public interface Facet<O> {

    String getName();

    Facet<O> setName(String name);

    IRI getType();

    Facet<O> setType(IRI type);

    Facet<O> addValue(O value);

    Collection<O> getValues();

}
