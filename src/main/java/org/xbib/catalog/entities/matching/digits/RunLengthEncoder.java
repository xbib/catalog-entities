package org.xbib.catalog.entities.matching.digits;

import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class RunLengthEncoder {

    private final List<Number> members = new LinkedList<>();

    private final NumberFormat format = NumberFormat.getInstance();

    public RunLengthEncoder member(Number number) {
        this.members.add(number);
        return this;
    }

    public String encode() {
        StringBuilder sb = new StringBuilder();
        for (Object o : members) {
            if (o instanceof Number) {
                Number number = (Number) o;
                String s = format.format(number);
                if (s.length() <= 9) {
                    sb.append(Integer.toString(s.length())).append(s);
                } else {
                    throw new IllegalArgumentException("number too long");
                }
            } else {
                sb.append(o.toString());
            }
        }
        return sb.toString();
    }

}
