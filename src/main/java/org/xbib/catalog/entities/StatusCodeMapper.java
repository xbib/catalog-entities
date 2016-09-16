package org.xbib.catalog.entities;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class StatusCodeMapper {

    private Map<String, Object> map = new HashMap<>();

    @SuppressWarnings("unchecked")
    public StatusCodeMapper load(String path) throws IOException {
        ValueMaps valueMaps = new ValueMaps();
        map.putAll(valueMaps.getMap(path, path));
        return this;
    }

    public StatusCodeMapper add(Map<String, Object> map) {
        this.map.putAll(map);
        return this;
    }

    public Map<String, Object> getMap() {
        return map;
    }

    @SuppressWarnings("unchecked")
    public List<String> lookup(String value) {
        return map.containsKey(value) ? (List<String>) map.get(value) : null;
    }

}
