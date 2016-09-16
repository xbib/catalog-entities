package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.util.Map;

/**
 * Physical description.
 * http://www.dnb.de/SharedDocs/Downloads/DE/DNB/standardisierung/protokolle/pEgDf20050411v.pdf?__blob=publicationFile
 *
 * http://www.bsb-muenchen.de/fileadmin/imageswww/pdf-dateien/abteilungen/Schule/RAK-NBM-BVB-ALEPH-2014-03_Handout.pdf
 */
public class PhysicalDescription extends CatalogEntity {

    private static final String FACET_NAME = "dc.format";

    private String predicate;

    private Map<String, Object> codes;

    private Map<String, Object> facetcodes;

    public PhysicalDescription(Map<String, Object> params) {
        super(params);
        this.predicate = this.getClass().getSimpleName();
        if (params.containsKey("_predicate")) {
            this.predicate = params.get("_predicate").toString();
        }
        this.codes = getCodes();
        this.facetcodes = getFacetCodes();
    }

    @SuppressWarnings("unchecked")
    @Override
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        String value = getValue(field);
        if (codes != null) {
            for (int i = 0; i < value.length(); i++) {
                Map<String, Object> q = (Map<String, Object>) codes.get(Integer.toString(i));
                if (q != null) {
                    String code = (String) q.get(value.substring(i, i + 1));
                    if (code == null && i + 1 < value.length()) {
                        // two letters?
                        code = (String) q.get(value.substring(i, i + 2));
                    }
                    worker.getWorkerState().getResource().add(predicate, code);
                }
            }
        }
        if (facetcodes != null) {
            for (int i = 0; i < value.length(); i++) {
                Map<String, Object> q = (Map<String, Object>) facetcodes.get(Integer.toString(i));
                if (q != null) {
                    String code = (String) q.get(value.substring(i, i + 1));
                    if (code == null && i + 1 < value.length()) {
                        // two letters?
                        code = (String) q.get(value.substring(i, i + 2));
                    }
                    facetize(worker, code);
                }
            }
        }
        return super.transform(worker, field); // done
    }

    @Override
    protected String getFacetName() {
        return FACET_NAME;
    }
}
