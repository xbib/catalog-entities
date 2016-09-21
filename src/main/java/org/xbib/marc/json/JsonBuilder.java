/*
   Copyright 2016 Jörg Prante

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.xbib.marc.json;

import java.io.IOException;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;

/**
 * Builds a {@link JsonObject} or {@link JsonArray}.
 *
 * @param <T> The type of JSON object to build.
 */
public final class JsonBuilder<T> {
    private Deque<Object> json = new LinkedList<>();
    private T root;

    JsonBuilder(T root) {
        this.root = root;
        json.push(root);
    }

    /**
     * Completes this builder, closing any unclosed objects and returns the built object.
     * @return JSON class
     */
    public T done() {
        return root;
    }

    public JsonBuilder<T> array(Collection<?> c) {
        return value(c);
    }

    public JsonBuilder<T> array(String key, Collection<?> c) {
        return value(key, c);
    }

    public JsonBuilder<T> object(Map<?, ?> map) {
        return value(map);
    }

    public JsonBuilder<T> object(String key, Map<?, ?> map) {
        return value(key, map);
    }

    public JsonBuilder<T> nul() {
        return value((Object) null);
    }

    public JsonBuilder<T> nul(String key) {
        return value(key, (Object) null);
    }

    public JsonBuilder<T> value(Object o) {
        arr().add(o);
        return this;
    }

    public JsonBuilder<T> value(String key, Object o) {
        obj().put(key, o);
        return this;
    }

    public JsonBuilder<T> value(String s) {
        arr().add(s);
        return this;
    }

    public JsonBuilder<T> value(int i) {
        return value((Object) i);
    }

    public JsonBuilder<T> value(long l) {
        return value((Object) l);
    }

    public JsonBuilder<T> value(boolean b) {
        return value((Object) b);
    }

    public JsonBuilder<T> value(double d) {
        return value((Object) d);
    }

    public JsonBuilder<T> value(float f) {
        return value((Object) f);
    }

    public JsonBuilder<T> value(Number n) {
        return value((Object) n);
    }

    public JsonBuilder<T> value(String key, String s) {
        return value(key, (Object) s);
    }

    public JsonBuilder<T> value(String key, int i) {
        return value(key, (Object) i);
    }

    public JsonBuilder<T> value(String key, long l) {
        return value(key, (Object) l);
    }

    public JsonBuilder<T> value(String key, boolean b) {
        return value(key, (Object) b);
    }

    public JsonBuilder<T> value(String key, double d) {
        return value(key, (Object) d);
    }

    public JsonBuilder<T> value(String key, float f) {
        return value(key, (Object) f);
    }

    public JsonBuilder<T> value(String key, Number n) {
        return value(key, (Object) n);
    }

    public JsonBuilder<T> array() {
        JsonArray a = new JsonArray();
        value(a);
        json.push(a);
        return this;
    }

    public JsonBuilder<T> object() {
        JsonObject o = new JsonObject();
        value(o);
        json.push(o);
        return this;
    }

    public JsonBuilder<T> array(String key) {
        JsonArray a = new JsonArray();
        value(key, a);
        json.push(a);
        return this;
    }

    public JsonBuilder<T> object(String key) {
        JsonObject o = new JsonObject();
        value(key, o);
        json.push(o);
        return this;
    }

    public JsonBuilder<T> end() throws IOException {
        if (json.size() == 1) {
            throw new IOException("Cannot end the root object or array");
        }
        json.pop();
        return this;
    }

    private JsonObject obj() {
        return (JsonObject) json.peek();
    }

    private JsonArray arr() {
        return (JsonArray) json.peek();
    }
}
