package org.xbib.catalog.entities.matching;

import org.xbib.catalog.entities.matching.string.EncoderException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

/**
 * A ccomposite key holds one or more keys,
 * where the encoded key representations are used to generate a
 * uniform resource identifier.
 * The order of the keys is maintained as the order of adding keys.
 * A key can be updated or removed.
 *
 * @param <T> type parameter
 */
public interface CompositeKey<T> extends Collection<Key<T>> {

    /**
     * Encode cluster key as string or null.
     *
     * @return the encoded cluster key or null
     * @throws EncoderException if encoding fails
     */
    String encodeToString() throws EncoderException;

    /**
     * Encode cluster key as a Uniform Resource Identifier.
     *
     * @param prefix the URI prefix
     * @return the uri
     * @throws URISyntaxException if a cluster key can not be constructed as an URI
     * @throws EncoderException if encoding fails
     */
    URI encodeToURI(String prefix) throws URISyntaxException, EncoderException;

    /**
     * Get component for a given domain.
     *
     * @param domain the domain
     * @return the cluster key component
     */
    Key<T> getComponent(Domain domain);

    /**
     * Update component.
     *
     * @param component component
     */
    void update(Key<T> component);

    /**
     * Set or unset usable flag. This flag can invalidate the key, if the application
     * decides the key should not be used, but retains the components.
     * @param usable true for uable
     */
    void setUsable(boolean usable);

    /**
     * Get usable flag.
     *
     * @return true is usable
     */
    boolean getUsable();
}
