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

    private final Map<String, Object> transformFields;

    private final Map<String, Object> transformSubfields;

    private final Map<String, Object> transformSubfieldsTail;

    @SuppressWarnings("unchecked")
    public TransformFields(Settings settings) throws IOException {
        String resource = settings.get("transform_fields");
        this.transformFields = resource != null && resource.endsWith(".json") ?
                new ObjectMapper().readValue(getClass().getClassLoader().getResource(resource).openStream(), Map.class) :
                settings.getAsSettings("transform").getAsStructuredMap();
        resource = settings.get("transform_subfields");
        this.transformSubfields = resource != null && resource.endsWith(".json") ?
                new ObjectMapper().readValue(getClass().getClassLoader().getResource(resource).openStream(), Map.class) :
                settings.getAsSettings("transform_subfields").getAsStructuredMap();
        resource = settings.get("transform_subfields_tail");
        this.transformSubfieldsTail = resource != null && resource.endsWith(".json") ?
                new ObjectMapper().readValue(getClass().getClassLoader().getResource(resource).openStream(), Map.class) :
                settings.getAsSettings("transform_subfields_tail").getAsStructuredMap();
        logger.log(Level.INFO, "transform fields: " + transformFields.size() +
                " transform subfields: " + transformSubfields.size() +
                " transform subfields (tail): " + transformSubfieldsTail.size());
    }

    public void createTransformerFields(MarcFieldTransformers marcFieldTransformers) {
        MarcFieldTransformer.Builder builder = MarcFieldTransformer.builder()
                .operator(MarcFieldTransformer.Operator.HEAD)
                .ignoreSubfieldIds();
        for (Map.Entry<String, Object> entry : transformFields.entrySet()) {
            builder.fromTo(entry.getKey(), (String) entry.getValue());
        }
        marcFieldTransformers.add(builder.build());
    }

    public void createTransformerSubfields(MarcFieldTransformers marcFieldTransformers) {
        MarcFieldTransformer.Builder builder = MarcFieldTransformer.builder()
                .operator(MarcFieldTransformer.Operator.HEAD);
        for (Map.Entry<String, Object> entry : transformSubfields.entrySet()) {
            builder.fromTo(entry.getKey(), (String) entry.getValue());
        }
        marcFieldTransformers.add(builder.build());
    }

    public void createTransformerSubfieldsTail(MarcFieldTransformers marcFieldTransformers) {
        MarcFieldTransformer.Builder builder = MarcFieldTransformer.builder()
                .operator(MarcFieldTransformer.Operator.TAIL);
        for (Map.Entry<String, Object> entry : transformSubfieldsTail.entrySet()) {
            builder.fromTo(entry.getKey(), (String) entry.getValue());
        }
        marcFieldTransformers.add(builder.build());
    }
}
