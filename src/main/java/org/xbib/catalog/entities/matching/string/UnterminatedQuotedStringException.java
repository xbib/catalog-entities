package org.xbib.catalog.entities.matching.string;

/**
 * String tokenizer exception.
 *
 */
public class UnterminatedQuotedStringException extends RuntimeException {

    private static final long serialVersionUID = 1337724432810924343L;

    /**
     * Creates a new String tokenizer object.
     * @param msg msg
     */
    public UnterminatedQuotedStringException(String msg) {
        super(msg);
    }
}
