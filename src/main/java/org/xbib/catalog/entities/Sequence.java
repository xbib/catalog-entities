package org.xbib.catalog.entities;

import java.util.LinkedList;

/**
 * A Sequence.
 *
 * @param <R> the sequence type
 */
public class Sequence<R> {

    private String name;

    private LinkedList<R> resources = new LinkedList<>();

    public String getName() {
        return name;
    }

    public Sequence<R> setName(String name) {
        this.name = name;
        return this;
    }

    public Sequence<R> add(R resource) {
        if (resource != null) {
            resources.add(resource);
        }
        return this;
    }

    public LinkedList<R> getResources() {
        return resources;
    }

}
