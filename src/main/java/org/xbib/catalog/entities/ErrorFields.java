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

    private final List<String> errorSubfields;

    @SuppressWarnings("unchecked")
    public ErrorFields(Settings settings) throws IOException {
        String resource = settings.get("error_fields");
        this.errorFields = resource != null && resource.endsWith(".json") ?
             new ObjectMapper().readValue(getClass().getClassLoader().getResource(resource).openStream(), List.class) :
             Arrays.asList(settings.getAsArray("error_fields"));
        resource = settings.get("error_subfields");
        this.errorSubfields = resource != null && resource.endsWith(".json") ?
                new ObjectMapper().readValue(getClass().getClassLoader().getResource(resource).openStream(), List.class) :
                Arrays.asList(settings.getAsArray("error_subfields"));
        logger.log(Level.INFO, "error fields: " + errorFields.size() + " error subfields: " + errorSubfields.size());
    }

    public void createFieldTransformers(MarcFieldTransformers marcFieldTransformers) {
        // remove erraneous fields
        MarcFieldTransformer t0 = MarcFieldTransformer.builder()
                .operator(MarcFieldTransformer.Operator.HEAD)
                .ignoreIndicator()
                .drop(errorFields)
                .build();
        marcFieldTransformers.add(t0);
        // remove erraneous subfields
        MarcFieldTransformer t1 = MarcFieldTransformer.builder()
                .operator(MarcFieldTransformer.Operator.HEAD)
                .ignoreSubfieldIds()
                .drop(errorSubfields)
                .build();
        marcFieldTransformers.add(t1);
    }
}
