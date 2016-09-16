package org.xbib.catalog.entities.matching.name;

import org.xbib.catalog.entities.matching.Domain;
import org.xbib.catalog.entities.matching.Key;
import org.xbib.catalog.entities.matching.string.EncoderException;
import org.xbib.catalog.entities.matching.string.HaasePhonetikEncoder;

import java.text.Normalizer;
import java.util.TreeSet;

/**
 * German name
 *
 */
public class GermanNameKey extends TreeSet<String> implements Key<String> {

    private static final long serialVersionUID = 740094768192592666L;
    private char delimiter = '/';

    public Domain getDomain() {
        return Domain.CREATOR;
    }

    public void setDelimiter(char delimiter) {
        this.delimiter = delimiter;
    }

    public char getDelimiter() {
        return delimiter;
    }

    public boolean isUsable() {
        return !isEmpty();
    }

    /**
     * Add author component.
     * Remove all characters not in  Unicode group "L" (letter),
     * or "N" (number).
     * Normalize author names for phonetic encoding.
     * Only normlized forms with more than two characters are added.
     *
     * @param value value
     * @return true if component was added
     */
    @Override
    public boolean add(String value) {
        for (String s : value.split("\\p{P}|\\p{Z}")) {
            String normalized = normalize(s);
            int n = size();
            if (n < 5 && (normalized.length() > 2 || n < 1)) {
                super.add(normalized);
            }
        }
        return true;
    }

    public String encode() throws EncoderException {
        HaasePhonetikEncoder enc = new HaasePhonetikEncoder();
        StringBuilder sb = new StringBuilder();
        for (String s : this) {
            String encoded = enc.encode(s);
            if (encoded.length() > 0) {
                sb.append(encoded).append(delimiter);
            }
        }
        int len = sb.length();
        if (len > 0) {
            sb.deleteCharAt(len - 1);
        }
        return sb.toString();
    }

    protected String normalize(String value) {
        String s = value.replaceAll("[^\\p{L}\\p{N}]", "");
        return Normalizer.normalize(s, Normalizer.Form.NFD);
    }
}
