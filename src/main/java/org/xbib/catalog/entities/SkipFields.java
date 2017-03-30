package org.xbib.catalog.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.xbib.content.settings.Settings;
import org.xbib.marc.transformer.field.MarcFieldTransformer;
import org.xbib.marc.transformer.field.MarcFieldTransformers;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class SkipFields {

    private static final Logger logger = Logger.getLogger(SkipFields.class.getName());

    private static final String JSON = ".json";

    private static final String SKIP_FIELDS = "skip_fields";

    private static final String SKIP_INDICATORS = "skip_indicators";

    private static final String SKIP_SUBFIELDS = "skip_subfields";

    private final List<String> skipFields;

    private final List<String> skipIndicators;

    private final List<String> skipSubfields;

    @SuppressWarnings("unchecked")
    public SkipFields(Settings settings) throws IOException {
        String resource = settings.get(SKIP_FIELDS);
        this.skipFields = resource != null && resource.endsWith(JSON) ?
                new ObjectMapper().readValue(getClass().getClassLoader().getResource(resource).openStream(), List.class) :
                Arrays.asList(settings.getAsArray(SKIP_FIELDS));

        resource = settings.get(SKIP_INDICATORS);
        this.skipIndicators = resource != null && resource.endsWith(JSON) ?
                new ObjectMapper().readValue(getClass().getClassLoader().getResource(resource).openStream(), List.class) :
                Arrays.asList(settings.getAsArray(SKIP_INDICATORS));

        resource = settings.get(SKIP_SUBFIELDS);
        this.skipSubfields = resource != null && resource.endsWith(JSON) ?
                new ObjectMapper().readValue(getClass().getClassLoader().getResource(resource).openStream(), List.class) :
                Arrays.asList(settings.getAsArray(SKIP_SUBFIELDS));

        logger.log(Level.INFO, () -> MessageFormat.format("skip: fields: {0} indicators: {1} subfields: {2}",
                skipFields.size(), skipIndicators.size(), skipSubfields.size()));
    }

    public void createFieldTransformers(MarcFieldTransformers marcFieldTransformers) {
        // remove superfluous tags
        MarcFieldTransformer t0 = MarcFieldTransformer.builder()
                .operator(MarcFieldTransformer.Operator.HEAD)
                .ignoreIndicator()
                .drop(skipFields)
                .build();
        marcFieldTransformers.add(t0);
        // remove superfluous tags with indicators
        MarcFieldTransformer t1 = MarcFieldTransformer.builder()
                .operator(MarcFieldTransformer.Operator.HEAD)
                .ignoreSubfieldIds()
                .drop(skipIndicators)
                .build();
        marcFieldTransformers.add(t1);
        // remove superfluous tags with subfields
        MarcFieldTransformer t2 = MarcFieldTransformer.builder()
                .operator(MarcFieldTransformer.Operator.HEAD)
                .drop(skipSubfields)
                .build();
        marcFieldTransformers.add(t2);
    }
}
