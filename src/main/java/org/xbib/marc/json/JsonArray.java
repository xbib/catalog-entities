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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Extends an {@link ArrayList} with helper methods to determine the underlying JSON type of the list element.
 */
public class JsonArray extends ArrayList<Object> {

    private static final long serialVersionUID = -114099190105369669L;

    /**
     * Creates an empty {@link JsonArray} with the default capacity.
     */
    public JsonArray() {
        super();
    }

    /**
     * Creates an empty {@link JsonArray} with the default initial capacity.
     * @param initialCapacity initial capacity
     */
    public JsonArray(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Creates an empty {@link JsonArray} from the given collection of objects.
     * @param collection collection
     */
    public JsonArray(Collection<?> collection) {
        super(collection);
    }

    /**
     * Creates a {@link JsonArray} from an array of contents.
     * @param contents contents
     * @return JSON array
     */
    public static JsonArray from(Object... contents) {
        return new JsonArray(Arrays.asList(contents));
    }

    /**
     * Creates a {@link JsonBuilder} for a {@link JsonArray}.
     * @return JSON builder
     */
    public static JsonBuilder<JsonArray> builder() {
        return new JsonBuilder<>(new JsonArray());
    }

    /**
     * Returns the underlying object at the given index, or null if it does not exist.
     */
    @Override
    public Object get(int key) {
        return key < size() ? super.get(key) : null;
    }

    /**
     * Returns the {@link JsonArray} at the given index, or null if it does not exist or is the wrong type.
     * @param key key
     * @return JSON array
     */
    public JsonArray getArray(int key) {
        return getArray(key, null);
    }

    /**
     * Returns the {@link JsonArray} at the given index, or the default if it does not exist or is the wrong type.
     * @param key key
     * @param value value
     * @return JSON array
     */
    public JsonArray getArray(int key, JsonArray value) {
        Object o = get(key);
        if (o instanceof JsonArray) {
            return (JsonArray) get(key);
        }
        return value;
    }

    /**
     * Returns the {@link Boolean} at the given index, or false if it does not exist or is the wrong type.
     * @param key key
     * @return true if boolean value is true
     */
    public boolean getBoolean(int key) {
        return getBoolean(key, false);
    }

    /**
     * Returns the {@link Boolean} at the given index, or the default if it does not exist or is the wrong type.
     * @param key key
     * @param value value
     * @return true if boolean value is true
     */
    public boolean getBoolean(int key, Boolean value) {
        Object o = get(key);
        if (o instanceof Boolean) {
            return (Boolean) o;
        }
        return value;
    }

    /**
     * Returns the {@link Double} at the given index, or 0.0 if it does not exist or is the wrong type.
     * @param key key
     * @return the double value
     */
    public double getDouble(int key) {
        return getDouble(key, 0);
    }

    /**
     * Returns the {@link Double} at the given index, or the default if it does not exist or is the wrong type.
     * @param key key
     * @param value value
     * @return double value
     */
    public double getDouble(int key, double value) {
        Object o = get(key);
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        }
        return value;
    }

    /**
     * Returns the {@link Float} at the given index, or 0.0f if it does not exist or is the wrong type.
     * @param key key
     * @return the float value
     */
    public float getFloat(int key) {
        return getFloat(key, 0);
    }

    /**
     * Returns the {@link Float} at the given index, or the default if it does not exist or is the wrong type.
     * @param key key
     * @param value value
     * @return the float value
     */
    public float getFloat(int key, float value) {
        Object o = get(key);
        if (o instanceof Number) {
            return ((Number) o).floatValue();
        }
        return value;
    }

    /**
     * Returns the {@link Integer} at the given index, or 0 if it does not exist or is the wrong type.
     * @param key key
     * @return the integer value
     */
    public int getInt(int key) {
        return getInt(key, 0);
    }

    /**
     * Returns the {@link Integer} at the given index, or the default if it does not exist or is the wrong type.
     * @param key key
     * @param value value
     * @return the integer value
     */
    public int getInt(int key, int value) {
        Object o = get(key);
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        return value;
    }

    /**
     * Returns the {@link Number} at the given index, or null if it does not exist or is the wrong type.
     * @param key key
     * @return the number
     */
    public Number getNumber(int key) {
        return getNumber(key, null);
    }

    /**
     * Returns the {@link Number} at the given index, or the default if it does not exist or is the wrong type.
     * @param key key
     * @param value value
     * @return the number
     */
    public Number getNumber(int key, Number value) {
        Object o = get(key);
        if (o instanceof Number) {
            return (Number) o;
        }
        return value;
    }

    /**
     * Returns the {@link JsonObject} at the given index, or null if it does not exist or is the wrong type.
     * @param key key
     * @return JSON object
     */
    public JsonObject getObject(int key) {
        return getObject(key, null);
    }

    /**
     * Returns the {@link JsonObject} at the given index, or the default if it does not exist or is the wrong type.
     * @param key key
     * @param value value
     * @return the JSON object
     */
    public JsonObject getObject(int key, JsonObject value) {
        Object o = get(key);
        if (o instanceof JsonObject) {
            return (JsonObject) get(key);
        }
        return value;
    }

    /**
     * Returns the {@link String} at the given index, or null if it does not exist or is the wrong type.
     * @param key key
     * @return the string
     */
    public String getString(int key) {
        return getString(key, null);
    }

    /**
     * Returns the {@link String} at the given index, or the default if it does not exist or is the wrong type.
     * @param key key
     * @param value value
     * @return the string
     */
    public String getString(int key, String value) {
        Object o = get(key);
        if (o instanceof String) {
            return (String) get(key);
        }
        return value;
    }

    /**
     * Returns true if the array has an element at that index (even if that element is null).
     * @param key key
     * @return true if array has element
     */
    public boolean has(int key) {
        return key < size();
    }

    /**
     * Returns true if the array has a boolean element at that index.
     * @param key key
     * @return true if boolean
     */
    public boolean isBoolean(int key) {
        return get(key) instanceof Boolean;
    }

    /**
     * Returns true if the array has a null element at that index.
     * @param key key
     * @return true if null
     */
    public boolean isNull(int key) {
        return key < size() && get(key) == null;
    }

    /**
     * Returns true if the array has a number element at that index.
     * @param key key
     * @return true if number
     */
    public boolean isNumber(int key) {
        return get(key) instanceof Number;
    }

    /**
     * Returns true if the array has a string element at that index.
     * @param key key
     * @return true if string
     */
    public boolean isString(int key) {
        return get(key) instanceof String;
    }
}
