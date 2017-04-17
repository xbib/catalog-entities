package org.xbib.catalog.entities.matching.endeavor;

import org.xbib.catalog.entities.matching.string.BaseformEncoder;
import org.xbib.catalog.entities.matching.string.EncoderException;
import org.xbib.catalog.entities.matching.string.WordBoundaryEntropyEncoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A work created by an author.
 */
public class AuthoredWorkKey implements Key {

    private static final Logger logger = Logger.getLogger(AuthoredWorkKey.class.getName());

    private static final Pattern p1 = Pattern.compile(".*Cover and Back matter.*", Pattern.CASE_INSENSITIVE);

    private static final Pattern yearPattern = Pattern.compile("\\d{4}");

    private StringBuilder workName;

    private StringBuilder authorName;

    private StringBuilder chronology;

    private final WordBoundaryEntropyEncoder encoder = new WordBoundaryEntropyEncoder();

    /* These work titles can not be work titles and are blacklisted */
    private static final Set<String> blacklist = readResource("work-blacklist.txt");

    public AuthoredWorkKey() {
    }

    public AuthoredWorkKey workName(CharSequence workName) {
        if (workName != null) {
            this.workName = new StringBuilder(workName);
        }
        return this;
    }

    public AuthoredWorkKey authorName(Collection<String> authorNames) {
        authorNames.forEach(this::authorName);
        return this;
    }

    /**
     * "Forename Givenname" or "Givenname, Forname".
     *
     * @param authorName author name
     * @return this
     */
    public AuthoredWorkKey authorName(String authorName) {
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

    public AuthoredWorkKey authorNameWithForeNames(String lastName, String foreName) {
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
     *
     * @param lastName last name
     * @param initials initials
     * @return work author key
     */
    public AuthoredWorkKey authorNameWithInitials(String lastName, String initials) {
        String s = initials;
        if (s != null) {
            s = s.replaceAll("\\s+", "");
        }
        boolean b = s != null && s.length() > 0;
        if (lastName != null) {
            if (this.authorName == null) {
                this.authorName = new StringBuilder(lastName);
                if (b) {
                    this.authorName.append(' ').append(s);
                }
            } else {
                this.authorName.append(lastName);
                if (b) {
                    this.authorName.append(' ').append(s);
                }
            }
        }
        return this;
    }

    public AuthoredWorkKey year(String year) {
        // searching for gregorian dates, clean all characters except the first four digits
        Matcher matcher = yearPattern.matcher(year);
        if (matcher.find()) {
            chronology(matcher.group());
        }
        return this;
    }

    public AuthoredWorkKey chronology(String chronology) {
        if (chronology != null) {
            if (this.chronology == null) {
                this.chronology = new StringBuilder();
            }
            this.chronology.append(".").append(chronology.replaceAll("\\s+", ""));
        }
        return this;
    }

    @Override
    public String createKey() {
        if (workName == null || workName.length() == 0) {
            return null;
        }
        String wName = BaseformEncoder.normalizedFromUTF8(workName.toString())
                .replaceAll("aeiou", "");
        try {
            wName = encoder.encode(wName);
        } catch (EncoderException e) {
            logger.log(Level.FINE, e.getMessage(), e);
        }
        if (isBlacklisted(workName)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("w").append(wName);
        if (authorName != null) {
            String aName = BaseformEncoder.normalizedFromUTF8(authorName.toString())
                    .replaceAll("aeiou", "");
            try {
                aName = encoder.encode(aName);
            } catch (EncoderException e) {
                logger.log(Level.FINE, e.getMessage(), e);
            }
            sb.append(".a").append(aName);
        }
        if (chronology != null) {
            sb.append(chronology);
        }
        return sb.toString();
    }

    public boolean isValidKey() {
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

    public Set<String> blacklist() {
        return blacklist;
    }

    public boolean isBlacklisted(CharSequence work) {
        return blacklist.contains(work.toString()) || p1.matcher(work).matches();
    }

    private static Set<String> readResource(String resource) {
        URL url = AuthoredWorkKey.class.getResource(resource);
        Set<String> set = new LinkedHashSet<>();
        if (url != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(),
                    StandardCharsets.UTF_8))) {
                reader.lines().filter(line -> !line.startsWith("#")).forEach(set::add);
            } catch (IOException e) {
                logger.log(Level.FINE, e.getMessage(), e);
            }
        }
        return set;
    }
}
