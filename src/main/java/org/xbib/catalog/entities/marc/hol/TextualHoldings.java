package org.xbib.catalog.entities.marc.hol;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.catalog.entities.EnumerationAndChronologyHelper;
import org.xbib.content.rdf.Resource;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 *
 */
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
        String id = worker.getWorkerState().getIdentifier();
        EnumerationAndChronologyHelper eac = new EnumerationAndChronologyHelper(id, field, getMovingwallPatterns());
        for (MarcField.Subfield subfield : field.getSubfields()) {
            String value = subfield.getValue();
            worker.getWorkerState().getResource().add("textualholdings", value);
            if ("a".equals(subfield.getId())) {
                Resource r = worker.getWorkerState().getResource().newResource("holdings");
                Resource parsedHoldings = eac.parseToResource(value, r);
                if (!parsedHoldings.isEmpty()) {
                    Set<Integer> dates = eac.dates(parsedHoldings);
                    for (Integer date : dates) {
                        worker.getWorkerState().getResource().add("dates", date);
                    }
                } else {
                    logger.log(Level.WARNING, () -> MessageFormat.format("no dates found in field {0}", field));
                }
            }
        }
        return super.transform(worker, field);
    }

}
