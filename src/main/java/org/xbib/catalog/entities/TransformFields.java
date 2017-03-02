package org.xbib.catalog.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.xbib.content.settings.Settings;
import org.xbib.marc.transformer.field.MarcFieldTransformer;
import org.xbib.marc.transformer.field.MarcFieldTransformers;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 */
public class TransformFields {

    private static final Logger logger = Logger.getLogger(TransformFields.class.getName());

    private static final String JSON = ".json";

    private static final String TRANSFORM_FIELDS = "transform_fields";

    private static final String TRANSFORM_SUBFIELDS = "transform_subfields";

    private static final String TRANSFORM_SUBFIELDS_TAIL = "transform_subfields_tail";

    private final Map<String, Object> transformFields;

    private final Map<String, Object> transformSubfields;

    private final Map<String, Object> transformSubfieldsTail;

    @SuppressWarnings("unchecked")
    public TransformFields(Settings settings) throws IOException {
        String resource = settings.get(TRANSFORM_FIELDS);
        this.transformFields = resource != null && resource.endsWith(JSON) ?
                new ObjectMapper().readValue(getClass().getClassLoader().getResource(resource).openStream(), Map.class) :
                settings.getAsSettings(TRANSFORM_FIELDS).getAsStructuredMap();
        resource = settings.get(TRANSFORM_SUBFIELDS);
        this.transformSubfields = resource != null && resource.endsWith(JSON) ?
                new ObjectMapper().readValue(getClass().getClassLoader().getResource(resource).openStream(), Map.class) :
                settings.getAsSettings(TRANSFORM_SUBFIELDS).getAsStructuredMap();
        resource = settings.get(TRANSFORM_SUBFIELDS_TAIL);
        this.transformSubfieldsTail = resource != null && resource.endsWith(JSON) ?
                new ObjectMapper().readValue(getClass().getClassLoader().getResource(resource).openStream(), Map.class) :
                settings.getAsSettings(TRANSFORM_SUBFIELDS_TAIL).getAsStructuredMap();
        logger.log(Level.INFO, () -> MessageFormat.format("transform fields: {0} subfields: {1} subfields (tail): {2}",
                transformFields.size(), transformSubfields.size(), transformSubfieldsTail.size()));
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
