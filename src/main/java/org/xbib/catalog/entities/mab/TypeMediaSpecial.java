package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class TypeMediaSpecial extends CatalogEntity {

    private static final Logger logger = Logger.getLogger(TypeMediaSpecial.class.getName());

    private String facet = "dc.format";

    private String predicate;

    private Map<Pattern, String> patterns;

    @SuppressWarnings("unchecked")
    public TypeMediaSpecial(Map<String, Object> params) {
        super(params);
        if (params.containsKey("_facet")) {
            this.facet = params.get("_facet").toString();
        }
        this.predicate = this.getClass().getSimpleName();
        if (params.containsKey("_predicate")) {
            this.predicate = params.get("_predicate").toString();
        }
        Map<String, Object> regexes = (Map<String, Object>) getParams().get("regexes");
        if (regexes != null) {
            patterns = new HashMap<>();
            for (Map.Entry<String, Object> entry : regexes.entrySet()) {
                String key = entry.getKey();
                patterns.put(Pattern.compile(Pattern.quote(key), Pattern.CASE_INSENSITIVE), (String) regexes.get(key));
            }
        }
    }

    @Override
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        String value = field.getSubfields().getFirst().getValue();
        if (!value.isEmpty()) {
            List<String> list = findCodes(value);
            if (list.isEmpty()) {
                logger.log(Level.WARNING, MessageFormat.format("no media type detected from value: \"{0}\" in field {1}",
                        value, field));
            }
            for (String code : list) {
                worker.getWorkerState().getResource().add(predicate, code);
                // facetize here, so we have to find codes only once
                facetize(worker, code);
            }
        }
        return null; // done!
    }

    @SuppressWarnings("unchecked")
    private List<String> findCodes(String value) {
        List<String> list = new LinkedList<>();
        Map<String, Object> rak = (Map<String, Object>) getParams().get("rak");
        if (rak != null && rak.containsKey(value)) {
            list.add((String) rak.get(value));
        }
        if (patterns != null) {
            // pattern matching
            for (Map.Entry<Pattern, String> entry : patterns.entrySet()) {
                Pattern p = entry.getKey();
                Matcher m = p.matcher(value);
                if (m.find()) {
                    String v = patterns.get(p);
                    if (v != null) {
                        list.add(v);
                    }
                }
            }
        }
        return list;
    }

    @Override
    protected String getFacetName() {
        return facet;
    }

}
