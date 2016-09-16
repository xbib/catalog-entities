package org.xbib.catalog.entities.matching;

import java.util.HashMap;
import java.util.Map;

public enum Domain {

    MATERIAL("M"),
    TITLE("T"),
    CREATOR("C"),
    NUMBER("N"),
    DATE("D"),
    GENERIC("G"),
    EDITION("E"),
    ORDERED_PART_TITLE("P"),
    SIMPLE("S");
    private String value;
    private static Map<String, Domain> map;

    private Domain(String value) {
        this.value = value;
        map(value, this);
    }

    private static void map(String value, Domain domain) {
        if (map == null) {
            map = new HashMap<>();
        }
        map.put(value, domain);
    }

    public static Domain getDomain(String domain) throws InvalidDomainException {
        if (!map.containsKey(domain)) {
            throw new InvalidDomainException(domain);
        }
        return map.get(domain);
    }

    @Override
    public String toString() {
        return value;
    }
}
