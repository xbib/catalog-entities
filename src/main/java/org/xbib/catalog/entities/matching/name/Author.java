package org.xbib.catalog.entities.matching.name;

import org.xbib.catalog.entities.matching.endeavor.Identifiable;
import org.xbib.catalog.entities.matching.string.BaseformEncoder;
import org.xbib.catalog.entities.matching.string.EncoderException;
import org.xbib.catalog.entities.matching.string.WordBoundaryEntropyEncoder;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An identifiable endeavor for an author.
 */
public class Author implements Identifiable {

    private static final Logger logger = Logger.getLogger(Author.class.getName());

    private StringBuilder authorName;

    private final WordBoundaryEntropyEncoder encoder = new WordBoundaryEntropyEncoder();

    public Author() {
    }

    public Author authorName(Collection<String> authorNames) {
        authorNames.forEach(this::authorName);
        return this;
    }

    /**
     * "Forename Givenname" or "Givenname, Forname".
     *
     * @param authorName author name
     * @return this
     */
    public Author authorName(String authorName) {
        if (authorName == null) {
            return this;
        }
        if (this.authorName == null) {
            this.authorName = new StringBuilder();
        }
        String[] s = authorName.split("\\s+");
        if (s.length > 0) {
            // check if there is a comma, then it's "Givenname, Forname"
            if (s[0].indexOf(',') >= 0) {
                String lastname = s[0];
                this.authorName.append(lastname);
                if (s.length > 1) {
                    this.authorName.append(' ');
                }
                for (int i = 1; i < s.length; i++) {
                    if (s[i].length() > 0) {
                        this.authorName.append(s[i].charAt(0));
                    }
                }
            } else {
                // get last author name part first
                String lastName = s[s.length - 1];
                this.authorName.append(lastName);
                if (s.length > 1) {
                    this.authorName.append(' ');
                }
                for (int i = 0; i < s.length - 1; i++) {
                    if (s[i].length() > 0) {
                        this.authorName.append(s[i].charAt(0));
                    }
                }
            }
        }
        return this;
    }

    public Author authorNameWithForeNames(String lastName, String foreName) {
        if (foreName == null) {
            return authorName(lastName);
        }
        StringBuilder sb = new StringBuilder();
        for (String s : foreName.split("\\s+")) {
            if (s.length() > 0) {
                sb.append(s.charAt(0));
            }
        }
        if (lastName != null) {
            if (this.authorName == null) {
                this.authorName = new StringBuilder(lastName);
                if (sb.length() > 0) {
                    this.authorName.append(' ').append(sb);
                }
            } else {
                this.authorName.append(lastName);
                if (sb.length() > 0) {
                    this.authorName.append(' ').append(sb);
                }
            }
        }
        return this;
    }

    /**
     * "Smith J".
     * @param lastName last name
     * @param initials initials
     * @return work author key
     */
    public Author authorNameWithInitials(String lastName, String initials) {
        String s = initials;
        if (s != null) {
            s = s.replaceAll("\\s+", "");
        }
        if (lastName != null) {
            if (this.authorName == null) {
                this.authorName = new StringBuilder(lastName);
                if (s != null && s.length() > 0) {
                    this.authorName.append(' ').append(s);
                }
            } else {
                this.authorName.append(lastName);
                if (s != null && s.length() > 0) {
                    this.authorName.append(' ').append(s);
                }
            }
        }
        return this;
    }

    @Override
    public String createIdentifier() {
        StringBuilder sb = new StringBuilder();
        if (authorName != null) {
            String aName = BaseformEncoder.normalizedFromUTF8(authorName.toString())
                    .replaceAll("aeiou", "");
            try {
                aName = encoder.encode(aName);
            } catch (EncoderException e) {
                logger.log(Level.FINE, e.getMessage(), e);
            }
            sb.append(aName);
        }
        return sb.toString();
    }
}
