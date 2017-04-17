package org.xbib.catalog.entities.matching.name;


import org.xbib.catalog.entities.matching.Domain;
import org.xbib.catalog.entities.matching.DomainKey;
import org.xbib.catalog.entities.matching.string.DoubleMetaphoneEncoder;
import org.xbib.catalog.entities.matching.string.EncoderException;

import java.text.Normalizer;
import java.util.TreeSet;

/**
 * Name key.
 *
 */
public class NameKey extends TreeSet<String> implements DomainKey<String> {

    private static final long serialVersionUID = -8452516356385112613L;
    private char delimiter = '/';

    @Override
    public Domain getDomain() {
        return Domain.CREATOR;
    }

    @Override
    public void setDelimiter(char delimiter) {
        this.delimiter = delimiter;
    }

    @Override
    public char getDelimiter() {
        return delimiter;
    }

    @Override
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

    @Override
    public String encode() throws EncoderException {
        DoubleMetaphoneEncoder enc = new DoubleMetaphoneEncoder();
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

    @Override
    public boolean equals(Object object) {
        return this == object || (object instanceof NameKey && hashCode() == object.hashCode());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
