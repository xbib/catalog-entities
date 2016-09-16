package org.xbib.catalog.entities.matching.string;

import java.util.Arrays;
import java.util.List;

/**
 * Encodes a string into a soundex value. Soundex is an encoding used to relate
 * similar names, but can also be used as a general purpose scheme to find word
 * with similar phonemes.
 *
 */
public class SoundexEncoder implements StringEncoder {

    /**
     * This is a default mapping of the 26 letters used in US english.
     */
    private static final List<Character> US_ENGLISH_MAPPING =
            Arrays.asList('0', '1', '2', '3', '0', '1', '2', '0',
                    '0', '2', '2', '4', '5', '5', '0', '1',
                    '2', '6', '2', '3', '0', '1', '0', '2', '0', '2');
    /**
     * This static variable contains an instance of the Soundex using the
     * US_ENGLISH mapping.
     */
    public static final SoundexEncoder US_ENGLISH = new SoundexEncoder();
    /**
     * Every letter of the alphabet is "mapped" to a numerical value. This char
     * array holds the values to which each letter is mapped. This implementation
     * contains a default map for US_ENGLISH
     */
    private List<Character> soundexMapping;
    /**
     * The maximum length of a Soundex code - Soundex codes are only four
     * characters by definition.
     */
    private int maxLength = 4;

    /**
     * Creates an instance of the Soundex object using the default US_ENGLISH
     * mapping.
     */
    public SoundexEncoder() {
        this(US_ENGLISH_MAPPING);
    }

    /**
     * Creates a soundex instance using a custom mapping. This constructor can be
     * used to customize the mapping, and/or possibly provide an internationalized
     * mapping for a non-Western character set.
     *
     * @param mapping Mapping array to use when finding the corresponding code for a
     *                given character
     */
    public SoundexEncoder(List<Character> mapping) {
        this.soundexMapping = mapping;
    }

    /**
     * Retrieves the Soundex code for a given string
     *
     * @param str String to encode using the Soundex algorithm
     * @return A soundex code for the String supplied
     */
    public String encode(String str) throws EncoderException {
        if (null == str || str.length() == 0) {
            return str;
        }
        char out[] = {'0', '0', '0', '0'};
        char last, mapped;
        int incount = 1, count = 1;
        out[0] = Character.toUpperCase(str.charAt(0));
        last = getMappingCode(str.charAt(0));
        while ((incount < str.length()) && (mapped = getMappingCode(str.charAt(incount++))) != 0 && (count < this.maxLength)) {
            if ((mapped != '0') && (mapped != last)) {
                out[count++] = mapped;
            }
            last = mapped;
        }
        return new String(out);
    }

    /**
     * Used internally by the SoundEx algorithm.
     *
     * @param c character to use to retrieve mapping code
     * @return Mapping code for a particular character
     */
    private char getMappingCode(char c) {
        if (!Character.isLetter(c)) {
            return 0;
        }
        return soundexMapping.get(Character.toUpperCase(c) - 'A');
    }

    /**
     * Returns the maxLength. Standard Soundex
     *
     * @return int
     */
    public int getMaxLength() {
        return this.maxLength;
    }

    /**
     * Sets the maxLength.
     *
     * @param maxLength The maxLength to set
     */
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }
}