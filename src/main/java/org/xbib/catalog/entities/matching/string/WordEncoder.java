package org.xbib.catalog.entities.matching.string;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WordEncoder {

    private final static Pattern word = Pattern.compile("[\\P{IsWord}]");

    public List<String> splitWord(String string) {
        return Arrays.asList(string.split(word.pattern(), 0))
                .stream()
                .filter(s ->  s != null && !s.isEmpty())
                .collect(Collectors.toList());
    }
}
