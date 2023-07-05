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
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * Class to implement a singleton Store
 * <p>
 * Store methods are atomic and thread-safe. Complex usage should lock the Store using either lock() / unlock() or use the Lock returned from getLock().
 * <p>
 * Locking of Objects in the Store is the responsibility of the caller.
 */
public class Store {

    private final static Lock LOCK = new ReentrantLock(true);
    private final static Map<String, Object> MAP = new LinkedHashMap<>();

    /**
     * Constructor
     */
    private Store() {
        // DO NOTHING
    }

    /**
     * Method to lock the Store, returning the Lock
     *
     * @return the Lock
     */
    public static Lock lock() {
        LOCK.lock();
        return LOCK;
    }

    /**
     * Method to unlock the Store, returning the Lock
     *
     * @return the Lock
     */
    public static Lock unlock() {
        LOCK.unlock();
        return LOCK;
    }

    /**
     * Method to get the Store's Lock
     *
     * @return the Lock
     */
    public static Lock getLock() {
        return LOCK;
    }

    /**
     * Method to put an Object into the Store
     *
     * @param key key
     * @param object object
     * @return an Optional containing the existing Object, or an empty Optional if an Object didn't exist
     */
    public static Optional<Object> put(String key, Object object) {
        try {
            lock();
            return Optional.ofNullable(MAP.put(key, object));
        } finally {
            unlock();
        }
    }

    /**
     * Method to put an Object into the store if it's not present
     *
     * @param key key
     * @param function function
     * @return an Optional containing the existing Object, or an empty Optional if an Object didn't exist
     */
    public static Optional<Object> putIfAbsent(String key, Function<String, Object> function) {
        try {
            lock();
            return Optional.ofNullable(MAP.computeIfAbsent(key, function));
        } finally {
            unlock();
        }
    }

    /**
     * Method to get an Object from the Store
     *
     * @param key key
     * @return an Optional that contains the Object
     */
    public static Optional<Object> get(String key) {
        try {
            lock();
            return Optional.ofNullable(MAP.get(key));
        } finally {
            unlock();
        }
    }

    /**
     * Method to get an Object from the Store
     *
     * @param key key
     * @param clazz clazz
     * @return an Optional that contains the Object
     * @param <T>
     */
    public static <T> Optional<T> get(String key, Class<T> clazz) {
        try {
            lock();
            return Optional.ofNullable(clazz.cast(MAP.get(key)));
        } finally {
            unlock();
        }
    }
}
