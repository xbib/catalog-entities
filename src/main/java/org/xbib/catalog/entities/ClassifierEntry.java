package org.xbib.catalog.entities;

/**
 *
 */
public class ClassifierEntry {
    String doc;
    String code;
    String text;

    ClassifierEntry(String doc, String code, String text) {
        this.doc = doc;
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return code;
    }

    public String getText() {
        return text;
    }
}
