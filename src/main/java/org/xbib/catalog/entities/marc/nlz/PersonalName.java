package org.xbib.catalog.entities.marc.nlz;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.content.rdf.Resource;
import org.xbib.content.resource.IRI;

import java.io.IOException;
import java.util.Map;

/**
 *
 */
public class PersonalName extends CatalogEntity {

    private static final IRI FOAF_AGENT = IRI.create("foaf:agent");
    private static final IRI FOAF_NAME = IRI.create("foaf:name");
    private static final IRI DC_CREATOR = IRI.create("dc:creator");

    public PersonalName(Map<String, Object> params) {
        super(params);
    }

    @Override
    public String transform(CatalogEntityWorker worker,
                            String resourcePredicate, Resource resource, String property, String value) throws IOException {
        Resource r = worker.getWorkerState().getResource();
        if ("personalName".equals(property)) {
            String name = capitalize(value.toLowerCase().substring(0, value.length() - 1), " -");
            r.newResource(DC_CREATOR)
                    .a(FOAF_AGENT)
                    .add(FOAF_NAME, name);
            worker.getWorkerState().getAuthoredWorkKey().authorName(name);
        }
        return value;
    }

    private String capitalize(final String str, String delimiters) {
        final int delimLen = delimiters == null ? -1 : delimiters.length();
        if (str == null || str.isEmpty() || delimLen == 0) {
            return str;
        }
        final char[] buffer = str.toCharArray();
        boolean b = true;
        for (int i = 0; i < buffer.length; i++) {
            final char ch = buffer[i];
            if (delimiters != null && delimiters.indexOf(ch) >= 0) {
                b = true;
            } else if (b) {
                buffer[i] = Character.toTitleCase(ch);
                b = false;
            }
        }
        return new String(buffer);
    }

}
