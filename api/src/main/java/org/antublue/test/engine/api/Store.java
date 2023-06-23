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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Class to implement a Store that allows for sharing objects between tests
 */
public class Store {

    private static final Map<String, Object> objectCache;

    static {
        objectCache = new HashMap<>();
    }

    /**
     * Constructor
     */
    private Store() {
        // DO NOTHING
    }

    /**
     * Method to get a named Object
     *
     * @param name name
     * @return the return value
     * @param <T>
     */
    public static <T> T get(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }

        name = name.trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name is empty");
        }

        synchronized (objectCache) {
            return (T) objectCache.get(name);
        }
    }

    /**
     * Method to get a named Object executing the supplied Function to create the Object if it doesn't exist
     *
     * @param name name
     * @param function function
     * @return the return value
     * @param <T>
     */
    public static <T> T getOrCreate(String name, Function<String, ? extends T> function) {
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }

        name = name.trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name is empty");
        }

        synchronized (objectCache) {
            T t = (T) objectCache.get(name);
            if (t == null) {
                t = function.apply(name);
                objectCache.put(name, t);
            }

            return t;
        }
    }

    /**
     * Method to remove a named Object
     *
     * @param name name
     * @return the return value
     * @param <T>
     */
    public static <T> T remove(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }

        name = name.trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name is empty");
        }

        synchronized (objectCache) {
            return (T) objectCache.remove(name);
        }
    }
}
