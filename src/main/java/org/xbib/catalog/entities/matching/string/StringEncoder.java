package org.xbib.catalog.entities.matching.string;

/**
 * Interface for string encoding.
 *
 */
public interface StringEncoder {

    /**
     * Encodes a String and returns a String.
     *
     * @param s a String to encode
     * @return the encoded String
     * @throws EncoderException if there is an error condition during the encoding process.
     */
    String encode(String s) throws EncoderException;
}
