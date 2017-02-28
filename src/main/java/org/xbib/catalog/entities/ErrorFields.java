package org.xbib.catalog.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.xbib.content.settings.Settings;
import org.xbib.marc.transformer.field.MarcFieldTransformer;
import org.xbib.marc.transformer.field.MarcFieldTransformers;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class ErrorFields {

    private static final Logger logger = Logger.getLogger(ErrorFields.class.getName());

    private final List<String> errorFields;

    @SuppressWarnings("unchecked")
    public ErrorFields(Settings settings) throws IOException {
        String resource = settings.get("errorfields");
        this.errorFields = resource.endsWith(".json") ?
             new ObjectMapper().readValue(getClass().getClassLoader().getResource(resource).openStream(), List.class) :
             Arrays.asList(settings.getAsArray("errorfields"));
        logger.log(Level.INFO, "errorfields: " + errorFields.size());
    }

    public void createFieldTransformers(MarcFieldTransformers marcFieldTransformers) {
        // remove erraneous tags
        MarcFieldTransformer t0 = MarcFieldTransformer.builder()
                .operator(MarcFieldTransformer.Operator.HEAD)
                .ignoreIndicator()
                .drop(errorFields)
                .build();
        marcFieldTransformers.add(t0);
    }
}
