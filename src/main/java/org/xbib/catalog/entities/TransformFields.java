package org.xbib.catalog.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.xbib.content.settings.Settings;
import org.xbib.marc.transformer.field.MarcFieldTransformer;
import org.xbib.marc.transformer.field.MarcFieldTransformers;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 */
public class TransformFields {

    private static final Logger logger = Logger.getLogger(TransformFields.class.getName());

    private final Map<String, Object> transformTags;

    private final Map<String, Object> transformTagSubfields;

    private final Map<String, Object> transformTagSubfieldsTail;

    @SuppressWarnings("unchecked")
    public TransformFields(Settings settings) throws IOException {
        String resource = settings.get("transform");
        this.transformTags = resource.endsWith(".json") ?
                new ObjectMapper().readValue(getClass().getClassLoader().getResource(resource).openStream(), Map.class) :
                settings.getAsSettings("transform").getAsStructuredMap();
        logger.log(Level.INFO, "transformTags: " + transformTags.size());
        resource = settings.get("transform_with_subfields");
        this.transformTagSubfields = resource.endsWith(".json") ?
                new ObjectMapper().readValue(getClass().getClassLoader().getResource(resource).openStream(), Map.class) :
                settings.getAsSettings("transform_with_subfields").getAsStructuredMap();
        logger.log(Level.INFO, "transformTagSubfields: " + transformTagSubfields.size());
        resource = settings.get("transform_with_subfields_tail");
        this.transformTagSubfieldsTail = resource.endsWith(".json") ?
                new ObjectMapper().readValue(getClass().getClassLoader().getResource(resource).openStream(), Map.class) :
                settings.getAsSettings("transform_with_subfields_tail").getAsStructuredMap();
        logger.log(Level.INFO, "transformTagSubfieldsTail: " + transformTagSubfieldsTail.size());
    }

    public void createTagTransformers(MarcFieldTransformers marcFieldTransformers) {
        MarcFieldTransformer.Builder builder = MarcFieldTransformer.builder()
                .operator(MarcFieldTransformer.Operator.HEAD)
                .ignoreSubfieldIds();
        for (Map.Entry<String, Object> entry : transformTags.entrySet()) {
            builder.fromTo(entry.getKey(), (String) entry.getValue());
        }
        marcFieldTransformers.add(builder.build());
    }

    public void createTagSubfieldTransformers(MarcFieldTransformers marcFieldTransformers) {
        MarcFieldTransformer.Builder builder = MarcFieldTransformer.builder()
                .operator(MarcFieldTransformer.Operator.HEAD);
        for (Map.Entry<String, Object> entry : transformTagSubfields.entrySet()) {
            builder.fromTo(entry.getKey(), (String) entry.getValue());
        }
        marcFieldTransformers.add(builder.build());
    }

    public void createTagSubfieldTailTransformers(MarcFieldTransformers marcFieldTransformers) {
        MarcFieldTransformer.Builder builder = MarcFieldTransformer.builder()
                .operator(MarcFieldTransformer.Operator.TAIL);
        for (Map.Entry<String, Object> entry : transformTagSubfieldsTail.entrySet()) {
            builder.fromTo(entry.getKey(), (String) entry.getValue());
        }
        marcFieldTransformers.add(builder.build());
    }
}
