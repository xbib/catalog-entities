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

import java.util.HashMap;
import java.util.Map;

/**
 * Extends a {@link HashMap} with helper methods to determine the underlying JSON type of the map element.
 */
public class JsonObject extends HashMap<String, Object> {

    private static final long serialVersionUID = 625227776070102674L;

    /**
     * Creates an empty {@link JsonObject} with the default capacity.
     */
    public JsonObject() {
        super();
    }

    /**
     * Creates a {@link JsonObject} from an existing {@link Map}.
     * @param map map
     */
    public JsonObject(Map<? extends String, ? extends Object> map) {
        super(map);
    }

    /**
     * Creates a {@link JsonObject} with the given initial capacity.
     * @param initialCapacity initial capacity
     */
    public JsonObject(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Creates a {@link JsonObject} with the given initial capacity and load factor.
     * @param initialCapacity  initial capacity
     * @param loadFactor load factor
     */
    public JsonObject(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * Creates a {@link JsonBuilder} for a {@link JsonObject}.
     * @return JSON builder
     */
    public static JsonBuilder<JsonObject> builder() {
        return new JsonBuilder<>(new JsonObject());
    }

    /**
     * Returns the {@link JsonArray} at the given key, or null if it does not exist or is the wrong type.
     * @param key key
     * @return JSON array
     */
    public JsonArray getArray(String key) {
        return getArray(key, null);
    }

    /**
     * Returns the {@link JsonArray} at the given key, or the default if it does not exist or is the wrong type.
     * @param key key
     * @param value value
     * @return JSON array
     */
    public JsonArray getArray(String key, JsonArray value) {
        Object o = get(key);
        if (o instanceof JsonArray) {
            return (JsonArray) get(key);
        }
        return value;
    }

    /**
     * Returns the {@link Boolean} at the given key, or false if it does not exist or is the wrong type.
     * @param key key
     * @return boolean
     */
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    /**
     * Returns the {@link Boolean} at the given key, or the default if it does not exist or is the wrong type.
     * @param key key
     * @param value value
     * @return boolean
     */
    public boolean getBoolean(String key, Boolean value) {
        Object o = get(key);
        if (o instanceof Boolean) {
            return (Boolean) o;
        }
        return value;
    }

    /**
     * Returns the {@link Double} at the given key, or 0.0 if it does not exist or is the wrong type.
     * @param key key
     * @return double
     */
    public double getDouble(String key) {
        return getDouble(key, 0);
    }

    /**
     * Returns the {@link Double} at the given key, or the default if it does not exist or is the wrong type.
     * @param key key
     * @param value value
     * @return double
     */
    public double getDouble(String key, double value) {
        Object o = get(key);
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        }
        return value;
    }

    /**
     * Returns the {@link Float} at the given key, or 0.0f if it does not exist or is the wrong type.
     * @param key key
     * @return float
     */
    public float getFloat(String key) {
        return getFloat(key, 0);
    }

    /**
     * Returns the {@link Float} at the given key, or the default if it does not exist or is the wrong type.
     * @param key key
     * @param value value
     * @return float
     */
    public float getFloat(String key, float value) {
        Object o = get(key);
        if (o instanceof Number) {
            return ((Number) o).floatValue();
        }
        return value;
    }

    /**
     * Returns the {@link Integer} at the given key, or 0 if it does not exist or is the wrong type.
     * @param key key
     * @return integer
     */
    public int getInt(String key) {
        return getInt(key, 0);
    }

    /**
     * Returns the {@link Integer} at the given key, or the default if it does not exist or is the wrong type.
     * @param key key
     * @param value value
     * @return integer
     */
    public int getInt(String key, int value) {
        Object o = get(key);
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        return value;
    }

    /**
     * Returns the {@link Number} at the given key, or null if it does not exist or is the wrong type.
     * @param key key
     * @return number
     */
    public Number getNumber(String key) {
        return getNumber(key, null);
    }

    /**
     * Returns the {@link Number} at the given key, or the default if it does not exist or is the wrong type.
     * @param key key
     * @param value  value
     * @return number
     */
    public Number getNumber(String key, Number value) {
        Object o = get(key);
        if (o instanceof Number) {
            return (Number) o;
        }
        return value;
    }

    /**
     * Returns the {@link JsonObject} at the given key, or null if it does not exist or is the wrong type.
     * @param key key
     * @return JSON object
     */
    public JsonObject getObject(String key) {
        return getObject(key, null);
    }

    /**
     * Returns the {@link JsonObject} at the given key, or the default if it does not exist or is the wrong type.
     * @param key key
     * @param value value
     * @return JSON object
     */
    public JsonObject getObject(String key, JsonObject value) {
        Object o = get(key);
        if (o instanceof JsonObject) {
            return (JsonObject) get(key);
        }
        return value;
    }

    /**
     * Returns the {@link String} at the given key, or null if it does not exist or is the wrong type.
     * @param key key
     * @return string
     */
    public String getString(String key) {
        return getString(key, null);
    }

    /**
     * Returns the {@link String} at the given key, or the default if it does not exist or is the wrong type.
     * @param key key
     * @param value value
     * @return string
     */
    public String getString(String key, String value) {
        Object o = get(key);
        if (o instanceof String) {
            return (String) get(key);
        }
        return value;
    }

    /**
     * Returns true if the object has an element at that key (even if that element is null).
     * @param key key
     * @return true if object has element
     */
    public boolean has(String key) {
        return super.containsKey(key);
    }

    /**
     * Returns true if the object has a boolean element at that key.
     * @param key key
     * @return true if boolean
     */
    public boolean isBoolean(String key) {
        return get(key) instanceof Boolean;
    }

    /**
     * Returns true if the object has a null element at that key.
     * @param key key
     * @return true if null
     */
    public boolean isNull(String key) {
        return super.containsKey(key) && get(key) == null;
    }

    /**
     * Returns true if the object has a number element at that key.
     * @param key key
     * @return true if number
     */
    public boolean isNumber(String key) {
        return get(key) instanceof Number;
    }

    /**
     * Returns true if the object has a string element at that key.
     * @param key key
     * @return true if string
     */
    public boolean isString(String key) {
        return get(key) instanceof String;
    }
}
