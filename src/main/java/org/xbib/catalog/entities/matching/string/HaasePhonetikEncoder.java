package org.xbib.catalog.entities.matching.string;

/**
 * Ge&auml;nderter Algorithmus aus der Matching Toolbox von Rainer Schnell
 * und J&ouml;rg Reiher
 * Die Kölner Phonetik wurde für den Einsatz in Namensdatenbanken wie
 * der Verwaltung eines Krankenhauses durch Martin Haase (Institut für
 * Sprachwissenschaft, Universität zu Köln) und Kai Heitmann (Insitut für
 * medizinische Statistik, Informatik und Epidemiologie, Köln)  überarbeitet.
 * M. Haase und K. Heitmann. Die Erweiterte Kölner Phonetik. 526, 2000.
 * nach: Martin Wilz, Aspekte der Kodierung phonetischer Ähnlichkeiten
 * in deutschen Eigennamen, Magisterarbeit.
 * http://www.uni-koeln.de/phil-fak/phonetik/Lehre/MA-Arbeiten/magister_wilz.pdf
 *
 */
public class HaasePhonetikEncoder extends KoelnerPhonetikEncoder {

    private final static String[] HAASE_VARIATIONS_PATTERNS = {"OWN", "RB", "WSK", "A$", "O$", "SCH",
            "GLI", "EAU$", "^CH", "AUX", "EUX", "ILLE"};
    private final static String[] HAASE_VARIATIONS_REPLACEMENTS = {"AUN", "RW", "RSK", "AR", "OW", "CH",
            "LI", "O", "SCH", "O", "O", "I"};

    /**
     * @return patterns
     */
    @Override
    protected String[] getPatterns() {
        return HAASE_VARIATIONS_PATTERNS;
    }

    /**
     * @return replacements
     */
    @Override
    protected String[] getReplacements() {
        return HAASE_VARIATIONS_REPLACEMENTS;
    }

    /**
     * @return code
     */
    @Override
    protected char getCode() {
        return '9';
    }
}
