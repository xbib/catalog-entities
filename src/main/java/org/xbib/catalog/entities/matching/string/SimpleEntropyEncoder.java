package org.xbib.catalog.entities.matching.string;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A simple entropy encoder
 * Inspired by:
 * Character coding for bibliographical record control.
 * E. J. Yannakoudakis, F. H. Ayres and J. A. W. Huggill.
 * Computer Centre, University of Bradford, 1980
 * Yannakoudakis, E. J. Derived search keys for bibliographic
 * retrieval. SIGIR Forum 17, 4 (Jun. 1983), 220-237.
 *
 */
public class SimpleEntropyEncoder implements StringEncoder {

    /**
     * Encode a string by a simple entropy-based method.
     * Strategy: count characters in lower-case string,
     * select only characters with a frequency of 1,
     * drop space characters.
     *
     * @param s s
     * @return encoded string
     * @throws EncoderException if encoding fails
     */
    @Override
    public String encode(String s) throws EncoderException {
        LinkedHashMap<Character, Integer> freq = new LinkedHashMap<>();
        for (char ch : s.toLowerCase().toCharArray()) {
            freq.put(ch, freq.containsKey(ch) ? freq.get(ch) + 1 : 0);
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Character, Integer> entry: freq.entrySet()) {
            char ch = entry.getKey();
            if (!Character.isWhitespace(ch) && entry.getValue() < 2) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
}
