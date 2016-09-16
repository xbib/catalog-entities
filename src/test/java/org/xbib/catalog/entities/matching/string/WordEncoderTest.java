package org.xbib.catalog.entities.matching.string;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class WordEncoderTest {

    @Test
    public void testParseQuotedString() throws Exception {
        // does not split "geht's", "Film?"
        String msg =  "Heute spielen wir den Boß - Wo geht's denn hier zum Film?";
        List<String> l = parseQuot(msg);
        //logger.info("token msg = " + msg + " l = " + l.size());
        for (String s : l) {
            //System.err.println("token = '" + s + "'");
        }
    }

    @Test
    public void testWordSplit() throws Exception {
        // splits "geht's" -> "geht", "s" and "Film?" -> "Film"
        String msg = "Heute spielen wir den Boß - Wo geht's denn hier zum Film?";
        WordEncoder we = new WordEncoder();
        for (String s : we.splitWord(msg)) {
            //System.err.println("split word = '" + s + "'");
        }
    }
    
    private List<String> parseQuot(String string) {
        List<String> l = new LinkedList<String>();
        Pattern word = Pattern.compile("[\\P{IsWord}]");
        try {
            QuotedStringTokenizer q = new QuotedStringTokenizer(string, " \t\n\r\f", "\"", '\\', false);
            q.forEachRemaining(new Consumer<String>() {
                @Override
                public void accept(String s) {
                    if (s != null && !s.isEmpty() && !word.matcher(s).matches()) {
                        l.add(s);
                    }
                }
            });
        } catch (UnterminatedQuotedStringException e) {
            //
        }
        return l;
    }
    
    private List<String> parseQuotedString(String value) {
        List<String> result = new LinkedList<String>();
        QuotedStringTokenizer tokenizer = new QuotedStringTokenizer(value);
        try {
            while (tokenizer.hasMoreTokens()) {
                result.add(tokenizer.nextToken());
            }
        } catch (IllegalArgumentException e) {
            //
        }
        return result;
    }
}
