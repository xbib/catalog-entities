package org.xbib.catalog.entities.matching.structure;

import org.xbib.catalog.entities.matching.Domain;
import org.xbib.catalog.entities.matching.DomainKey;

import java.util.LinkedHashSet;

/**
 * A bibliographic member component can denote more precisely an
 * expression, manfestation, or item, which serves as a member of
 * an author/title cluster.
 * A member statement is typically a volume title, an edition statement,
 * or a reference of an article in a journal.
 * Because the characteristics
 * of the member classes are so different, they are not encoded except
 * that all characters not being a number or a letter are removed in order
 * to ensure a valid URI construction.
 * The usage of member components depends on application specific tasks.
 * For accomplishing this mor easily, a domain parameter has been added.
 * The default domain is "G" for generic member.
 *
 */
public class MemberComponent extends LinkedHashSet<String>
        implements DomainKey<String> {

    private static final long serialVersionUID = -7488832334746656547L;
    private char delimiter = '/';

    @Override
    public Domain getDomain() {
        return Domain.GENERIC;
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
     * Add generic member component.
     *
     * @param value value
     */
    @Override
    public boolean add(String value) {
        return this.add(Domain.GENERIC, value);
    }

    public boolean add(Domain domain, Comparable<?> c) {
        if (c == null || domain == null) {
            return false;
        }
        // strip any character which is not a number or letter 
        // from a member statement to ensure a valid URI
        String s = c.toString().replaceAll("[^\\p{L}\\p{N}]", "");
        return s.length() > 0 && super.add(domain + s);
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
        return this == object || (object instanceof MemberComponent && hashCode() == object.hashCode());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
