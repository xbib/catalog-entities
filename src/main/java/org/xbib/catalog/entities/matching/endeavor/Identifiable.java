package org.xbib.catalog.entities.matching.endeavor;

/**
 * An interface for an identifiable endeavor.
 * This could be a text, article, monograph, serial, artwork, or other publication.
 *
 */
public interface Identifiable {

    String createIdentifier();
}
