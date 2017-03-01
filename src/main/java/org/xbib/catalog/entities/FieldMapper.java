package org.xbib.catalog.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.xbib.content.settings.Settings;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 */
public class FieldMapper {

    private static final Logger logger = Logger.getLogger(FieldMapper.class.getName());

    private final Map<String, Object> source;

    private final Map<String, Object> target;

    public FieldMapper(Settings settings) throws IOException {
        String resource = settings.get("fieldMapperSource");
        this.source = resource.endsWith(".json") ?
                new ObjectMapper().readValue(getClass().getClassLoader().getResource(resource).openStream(), Map.class) :
                settings.getAsSettings("fieldMapperSource").getAsStructuredMap();
        logger.log(Level.INFO, "source: " + source.size());
        resource = settings.get("fieldMapperTarget");
        this.target = resource.endsWith(".json") ?
                new ObjectMapper().readValue(getClass().getClassLoader().getResource(resource).openStream(), Map.class) :
                settings.getAsSettings("fieldMapperTarget").getAsStructuredMap();
        logger.log(Level.INFO, "target: " + target.size());
    }

    public void map() {
        // TODO

    }
}
