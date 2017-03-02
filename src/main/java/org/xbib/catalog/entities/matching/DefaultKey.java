package org.xbib.catalog.entities.matching;

import java.util.LinkedList;

/**
 * Simple component.
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

    @Override
    public Domain getDomain() {
        return domain;
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
    public boolean equals(Object object) {
        return this == object || (object instanceof DefaultKey && hashCode() == object.hashCode());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return domain + ":" + encode();
    }
}
