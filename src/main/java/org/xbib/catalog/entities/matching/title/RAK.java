package org.xbib.catalog.entities.matching.title;

/**
 */
public class RAK {

    public static String clean(String titleString) {
        // u00ac = ¬
        return titleString
                .replaceAll("<<(.*?)>>", "\u0098$1\u009C")
                .replaceAll("<(.*?)>", "[$1]")
                .replaceAll("¬(.*?)¬", "\u0098$1\u009C");
    }
}
