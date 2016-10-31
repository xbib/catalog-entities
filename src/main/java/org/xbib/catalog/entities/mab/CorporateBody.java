package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.content.rdf.Resource;

import java.util.Map;

/**
 *
 */
public class CorporateBody extends CatalogEntity {

    public CorporateBody(Map<String, Object> params) {
        super(params);
    }

    /**
     * Documentation.
     * <code>
     * e = Name Konferenz
     * 9 = GND-ID (neu) / Norm-ID (alt)
     * b = untergeordnete Einheit
     * c = Ort
     * d = Datum
     * h = Zusatz
     * n = Zählung
     * </code>
     *
     * <code>
     * g = Geografikum (Gebietskörperschaft) (NW)
     * 9 = GND-Identifikationsnummer
     * h = Zusatz (W)
     * x = nachgeordneter Teil (W)
     * z = geografische Unterteilung (W)
     * </code>
     *
     * <code>
     * a = Name (alt)
     * k = Name Körperschaft (neu)
     * 9 = GND-ID (neu) / Norm-ID (alt)
     * b = untergeordnete Körperschaft
     * h = Zusatz
     * n = Zählung
     * </code>
     */

    @Override
    public String transform(CatalogEntityWorker worker,
                            String predicate, Resource resource, String property, String value) {
        if ("identifier".equals(property)) {
            resource.add("identifier", value);
            if (value.startsWith("(DE-588)")) {
                // GND-ID: upper case, with hyphen
                resource.add("identifierGND", value.substring(8));
            } else if (value.startsWith("(DE-101)")) {
                // DNB-ID: upper case, with hyphen
                resource.add("identifierDNB", value.substring(8));
            } else if (value.startsWith("(DE-600)")) {
                // ZDB-ID does not matter at all, we use lower case without hyphen
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
}
