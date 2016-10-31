package org.xbib.catalog.entities.matching.endeavor;

/**
 * An enumeration of bibliographic materials.
 */
public enum BibliographicType implements Comparable<BibliographicType> {

    BOOK_WITH_ID,
    SERIAL_WITH_ID,
    MUSIC_WITH_ID,
    REFERENCE_WITH_ID,
    ARTICLE_WITH_ID,
    BOOK,
    SERIAL,
    MUSIC,
    ARTICLE,
    REFERENCE,
    OTHER,
    UNKNOWN;
}
