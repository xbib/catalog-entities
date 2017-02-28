package org.xbib.catalog.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.xbib.content.settings.Settings;
import org.xbib.marc.transformer.field.MarcFieldTransformer;
import org.xbib.marc.transformer.field.MarcFieldTransformers;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class SkipFields {

    private final List<String> skipTags;

    private final List<String> skipSubfields;

    @SuppressWarnings("unchecked")
    public SkipFields(Settings settings) throws IOException {
        String resource = settings.get("skiptags");
        this.skipTags = resource.endsWith(".json") ?
                new ObjectMapper().readValue(getClass().getClassLoader().getResource(resource).openStream(), List.class) :
                Arrays.asList(settings.getAsArray("skiptags"));
        resource = settings.get("skipsubfields");
        this.skipSubfields = resource.endsWith(".json") ?
                new ObjectMapper().readValue(getClass().getClassLoader().getResource(resource).openStream(), List.class) :
                Arrays.asList(settings.getAsArray("skipsubfields"));
    }

    public void createFieldTransformers(MarcFieldTransformers marcFieldTransformers) {
        // remove superfluous tags
        MarcFieldTransformer t0 = MarcFieldTransformer.builder()
                .operator(MarcFieldTransformer.Operator.HEAD)
                .ignoreIndicator()
                .drop(skipTags)
                .build();
        marcFieldTransformers.add(t0);
        // remove superfluous subfields
        MarcFieldTransformer t1 = MarcFieldTransformer.builder()
                .operator(MarcFieldTransformer.Operator.HEAD)
                .ignoreSubfieldIds()
                .drop(skipSubfields)
                .build();
        marcFieldTransformers.add(t1);
    }
}
