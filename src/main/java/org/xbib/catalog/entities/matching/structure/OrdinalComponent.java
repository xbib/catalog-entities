package org.xbib.catalog.entities.matching.structure;

import org.xbib.catalog.entities.matching.Domain;
import org.xbib.catalog.entities.matching.title.TitleKey;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ordered part title key.
 * An ordered part title is a title component that can be naturally ordered in sequence,
 * where it may consist of any alphanumerical content
 *
 */
public class OrdinalComponent extends TitleKey {

    private final Pattern numericPattern = Pattern.compile("^(\\d+).*");

    /**
     * The domain name.
     *
     * @return the domain name
     */
    @Override
    public Domain getDomain() {
        return Domain.ORDERED_PART_TITLE;
    }

    /**
     * Add a component.
     *
     * @param value value
     */
    @Override
    public boolean add(String value) {
        // check if there is a numeral at the beginning, then it's a numbered part
        Matcher m = numericPattern.matcher(value);
        if (m.matches()) {
            return super.add(m.group());
        } else {
            return super.add(value);
        }
    }

}
