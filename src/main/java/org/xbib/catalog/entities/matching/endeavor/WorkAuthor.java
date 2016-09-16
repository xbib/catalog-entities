package org.xbib.catalog.entities.matching.endeavor;

import org.xbib.catalog.entities.matching.string.BaseformEncoder;
import org.xbib.catalog.entities.matching.string.EncoderException;
import org.xbib.catalog.entities.matching.string.WordBoundaryEntropyEncoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A work created by an author
 */
public class WorkAuthor implements Identifiable {

    private StringBuilder workName;

    private StringBuilder authorName;

    private StringBuilder chronology;

    private final WordBoundaryEntropyEncoder encoder = new WordBoundaryEntropyEncoder();

    /* These work titles can not be work titles and are blacklisted */
    private final static Set<String> blacklist = readResource("work-blacklist.txt");

    public WorkAuthor() {
    }

    public WorkAuthor workName(CharSequence workName) {
        if (workName != null) {
            this.workName = new StringBuilder(workName);
        }
        return this;
    }


    public WorkAuthor authorName(Collection<String> authorNames) {
        authorNames.forEach(this::authorName);
        return this;
    }

    /**
     * "Forename Givenname" or "Givenname, Forname"
     *
     * @param authorName author name
     * @return this
     */
    public WorkAuthor authorName(String authorName) {
        if (authorName == null) {
            return this;
        }
        // check if this is the work name
        if (workName != null && !authorName.isEmpty() && authorName.equals(workName.toString())) {
            return this;
        }
        if (this.authorName == null) {
            this.authorName = new StringBuilder();
        }
        String[] s = authorName.split("\\s+");
        if (s.length > 0) {
            // check if there is a comma, then it's "Givenname, Forname"
            if (s[0].indexOf(',') > 0) {
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

    public WorkAuthor authorNameWithForeNames(String lastName, String foreName) {
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
     * "Smith J"
     * @param lastName last name
     * @param initials initials
     * @return work author key
     */
    public WorkAuthor authorNameWithInitials(String lastName, String initials) {
        if (initials != null) {
            initials = initials.replaceAll("\\s+", "");
        }
        if (lastName != null) {
            if (this.authorName == null) {
                this.authorName = new StringBuilder(lastName);
                if (initials != null && initials.length() > 0) {
                    this.authorName.append(' ').append(initials);
                }
            } else {
                this.authorName.append(lastName);
                if (initials != null && initials.length() > 0) {
                    this.authorName.append(' ').append(initials);
                }
            }
        }
        return this;
    }

    public WorkAuthor chronology(String chronology) {
        if (chronology != null) {
            if (this.chronology == null) {
                this.chronology = new StringBuilder();
            }
            this.chronology.append(".").append(chronology.replaceAll("\\s+", ""));
        }
        return this;
    }

    public String createIdentifier() {
        if (workName == null || workName.length() == 0) {
            return null;
        }
        if (!isValidWork()) {
            return null;
        }
        String wName = BaseformEncoder.normalizedFromUTF8(workName.toString())
                .replaceAll("aeiou", ""); // TODO Unicode vocal category?
        try {
            wName = encoder.encode(wName);
        } catch (EncoderException e) {
            // ignore
        }
        if (isBlacklisted(workName)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("w").append(wName);
        if (authorName != null) {
            String aName = BaseformEncoder.normalizedFromUTF8(authorName.toString())
                    .replaceAll("aeiou", ""); // TODO Unicode vocal category?
            try {
                aName = encoder.encode(aName);
            } catch (EncoderException e) {
                //ignore
            }
            sb.append(".a").append(aName);
        }
        if (chronology != null) {
            sb.append(chronology);
        }
        return sb.toString();
    }

    public boolean isValidWork() {
        if (workName == null) {
            return false;
        }
        if (authorName == null)  {
            // only a single word in work name and no author name --> this key is not valid
            int pos = workName.toString().indexOf(' ');
            if (pos < 0) {
                return false;
            }
        }
        return true;
    }

    private final static Pattern p1 = Pattern.compile(".*Cover and Back matter.*", Pattern.CASE_INSENSITIVE);

    public Set<String> blacklist() {
        return blacklist;
    }

    public boolean isBlacklisted(CharSequence work) {
        return blacklist.contains(work.toString()) || p1.matcher(work).matches();
    }

    private static Set<String> readResource(String resource) {
        URL url = WorkAuthor.class.getResource(resource);
        Set<String> set = new HashSet<>();
        if (url != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), Charset.forName("UTF-8")))) {
                reader.lines().filter(line -> !line.startsWith("#")).forEach(set::add);
            } catch (IOException e) {
                // do nothing
            }
        }
        return set;
    }
}
