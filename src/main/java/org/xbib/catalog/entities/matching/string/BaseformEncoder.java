package org.xbib.catalog.entities.matching.string;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * A base form encoder
 */
public class BaseformEncoder {

    private static final Pattern p = Pattern.compile("[^\\p{L}\\p{Space}]");

    public static String normalizedFromUTF8(String name) {
        String s = Normalizer.normalize(name, Normalizer.Form.NFD);
        s = p.matcher(s).replaceAll(""); // remove diacritics, accents, etc.
        s = s.toLowerCase(Locale.ENGLISH); // just english lowercase rules (JVM independent)
        return s;
    }
}
