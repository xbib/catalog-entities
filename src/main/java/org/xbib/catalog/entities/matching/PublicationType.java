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
     * The domain of the publication type component
     *
     * @return domain
     */
    public Domain getDomain() {
        return Domain.MATERIAL;
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
     * Encode type
     *
     * @return encoded type
     */
    public String encode() {
        if (isEmpty()) {
            throw new IllegalArgumentException("empty component");
        }
        return get(0).getString();
    }

    /**
     * Return current publication type component
     *
     * @return the bibliographic type
     */
    public BibliographicType getType() {
        return get(0);
    }
}
