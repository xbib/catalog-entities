package org.xbib.catalog.entities;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.content.rdf.Resource;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 *
 */
public class EnumerationAndChronologyHelperTest extends Assert {

    private static final Integer currentYear = ZonedDateTime.now().getYear();

    @Test
    public void test() {

        String[] specs = {
                "1961",
                "1965/66",
                "1968/70",
                "1921=1339",
                "1.1981=1401",
                "An V= [1796/97]",
                "1.5678=[1917/18]",
                "5717=[1956/57]",
                "1.1970",
                "1.1970/71",
                "2.1938/40(1942)",
                "9.1996/97(1997)",
                "1971 -",
                "1.1971 -",
                "1963 - 1972",
                "6.1961/64 - 31.1970",
                "1.1963 - 12.1972",
                "1961/62(1963)",
                "1965/70(1971/72)",
                "1961/62(1962)",
                "115.1921/22(1923) - 1125.1937",
                "1992,14140(12. März)",
                "SS 1922",
                "WS 1948/49",
                "3.1858,6 - 24.1881,3",
                "1.1970; 3.1972; 7.1973",
                "1.1960 - 5.1963; 11.1964; 23.1971 -",
                "2.1970,3",
                "4.1961,Aug.",
                "3.1971,Jan./Febr.",
                "2.1947,15.Mai -",
                "1963,21(22.Mai) -",
                "[19]81/82 - [19]83",
                "1981,31 - 25.1997",
                "1.1983 - 79/80.1992",
                "115.1921/22(1923) - 116.1922/23(1924)"
        };
        String[] dates = {
                "[1961]",
                "[1965, 1966]",
                "[1968, 1970]",
                "[1921]",
                "[1981]",
                "[1796, 1797]",
                "[1917, 1918]",
                "[1956, 1957]",
                "[1970]",
                "[1970, 1971]",
                "[1938, 1942]",
                "[1996, 1997]",
                "",
                "",
                "[1963, 1964, 1965, 1966, 1967, 1968, 1969, 1970, 1971, 1972]",
                "[1961, 1962, 1963, 1964, 1965, 1966, 1967, 1968, 1969, 1970]",
                "[1963, 1964, 1965, 1966, 1967, 1968, 1969, 1970, 1971, 1972]",
                "[1961, 1963]",
                "[1965, 1971]",
                "[1961, 1962]",
                "[1921, 1923, 1924, 1925, 1926, 1927, 1928, 1929, 1930, 1931, 1932, 1933, 1934, 1935, 1936, 1937]",
                "[1992]",
                "[1922]",
                "[1948, 1949]",
                "[1858, 1859, 1860, 1861, 1862, 1863, 1864, 1865, 1866, 1867, 1868, 1869, 1870, 1871, 1872, 1873, " +
                        "1874, 1875, 1876, 1877, 1878, 1879, 1880, 1881]",
                "[1970, 1972, 1973]",
                "",
                "[1970]",
                "[1961]",
                "[1971]",
                "",
                "",
                "[1981, 1982, 1983]",
                "[1981, 1982, 1983, 1984, 1985, 1986, 1987, 1988, 1989, 1990, 1991, 1992, 1993, 1994, 1995, 1996, 1997]",
                "[1983, 1984, 1985, 1986, 1987, 1988, 1989, 1990, 1991, 1992]",
                "[1921, 1922, 1923, 1924]"
        };
        // set dynamic years
        int year = currentYear;
        dates[12] = "[" + createYearList(1971, year) + "]";
        dates[13] = "[" + createYearList(1971, year) + "]";
        dates[26] = "[1960, 1961, 1962, 1963, 1964, " + createYearList(1971, year) + "]";
        dates[30] = "[" + createYearList(1947, year) + "]";
        dates[31] = "[" + createYearList(1963, year) + "]";

        for (int i = 0; i < specs.length; i++) {
            EnumerationAndChronologyHelper eac = new EnumerationAndChronologyHelper(null, null, null);
            String s = specs[i];
            Resource r = eac.parseToResource(s);
            Set<Integer> d = eac.dates(r);
            assertEquals(dates[i], d.toString());
        }
    }

    @Test
    public void testMovingwall() {
        List<Pattern> p = Collections.singletonList(Pattern.compile("Letzte (\\d+) Jg"));
        EnumerationAndChronologyHelper eac = new EnumerationAndChronologyHelper(null, null, p);
        String s = "Letzte 10 Jg.";
        Resource r = eac.parseToResource(s);
        Set<Integer> d = eac.dates(r);
        // yeah, moving wall
        Set<Integer> set = new TreeSet<>();
        for (int i = 0; i < 11; i++) {
            set.add(currentYear - i);
        }
        assertEquals(d.toString(), set.toString());
    }

    private String createYearList(int from, int to) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i <= to; i++) {
            if (sb.length() > 1) {
                sb.append(", ");
            }
            sb.append(Integer.toString(i));
        }
        return sb.toString();
    }
}
