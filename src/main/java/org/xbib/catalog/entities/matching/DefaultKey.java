package org.xbib.catalog.entities.matching;

import java.util.LinkedList;

/**
 * Simple component
 */
public class DefaultKey extends LinkedList<String> implements Key<String> {

    private static final long serialVersionUID = 8678339287438796574L;
    private Domain domain;
    private char delimiter = '/';

    public DefaultKey(Domain domain, String value) throws InvalidDomainException {
        this.domain = domain;
        add(value);
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public Domain getDomain() {
        return domain;
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

    public String encode() {
        StringBuilder sb = new StringBuilder();
        for (String s : this) {
            sb.append(s).append(delimiter);
        }
        int len = sb.length();
        if (len > 0) {
            sb.deleteCharAt(len - 1);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return domain + ":" + encode();
    }
}
