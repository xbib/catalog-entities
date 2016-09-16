package org.xbib.catalog.entities.matching;

import org.xbib.catalog.entities.matching.string.EncoderException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A segmented key
 */
public class DefaultCompositeKey<T> extends AbstractCollection<Key<T>>
        implements CompositeKey<T> {

    private final List<Key<T>> list = new LinkedList<>();

    /**
     * delimiter
     */
    private char delimiter;
    /**
     * component delimiter
     */
    private char componentDelimiter;
    /**
     * the key code
     */
    private String key;
    /**
     * usable flag
     */
    private boolean usable;

    public DefaultCompositeKey() {
        this(':', '/');
    }

    public DefaultCompositeKey(char componentDelimiter, char delimiter) {
        this.componentDelimiter = componentDelimiter;
        this.delimiter = delimiter;
    }

    @Override
    public boolean add(Key<T> component) {
        return list.add(component);
    }

    @Override
    public Iterator<Key<T>> iterator() {
        return list.iterator();
    }

    @Override
    public int size() {
        return list.size();
    }

    /**
     * Get URI of this key. Create key if not already encoded.
     *
     * @param prefix the prefix
     * @return uri
     * @throws URISyntaxException if URI syntax is invalid
     * @throws EncoderException if encoding fails
     */
    public URI encodeToURI(String prefix) throws URISyntaxException, EncoderException {
        if (key == null) {
            key = encodeKey(new StringBuilder(prefix));
        }
        return URI.create(key);
    }

    public String encodeToString() throws EncoderException {
        if (key == null) {
            key = encodeKey(new StringBuilder());
        }
        return key;
    }

    public void update(Key<T> component) {
        for (int i = 0; i < size(); i++) {
            Key<T> segment = list.get(i);
            if (component.getDomain().equals(segment.getDomain())) {
                list.set(i, component);
            }
        }
    }

    public void remove(Key<T> component) {
        super.remove(component);
    }

    public Key<T> getComponent(Domain domain) {
        for (Key<T> segment : this) {
            if (domain.equals(segment.getDomain())) {
                return segment;
            }
        }
        return null;
    }

    public boolean isUsable() {
        boolean anyusable = false;
        for (Key<T> segment : this) {
            anyusable = anyusable || segment.isUsable();
        }
        return anyusable;
    }

    public void setUsable(boolean usable) {
        this.usable = usable;
    }

    public boolean getUsable() {
        return usable;
    }

    public static DefaultCompositeKey<String> parse(URI key, char componentDelimiter, char delimiter) {
        DefaultCompositeKey<String> k = new DefaultCompositeKey<>(componentDelimiter, delimiter);
        for (String s : key.getSchemeSpecificPart().split(String.valueOf(componentDelimiter))) {
            if (s.length() > 0) {
                String domain = s.substring(0, 1);
                String value = s.substring(1);
                try {
                    k.add(new DefaultKey(Domain.getDomain(domain), value));
                } catch (InvalidDomainException e) {
                    // silently ignore invalid cluster domains
                }
            }
        }
        return k;
    }

    @Override
    public String toString() {
        try {
            return encodeToString();
        } catch (EncoderException e) {
            return "EncoderException: " + e.getMessage();
        }
    }

    private String encodeKey(StringBuilder sb) throws EncoderException {
        for (Key<T> segment : this) {
            if (segment.isUsable()) {
                char segmentDelimiter = segment.getDelimiter();
                segment.setDelimiter(delimiter);
                sb.append(segment.getDomain()).append(segment.encode()).append(componentDelimiter);
                segment.setDelimiter(segmentDelimiter);
            }
        }
        int len = sb.length();
        if (len > 1) {
            sb.deleteCharAt(len - 1);
        }
        return sb.toString();
    }
}
