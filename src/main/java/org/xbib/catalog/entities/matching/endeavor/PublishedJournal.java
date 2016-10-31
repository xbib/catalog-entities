package org.xbib.catalog.entities.matching.endeavor;

import org.xbib.catalog.entities.matching.string.BaseformEncoder;
import org.xbib.catalog.entities.matching.string.EncoderException;
import org.xbib.catalog.entities.matching.string.WordBoundaryEntropyEncoder;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class PublishedJournal implements Identifiable {

    private static final Logger logger = Logger.getLogger(PublishedJournal.class.getName());

    private String journalName;

    private String publisherName;

    public PublishedJournal() {
    }

    public PublishedJournal journalName(String journalName) {
        this.journalName = journalName;
        return this;
    }

    public PublishedJournal publisherName(String publisherName) {
        this.publisherName = publisherName;
        return this;
    }

    @Override
    public String createIdentifier() {
        if (journalName == null) {
            return null;
        }
        // remove punctuation
        journalName = journalName.replaceAll("\\p{P}", "");
        // remove "... series"
        journalName = journalName.replaceAll(" [sS]eries$", "");
        WordBoundaryEntropyEncoder encoder = new WordBoundaryEntropyEncoder();
        StringBuilder sb = new StringBuilder();
        sb.append("s");
        String shortJournalName = BaseformEncoder.normalizedFromUTF8(journalName);
        int l = shortJournalName.length();
        if (l == 0) {
            shortJournalName = journalName; // restore non-latin-script titles
        } else {
            try {
                shortJournalName = encoder.encode(shortJournalName);
            } catch (EncoderException e) {
                logger.log(Level.FINEST, e.getMessage(), e);
            }
        }
        shortJournalName = shortJournalName.replaceAll("\\s", "");
        sb.append(shortJournalName);
        if (publisherName != null) {
            publisherName = publisherName.replaceAll("\\p{P}", "");
            String shortPublisherName = BaseformEncoder.normalizedFromUTF8(publisherName);
            l = shortPublisherName.length();
            if (l == 0) {
                shortPublisherName = publisherName; // restore
            } else {
                try {
                    shortPublisherName = encoder.encode(shortPublisherName);
                } catch (EncoderException e) {
                    logger.log(Level.FINEST, e.getMessage(), e);
                }
            }
            shortPublisherName = shortPublisherName.replaceAll("\\s", "");
            sb.append(shortPublisherName);
        }
        return sb.toString();
    }
}
