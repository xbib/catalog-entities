package org.xbib.catalog.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.xbib.catalog.entities.mab.Identifier;
import org.xbib.content.settings.Settings;
import org.xbib.marc.transformer.value.MarcValueTransformer;
import org.xbib.marc.transformer.value.MarcValueTransformers;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class ISILTransformer implements MarcValueTransformer {

    private static final Logger logger = Logger.getLogger(ISILTransformer.class.getName());

    private final IdentifierMapper identifierMapper;

    public ISILTransformer(Settings settings) throws IOException {
        this.identifierMapper = new IdentifierMapper();
        ValueMapper valueMapper = new ValueMapper();
        Map<String, Object> sigel2isil = valueMapper.getMap(settings.get("sigel2isil",
                "org/xbib/catalog/entities/mab/sigel2isil.json"), "sigel2isil");
        identifierMapper.add(sigel2isil);
        URL url = getClass().getClassLoader().getResource(settings.get("tab_sigel",
                "org/xbib/catalog/entities/mab/hbz/tab_sigel"));
        try {
            if (url != null) {
                identifierMapper.load(url.openStream(), StandardCharsets.ISO_8859_1);
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "unable to load tab_sigel from classpath", e);
        }
    }

    @Override
    public String transform(String value) {
        return identifierMapper.lookup(value);
    }

    @SuppressWarnings("unchecked")
    public void createTransformer(Settings settings, MarcValueTransformers marcValueTransformers) throws IOException {
        String resource = settings.get("transform2isil");
        List<String> transform2isil = resource != null && resource.endsWith(".json") ?
                new ObjectMapper().readValue(getClass().getClassLoader().getResource(resource).openStream(), List.class) :
                Arrays.asList(settings.getAsArray("transform2isil"));
        logger.log(Level.INFO, () -> MessageFormat.format("transform2isil: {0}", transform2isil.size()));
        for (String field : transform2isil) {
            marcValueTransformers.setMarcValueTransformer(field, this);
        }
    }

    public IdentifierMapper getIdentifierMapper() {
        return identifierMapper;
    }
}
