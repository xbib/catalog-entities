package org.xbib.catalog.entities.matching;

import org.xbib.catalog.entities.matching.endeavor.BibliographicType;

import java.util.LinkedList;

/**
 * Publication type.
 *
 */
public class PublicationType extends LinkedList<BibliographicType>
        implements Key<BibliographicType> {

    private static final long serialVersionUID = 4758698943770255338L;
    private char delimiter = '/';

    /**
     * The domain of the publication type component.
     *
     * @return domain
     */
    @Override
    public Domain getDomain() {
        return Domain.MATERIAL;
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
     * Encode type.
     *
     * @return encoded type
     */
    public String encode() {
        if (isEmpty()) {
            throw new IllegalArgumentException("empty component");
        }
        return get(0).toString();
    }

    /**
     * Return current publication type component.
     *
     * @return the bibliographic type
     */
    public BibliographicType getType() {
        return get(0);
    }

    @Override
    public boolean equals(Object object) {
        return this == object || (object instanceof PublicationType && hashCode() == object.hashCode());
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
