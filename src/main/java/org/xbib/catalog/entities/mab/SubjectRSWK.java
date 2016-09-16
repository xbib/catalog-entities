package org.xbib.catalog.entities.mab;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.rdf.Resource;

import java.util.Map;

/**
 *
 */
public class SubjectRSWK extends CatalogEntity {

    public SubjectRSWK(Map<String, Object> params) {
        super(params);
    }

    /*
         * alt: 9 ID p Personenschlagwort g geographisch-ethnographisches
         * Schlagwort s Sachschlagwort k Koerperschaftsschlagwort: Ansetzung
         * unter dem Individualnamen c Koerperschaftsschlagwort: Ansetzung unter
         * dem Ortssitz z Zeitschlagwort f Formschlagwort t Werktitel als
         * Schlagwort blank Unterschlagwort einer Ansetzungskette
         *
         *
         * neu: 902: Unterfelder: p = Personenschlagwort (NW) g = Geografikum
         * (Gebietskörperschaft) (NW) e = Kongressname (NW) k = Körperschaft s =
         * Sachschlagwort (NW), Version (NW) b = Untergeordnete Körperschaft,
         * untergeordnete Einheit (W) c = Beiname (NW), Ort (NW) d = Datum (NW)
         * h = Zusatz (W) z = Zeitschlagwort = geographische Unterteilung (W) f
         * = Formschlagwort (NW), Erscheinungsjahr eines Werkes (NW) t =
         * Werktitel als Schlagwort (NW) m = Besetzung im Musikbereich (W) n =
         * Zählung (NW) o = Angabe des Musikarrangements (NW) u = Titel eines
         * Teils/einer Abteilung eines Werkes (W) r = Tonart (NW) x =
         * nachgeordneter Teil (W) 9 = GND-Identifikationsnummer a =
         * (Alt-)Schlagwort ohne IDN-Verknüpfung (NW)
         *
     */

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

}
