package org.xbib.catalog.entities.matching;

import org.xbib.catalog.entities.matching.string.EncoderException;

import java.util.Collection;

/**
 * A key consists of a list of elements which are added to this component.
 * After adding, all elements are encoded for being
 * combined with other components into a cluster key.
 *
 * @param <T> type parameter
 */
public interface Key<T> extends Collection<T> {

    /**
     * The cluster key component domain.
     *
     * @return the domain name
     */
    Domain getDomain();

    /**
     * Encode this component.
     *
     * @return the encoded component
     * @throws EncoderException if cencoder fails
     */
    String encode() throws EncoderException;

    /**
     * Set delimiter char.
     *
     * @param delimiter delimiter
     */
    void setDelimiter(char delimiter);

    /**
     * Get the delimiting character between component elements.
     *
     * @return the delimiter
     */
    char getDelimiter();

    /**
     * Return true if the component is usable.
     * @return true if usable
     */
    boolean isUsable();
}
