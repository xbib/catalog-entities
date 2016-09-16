package org.xbib.catalog.entities.matching.endeavor;

import org.xbib.catalog.entities.matching.string.BaseformEncoder;
import org.xbib.catalog.entities.matching.string.EncoderException;
import org.xbib.catalog.entities.matching.string.WordBoundaryEntropyEncoder;

public class PublishedJournal implements Identifiable {

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

    public String createIdentifier() {
        if (journalName == null) {
            return null;
        }
        // remove punctuation
        journalName = journalName.replaceAll("\\p{P}","");
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
                // ignore
            }
        }
        shortJournalName = shortJournalName.replaceAll("\\s","");
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
                    // ignore
                }
            }
            shortPublisherName = shortPublisherName.replaceAll("\\s", "");
            sb.append(shortPublisherName);
        }
        return sb.toString();
    }
}
