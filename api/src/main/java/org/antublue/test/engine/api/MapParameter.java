/*
 * Copyright (C) 2023 The AntuBLUE test-engine project authors
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
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Class to implement a Map like object to contain key / values
 * <p>
 * Allows casting of values when using "get(String key)"
 */
@SuppressWarnings("unchecked")
public final class MapParameter implements Parameter {

    private final String name;
    private final Map<String, Object> map;

    /**
     * Constructor
     */
    private MapParameter(String name) {
        this.name = name;
        this.map = new LinkedHashMap<>();
    }

    /**
     * Method to get the name
     *
     * @return the return value
     */
    public String name() {
        return name;
    }

    /**
     * Method to return whether a key exists in the MapParameter
     *
     * @param key key
     * @return the return value
     */
    public boolean containsKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("key is null or empty");
        }

        return map.containsKey(key);
    }

    /**
     * Method to put a key / value into the MapParameter
     *
     * @param key key
     * @param object object
     * @return the return value
     */
    public MapParameter put(String key, Object object) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("key is null or empty");
        }

        map.put(key, object);
        return this;
    }

    /**
     * Method to add a key / value if it doesn't exist in this MapParameter
     *
     * @param key key with which the specified value is to be associated
     * @param object value to be associated with the specified key
     * @return the return value
     */
    public <T> T putIfAbsent(String key, Object object) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("key is null or empty");
        }

        return (T) putIfAbsent(key.trim(), object);
    }
    /**
     * Method to get value from the MapParameter
     *
     * @param key key
     * @return the return value
     * @param <T> the return type
     */
    public <T> T get(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("key is null or empty");
        }

        return (T) map.get(key.trim());
    }

    /**
     * Method to get a value from the MapParameter cast to a specific type
     *
     * @param key key
     * @param clazz clazz
     * @return the return value
     * @param <T> the return type
     */
    public <T> T get(String key, Class<T> clazz) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("key is null or empty");
        }

        Object value = get(key);
        if (value == null) {
            return null;
        } else {
            return clazz.cast(value);
        }
    }

    /**
     * Method to remove a value for a key from the MapParameter
     *
     * @param key key
     * @return the return value
     * @param <T> the return type
     */
    public <T> T remove(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("key is null or empty");
        }

        return (T) map.remove(key);
    }

    /**
     * Method to remove a value for a key from the MapParameter cast tot a specific type
     *
     * @param key key
     * @param clazz clazz
     * @return the return value
     * @param <T> the return type
     */
    public <T> T remove(String key, Class<T> clazz) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("key is null or empty");
        }

        Object value = map.remove(key);
        if (value == null) {
            return (T) null;
        } else {
            return clazz.cast(value);
        }
    }

    /**
     * Method to clear all MapParameter key / values
     */
    public MapParameter clear() {
        map.clear();
        return this;
    }

    /**
     * Method to get the MapParameter keySet
     *
     * @return the return value
     */
    public Set<String> keySet() {
        return map.keySet();
    }

    /**
     * Method to get the MapParameter entrySet
     *
     * @return the return value
     */
    public Set<Map.Entry<String, Object>> entrySet() {
        return map.entrySet();
    }

    @Override
    public String toString() {
        return map.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapParameter that = (MapParameter) o;
        return Objects.equals(name, that.name) && Objects.equals(map, that.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, map);
    }

    public static MapParameter named(String name) {
        return new MapParameter(name);
    }
}