package org.xbib.catalog.entities.marc.nlz;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.content.rdf.Resource;
import org.xbib.content.resource.IRI;
import org.xbib.content.resource.Node;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class HostItemEntry extends CatalogEntity {

    // 84:3 (1987:Sept.) 39
    // 2:1 ([1979]:Fall) 13
    private static final Pattern partPattern1 = Pattern.compile("^(.*?)\\:(.*?)\\s*\\(\\[*(\\d{4,})\\]*.*?\\)(.*?)$");

    // 22 (1924/1925) 115
    // 3 ([1962]) 33
    private static final Pattern partPattern2 = Pattern.compile("^(.*?)\\s*\\(\\[*(\\d{4,})\\]*.*?\\)(.*?)$");

    private static final Pattern titlePattern = Pattern.compile("^(.*?)\\.\\.\\s\\-\\s(.*?)\\s\\:\\s(.*?)$");
    private static final Map<String, String> brokenJournalTitles = new HashMap<String, String>() {
        private static final long serialVersionUID = -2061323683924127850L;

        {
            put("Zeitschrift fur Celtische Philologie", "Zeitschrift für celtische Philologie");
            put("Zeitschrift für Celtische Philologie; The Revenue Celtique", "Zeitschrift für celtische Philologie");
            put("Zeitschrift fur Orthographie", "Zeitschrift für Orthographie");
            put("Political quarterly", "The Political Quarterly");
            put("Zeitschrift fur Offentliches Recht Reappears", "Zeitschrift für Öffentliches Recht");
            put("Zeitschrift der deutschen morgenlandischen Gesellschaft",
                    "Zeitschrift der deutschen morgenländischen Gesellschaft");
            put("Zeitschrift für Deutscher Verein für Kunstwissenschaft", "Zeitschrift für Kunstwissenschaft");
            put("Canadian journal of economics and political science/Revue canadienne d'économique et de science politique",
                    "Canadian journal of economics and political science");
            put("Canadian modern language review/Revue canadienne des langues vivantes", "Canadian Modern Language Review");
        }
    };
    private static final IRI DC_DATE = IRI.create("dc:date");
    private static final IRI DC_PUBLISHER = IRI.create("dc:publisher");
    private static final IRI FABIO_JOURNAL = IRI.create("fabio:Journal");
    private static final IRI FABIO_PERIODICAL_VOLUME = IRI.create("fabio:PeriodicalVolume");
    private static final IRI FABIO_PERIODICAL_ISSUE = IRI.create("fabio:PeriodicalIssue");
    private static final IRI FABIO_PRINT_OBJECT = IRI.create("fabio:PrintObject");
    private static final IRI FRBR_PARTOF = IRI.create("frbr:partOf");
    private static final IRI FRBR_EMBODIMENT = IRI.create("frbr:embodiment");
    private static final IRI PRISM_PUBLICATION_DATE = IRI.create("prism:publicationDate");
    private static final IRI PRISM_PUBLICATION_NAME = IRI.create("prism:publicationName");
    private static final IRI PRISM_LOCATION = IRI.create("prism:location");
    private static final IRI PRISM_ISSN = IRI.create("prism:issn");
    private static final IRI PRISM_VOLUME = IRI.create("prism:volume");
    private static final IRI PRISM_NUMBER = IRI.create("prism:number");
    private static final IRI PRISM_STARTING_PAGE = IRI.create("prism:startingPage");

    public HostItemEntry(Map<String, Object> params) {
        super(params);
    }

    @Override
    public String transform(CatalogEntityWorker worker,
                            String resourcePredicate, Resource resource, String property, String value) throws IOException {
        Resource r = worker.getWorkerState().getResource();
        switch (property) {
            case "relatedParts": {
                String volume = null;
                String issue = null;
                String date = null;
                String page = null;
                Matcher matcher = partPattern1.matcher(value);
                if (matcher.matches()) {
                    volume = matcher.group(1);
                    issue = matcher.group(2);
                    int pos = issue.indexOf(':');
                    if (pos > 0) {
                        volume = issue.substring(0, pos);
                        issue = issue.substring(pos + 1);
                    }
                    date = matcher.group(3);
                    page = matcher.group(4);
                } else {
                    matcher = partPattern2.matcher(value);
                    if (matcher.matches()) {
                        volume = matcher.group(1);
                        date = matcher.group(2);
                        page = matcher.group(3);
                    }
                }
                if (page != null) {
                    r.newResource(FRBR_EMBODIMENT)
                            .a(FABIO_PRINT_OBJECT)
                            .add(PRISM_STARTING_PAGE, page);
                }
                if (volume != null) {
                    r.newResource(FRBR_EMBODIMENT)
                            .a(FABIO_PERIODICAL_VOLUME)
                            .add(PRISM_VOLUME, volume);
                }
                if (issue != null) {
                    r.newResource(FRBR_EMBODIMENT)
                            .a(FABIO_PERIODICAL_ISSUE)
                            .add(PRISM_NUMBER, issue);
                }
                if (date != null) {
                    r.add(PRISM_PUBLICATION_DATE, date);
                    worker.getWorkerState().getAuthoredWorkKey()
                            .chronology(date.substring(0, 4));
                }
                worker.getWorkerState().getAuthoredWorkKey()
                        .chronology(volume)
                        .chronology(issue);
                break;
            }
            case "title": {
                Matcher matcher = titlePattern.matcher(value);
                if (matcher.matches()) {
                    String journalTitle = matcher.group(1).trim();
                    if (brokenJournalTitles.containsKey(journalTitle)) {
                        journalTitle = brokenJournalTitles.get(journalTitle);
                    }
                    String cleanTitle = journalTitle
                            .replaceAll("\\p{C}", "")
                            .replaceAll("\\p{Space}", "")
                            .replaceAll("\\p{Punct}", "");
                    String publishingPlace = matcher.group(2).trim();
                    String publisherName = matcher.group(3).trim();
                    if (publisherName.endsWith(".")) {
                        publisherName = publisherName.substring(0, publisherName.length() - 1);
                    }
                    Resource serial = worker.getWorkerState().getSerialsMap().get(cleanTitle.toLowerCase());
                    if (serial == null && journalTitle.startsWith("The")) {
                        journalTitle = journalTitle.substring(4);
                        cleanTitle = journalTitle
                                .replaceAll("\\p{C}", "")
                                .replaceAll("\\p{Space}", "")
                                .replaceAll("\\p{Punct}", "");
                        serial = worker.getWorkerState().getSerialsMap().get(cleanTitle.toLowerCase());
                    }
                    Resource j = r.newResource(FRBR_PARTOF)
                            .a(FABIO_JOURNAL)
                            .add(PRISM_PUBLICATION_NAME, journalTitle)
                            .add(PRISM_LOCATION, publishingPlace)
                            .add(DC_PUBLISHER, publisherName);
                    if (serial != null) {
                        for (Node issn : serial.objects(PRISM_ISSN)) {
                             j.add(PRISM_ISSN, issn.toString());
                        }
                    } else {
                        worker.getWorkerState().getMissingSerials().put(journalTitle, true);
                    }
                }
                break;
            }
            default:
                break;
        }
        return value;
    }

}
