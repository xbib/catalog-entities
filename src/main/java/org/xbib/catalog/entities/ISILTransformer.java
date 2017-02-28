package org.xbib.catalog.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.xbib.content.settings.Settings;
import org.xbib.marc.transformer.value.MarcValueTransformer;
import org.xbib.marc.transformer.value.MarcValueTransformers;

import java.io.IOException;
import java.net.URL;
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
        identifierMapper.load(new URL(settings.get("tab_sigel",
                "http://index.hbz-nrw.de/alephxml/tab_sigel")).openStream());
    }

    @Override
    public String transform(String value) {
        return identifierMapper.lookup(value);
    }

    @SuppressWarnings("unchecked")
    public void createTransformer(Settings settings, MarcValueTransformers marcValueTransformers) throws IOException {
        String resource = settings.get("transform2isil");
        List<String> transform2isil = resource.endsWith(".json") ?
                new ObjectMapper().readValue(getClass().getClassLoader().getResource(resource).openStream(), List.class) :
                Arrays.asList(settings.getAsArray("transform2isil"));
        logger.log(Level.INFO, "transform2isil: " + transform2isil.size());
        for (String field : transform2isil) {
            marcValueTransformers.setMarcValueTransformer(field, this);
        }
    }
}
