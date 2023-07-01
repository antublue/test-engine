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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * Class to implement a Store that allows for sharing named Objects between tests
 */
@SuppressWarnings("unchecked")
public class Store {

    private static final ReentrantLock LOCK = new ReentrantLock(true);
    private static final Map<String, Object> OBJECT_MAP = new LinkedHashMap<>();

    /**
     * Constructor
     */
    private Store() {
        // DO NOTHING
    }

    /**
     * Method to get the Store Lock
     * <p>
     * By using the Store Lock, you can call multiple Store methods atomically
     *
     * @return
     */
    public static ReentrantLock getLock() {
        return LOCK;
    }

    /**
     * Method to put a named Object into the Store
     * <p>
     * null values are accepted
     *
     * @param name
     * @param value
     */
    public static void put(String name, Object value) {
        name = validate(name);

        try {
            LOCK.lock();
            OBJECT_MAP.put(name, value);
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Method to get a named Object
     *
     * @param name name
     * @return the return value
     * @param <T>
     */
    public static <T> Optional<T> get(String name) {
        name = validate(name);

        try {
            LOCK.lock();
            return Optional.ofNullable((T) OBJECT_MAP.get(name));
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Method to get a named Object cast to a specific type
     *
     * @param name name
     * @param clazz clazz
     * @return the return value
     * @param <T>
     */
    public static <T> Optional<T> get(String name, Class<T> clazz) {
        name = validate(name);

        if (clazz == null) {
            throw new IllegalArgumentException("clazz is null");
        }

        try {
            LOCK.lock();
            return Optional.ofNullable(clazz.cast(OBJECT_MAP.get(name)));
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Method to get a named Object, executing the supplied Function if the named Object doesn't exist
     *
     * @param name name
     * @param function function
     * @return the return value
     * @param <T>
     */
    public static <T> T computeIfAbsent(String name, Function<String, T> function) {
        name = validate(name);

        if (function == null) {
            throw new IllegalArgumentException("function is null");
        }

        try {
            LOCK.lock();
            return (T) OBJECT_MAP.computeIfAbsent(name, function);
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Method to remove a named Object, returning the value is one exists
     * <p>
     * Be cautious using remove when sharing an Object between classes
     *
     * @param name name
     * @return the return value
     * @param <T>
     */
    public static <T> Optional<T> remove(String name) {
        name = validate(name);

        try {
            LOCK.lock();
            return Optional.ofNullable((T) OBJECT_MAP.remove(name));
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Method to get a keySet of Store keys
     * <p>
     * The keySet will be a copy
     *
     * @return the return value
     */
    public static Set<String> keySet() {
        try {
            LOCK.lock();
            return new LinkedHashSet<>(OBJECT_MAP.keySet());
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Method to valide a name is not null and not empty, returning the name trimmed
     *
     * @param name name
     * @return the return value
     */
    private static String validate(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }

        name = name.trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name is empty");
        }

        return name;
    }
}
