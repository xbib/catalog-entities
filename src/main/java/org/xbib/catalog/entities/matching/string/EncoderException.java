package org.xbib.catalog.entities.matching.string;

/**
 * String encoding exception.
 */
public class EncoderException extends Exception {

    private static final long serialVersionUID = 5633325561421289098L;

    /**
     * Creates a new EncoderException object.
     */
    public EncoderException() {
        super();
    }

    /**
     * Creates a new EncoderException object.
     *
     * @param msg the message
     */
    public EncoderException(String msg) {
        super(msg);
    }

    /**
     * Creates a new EncoderException object.
     *
     * @param t the throwable object
     */
    public EncoderException(Throwable t) {
        super(t);
    }

    /**
     * Creates a new EncoderException object.
     *
     * @param msg the message
     * @param t   the throwable object
     */
    public EncoderException(String msg, Throwable t) {
        super(msg, t);
    }
}
