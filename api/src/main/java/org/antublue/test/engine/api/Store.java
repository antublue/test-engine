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
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Class to implement a singleton Store
 *
 * <p>Store methods are atomic and thread-safe. Complex usage should lock the Store using either
 * lock() / unlock() or use the Lock returned from getLock()
 *
 * <p>Locking of Objects in the Store is the responsibility of the calling code
 */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class Store {

    private static final Store GLOBAL_STORE = new Store();

    private final Lock lock;
    private final Map<String, Object> map;

    /** Constructor */
    public Store() {
        lock = new ReentrantLock(true);
        map = new LinkedHashMap<>();
    }

    /**
     * Method to lock the Store, returning the Store's Lock
     *
     * @return the Lock
     */
    public Lock lock() {
        lock.lock();
        return lock;
    }

    /**
     * Method to unlock the Store, returning the Store's Lock
     *
     * @return the Store Lock
     */
    public Lock unlock() {
        lock.unlock();
        return lock;
    }

    /**
     * Method to get the Store's Lock
     *
     * @return the Store Lock
     */
    public Lock getLock() {
        return lock;
    }

    /**
     * Method to put an Object into the Store. Accepts a null Object.
     *
     * @param key key
     * @param object object
     * @return an Optional containing the existing Object, or an empty Optional if an Object doesn't
     *     exist
     */
    public Optional<Object> put(String key, Object object) {
        String validKey = validateKey(key);

        try {
            lock();
            return Optional.ofNullable(map.put(validKey, object));
        } finally {
            unlock();
        }
    }

    /**
     * Method to put an Object into the store. If an Object doesn't exist, execute the Function to
     * create an Object and store it
     *
     * @param key key
     * @param function function
     * @return an Optional containing the existing Object, or an Optional containing the Object
     *     returned by the Function
     */
    public Optional<Object> putIfAbsent(String key, Function<String, Object> function) {
        String validKey = validateKey(key);
        validateObject(function, "function is null");

        try {
            lock();
            return Optional.ofNullable(map.computeIfAbsent(validKey, function));
        } finally {
            unlock();
        }
    }

    /**
     * Method to get an Object from the Store
     *
     * @param key key
     * @return an Optional containing the existing Object, or an empty Optional if an Object doesn't
     *     exist
     */
    public Optional<Object> get(String key) {
        String validKey = validateKey(key);

        try {
            lock();
            return Optional.ofNullable(map.get(validKey));
        } finally {
            unlock();
        }
    }

    /**
     * Method to get an Object from the Store cast to a specific type
     *
     * @param key key
     * @param clazz clazz
     * @return an Optional containing the existing Object, or an empty Optional if an Object doesn't
     *     exist
     * @param <T> the return type
     */
    public <T> Optional<T> get(String key, Class<T> clazz) {
        String validKey = validateKey(key);
        validateObject(clazz, "class is null");

        try {
            lock();
            return Optional.ofNullable(clazz.cast(map.get(validKey)));
        } finally {
            unlock();
        }
    }

    /**
     * Method to remove an Object from the Store, closing the Object if it implements AutoClosable
     *
     * @param key key
     * @return an Optional containing the existing Object, or an empty Optional if an Object doesn't
     *     exist
     */
    public Optional<Object> remove(String key) {
        try {
            lock();
            return Optional.ofNullable(map.remove(key));
        } finally {
            unlock();
        }
    }

    /**
     * @param key key
     * @param clazz clazz
     * @return an Optional containing the existing Object, or an empty Optional if an Object doesn't
     *     exist
     * @param <T> the return type
     */
    public <T> Optional<T> remove(String key, Class<T> clazz) {
        try {
            lock();
            return Optional.ofNullable(clazz.cast(map.remove(key)));
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
    public void removeAndClose(String key) throws StoreException {
        remove(key)
                .ifPresent(
                        o -> {
                            if (o instanceof AutoCloseable) {
                                try {
                                    ((AutoCloseable) o).close();
                                } catch (Throwable t) {
                                    throw new StoreException(
                                            String.format(
                                                    "Exception closing Object for key [%s] object"
                                                            + " [%s]",
                                                    key, o.getClass().getName()),
                                            t);
                                }
                            }
                        });
    }

    public void removeAndProcess(String key, Consumer<Object> consumer) {
        AtomicReference<StoreException> storeExceptionAtomicReference = new AtomicReference<>();

        remove(key)
                .ifPresent(
                        new Consumer<Object>() {

                            /**
                             * Performs this operation on the given argument.
                             *
                             * @param o the input argument
                             */
                            @Override
                            public void accept(Object o) {
                                try {
                                    consumer.accept(o);
                                } catch (Throwable t) {
                                    storeExceptionAtomicReference.set(
                                            new StoreException(
                                                    String.format(
                                                            "Exception closing Object for key [%s]"
                                                                    + " object [%s]",
                                                            key, o.getClass().getName()),
                                                    t));
                                }
                            }
                        });

        StoreException storeException = storeExceptionAtomicReference.get();
        if (storeException != null) {
            throw storeException;
        }
    }

    /**
     * Method to validate a key is not null and not blank
     *
     * @param key key
     * @return the key trimmed
     */
    private String validateKey(String key) {
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
    private void validateObject(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /** Method to get a singleton Store */
    public static Store singleton() {
        return GLOBAL_STORE;
    }
}
