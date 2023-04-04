/*
 * Copyright 2022-2023 Douglas Hoard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.antublue.test.engine.api;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Class to implement a Map to contains values that has casting capabilities
 */
@SuppressWarnings("unchecked")
public class Map extends LinkedHashMap<String, Object> {

    /**
     * Constructor
     */
    public Map() {
        super();
    }

    /**
     * Method to return whether a key exists in the map
     *
     * @param key key whose presence in this map is to be tested
     * @return
     */
    public boolean containsKey(Object key) {
        validateKey(key);
        return super.containsKey(key);
    }

    /**
     * Method to put a key / value into the map
     *
     * @param key
     * @param object
     * @return
     */
    public Map put(String key, Object object) {
        validateKey(key);
        super.put(key, object);
        return this;
    }

    /**
     * Method to merge a map into this map
     *
     * @param map mappings to be stored in this map
     */
    public void putAll(java.util.Map map) {
        Objects.requireNonNull(map);

        Set<java.util.Map.Entry<Object, Object>> set = map.entrySet();
        for (java.util.Map.Entry<Object, Object> entry : set) {
            String key = validateKey(entry.getKey());
            put(key, entry.getValue());
        }
    }

    /**
     * Method to add a key / value if it doesn't exist in this map
     *
     * @param key key with which the specified value is to be associated
     * @param object value to be associated with the specified key
     * @return
     */
    public Object putIfAbsent(String key, Object object) {
        validateKey(key);
        return putIfAbsent(key, object);
    }

    /**
     * Method to merge a map into this map (Unsupported)
     *
     * @param key
     * @param value
     * @param remappingFunction
     * @return
     */
    public Object merge(String key, Object value, BiFunction<Object, Object, Object> remappingFunction) {
        throw new UnsupportedOperationException("Merge is not supported");
    }

    /**
     * Method to get value from the map
     *
     * @param key
     * @return
     * @param <T>
     */
    public <T> T get(String key) {
        Objects.requireNonNull(key);

        if (key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key is empty");
        }

        return (T) super.get(key);
    }

    /**
     * Method to get a value from the map cast to a specific type
     *
     * @param clazz
     * @return
     * @param <T>
     */
    public <T> T get(String key, Class<T> clazz) {
        Object value = get(key);
        if (value == null) {
            return null;
        } else {
            return clazz.cast(value);
        }
    }

    private static String validateKey(Object key) {
        if (key == null) {
            throw new IllegalArgumentException("Key is null");
        }

        if (!(key instanceof String)) {
            throw new IllegalArgumentException("Illegal key type [" + key.getClass().getName() + "] String is required");
        }

        String stringKey = (String) key;
        if (stringKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Key is empty");
        }

        return stringKey;
    }
}
