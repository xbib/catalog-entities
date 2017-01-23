package org.xbib.catalog.entities.marc.nlz;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.catalog.entities.matching.endeavor.AuthoredWork;
import org.xbib.content.rdf.Resource;
import org.xbib.content.resource.IRI;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class Title extends CatalogEntity {

    private static final Logger logger = Logger.getLogger(Title.class.getName());

    private static final IRI DC_TITLE = IRI.create("dc:title");

    private static final IRI FABIO_ARTICLE = IRI.create("fabio:Article");

    private static final IRI FABIO_REVIEW = IRI.create("fabio:Review");

    public Title(Map<String, Object> params) {
        super(params);
    }

    @Override
    public List<String> transform(CatalogEntityWorker worker,
                                  String resourcePredicate, Resource resource, String property, String value) throws IOException {
        Resource r = worker.getWorkerState().getResource();
        IRI type = null;
        if ("title".equals(property)) {
            String s = value;
            if (s.endsWith(".")) {
                s = s.substring(0, s.length() - 1);
            }
            if (s.endsWith(".")) {
                s = s.substring(0, s.length() - 1);
            }
            if (s.endsWith(" / ")) {
                s = s.substring(0, s.length() - 3);
            }
            if (s.endsWith(" /")) {
                s = s.substring(0, s.length() - 2);
            }
            s = s.trim();
            if (s.endsWith("(Book Review)")) {
                s = s.substring(0, s.length() - 13).trim();
                type = FABIO_REVIEW;
            } else {
                if (s.startsWith("Article Review: ")) {
                    s = s.substring(16);
                } else {
                    type = FABIO_ARTICLE;
                }
            }
            String cleanTitle = value.replaceAll("\\p{C}", "")
                    .replaceAll("\\p{Space}", "")
                    .replaceAll("\\p{Punct}", "");
            AuthoredWork authoredWorkKey = worker.getWorkerState().getAuthoredWorkKey();
            if (!authoredWorkKey.isBlacklisted(value)) {
                authoredWorkKey.workName(cleanTitle);
                r.a(type);
                r.add(DC_TITLE, s);
            } else {
                logger.log(Level.WARNING, () -> MessageFormat.format("{0} blacklisted title: {1}",
                        worker.getWorkerState().getIdentifier(), cleanTitle));
            }
        }
        return Collections.singletonList(value);
    }

}
