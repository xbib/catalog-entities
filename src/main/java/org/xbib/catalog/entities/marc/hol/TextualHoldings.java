package org.xbib.catalog.entities.marc.hol;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.catalog.entities.EnumerationAndChronologyHelper;
import org.xbib.marc.MarcField;
import org.xbib.rdf.Resource;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class TextualHoldings extends CatalogEntity {

    private static final Logger logger = Logger.getLogger(TextualHoldings.class.getName());

    private List<Pattern> movingwallPatterns;

    @SuppressWarnings("unchecked")
    public TextualHoldings(Map<String, Object> params) {
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
            String value = subfield.getValue();
            worker.getWorkerState().getResource().add("textualholdings", value);
            if (subfield.getId().equals("a")) {
                Resource r = worker.getWorkerState().getResource().newResource("holdings");
                Resource parsedHoldings = eac.parse(value, r, getMovingwallPatterns());
                if (!parsedHoldings.isEmpty()) {
                    Set<Integer> dates = eac.dates(worker.getWorkerState().getResource().id(), parsedHoldings);
                    for (Integer date : dates) {
                        worker.getWorkerState().getResource().add("dates", date);
                    }
                } else {
                    logger.log(Level.WARNING, MessageFormat.format("no dates found in field {0}", field));
                }
            }
        }
        return super.transform(worker, field);
    }

}
