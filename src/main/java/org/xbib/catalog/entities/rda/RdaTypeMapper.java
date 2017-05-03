package org.xbib.catalog.entities.rda;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class RdaTypeMapper {

    private final Map<String, Object> english;

    private final Map<String, Object> german;

    private Map<String, Object> ger2eng;

    @SuppressWarnings("unchecked")
    public RdaTypeMapper() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResource("org/xbib/catalog/entities/mab/imd_en.json")
                .openStream();
        this.english = new ObjectMapper().readValue(inputStream, Map.class);
        inputStream.close();
        inputStream = getClass().getClassLoader().getResource("org/xbib/catalog/entities/mab/imd.json")
                .openStream();
        this.german = new ObjectMapper().readValue(inputStream, Map.class);
        inputStream.close();
        this.ger2eng = new HashMap<>();
        for (String key : new String[]{"rda.media", "rda.content", "rda.carrier"}) {
            Map<String, Object> eng = (Map<String, Object>) english.get(key);
            Map<String, Object> ger = (Map<String, Object>) german.get(key);
            for (String k : eng.keySet()) {
                String englishWord = eng.get(k).toString();
                String germanWord = ger.get(k).toString();
                ger2eng.put(germanWord, englishWord);
            }
        }
    }

    public Map<String, Object> getGer2Eng() {
        return ger2eng;
    }
}
