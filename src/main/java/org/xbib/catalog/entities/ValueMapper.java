package org.xbib.catalog.entities;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class ValueMapper {

    private static final Logger logger = Logger.getLogger(ValueMapper.class.getName());

    private static final Map<String, Object> maps = new HashMap<>();

    private final ClassLoader classLoader;

    public ValueMapper() {
        this.classLoader = getClass().getClassLoader();
    }

    public ValueMapper(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @SuppressWarnings("unchecked")
    public synchronized Map<String, Object> getMap(String path, String key) throws IOException {
        if (!maps.containsKey(key)) {
            URL url = classLoader.getResource(path);
            if (url != null) {
                try (InputStream in = url.openStream()) {
                    maps.put(key, new ObjectMapper().readValue(in, HashMap.class));
                }
            } else {
                logger.log(Level.WARNING, "value map not found at: %s", path);
            }
        }
        return (Map<String, Object>) maps.get(key);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getMap(String key) {
        return (Map<String, Object>) maps.get(key);
    }
}
