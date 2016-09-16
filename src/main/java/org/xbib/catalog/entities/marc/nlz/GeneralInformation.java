package org.xbib.catalog.entities.marc.nlz;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.iri.IRI;
import org.xbib.marc.MarcField;
import org.xbib.rdf.Literal;
import org.xbib.rdf.memory.MemoryLiteral;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class GeneralInformation extends CatalogEntity {

    private static final Logger logger = Logger.getLogger(GeneralInformation.class.getName());

    private static final IRI DC_DATE = IRI.create("dc:date");

    public GeneralInformation(Map<String, Object> params) {
        super(params);
    }

    /**
     * Example "991118d19612006xx z||p|r ||| 0||||0ger c"
     */
    @Override
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        String value = getValue(field);
        if (value.length() != 40) {
            logger.log(Level.WARNING, "broken GeneralInformation field, length is not 40");
        }
        String date1 = value.length() > 11 ? value.substring(7, 11) : "0000";
        Integer date = check(date1);
        worker.getWorkerState().getResource().add(DC_DATE, new MemoryLiteral(date).type(Literal.GYEAR));
        return super.transform(worker, field);
    }

    // check for valid date, else return null
    private Integer check(String date) {
        try {
            int d = Integer.parseInt(date);
            if (d == 9999) {
                return null;
            }
            return d;
        } catch (Exception e) {
            return null;
        }
    }

}
