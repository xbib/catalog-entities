package org.xbib.catalog.entities.matching.abbreviations;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 *
 */
public class Abbrevations {

    private static ResourceBundle forBibliographicRules(String ruleName) {
        return ResourceBundle.getBundle(Abbrevations.class.getPackage().getName() + "." + ruleName);
    }

    private static ResourceBundle forBibliographicRules(String ruleName, Locale locale) {
        return ResourceBundle.getBundle(Abbrevations.class.getPackage().getName() + "." + ruleName, locale);
    }

    static class Bundle extends PropertyResourceBundle {

        public Bundle(InputStream stream) throws IOException {
            super(stream);
        }
    }

}
