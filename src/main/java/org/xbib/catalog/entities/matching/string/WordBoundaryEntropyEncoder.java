package org.xbib.catalog.entities.matching.string;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A word-boundary aware entropy encoder.
 *
 * Each character at word beginning or end of string is kept as upper case, the rest
 * will collabse into lower case. This helps preserving words.
 *
 * The reason for preserving that kind of information is that errors are very rare at boundaries
 * and the too eager dropping of single frequence character  are compensated.
 *
 * There are also lone characters forming a word in a journal title, e.g.
 * "Physical Review A", Physical Review B" which are preserved this way.
 *
 * Inspired by:
 * Character coding for bibliographical record control.
 * E. J. Yannakoudakis, F. H. Ayres and J. A. W. Huggill.
 * Computer Centre, University of Bradford, 1980
 * Yannakoudakis, E. J. Derived search keys for bibliographic
 * retrieval. SIGIR Forum 17, 4 (Jun. 1983), 220-237.
 *
 */
public class WordBoundaryEntropyEncoder implements StringEncoder {

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
        char[] chars = s.toLowerCase().toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char ch = chars[i];
            // first character, character after space = first character of a word, and final character
            if (i == 0 || (i > 0 && chars[i - 1] == ' ') || i + 1 == chars.length) {
                ch = Character.toUpperCase(ch);
            }
            freq.put(ch, freq.containsKey(ch) ? freq.get(ch) + 1 : 0);
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Character, Integer> entry: freq.entrySet()) {
            char ch = entry.getKey();
            if (Character.isWhitespace(ch)) {
                continue;
            }
            if (entry.getValue() < 2) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
}
