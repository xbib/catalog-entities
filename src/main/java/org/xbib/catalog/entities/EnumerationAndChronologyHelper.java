package org.xbib.catalog.entities;

import org.xbib.content.rdf.Resource;
import org.xbib.content.rdf.internal.DefaultAnonymousResource;
import org.xbib.content.resource.IRI;
import org.xbib.content.resource.Node;
import org.xbib.marc.MarcField;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parsing patterns of enumeration and chronology.
 * Rules are given by Zeitschriftendatenbank
 * http://support.d-nb.de/iltis/katricht/zdb/8032.pdf
 */
public class EnumerationAndChronologyHelper {

    private static final Logger logger = Logger.getLogger(EnumerationAndChronologyHelper.class.getName());

    private static final Integer currentYear = LocalDate.now().getYear();

    // yyyy
    private static final Pattern[] p1a = {
            // 1921=1339
            // 1921/22=1339
            Pattern.compile("(\\d{4})(/?\\d{2,4})\\s*="),
            // 1965/70(1971/72)
            Pattern.compile("(\\d{4})(/?\\d{2,4})\\((\\d{4})(/?\\d{2,4})\\)"),
            // 1965/66
            // 1968/70
            // 1961/62(1963)
            // 1961/62(1962)
            // WS 1948/49
            Pattern.compile("(\\d{4})(/?\\d{2,4})"),
            // 1961
            // 1992,14140(12. März)
            // SS 1922
            Pattern.compile("(\\d{4})")
    };
    // i.yyyy
    private static final Pattern[] p1b = {
            // 1.1970/71
            // 2.1938/40(1942)
            // 9.1996/97(1997)
            Pattern.compile("(\\d+)\\.(\\d{4})(/\\d{0,4})"),
            // 1.1970
            // 2.1970,3
            // 4.1961,Aug.
            // 3.1971,Jan./Febr.
            // 1.1970; 3.1972; 7.1973
            Pattern.compile("(\\d+)\\.(\\d{4})"),
            // 1.[19]93,1
            Pattern.compile("(\\d+)\\.(\\[?\\d{2,4}\\]?\\d{2,4}/?\\d{0,4})")
    };
    // yyyy=yyyy
    private static final Pattern[] p1c = {
            // An V= [1796/97]
            // 1.5678=[1917/18]
            Pattern.compile("=\\s*\\[(\\d{4})(/?\\d{0,4})\\]"),
            // 1.1981=1401
            Pattern.compile("\\.(\\d{4}/?\\d{0,4})\\s*=")
    };
    // yyyy -
    private static final Pattern[] p2a = new Pattern[]{
            // 1971 -
            Pattern.compile("(\\d{4}/?\\d{0,4}).*\\-\\s*$"),
            // 1963,21(22.Mai) -
            Pattern.compile("(\\d{4}).*\\-\\s*$"),
            // [19]51,1 - [19]52,5
            Pattern.compile("(\\[\\d{2,4}\\]\\d{2,4}).*\\-\\s*$")
    };
    // i.yyyy -
    private static final Pattern[] p2b = {
            // 1.1971 -
            // 2.1947,15.Mai -
            Pattern.compile("(\\d+)\\.(\\d{4}/?\\d{0,4}).*?\\-\\s*$"),
            // 63.2011,Okt. -
            Pattern.compile("(\\d{0,4})\\.(\\d{4}).*?\\-\\s*$"),
            Pattern.compile("(.*?)\\.(\\d{4}/?\\d{0,4}).*\\-\\s*$")
    };
    // yyyy,v -
    private static final Pattern[] p2c = {
            Pattern.compile("(\\d{4}/?\\d{0,4}),(.*?)\\s*\\-\\s*$")
    };
    // yyyy - yyyy
    private static final Pattern[] p3a = {
            // 1963 - 1972
            Pattern.compile("(\\d{4}/?\\d{0,4}).*\\-\\s*(\\d{4}/?\\d{0,4})"),
            // [19]51,1 - [19]52,5
            Pattern.compile("(\\[\\d{2,4}\\]\\d{2,4}/?\\d{0,4}).*\\-\\s*(\\[\\d{2,4}\\]\\d{2,4}/?\\d{0,4})")
    };
    // i.yyyy - i.yyyy
    private static final Pattern[] p3b = {
            // 6.1961/64 - 31.1970
            // 1.1963 - 12.1972
            // 115.1921/22(1923) - 1125.1937
            // 3.1858,6 - 24.1881,3
            // 1.1960 - 5.1963; 11.1964; 23.1971 -
            Pattern.compile("(\\d+)\\.(\\d{4}/?\\d{0,4}).*\\-\\s*(\\d+)\\.(\\d{4}/?\\d{0,4})"),
            Pattern.compile("(.*?)\\.(\\d{4}/?\\d{0,4}).*\\-\\s*(.*?)\\.(\\d{4}/?\\d{0,4})"),
            // [19]81/82 - [19]83
            Pattern.compile("(\\d+)\\.(\\[\\d{2,4}\\]\\d{2,4})(/\\d{0,4}).*\\-"
                    + "\\s*(\\d+)\\.(\\[\\d{2,4}\\]\\d{2,4})(/\\d{0,4})"),
            // 1.[19]51,1 - 1.[19]52,5
            Pattern.compile("(\\d+)\\.(\\[\\d{2,4}\\]\\d{2,4}).*\\-\\s*(\\d+)\\.(\\[\\d{2,4}\\]\\d{2,4})")
    };
    // yyyy,v - yyyy,v
    private static final Pattern[] p3c = {
            Pattern.compile("(\\d{4}),(.*?)\\s*\\-\\s*(\\d{4}),(.*?)")
    };
    // yyyy,v - i.yyyy
    private static final Pattern[] p3d = {
            // 1981,31 - 25.1997
            // 115.1921/22(1923) - 1125.1937
            Pattern.compile("(\\d{4}),(.*?)\\s*\\-\\s*(\\d+)\\.(\\d{4})")
    };
    // i.yyyy - yyyy,v
    private static final Pattern[] p3e = {
            Pattern.compile("(.*?)\\.(\\d{4})\\s*\\-\\s*(\\d{4}),(.*?)")
    };

    /**
     * Berichtsjahr/Erscheinungsjahr.
     * report date / publication date
     */

    // Jahrgänge, mit zwei Zählungen, jeweils Berichtsjahr/Erscheinungsjahr, geschlossen
    // 115.1921/22(1923) - 116.1922/23(1924)
    // 1 = Zählung
    // 2 = Berichtsjahr
    // 3 = Erscheinungsjahr (yyyy), (yyyy/yy), (yyyy/yyyy)
    // 4 = Zählung
    // 5 = Berichtsjahr
    // 6 = Erscheinungsjahr (yyyy), (yyyy/yy), (yyyy/yyyy)
    private static final Pattern[] g1a = {
            Pattern.compile("(.*?)\\.(\\d{4}/?\\d{0,4})\\((\\d{4}/?\\d{0,4})\\)\\s*\\-"
                    + "\\s*(.*?)\\.(\\d{4}/?\\d{0,4})\\((\\d{4}/?\\d{0,4})\\)"),
    };

    // Jahrgänge, mit zwei Zählungen, einmal Berichtsjahr/Erscheinungsjahr, geschlossen
    // 115.1921/22(1923) - 1125.1937
    // 1 = Zählung
    // 2 = Berichtsjahr
    // 3 = Erscheinungsjahr (yyyy), (yyyy/yy), (yyyy/yyyy)
    // 4 = Zählung
    // 5 = Berichtsjahr
    private static final Pattern[] g1b = {
            Pattern.compile("(.*?)\\.(\\d{4}/?\\d{0,4})\\((\\d{4}/?\\d{0,4})\\)\\s*\\-\\s*(.*?)\\.(\\d{4}/?\\d{0,4})"),
    };

    // Jahrgänge, mit Zählung, offen
    // 115.1921/22(1923) -
    // 1 = Zählung
    // 2 = Berichtsjahr
    // 3 = Erscheinungsjahr (yyyy), (yyyy/yy), (yyyy/yyyy)
    private static final Pattern[] g1c = {
            Pattern.compile("(.*?)\\.(\\d{4}/?\\d{0,4})\\((\\d{4}/?\\d{0,4})\\)\\s*\\-")
    };

    // einzelner Jahrgang mit Zählung
    // 2.1938/40(1942)
    // 9.1996/97(1997)
    // 1 = Zählung Berichtsjahr
    // 2 = Berichtsjahr
    // 3 = Erscheinungsjahr (yyyy), (yyyy/yy), (yyyy/yyyy)
    private static final Pattern[] g1d = {
            Pattern.compile("(.*?)\\.(\\d{4}/?\\d{0,4})\\((\\d{4}/?\\d{0,4})\\)")
    };

    // einzelner Jahrgang
    // 1961/62(1963)
    // 1965/70(1971/72)
    // 1 = Berichtsjahr
    // 2 = Erscheinungsjahr
    private static final Pattern[] g1e = {
            Pattern.compile("(\\d{4}/?\\d{0,4})\\((\\d{4}/?\\d{0,4})\\)")
    };

    private final String id;

    private final MarcField marcField;

    private final Set<Integer> dates;

    private final List<Pattern> movingwalls;

    private final List<Integer> begin;

    private final List<Integer> end;

    private final List<String> beginVolume;

    private final List<String> endVolume;

    private final List<Integer> beginPubDate;

    private final List<Integer> endPubDate;

    private final List<Boolean> open;

    public EnumerationAndChronologyHelper(String id, MarcField marcField, List<Pattern> movingwalls) {
        this.id = id;
        this.marcField = marcField;
        this.movingwalls = movingwalls;
        this.dates = new TreeSet<>();
        this.begin = new LinkedList<>();
        this.end = new LinkedList<>();
        this.beginVolume = new LinkedList<>();
        this.endVolume = new LinkedList<>();
        this.beginPubDate = new LinkedList<>();
        this.endPubDate = new LinkedList<>();
        this.open = new LinkedList<>();
    }

    public List<Integer> getBegin() {
        return begin;
    }

    public List<Integer> getEnd() {
        return end;
    }

    public List<String> getBeginVolume() {
        return beginVolume;
    }

    public List<String> getEndVolume() {
        return endVolume;
    }

    public List<Integer> getBeginPubDate() {
        return beginPubDate;
    }

    public List<Integer> getEndPubDate() {
        return endPubDate;
    }

    public List<Boolean> getOpen() {
        return open;
    }

    public Resource parseToResource(String content) {
        return parseToResource(content, new DefaultAnonymousResource());
    }

    public Resource parseToResource(String content, Resource resource) {
        int groups = parseInternal(content);
        for (int i = 0; i < groups; i++) {
            resource.newResource("group")
                    .add("begindate", begin.get(i))
                    .add("enddate", end.get(i))
                    .add("beginvolume", beginVolume.get(i))
                    .add("endvolume", endVolume.get(i))
                    .add("beginpubdate", beginPubDate.get(i))
                    .add("endpubdate", endPubDate.get(i))
                    .add("open", open.get(i));
        }
        return resource;
    }

    private int parseInternal(String values) {
        if (values == null) {
            return 0;
        }
        int i = 0;
        for (String value : values.split(";")) {
            boolean found = false;
            // Jahrgänge, mit zwei Zählungen, jeweils Berichtsjahr/Erscheinungsjahr, geschlossen
            for (Pattern p : g1a) {
                Matcher m = p.matcher(value);
                found = m.find();
                if (found) {
                    begin.add(i, sanitizeDate(m.group(2)));
                    end.add(i, sanitizeDate(m.group(5)));
                    beginVolume.add(i, m.group(1));
                    endVolume.add(i, m.group(4));
                    beginPubDate.add(i, sanitizeDate(m.group(3)));
                    endPubDate.add(i, sanitizeDate(m.group(6)));
                    open.add(i, false);
                    i++;
                }
            }
            if (found) {
                continue;
            }
            // Jahrgänge, mit zwei Zählungen, einmal Berichtsjahr/Erscheinungsjahr, geschlossen
            for (Pattern p : g1b) {
                Matcher m = p.matcher(value);
                found = m.find();
                if (found) {
                    begin.add(i, sanitizeDate(m.group(2)));
                    end.add(i, sanitizeDate(m.group(5)));
                    beginVolume.add(i, m.group(1));
                    endVolume.add(i, m.group(4));
                    beginPubDate.add(i, sanitizeDate(m.group(3)));
                    endPubDate.add(i, sanitizeDate(m.group(5)));
                    open.add(i, false);
                    i++;
                }
            }
            if (found) {
                continue;
            }
            // Berichtsjahr/Erscheinungsjahr: Jahrgänge, mit Zählung, offen
            for (Pattern p : g1c) {
                Matcher m = p.matcher(value);
                found = m.find();
                if (found) {
                    begin.add(i, sanitizeDate(m.group(2)));
                    end.add(i, null);
                    beginVolume.add(i, m.group(1));
                    endVolume.add(i, null);
                    beginPubDate.add(i, sanitizeDate(m.group(3)));
                    endPubDate.add(i, null);
                    open.add(i, true);
                    i++;
                }
            }
            if (found) {
                continue;
            }
            // Berichtsjahr/Erscheinungsjahr: einzelner Eintrag mit Zählung
            for (Pattern p : g1d) {
                Matcher m = p.matcher(value);
                found = m.find();
                if (found) {
                    begin.add(i, sanitizeDate(m.group(2)));
                    end.add(i, null);
                    beginVolume.add(i, m.group(1));
                    endVolume.add(i, null);
                    beginPubDate.add(i, sanitizeDate(m.group(3)));
                    endPubDate.add(i, null);
                    open.add(i, false);
                    i++;
                }
            }
            if (found) {
                continue;
            }
            // Berichtsjahr/Erscheinungsjahr: einzelner Eintrag
            for (Pattern p : g1e) {
                Matcher m = p.matcher(value);
                found = m.find();
                if (found) {
                    begin.add(i, sanitizeDate(m.group(1)));
                    end.add(i, null);
                    beginVolume.add(i, null);
                    endVolume.add(i, null);
                    beginPubDate.add(i, sanitizeDate(m.group(2)));
                    endPubDate.add(i, null);
                    open.add(i, false);
                    i++;
                }
            }
            if (found) {
                continue;
            }
            found = false;
            for (Pattern p : p3e) {
                Matcher m = p.matcher(value);
                found = m.find();
                if (found) {
                    Integer b = sanitizeDate(m.group(2));
                    Integer e =  sanitizeDate(m.group(3));
                    begin.add(i, b);
                    end.add(i, e);
                    beginVolume.add(i, m.group(1));
                    endVolume.add(i, m.group(4));
                    beginPubDate.add(i, b);
                    endPubDate.add(i, e);
                    open.add(i, false);
                    i++;
                    break;
                }
            }
            if (found) {
                continue;
            }
            found = false;
            for (Pattern p : p3d) {
                Matcher m = p.matcher(value);
                found = m.find();
                if (found) {
                    Integer b = sanitizeDate(m.group(1));
                    Integer e = sanitizeDate(m.group(4));
                    begin.add(i, b);
                    end.add(i, e);
                    beginVolume.add(i, m.group(2));
                    endVolume.add(i, m.group(3));
                    beginPubDate.add(i, b);
                    endPubDate.add(i, e);
                    open.add(i, false);
                    i++;
                    break;
                }
            }
            if (found) {
                continue;
            }
            found = false;
            for (Pattern p : p3c) {
                Matcher m = p.matcher(value);
                found = m.find();
                if (found) {
                    Integer b = sanitizeDate(m.group(1));
                    Integer e = sanitizeDate(m.group(3));
                    begin.add(i, b);
                    end.add(i, e);
                    beginVolume.add(i, m.group(2));
                    endVolume.add(i, m.group(4));
                    beginPubDate.add(i, b);
                    endPubDate.add(i, e);
                    open.add(i, false);
                    i++;
                    break;
                }
            }
            if (found) {
                continue;
            }
            found = false;
            for (Pattern p : p3b) {
                Matcher m = p.matcher(value);
                found = m.find();
                if (found) {
                    Integer b = sanitizeDate(m.group(2));
                    Integer e = sanitizeDate(m.group(4));
                    begin.add(i, b);
                    end.add(i, e);
                    beginVolume.add(i, m.group(1));
                    endVolume.add(i, m.group(3));
                    beginPubDate.add(i, b);
                    endPubDate.add(i, e);
                    open.add(i, false);
                    i++;
                    break;
                }
            }
            if (found) {
                continue;
            }
            found = false;
            for (Pattern p : p3a) {
                Matcher m = p.matcher(value);
                found = m.find();
                if (found) {
                    if (m.groupCount() == 4) {
                        List<Integer> l1 = fractionDate(m.group(1) + m.group(2));
                        List<Integer> l2 = fractionDate(m.group(3) + m.group(4));
                        Integer b = sanitizeDate(l1.get(0));
                        Integer e = sanitizeDate(l2.get(l2.size() - 1));
                        begin.add(i, b);
                        end.add(i, e);
                        beginVolume.add(i, null);
                        endVolume.add(i, null);
                        beginPubDate.add(i, b);
                        endPubDate.add(i, e);
                        open.add(i, false);
                        i++;
                    } else {
                        List<Integer> l1 = fractionDate(m.group(1));
                        List<Integer> l2 = fractionDate(m.group(2));
                        Integer b = sanitizeDate(l1.get(0));
                        Integer e = sanitizeDate(l2.get(l2.size() - 1));
                        begin.add(i, b);
                        end.add(i, e);
                        beginVolume.add(i, null);
                        endVolume.add(i, null);
                        beginPubDate.add(i, b);
                        endPubDate.add(i, e);
                        open.add(i, false);
                        i++;
                    }
                    break;
                }
            }
            if (found) {
                continue;
            }
            found = false;
            for (Pattern p : p2c) {
                Matcher m = p.matcher(value);
                found = m.find();
                if (found) {
                    Integer b = sanitizeDate(m.group(1));
                    begin.add(i, b);
                    end.add(i, null);
                    beginVolume.add(i,  m.group(2));
                    endVolume.add(i, null);
                    beginPubDate.add(i, b);
                    endPubDate.add(i, null);
                    open.add(i, true);
                    i++;
                    break;
                }
            }
            if (found) {
                continue;
            }
            found = false;
            for (Pattern p : p2b) {
                Matcher m = p.matcher(value);
                found = m.find();
                if (found) {
                    Integer b = sanitizeDate(m.group(2));
                    begin.add(i, b);
                    end.add(i, null);
                    beginVolume.add(i,  m.group(1));
                    endVolume.add(i, null);
                    beginPubDate.add(i, b);
                    endPubDate.add(i, null);
                    open.add(i, true);
                    i++;
                    break;
                }
            }
            if (found) {
                continue;
            }
            found = false;
            for (Pattern p : p2a) {
                Matcher m = p.matcher(value);
                found = m.find();
                if (found) {
                    Integer b = sanitizeDate(m.group(1));
                    begin.add(i, b);
                    end.add(i, null);
                    beginVolume.add(i, null);
                    endVolume.add(i, null);
                    beginPubDate.add(i, b);
                    endPubDate.add(i, null);
                    open.add(i, true);
                    i++;
                    break;
                }
            }
            if (found) {
                continue;
            }
            found = false;
            for (Pattern p : p1c) {
                Matcher m = p.matcher(value);
                found = m.find();
                if (found) {
                    if (m.groupCount() > 1) {
                        List<Integer> l = fractionDate(m.group(1) + m.group(2));
                        Integer b = sanitizeDate(l.get(0));
                        begin.add(i, b);
                        end.add(i, null);
                        beginVolume.add(i, null);
                        endVolume.add(i, null);
                        beginPubDate.add(i, b);
                        endPubDate.add(i, null);
                        open.add(i, false);
                        i++;
                        if (l.size() > 1) {
                            b = sanitizeDate(l.get(1));
                            begin.add(i, b);
                            end.add(i, null);
                            beginVolume.add(i, null);
                            endVolume.add(i, null);
                            beginPubDate.add(i, b);
                            endPubDate.add(i, null);
                            open.add(i, false);
                            i++;
                        }
                    } else {
                        Integer b = sanitizeDate(m.group(1));
                        begin.add(i, b);
                        end.add(i, null);
                        beginVolume.add(i, null);
                        endVolume.add(i, null);
                        beginPubDate.add(i, b);
                        endPubDate.add(i, null);
                        open.add(i, false);
                        i++;
                    }
                    break;
                }
            }
            if (found) {
                continue;
            }
            found = false;
            for (Pattern p : p1b) {
                Matcher m = p.matcher(value);
                found = m.find();
                if (found) {
                    if (m.groupCount() == 3) {
                        List<Integer> l = fractionDate(m.group(2) + m.group(3));
                        Integer b = sanitizeDate(l.get(0));
                        begin.add(i, b);
                        end.add(i, null);
                        beginVolume.add(i,  m.group(1));
                        endVolume.add(i, null);
                        beginPubDate.add(i, b);
                        endPubDate.add(i, null);
                        open.add(i, false);
                        i++;
                        if (l.size() > 1) {
                            b = sanitizeDate(l.get(1));
                            begin.add(i, b);
                            end.add(i, null);
                            beginVolume.add(i,  m.group(1));
                            endVolume.add(i, null);
                            beginPubDate.add(i, b);
                            endPubDate.add(i, null);
                            open.add(i, false);
                            i++;
                        }
                    } else {
                        Integer b = sanitizeDate(m.group(2));
                        begin.add(i, b);
                        end.add(i, null);
                        beginVolume.add(i,  m.group(1));
                        endVolume.add(i, null);
                        beginPubDate.add(i, b);
                        endPubDate.add(i, null);
                        open.add(i, false);
                        i++;
                    }
                    break;
                }
            }
            if (found) {
                continue;
            }
            found = false;
            for (Pattern p : p1a) {
                Matcher m = p.matcher(value);
                found = m.find();
                if (found) {
                    if (m.groupCount() == 4) {
                        List<Integer> l1 = fractionDate(m.group(1) + m.group(2));
                        List<Integer> l2 = fractionDate(m.group(3) + m.group(4));
                        Integer b = sanitizeDate(l1.get(0));
                        begin.add(i, b);
                        end.add(i, null);
                        beginVolume.add(i, null);
                        endVolume.add(i, null);
                        beginPubDate.add(i, b);
                        endPubDate.add(i, null);
                        open.add(i, false);
                        i++;
                        if (l1.size() > 1) {
                            b = sanitizeDate(l1.get(1));
                            begin.add(i, b);
                            end.add(i, null);
                            beginVolume.add(i, null);
                            endVolume.add(i, null);
                            beginPubDate.add(i, b);
                            endPubDate.add(i, null);
                            open.add(i, false);
                            i++;
                        }
                        b = sanitizeDate(l2.get(0));
                        begin.add(i, b);
                        end.add(i, null);
                        beginVolume.add(i, null);
                        endVolume.add(i, null);
                        beginPubDate.add(i, b);
                        endPubDate.add(i, null);
                        open.add(i, false);
                        i++;
                        if (l2.size() > 1) {
                            b = sanitizeDate(l2.get(1));
                            begin.add(i, b);
                            end.add(i, null);
                            beginVolume.add(i, null);
                            endVolume.add(i, null);
                            beginPubDate.add(i, b);
                            endPubDate.add(i, null);
                            open.add(i, false);
                            i++;
                        }
                    } else if (m.groupCount() == 2) {
                        List<Integer> l = fractionDate(m.group(1) + m.group(2));
                        Integer b = sanitizeDate(l.get(0));
                        begin.add(i, b);
                        end.add(i, null);
                        beginVolume.add(i, null);
                        endVolume.add(i, null);
                        beginPubDate.add(i, b);
                        endPubDate.add(i, null);
                        open.add(i, false);
                        i++;
                        if (l.size() > 1) {
                            b = sanitizeDate(l.get(1));
                            begin.add(i, b);
                            end.add(i, null);
                            beginVolume.add(i, null);
                            endVolume.add(i, null);
                            beginPubDate.add(i, b);
                            endPubDate.add(i, null);
                            open.add(i, false);
                            i++;
                        }
                    } else {
                        Integer b = sanitizeDate(m.group(1));
                        begin.add(i, b);
                        end.add(i, null);
                        beginVolume.add(i, null);
                        endVolume.add(i, null);
                        beginPubDate.add(i, b);
                        endPubDate.add(i, null);
                        open.add(i, false);
                        i++;
                    }
                    break;
                }
            }
            if (found) {
                continue;
            }
            if (movingwalls != null) {
                for (Pattern p : movingwalls) {
                    Matcher m = p.matcher(value);
                    found = m.find();
                    if (found) {
                        Integer b = currentYear - Integer.parseInt(m.group(1));
                        begin.add(i, b);
                        end.add(i, null);
                        beginVolume.add(i, null);
                        endVolume.add(i, null);
                        beginPubDate.add(i, b);
                        endPubDate.add(i, null);
                        open.add(i, true);
                        i++;
                        break;
                    }
                }
            }
        }
        return i;
    }

    private Integer sanitizeDate(String date) {
        int pos = date.indexOf('/');
        if (pos > 0) {
            if (pos > 4) {
                return null; // invalid date
            }
            String s = date.substring(0, pos).replaceAll("[^\\d]", "");
            return sanitizeDate(Integer.parseInt(s));
        } else {
            // remove non-numeric characters
            String s = date.replaceAll("[^\\d]", "");
            return s.length() == 4 ? sanitizeDate(Integer.parseInt(s)) : null;
        }
    }

    public Set<Integer> dates(Resource resource) {
        for (IRI iri : resource.predicates()) {
            resource.resources(iri).forEach(group -> {
                Iterator<Node> begindateCollection = group.objects("begindate").iterator();
                Object begindate = begindateCollection.hasNext() ?
                        begindateCollection.next() : null;
                Iterator<Node> enddateCollection = group.objects("enddate").iterator();
                Object enddate = enddateCollection.hasNext() ?
                        enddateCollection.next() : null;
                Iterator<Node> beginpubdateCollection = group.objects("beginpubdate").iterator();
                Object beginpubdate = beginpubdateCollection.hasNext() ?
                        beginpubdateCollection.next() : null;
                Iterator<Node> endpubdateCollection = group.objects("endpubdate").iterator();
                Object endpubdate = endpubdateCollection.hasNext() ?
                        endpubdateCollection.next() : null;
                Iterator<Node> openCollection = group.objects("open").iterator();
                Object openCollectionObject = openCollection.hasNext() ?
                        openCollection.next() : null;
                List<Integer> starts;
                int beginNumber = -1;
                if (begindate != null) {
                    starts = fractionDate(begindate.toString());
                    dates.addAll(starts);
                    beginNumber = starts.get(0);
                }
                if (beginpubdate != null) {
                    starts = fractionDate(beginpubdate.toString());
                    dates.addAll(starts);
                    beginNumber = starts.get(0);
                }
                int endNumber = -1;
                List<Integer> ends;
                if (enddate != null) {
                    ends = fractionDate(enddate.toString());
                    dates.addAll(ends);
                    endNumber = ends.get(0);
                }
                if (endpubdate != null) {
                    ends = fractionDate(endpubdate.toString());
                    dates.addAll(ends);
                    endNumber = ends.get(0);
                }
                if (openCollectionObject != null && "true^^xsd:boolean".equals(openCollectionObject.toString())) {
                    endNumber = currentYear;
                }
                // add years from interval
                if (beginNumber >= 0 && endNumber >= 0) {
                    if (beginNumber > currentYear || endNumber > currentYear) {
                        logger.log(Level.WARNING,
                                MessageFormat.format("{0} {1} future dates: {2},{3} (from {4},{5})",
                                id, marcField, beginNumber, endNumber, begindate, enddate));
                    } else if (endNumber - beginNumber > 500) {
                        logger.log(Level.WARNING,
                                MessageFormat.format("{0} {1} too many years: {2},{3} (from {4},{5})",
                                id, marcField, beginNumber, endNumber, begindate, enddate));
                        // RDA: 1500
                        // Acta eruditorum: 1682
                        // Phil. Trans.: 1655 (but not in print)
                    } else if (beginNumber < 1500 || endNumber < 1500) {
                        logger.log(Level.WARNING, MessageFormat.format("{0} {1} too early: {2},{3} ({4},{5})",
                                id, marcField, beginNumber, endNumber, begindate, enddate));
                    } else {
                        for (int i = beginNumber; i <= endNumber; i++) {
                            dates.add(i);
                        }
                    }
                }
                if (dates.size() > 500) {
                    logger.log(Level.WARNING, MessageFormat.format("{0} {1} too many dates: {2}",
                            id, marcField, dates.size()));
                }
            });
        }
        return dates;
    }

    private Integer sanitizeDate(Integer date) {
        return date > 1500 && date <= currentYear ? date : null;
    }

    private List<Integer> fractionDate(String date) {
        List<Integer> dates = new LinkedList<>();
        int pos = date.indexOf('/');
        if (pos > 0) {
            String s = date.substring(0, pos).replaceAll("[^\\d]", "");
            int base = Integer.parseInt(s);
            dates.add(base);
            int frac = 0;
            try {
                frac = Integer.parseInt(date.substring(pos + 1));
            } catch (NumberFormatException e) {
                // not important
            }
            if (frac >= 100 && frac <= currentYear) {
                dates.add(frac);
            } else if (frac > 0 && frac < 100) {
                // two digits frac, create full year
                frac += Integer.parseInt(s.substring(0, 2)) * 100;
                dates.add(frac);
            }
        } else {
            String s = date.replaceAll("[^\\d]", "");
            int base = Integer.parseInt(s);
            dates.add(base);
        }
        return dates;
    }
}
