package org.xbib.catalog.entities.matching.date;

import org.xbib.catalog.entities.matching.Domain;
import org.xbib.catalog.entities.matching.DomainKey;

import java.time.Year;
import java.util.LinkedList;
import java.util.Objects;

/**
 *
 */
public class YearKey extends LinkedList<String> implements DomainKey<String> {

    private static final long serialVersionUID = 4622928393831608899L;

    private char delimiter = '/';

    @Override
    public Domain getDomain() {
        return Domain.DATE;
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

    @Override
    public boolean add(String element) {
        return super.add(element.replaceAll("[^\\p{Digit}]", ""));
    }

    @Override
    public String encode() {
        StringBuilder sb = new StringBuilder();
        for (String s : this) {
            try {
                sb.append(formatYear(parseYear(s))).append(delimiter);
            } catch (Exception e) {
                throw new IllegalArgumentException("unable to encode date in " + s + ", reason: " + e.getMessage(), e);
            }
        }
        int len = sb.length();
        if (len > 0) {
            sb.deleteCharAt(len - 1);
        }
        return sb.toString();
    }

    private Year parseYear(String yearStr) {
        Objects.requireNonNull(yearStr);
        return Year.parse(yearStr);
    }

    private String formatYear(Year year) {
        Objects.requireNonNull(year);
        return year.toString();
    }


    @Override
    public boolean equals(Object object) {
        return this == object || (object instanceof YearKey && hashCode() == object.hashCode());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
