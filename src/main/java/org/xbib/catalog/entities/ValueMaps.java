package org.xbib.catalog.entities;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 */
public class ValueMaps {

    private static final Map<String, Object> maps = new HashMap<>();

    private final ClassLoader classLoader;

    public ValueMaps() {
        this.classLoader = getClass().getClassLoader();
    }

    public ValueMaps(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @SuppressWarnings("unchecked")
    public synchronized Map<String, Object> getMap(String path, String format) throws IOException {
        if (!maps.containsKey(format)) {
            URL url = classLoader.getResource(path);
            if (url == null) {
                throw new IllegalArgumentException("resource in class path does not exist " + path);
            }
            try (InputStream in = url.openStream()) {
                maps.put(format, new ObjectMapper().readValue(in, HashMap.class));
            }
        }
        return (Map<String, Object>) maps.get(format);
    }

    @SuppressWarnings("unchecked")
    public synchronized Map<String, String> getAssocStringMap(String path, String format) throws IOException {
        if (!maps.containsKey(format)) {
            URL url = classLoader.getResource(path);
            if (url == null) {
                throw new IllegalArgumentException("resource in class path does not exist " + path);
            }
            try (InputStream in = url.openStream()) {
                Map<String, Object> result = new ObjectMapper().readValue(in, HashMap.class);
                Object values = result.get(format);
                Collection<String> c = (Collection<String>) values;
                if (c != null) {
                    // assoc map
                    final Map<String, String> map = new HashMap<>();
                    Iterator<String> it = c.iterator();
                    for (int i = 0; i < c.size(); i += 2) {
                        map.put(it.next(), it.next());
                    }
                    maps.put(format, map);
                }
            }
        }
        return (Map<String, String>) maps.get(format);
    }
}
