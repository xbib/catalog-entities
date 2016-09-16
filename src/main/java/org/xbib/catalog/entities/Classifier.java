package org.xbib.catalog.entities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class Classifier {

    private String classifierID;

    private Map<String, Collection<ClassifierEntry>> map;
    private Function<String, ClassifierEntry> mapToKeyValue = (line) -> {
        String[] p = line.split("\t");
        return p.length > 2 ? new ClassifierEntry(classifierID + p[0].trim(), p[1].trim(), p[2].trim()) : null;
    };

    public Classifier load(InputStream in, String name, String classifierID) throws IOException {
        return load(new InputStreamReader(in, StandardCharsets.UTF_8), name, classifierID);
    }

    public Classifier load(Reader reader, String name, String classifierID) throws IOException {
        this.map = new HashMap<>();
        this.classifierID = classifierID;
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            bufferedReader.lines()
                    .map(mapToKeyValue)
                    .filter(Objects::nonNull)
                    .forEach(kv -> putEntry(map, name, kv));
        }
        return this;
    }

    private void putEntry(Map<String, Collection<ClassifierEntry>> map, String name, ClassifierEntry classifierEntry) {
        String key = name + '.' + classifierEntry.doc + '.';
        Collection<ClassifierEntry> entries = map.containsKey(key) ? map.get(key) : new LinkedList<>();
        entries.add(classifierEntry);
        map.put(key, entries);
        map.put(name + '.' + classifierEntry.doc + '.' + classifierEntry.code, entries);
    }

    public Map<String, Collection<ClassifierEntry>> getMap() {
        return map;
    }

    /**
     * One-to-one pattern matching.
     *
     * @param key the key e.g. DE-605.12345.ABC
     * @return found entry or null
     */
    public Collection<ClassifierEntry> lookup(String key) {
        return map.get(key);
    }

    /**
     * Full lookup with whitespace splitting and optional pattern.
     *
     * @param name    the namespace e.g. DE-605
     * @param doc     the doc ID e.g. 12345
     * @param code    the code e.g. ABC
     * @param pattern optional regex for examining the code
     * @return found entry or null
     */
    public Collection<ClassifierEntry> lookup(String name, String doc, String code, Pattern pattern) {
        if (code == null || code.isEmpty() || code.trim().isEmpty()) {
            return null;
        }
        String k = name + '.' + doc + '.' + code;
        Collection<ClassifierEntry> entries = map.containsKey(k) ? map.get(k) : null;
        if (entries != null) {
            return entries;
        }
        if (pattern == null) {
            // split code into fragments
            String[] array = code.split("\\s+");
            for (String s : array) {
                if (s.length() > 0 && !code.equals(s)) {
                    entries = lookup(name, doc, s, null);
                    if (entries != null) {
                        return entries;
                    }
                }
            }
            return null;
        } else {
            // pattern matching
            Matcher m = pattern.matcher(code);
            if (m.find()) {
                entries = lookup(name, doc, code.substring(m.start(), m.end()), null);
                if (entries != null) {
                    return entries;
                }
            }
        }
        return null;
    }


}
