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
 * Store methods are atomic and thread-safe. Complex usage should lock the Store
 * using either lock() / unlock() or use the Lock returned from getLock()
 * <p>
 * Locking of Objects in the Store is the responsibility of the calling code
 */
@SuppressWarnings("PMD.EmptyCatchBlock")
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
     * Method to lock the Store, returning the Store's Lock
     *
     * @return the Lock
     */
    public static Lock lock() {
        LOCK.lock();
        return LOCK;
    }

    /**
     * Method to unlock the Store, returning the Store's Lock
     *
     * @return the Store Lock
     */
    public static Lock unlock() {
        LOCK.unlock();
        return LOCK;
    }

    /**
     * Method to get the Store's Lock
     *
     * @return the Store Lock
     */
    public static Lock getLock() {
        return LOCK;
    }

    /**
     * Method to put an Object into the Store. Accepts a null Object.
     *
     * @param key key
     * @param object object
     * @return an Optional containing the existing Object, or an empty Optional if an Object doesn't exist
     */
    public static Optional<Object> put(String key, Object object) {
        String validKey = validateKey(key);

        try {
            lock();
            return Optional.ofNullable(MAP.put(validKey, object));
        } finally {
            unlock();
        }
    }

    /**
     * Method to put an Object into the store. If an Object doesn't exist, execute the Function to create an Object and store it
     *
     * @param key key
     * @param function function
     * @return an Optional containing the existing Object, or an Optional containing the Object returned by the Function
     */
    public static Optional<Object> putIfAbsent(String key, Function<String, Object> function) {
        String validKey = validateKey(key);
        validateObject(function, "function is null");

        try {
            lock();
            return Optional.ofNullable(MAP.computeIfAbsent(validKey, function));
        } finally {
            unlock();
        }
    }

    /**
     * Method to get an Object from the Store
     *
     * @param key key
     * @return an Optional containing the existing Object, or an empty Optional if an Object doesn't exist
     */
    public static Optional<Object> get(String key) {
        String validKey = validateKey(key);

        try {
            lock();
            return Optional.ofNullable(MAP.get(validKey));
        } finally {
            unlock();
        }
    }

    /**
     * Method to get an Object from the Store cast to a specific type
     *
     * @param key key
     * @param clazz clazz
     * @return an Optional containing the existing Object, or an empty Optional if an Object doesn't exist
     * @param <T> the return type
     */
    public static <T> Optional<T> get(String key, Class<T> clazz) {
        String validKey = validateKey(key);
        validateObject(clazz, "class is null");

        try {
            lock();
            return Optional.ofNullable(clazz.cast(MAP.get(validKey)));
        } finally {
            unlock();
        }
    }

    /**
     * Method to remove an Object from the Store, closing the Object if it implements AutoClosable
     *
     * @param key key
     * @return an Optional containing the existing Object, or an empty Optional if an Object doesn't exist
     */
    public static Optional<Object> remove(String key) {
        try {
            lock();
            return Optional.ofNullable(MAP.remove(key));
        } finally {
            unlock();
        }
    }

    /**
     *
     * @param key key
     * @param clazz clazz
     * @return an Optional containing the existing Object, or an empty Optional if an Object doesn't exist
     * @param <T> the return type
     */
    public static <T> Optional<T> remove(String key, Class<T> clazz) {
        try {
            lock();
            return Optional.ofNullable(clazz.cast(MAP.remove(key)));
        } finally {
            unlock();
        }
    }

    /**
     * Method to remove an Object from the Store, closing it if it's an instance of AutoCloseable
     *
     * @param key key
     * @throws StoreException exception if the AutoCloseable throws an Exception
     */
    public static void removeAndClose(String key) throws StoreException {
        remove(key).ifPresent(o -> {
            if (o instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) o).close();
                } catch (Throwable t) {
                    throw new StoreException(
                            String.format(
                                    "Exception closing Object for key [%s] object [%s]",
                                    key,
                                    o.getClass().getName()),
                            t);
                }
            }
        });
    }

    /**
     * Method to validate a key is not null and not blank
     *
     * @param key key
     * @return the key trimmed
     */
    private static String validateKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }

        if (key.trim().isEmpty()) {
            throw new IllegalArgumentException("key is empty");
        }

        return key.trim();
    }

    /**
     * Method to validate an Object is not null
     *
     * @param object object
     * @param message message
     */
    private static void validateObject(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
