package org.xbib.catalog.entities.matching.title;

import org.xbib.catalog.entities.matching.Domain;
import org.xbib.catalog.entities.matching.Key;
import org.xbib.catalog.entities.matching.string.EncoderException;
import org.xbib.catalog.entities.matching.string.SimpleEntropyEncoder;

import java.text.Normalizer;
import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Title key.
 *
 */
public class TitleKey extends AbstractCollection<String>
        implements Key<String> {

    private final LinkedList<String> list = new LinkedList<>();

    /**
     * We use an entropy-based encoder for titles in cluster keys.
     */
    private final SimpleEntropyEncoder enc = new SimpleEntropyEncoder();

    private char delimiter = '/';

    /**
     * The name of this component class.
     *
     * @return the name
     */
    @Override
    public Domain getDomain() {
        return Domain.TITLE;
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
    public Iterator<String> iterator() {
        return list.iterator();
    }

    @Override
    public int size() {
        return list.size();
    }

    /**
     * Add a component of title word.
     * Keep each title word in sequential order.
     * Normalize title word.
     * Remove librarian comments in brackets.
     * Remove all punctuation and non-visible characters.
     * Add the title word only to components if the number of components
     * will not exceed 5 or title word length is greater than 4
     * and number of components is greater than 1.
     * That is, at most one short title word is added
     * (for very compact and short titles) at the first component position.
     *
     * @param value the value
     */
    @Override
    public boolean add(String value) {
        String normalized = normalize(value);
        int n = size();
        return !(n > 5 || (normalized.length() < 4 && n > 1)) && list.add(normalized);
    }

    /**
     * Encode the titles.
     *
     * @return the encoded title form
     */
    @Override
    public String encode() throws EncoderException {
        StringBuilder sb = new StringBuilder();
        for (String s : this) {
            sb.append(enc.encode(s)).append(delimiter);
        }
        int len = sb.length();
        if (len > 0) {
            sb.deleteCharAt(len - 1);
        }
        return sb.toString();
    }

    protected String normalize(String value) {
        String s = value.replaceAll("\\[.+\\]", ""); // remove comments
        s = Normalizer.normalize(s, Normalizer.Form.NFKD);
        return s.replaceAll("[^\\p{L}\\p{N}]", "");
    }

}
