package org.xbib.catalog.entities.marc.zdb.bib;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.catalog.entities.EnumerationAndChronologyHelper;
import org.xbib.marc.MarcField;
import org.xbib.rdf.Resource;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

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
        EnumerationAndChronologyHelper eac = new EnumerationAndChronologyHelper();
        for (MarcField.Subfield subfield : field.getSubfields()) {
            if ("a".equals(subfield.getId())) {
                worker.getWorkerState().getResource().add("TextualEnumerationAndChronology", subfield.getValue());
                Resource r = worker.getWorkerState().getResource().newResource("EnumerationAndChronology");
                Resource parsedHoldings = eac.parse(subfield.getValue(), r, getMovingwallPatterns());
                if (!parsedHoldings.isEmpty()) {
                    Set<Integer> dates = eac.dates(r.id(), parsedHoldings);
                    for (Integer date : dates) {
                        worker.getWorkerState().getResource().add("Dates", date);
                    }
                }
            }
        }
        return super.transform(worker, field);
    }
}
