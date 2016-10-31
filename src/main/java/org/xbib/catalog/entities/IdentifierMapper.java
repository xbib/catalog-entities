package org.xbib.catalog.entities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class IdentifierMapper {

    private static final Pattern p = Pattern.compile("^1\\s(.{21})(.{5}).*");

    private Map<String, String> map = new HashMap<>();

    public Map<String, String> load(InputStream in) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.ISO_8859_1))) {
            bufferedReader.lines().forEach(line -> {
                if (line.trim().length() > 0 && !line.startsWith("!")) {
                    Matcher m = p.matcher(line);
                    if (m.matches()) {
                        String sigel = m.group(1).trim();
                        String owner = m.group(2).trim();
                        String isil = map.containsKey(sigel) ? map.get(sigel) : createISIL(sigel);
                        map.put(owner, isil);
                        map.put(sigel, isil);
                    }
                }
            });
        }
        return map;
    }

    private String createISIL(String sigel) {
        String isil = sigel;
        isil = isil.replaceAll("ä", "ae").replaceAll("ö", "oe").replaceAll("ü", "ue").replaceAll("\\s+", "");
        isil = isil.replace('/', '-');
        // heuristic
        if (!isil.startsWith("ZDB")) {
            isil = "DE-" + isil;
        }
        return isil;
    }

    public IdentifierMapper add(Map<String, String> map) {
        this.map.putAll(map);
        return this;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public String lookup(String value) {
        return map.containsKey(value) ? map.get(value) : value;
    }
}
