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
public class SkipFields {

    private static final Logger logger = Logger.getLogger(SkipFields.class.getName());

    private final List<String> skipFields;

    private final List<String> skipSubfields;

    @SuppressWarnings("unchecked")
    public SkipFields(Settings settings) throws IOException {
        String resource = settings.get("skip_fields");
        this.skipFields = resource != null && resource.endsWith(".json") ?
                new ObjectMapper().readValue(getClass().getClassLoader().getResource(resource).openStream(), List.class) :
                Arrays.asList(settings.getAsArray("skip_fields"));
        resource = settings.get("skip_subfields");
        this.skipSubfields = resource != null && resource.endsWith(".json") ?
                new ObjectMapper().readValue(getClass().getClassLoader().getResource(resource).openStream(), List.class) :
                Arrays.asList(settings.getAsArray("skip_fields"));
        logger.log(Level.INFO, "skip fields: " + skipFields.size() + " skip subfields: " + skipSubfields.size());
    }

    public void createFieldTransformers(MarcFieldTransformers marcFieldTransformers) {
        // remove superfluous tags
        MarcFieldTransformer t0 = MarcFieldTransformer.builder()
                .operator(MarcFieldTransformer.Operator.HEAD)
                .ignoreIndicator()
                .drop(skipFields)
                .build();
        marcFieldTransformers.add(t0);
        // remove superfluous tags with subfields
        MarcFieldTransformer t1 = MarcFieldTransformer.builder()
                .operator(MarcFieldTransformer.Operator.HEAD)
                .ignoreSubfieldIds()
                .drop(skipSubfields)
                .build();
        marcFieldTransformers.add(t1);
    }
}
