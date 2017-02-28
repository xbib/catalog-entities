package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.content.rdf.Resource;
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
public class TypeMedia extends CatalogEntity {

    private static final Logger logger = Logger.getLogger(TypeMedia.class.getName());
    private final Map<Pattern, String> patterns = new HashMap<>();
    private String facet = "dc.format";

    public TypeMedia(Map<String, Object> params) {
        super(params);
        if (params.containsKey("_facet")) {
            this.facet = params.get("_facet").toString();
        }
        Map<String, Object> regexes = getRegexes();
        if (regexes != null) {
            synchronized (patterns) {
                for (Map.Entry<String, Object> entry : regexes.entrySet()) {
                    String key = entry.getKey();
                    patterns.put(Pattern.compile(Pattern.quote(key), Pattern.CASE_INSENSITIVE), (String) regexes.get(key));
                }
            }
        }
    }

    @Override
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        String value = getValue(field);
        Resource resource = worker.getWorkerState().getResource().newResource("TypeMedia");
        for (String code : findCodes(value)) {
            resource.add("value", code);
            // facetize here, so we have to find codes only once
            facetize(worker, code);
        }
        return null; // done!
    }

    @SuppressWarnings("unchecked")
    private List<String> findCodes(String value) {
        boolean isRAK = false;
        List<String> list = new LinkedList<>();
        Map<String, Object> rak = (Map<String, Object>) getParams().get("rak");
        if (rak != null && rak.containsKey(value)) {
            list.add((String) rak.get(value));
            isRAK = true;
        }
        synchronized (patterns) {
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
        if (!isRAK && !list.isEmpty()) {
            logger.log(Level.WARNING,
                    () -> MessageFormat.format("additional media types {0} detected from value: \"{1}\"",
                    list, value));
        }
        if (list.isEmpty()) {
            logger.log(Level.WARNING,
                    () -> MessageFormat.format("no media type detected from value: \"{0}\"", value));
        }
        return list;
    }

    @Override
    protected String getFacetName() {
        return facet;
    }

}
