package org.xbib.catalog.entities.matching.endeavor;

import java.util.HashMap;
import java.util.Map;

/**
 * An enumeration of bibliographic materials
 *
 */
public enum BibliographicType implements Comparable<BibliographicType> {

    BOOK_WITH_ID("BOOK_ID"),

    SERIAL_WITH_ID("SERIAL_ID"),

    MUSIC_WITH_ID("MUSIC_ID"),

    REFERENCE_WITH_ID("REF_ID"),

    ARTICLE_WITH_ID("ART_ID"),

    BOOK("BOOK"),

    SERIAL("SERIAL"),

    MUSIC("MUSIC"),

    ARTICLE("ART"),

    REFERENCE("REF"),

    OTHER("OTHER"),

    UNKNOWN("X");

    private final String str;

    private Map<String, BibliographicType> tokens;

    private Map<BibliographicType, String> strings;

    private BibliographicType(String token) {
        this.str = token;
        map(token, this);
    }

    private void map(String token, BibliographicType type) {
        if (tokens == null) {
            tokens = new HashMap<>();
        }
        if (strings == null) {
            strings = new HashMap<>();
        }
        tokens.put(token, type);
        strings.put(type, token);
    }

    public String getString() {
        return str;
    }

}
