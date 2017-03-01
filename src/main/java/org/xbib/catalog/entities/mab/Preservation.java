package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.content.rdf.Resource;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class Preservation extends CatalogEntity {

    private Map<Pattern, String> patterns;

    public Preservation(Map<String, Object> params) {
        super(params);
        Map<String, Object> regexes = getRegexes();
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
        String value = getValue(field);
        Resource resource = worker.getWorkerState().getResource().newResource("Preservation");
        for (String code : findCodes(value)) {
            resource.add("value", code);
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
        } else if (list.isEmpty()) {
            list.add(value);
        }
        return list;
    }
}
