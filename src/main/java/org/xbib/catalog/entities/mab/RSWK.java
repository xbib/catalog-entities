package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.catalog.entities.Sequence;
import org.xbib.marc.MarcField;
import org.xbib.rdf.Resource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

/**
 *
 */
public class RSWK extends CatalogEntity {

    public RSWK(Map<String, Object> params) {
        super(params);
    }

    @Override
    public String transform(CatalogEntityWorker worker,
                            String predicate, Resource resource, String property, String value) {
        if ("subjectIdentifier".equals(property)) {
            resource.add("subjectIdentifier", value);
            if (value.startsWith("(DE-588)")) {
                // GND-ID: upper case, with hyphen
                resource.add("identifierGND", value.substring(8));
            } else if (value.startsWith("(DE-101)")) {
                // DNB-ID: upper case, with hyphen
                resource.add("identifierDNB", value.substring(8));
            } else if (value.startsWith("(DE-600)")) {
                // ZDB-ID does not matter at all
                resource.add("identifierZDB", value.substring(8).replaceAll("\\-", "").toLowerCase());
                return value.replaceAll("\\-", "").toLowerCase();
            }
            return null;
        }
        return value
                .replaceAll("<<(.*?)>>", "\u0098$1\u009C")
                .replaceAll("<(.*?)>", "[$1]")
                .replaceAll("¬(.*?)¬", "\u0098$1\u009C");
    }

    @Override
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) {
        try {
            Resource res = worker.append(worker.getWorkerState().getResource(), field, this);
            // sequence name is first indicator
            String sequence = "SubjectHeadingSequence.number" + field.getIndicator().charAt(0);
            worker.getWorkerState().getSequences().putIfAbsent(sequence, new Sequence<Resource>().setName(sequence));
            worker.getWorkerState().getSequences().get(sequence).add(res);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return null; // done!
    }
}
