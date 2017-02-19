package org.xbib.catalog.entities.marc.bib;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.catalog.entities.EnumerationAndChronologyHelper;
import org.xbib.content.rdf.Resource;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 *
 */
public class EnumerationAndChronology extends CatalogEntity {

    private List<Pattern> movingwallPatterns;

    @SuppressWarnings("unchecked")
    public EnumerationAndChronology(Map<String, Object> params) {
        super(params);
        List<String> movingwalls = (List<String>) params.get("movingwall");
        if (movingwalls != null) {
            List<Pattern> p = new LinkedList<>();
            for (String movingwall : movingwalls) {
                p.add(Pattern.compile(movingwall));
            }
            setMovingwallPatterns(p);
        }
    }

    public List<Pattern> getMovingwallPatterns() {
        return this.movingwallPatterns;
    }

    public void setMovingwallPatterns(List<Pattern> p) {
        this.movingwallPatterns = p;
    }

    @Override
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        String id = worker.getWorkerState().getIdentifier();
        EnumerationAndChronologyHelper eac = new EnumerationAndChronologyHelper(id, field, getMovingwallPatterns());
        for (MarcField.Subfield subfield : field.getSubfields()) {
            if ("a".equals(subfield.getId())) {
                worker.getWorkerState().getResource().add("TextualEnumerationAndChronology", subfield.getValue());
                Resource r = worker.getWorkerState().getResource().newResource("EnumerationAndChronology");
                Resource parsedHoldings = eac.parseToResource(subfield.getValue(), r);
                if (!parsedHoldings.isEmpty()) {
                    Set<Integer> dates = eac.dates(parsedHoldings);
                    for (Integer date : dates) {
                        worker.getWorkerState().getResource().add("Dates", date);
                    }
                }
            }
        }
        return super.transform(worker, field);
    }
}
