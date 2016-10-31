package org.xbib.catalog.entities.matching.string;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 */
public class WordEncoder {

    private static final Pattern word = Pattern.compile("[\\P{IsWord}]");

    public List<String> splitWord(String string) {
        return Arrays.stream(string.split(word.pattern(), 0))
                .filter(s ->  s != null && !s.isEmpty())
                .collect(Collectors.toList());
    }
}
