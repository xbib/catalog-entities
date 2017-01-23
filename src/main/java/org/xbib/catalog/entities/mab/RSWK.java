package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.catalog.entities.Sequence;
import org.xbib.catalog.entities.matching.title.RAK;
import org.xbib.content.rdf.Resource;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class RSWK extends CatalogEntity {

    public RSWK(Map<String, Object> params) {
        super(params);
    }

    @Override
    public List<String> transform(CatalogEntityWorker worker,
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
                return Collections.singletonList(value.replaceAll("\\-", "").toLowerCase());
            }
            return null;
        }
        return Collections.singletonList(RAK.clean(value));
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
