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

import java.io.Closeable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Class to implement a KeyStore to contain key / values
 * <p>
 * Allows casting of values when using "get(String key)"
 */
@SuppressWarnings("unchecked")
public final class KeyValueStore {

    private final Map<String, Object> map;

    /**
     * Constructor
     */
    public KeyValueStore() {
        this.map = new LinkedHashMap<>();
    }

    /**
     * Method to return whether a key exists in the KeyValueStore
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
     * Method to put a key / value into the KeyValueStore
     *
     * @param key key
     * @param object object
     * @return the return value
     */
    public KeyValueStore put(String key, Object object) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("key is null or empty");
        }

        map.put(key, object);
        return this;
    }

    /**
     * Method to add a key / value if it doesn't exist in this KeyValueStore
     *
     * @param key key with which the specified value is to be associated
     * @param object value to be associated with the specified key
     * @return the return value
     */
    public <T> T putIfAbsent(String key, Object object) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("key is null or empty");
        }

        return (T) map.putIfAbsent(key.trim(), object);
    }
    /**
     * Method to get value from the KeyValueStore
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
     * Method to get a value from the KeyValueStore cast to a specific type
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
     * Method to remove a value for a key from the KeyValueStore
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
     * Method to remove a value for a key from the KeyValueStore cast tot a specific type
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
            return null;
        } else {
            return clazz.cast(value);
        }
    }

    /**
     * Method to clear all KeyValueStore entries
     */
    public KeyValueStore clear() {
        map.clear();
        return this;
    }

    /**
     * Method to clear all KeyValueStore entries calling close on any Closeable or Autocloseable
     */
    public void dispose() {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Closeable) {
                try {
                    ((Closeable) value).close();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            } else if (value instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) value).close();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }

        map.clear();
    }

    /**
     * Method to get the KeyValueStore keySet
     *
     * @return the return value
     */
    public Set<String> keySet() {
        return map.keySet();
    }

    /**
     * Method to get the KeyValueStore entrySet
     *
     * @return the return value
     */
    public Set<Map.Entry<String, Object>> entrySet() {
        return map.entrySet();
    }

    @Override
    public String toString() {
        return "KeyValueStore{map=[" + map + "]}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeyValueStore that = (KeyValueStore) o;
        return Objects.equals(map, that.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map);
    }
}