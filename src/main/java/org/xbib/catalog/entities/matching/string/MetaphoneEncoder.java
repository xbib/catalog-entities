package org.xbib.catalog.entities.matching.string;

/**
 * A class to generate phonetic code. The initial Java implementation, William
 * B. Brogden. December, 1997 Permission given by wbrogden for code to be used
 * anywhere.
 * "Hanging on the Metaphone" by Lawrence Philips <i>Computer Language </i> of
 * Dec. 1990, p 39
 *
 */
public class MetaphoneEncoder implements StringEncoder {

    /**
     * The max code length for metaphone is 4
     */
    private int maxCodeLen = 4;

    /**
     * Find the metaphone value of a String. This is similar to the soundex
     * algorithm, but better at finding similar sounding words. All input is
     * converted to upper case. Limitations: Input format is expected to be a
     * single ASCII word with only characters in the A - Z range, no punctuation
     * or numbers.
     *
     * @param txt String to find the metaphone code for
     * @return A metaphone code corresponding to the String supplied
     */
    public String encode(String txt) throws EncoderException {
        int mtsz = 0;
        boolean hard = false;
        if ((txt == null) || (txt.length() == 0)) {
            return "";
        }
        // single character is itself
        if (txt.length() == 1) {
            return txt.toUpperCase();
        }
        char[] inwd = txt.toUpperCase().toCharArray();
        String tmpS;
        StringBuffer local = new StringBuffer(40); // manipulate
        StringBuffer code = new StringBuffer(10); //   output
        // handle initial 2 characters exceptions
        switch (inwd[0]) {
            case 'K':
            case 'G':
            case 'P': /* looking for KN, etc */
                if (inwd[1] == 'N') {
                    local.append(inwd, 1, inwd.length - 1);
                } else {
                    local.append(inwd);
                }
                break;
            case 'A': /* looking for AE */
                if (inwd[1] == 'E') {
                    local.append(inwd, 1, inwd.length - 1);
                } else {
                    local.append(inwd);
                }
                break;
            case 'W': /* looking for WR or WH */
                if (inwd[1] == 'R') { // WR -> R
                    local.append(inwd, 1, inwd.length - 1);
                    break;
                }
                if (inwd[1] == 'H') {
                    local.append(inwd, 1, inwd.length - 1);
                    local.setCharAt(0, 'W'); // WH -> W
                } else {
                    local.append(inwd);
                }
                break;
            case 'X': /* initial X becomes S */
                inwd[0] = 'S';
                local.append(inwd);
                break;
            default:
                local.append(inwd);
        } // now local has working string with initials fixed
        int wdsz = local.length();
        int n = 0;
        // max code size of 4 works well
        String frontv = "EIY";
        String vowels = "AEIOU";
        while ((mtsz < this.maxCodeLen) && (n < wdsz)) {
            char symb = local.charAt(n);
            // remove duplicate letters except C
            if ((symb != 'C') && (n > 0) && (local.charAt(n - 1) == symb)) {
                n++;
            } else { // not dup
                switch (symb) {
                    case 'A':
                    case 'E':
                    case 'I':
                    case 'O':
                    case 'U': {
                        if (n == 0) {
                            code.append(symb);
                            mtsz++;
                        }
                        break; // only use vowel if leading char
                    }
                    case 'B': {
                        code.append(symb);
                        mtsz++;
                        break;
                    }
                    case 'C': {
                        // lots of C special cases

            /* discard if SCI, SCE or SCY */
                        /*
      Variable used in Metaphone algorithm
     */
                        if ((n > 0) && (local.charAt(n - 1) == 'S') && (n + 1 < wdsz) && (frontv.indexOf(local.charAt(n + 1)) >= 0)) {
                            break;
                        }
                        tmpS = local.toString();
                        if (tmpS.indexOf("CIA", n) == n) { // "CIA" -> X
                            code.append('X');
                            mtsz++;
                            break;
                        }
                        if ((n + 1 < wdsz) && (frontv.indexOf(local.charAt(n + 1)) >= 0)) {
                            code.append('S');
                            mtsz++;
                            break; // CI,CE,CY -> S
                        }
                        if ((n > 0) && (tmpS.indexOf("SCH", n - 1) == n - 1)) { // SCH->sk
                            code.append('K');
                            mtsz++;
                            break;
                        }
                        /*
      Five values in the English language
     */
                        if (tmpS.indexOf("CH", n) == n) { // detect CH
                            if ((n == 0) && (wdsz >= 3) // CH consonant -> K consonant
                                    && (vowels.indexOf(local.charAt(2)) < 0)) {
                                code.append('K');
                            } else {
                                code.append('X'); // CHvowel -> X
                            }
                            mtsz++;
                        } else {
                            code.append('K');
                            mtsz++;
                        }
                        break;
                    }
                    case 'D': {
                        if ((n + 2 < wdsz) // DGE DGI DGY -> J
                                && (local.charAt(n + 1) == 'G') && (frontv.indexOf(local.charAt(n + 2)) >= 0)) {
                            code.append('J');
                            n += 2;
                        } else {
                            code.append('T');
                        }
                        mtsz++;
                        break;
                    }
                    case 'G': {
                        // GH silent at end or before consonant
                        if ((n + 2 == wdsz) && (local.charAt(n + 1) == 'H')) {
                            break;
                        }
                        if ((n + 2 < wdsz) && (local.charAt(n + 1) == 'H') && (vowels.indexOf(local.charAt(n + 2)) < 0)) {
                            break;
                        }
                        tmpS = local.toString();
                        if ((n > 0) && (tmpS.indexOf("GN", n) == n) || (tmpS.indexOf("GNED", n) == n)) {
                            break; // silent G
                        }
                        hard = (n > 0) && (local.charAt(n - 1) == 'G');
                        if ((n + 1 < wdsz) && (frontv.indexOf(local.charAt(n + 1)) >= 0) && (!hard)) {
                            code.append('J');
                        } else {
                            code.append('K');
                        }
                        mtsz++;
                        break;
                    }
                    case 'H': {
                        if (n + 1 == wdsz) {
                            break; // terminal H
                        }
                        /*
      Variable used in Metaphone algorithm
     */
                        String varson = "CSPTG";
                        if ((n > 0) && (varson.indexOf(local.charAt(n - 1)) >= 0)) {
                            break;
                        }
                        if (vowels.indexOf(local.charAt(n + 1)) >= 0) {
                            code.append('H');
                            mtsz++;// Hvowel
                        }
                        break;
                    }
                    case 'F':
                    case 'J':
                    case 'L':
                    case 'M':
                    case 'N':
                    case 'R': {
                        code.append(symb);
                        mtsz++;
                        break;
                    }
                    case 'K': {
                        if (n > 0) { // not initial
                            if (local.charAt(n - 1) != 'C') {
                                code.append(symb);
                            }
                        } else {
                            code.append(symb); // initial K
                        }
                        mtsz++;
                        break;
                    }
                    case 'P': {
                        if ((n + 1 < wdsz) && (local.charAt(n + 1) == 'H')) {
                            // PH -> F
                            code.append('F');
                        } else {
                            code.append(symb);
                        }
                        mtsz++;
                        break;
                    }
                    case 'Q': {
                        code.append('K');
                        mtsz++;
                        break;
                    }
                    case 'S': {
                        tmpS = local.toString();
                        if ((tmpS.indexOf("SH", n) == n) || (tmpS.indexOf("SIO", n) == n) || (tmpS.indexOf("SIA", n) == n)) {
                            code.append('X');
                        } else {
                            code.append('S');
                        }
                        mtsz++;
                        break;
                    }
                    case 'T': {
                        tmpS = local.toString(); // TIA TIO -> X
                        if ((tmpS.indexOf("TIA", n) == n) || (tmpS.indexOf("TIO", n) == n)) {
                            code.append('X');
                            mtsz++;
                            break;
                        }
                        if (tmpS.indexOf("TCH", n) == n) {
                            break;
                        }
                        // substitute numeral 0 for TH (resembles theta after all)
                        if (tmpS.indexOf("TH", n) == n) {
                            code.append('0');
                        } else {
                            code.append('T');
                        }
                        mtsz++;
                        break;
                    }
                    case 'V': {
                        code.append('F');
                        mtsz++;
                        break;
                    }
                    case 'W':
                    case 'Y': {
                        // silent if not followed by vowel
                        if ((n + 1 < wdsz) && (vowels.indexOf(local.charAt(n + 1)) >= 0)) {
                            code.append(symb);
                            mtsz++;
                        }
                        break;
                    }
                    case 'X': {
                        code.append('K');
                        code.append('S');
                        mtsz += 2;
                        break;
                    }
                    case 'Z': {
                        code.append('S');
                        mtsz++;
                        break;
                    }
                    default:
                        break;
                } // end switch
                n++;
            } // end else from symb != 'C'
            if (mtsz > this.maxCodeLen) {
                code.setLength(this.maxCodeLen);
            }
        }
        return code.toString();
    }

    /**
     * Tests is the metaphones of two strings are identical.
     *
     * @param str1 First of two strings to compare
     * @param str2 Second of two strings to compare
     * @return true if the metaphones of these strings are identical, false
     *         otherwise.
     */
    public boolean isMetaphoneEqual(String str1, String str2) {
        try {
            return encode(str1).equals(encode(str2));
        } catch (EncoderException e) {
            return false;
        }
    }

    /**
     * Returns the maxCodeLen.
     *
     * @return int
     */
    public int getMaxCodeLen() {
        return this.maxCodeLen;
    }

    /**
     * Sets the maxCodeLen.
     *
     * @param maxCodeLen The maxCodeLen to set
     */
    public void setMaxCodeLen(int maxCodeLen) {
        this.maxCodeLen = maxCodeLen;
    }
}