package org.xbib.catalog.entities.matching.date;

import org.xbib.catalog.entities.matching.Domain;
import org.xbib.catalog.entities.matching.Key;

import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

public class YearKey extends LinkedList<String> implements Key<String> {

    private static final long serialVersionUID = 4622928393831608899L;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy");

    private char delimiter = '/';

    public Domain getDomain() {
        return Domain.DATE;
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

    @Override
    public boolean add(String element) {
        return super.add(element.replaceAll("[^\\p{Digit}]", ""));
    }

    public String encode() {
        StringBuilder sb = new StringBuilder();
        for (String s : this) {
            try {
                sb.append(formatYear(parseYear(s))).append(delimiter);
            } catch (Exception e) {
                throw new IllegalArgumentException("unable to encode date in " + s + ", reason: " + e.getMessage());
            }
        }
        int len = sb.length();
        if (len > 0) {
            sb.deleteCharAt(len - 1);
        }
        return sb.toString();
    }

    private Date parseYear(String dateStr) {
        if (dateStr == null) {
            throw new IllegalArgumentException("null date?");
        }
        synchronized (sdf) {
            return sdf.parse(dateStr, new ParsePosition(0));
        }
    }

    private String formatYear(java.util.Date date) {
        if (date == null) {
            throw new IllegalArgumentException("null date?");
        }
        StringBuffer sb = new StringBuffer();
        synchronized (sdf) {
            sdf.format(date, sb, new FieldPosition(0));
        }
        return sb.toString();
    }
}
