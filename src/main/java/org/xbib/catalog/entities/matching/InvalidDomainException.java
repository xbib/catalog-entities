package org.xbib.catalog.entities.matching;

/**
 * Invalid cluster domain exception.
 */
public class InvalidDomainException extends Exception {

    private static final long serialVersionUID = -1787958986367971678L;

    /**
     * Creates a new exception object.
     *
     * @param msg message
     */
    public InvalidDomainException(String msg) {
        super(msg);
    }


}
