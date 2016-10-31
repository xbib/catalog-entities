package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.catalog.entities.TermFacet;
import org.xbib.content.rdf.Literal;
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
public class Extent extends CatalogEntity {

    private static final Logger logger = Logger.getLogger(Extent.class.getName());

    private String facet = "dc.format";

    private Map<Pattern, String> patterns;

    @SuppressWarnings("unchecked")
    public Extent(Map<String, Object> params) {
        super(params);
        if (params.containsKey("_facet")) {
            this.facet = params.get("_facet").toString();
        }
        Map<String, Object> regexes = (Map<String, Object>) getParams().get("regexes");
        if (regexes != null) {
            patterns = new HashMap<>();
            for (Map.Entry<String, Object> entry : regexes.entrySet()) {
                String key = entry.getKey();
                patterns.put(Pattern.compile(Pattern.quote(key), Pattern.CASE_INSENSITIVE), (String) regexes.get(key));
            }
        }
        logger.log(Level.FINE, MessageFormat.format("Pattern for extent format detection: {0}", patterns));
    }

    @Override
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        String value = getValue(field);
        for (String code : findCodes(value)) {
            worker.getWorkerState().getFacets().putIfAbsent(facet, new TermFacet().setName(facet).setType(Literal.STRING));
            worker.getWorkerState().getFacets().get(facet).addValue(code);
        }
        return this; // not done
    }

    private List<String> findCodes(String value) {
        List<String> list = new LinkedList<>();
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
}
