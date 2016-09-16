package org.xbib.catalog.entities;

import java.time.LocalDate;

/**
 *
 */
public class YearFacet extends TermFacet {

    private static final Integer maxYear = LocalDate.now().getYear() + 3;

    @Override
    public YearFacet addValue(String value) {
        if (value != null) {
            String s = value;
            if (!s.isEmpty()) {
                s = s.length() > 4 ? s.substring(0, 4) : s;
                int year = parseInt(s);
                if (year >= 700 && year < maxYear) {
                    super.addValue(s);
                }
            }
        }
        return this;
    }

    public int parseInt(final String s) {
        int num = 0;
        for (int i = 0; i < s.length(); i++) {
            num = num * 10 + (s.charAt(i) - '0');
        }
        return num;
    }

}
