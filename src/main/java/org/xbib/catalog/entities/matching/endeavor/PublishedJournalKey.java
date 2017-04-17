package org.xbib.catalog.entities.matching.endeavor;

import org.xbib.catalog.entities.matching.string.BaseformEncoder;
import org.xbib.catalog.entities.matching.string.EncoderException;
import org.xbib.catalog.entities.matching.string.WordBoundaryEntropyEncoder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * A key for a published journal.
 */
public class PublishedJournalKey implements Key {

    private List<String> titles;

    private List<String> publishingEntities;

    private String publicationType;

    public PublishedJournalKey() {
        this.titles = new ArrayList<>();
        this.publishingEntities = new ArrayList<>();
    }

    public PublishedJournalKey addTitle(String title) {
        if (title != null) {
            this.titles.addAll(split(clean(title)));
        }
        return this;
    }

    public void addTitle(Collection<String> titles) {
        if (titles != null) {
            titles.stream().filter(Objects::nonNull).forEach(this::addTitle);
        }
    }

    public void addPublishingEntity(String entity) {
        if (entity != null) {
            this.publishingEntities.addAll(split(clean(entity)));
        }
    }

    public void addPublishingEntity(Collection<String> entity) {
        if (entity != null) {
            entity.stream().filter(Objects::nonNull).forEach(this::addPublishingEntity);
        }
    }

    public void setPublicationType(String publicationType) {
        this.publicationType = publicationType;
    }

    public boolean isValidJournalKey() {
        return titles.size() > 0 && publishingEntities.size() > 0;
    }

    @Override
    public String createKey() {
        WordBoundaryEntropyEncoder encoder = new WordBoundaryEntropyEncoder();
        StringBuilder sb = new StringBuilder();
        for (String title : titles) {
            // remove punctuation, remove "... series"
            title = title.replaceAll("\\p{P}", "")
                    .replaceAll(" [sS]eries$", "");
            sb.append("s");
            String shortJournalName = BaseformEncoder.normalizedFromUTF8(title);
            if (shortJournalName.length() == 0) {
                shortJournalName = title; // restore non-latin-script titles, we don't know how to compact them.
            } else {
                try {
                    shortJournalName = encoder.encode(shortJournalName);
                } catch (EncoderException e) {
                    //
                }
            }
            shortJournalName = shortJournalName.replaceAll("\\s", "");
            sb.append(shortJournalName);
        }
        // reset encoder for publishing entities. This may enlarge match keys, but there may be very
        // similar publishers.
        encoder = new WordBoundaryEntropyEncoder();
        for (String publishingEntity : publishingEntities) {
            publishingEntity = publishingEntity.replaceAll("\\p{P}", "");
            String shortPublisherName = BaseformEncoder.normalizedFromUTF8(publishingEntity);
            if (shortPublisherName.length() == 0) {
                shortPublisherName = publishingEntity; // restore
            } else {
                try {
                    shortPublisherName = encoder.encode(shortPublisherName);
                } catch (EncoderException e) {
                    //
                }
            }
            shortPublisherName = shortPublisherName.replaceAll("\\s", "");
            sb.append(shortPublisherName);
        }
        // characterize this publication b ya given type. Maybe a carrier format or distribution mode.
        if (publicationType != null) {
            sb.append(publicationType);
        }
        return sb.toString();
    }


    protected String clean(String value) {
        if (value == null) {
            return null;
        }
        String s = value;
        int pos = s.indexOf("/ ");
        if (pos > 0) {
            s = s.substring(0, pos);
        }
        s = s.replaceAll("\\[.*?\\]", "").trim();
        return s;
    }

    protected List<String> split(String string) {
        String t = string;
        List<String> list = new ArrayList<>();
        if (t != null) {
            t = t.replaceAll(" ; ", "\n").replaceAll(" / ", "\n").replaceAll(" = ", "\n");
            for (String s : t.split("\n")) {
                if (s != null) {
                    if ("[...]".equals(s)) {
                        continue;
                    }
                    // remove transliteration prefix
                    if (s.startsWith("= ")) {
                        s = s.substring(2);
                    }
                    list.add(s.trim());
                }
            }
        }
        return list;
    }
}
