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

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Class to implement a Store
 *
 * <p>Store methods are atomic and thread-safe. Complex usage should lock the Store using either
 * lock() / unlock() or use the Lock returned from getLock()
 *
 * <p>Locking of Objects in the Store is the responsibility of the calling code
 */
@SuppressWarnings({"unchecked", "PMD.EmptyCatchBlock"})
@Deprecated
public final class Store {

    private final org.antublue.test.engine.api.util.Store store;

    /** Constructor */
    public Store() {
        store = new org.antublue.test.engine.api.util.Store();
    }

    /**
     * Method to get the singleton instance
     *
     * @return the singleton instance
     */
    @Deprecated
    public static Store singleton() {
        return getSingleton();
    }

    /**
     * Method to get the singleton instance
     *
     * @return the singleton instance
     */
    @Deprecated
    public static Store getSingleton() {
        return getInstance();
    }

    /**
     * Method to get the singleton instance
     *
     * @return the singleton instance
     */
    public static Store getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Method to lock the Store, returning the Store's Lock
     *
     * @return the Lock
     */
    public Lock lock() {
        return store.lock();
    }

    /**
     * Method to unlock the Store, returning the Store's Lock
     *
     * @return the Store Lock
     */
    public Lock unlock() {
        return store.unlock();
    }

    /**
     * Method to get the Store's Lock
     *
     * @return the Store Lock
     */
    public Lock getLock() {
        return store.getLock();
    }

    /**
     * Method to get the Store's keys. Keys that start with "." are considered hidden and will not
     * be returned.
     *
     * @return the Store keys
     */
    public Set<String> keySet() {
        return store.keySet();
    }

    /**
     * Method to put a value into the Store. Accepts a null value
     *
     * @param key key
     * @param value value
     * @return an Optional containing the value for the key, or an empty Optional if a value doesn't
     *     exist for the key
     */
    public Optional<Object> put(String key, Object value) {
        return store.put(key, value);
    }

    /**
     * Method to put a value into the store. If a value doesn't exist, execute the function to
     * create a value and add it
     *
     * @param key key
     * @param function function
     * @return an Optional containing the existing Object, or an Optional containing the Object
     *     returned by the Function
     */
    public Optional<Object> putIfAbsent(String key, Function<String, Object> function) {
        return store.putIfAbsent(key, function);
    }

    /**
     * Method to get a value from the store
     *
     * @param key key
     * @return an Optional containing the existing Object, or an empty Optional if an Object doesn't
     *     exist
     */
    public Optional<Object> get(String key) {
        return store.get(key);
    }

    /**
     * Method to get a value from the store, calling a function to transform it
     *
     * @param key key
     * @param function function
     * @return on Optional containing the value returned by the function
     * @param <T> the return type
     */
    public <T> Optional<T> get(String key, Function<Object, T> function) {
        return store.get(key, function);
    }

    /**
     * Method to get a value from the store, casting it to a specific type
     *
     * @param key key
     * @param clazz clazz
     * @return an Optional containing the existing value, or an empty Optional if a value doesn't
     *     exist
     * @param <T> the return type
     */
    public <T> Optional<T> get(String key, Class<T> clazz) {
        return store.get(key, clazz);
    }

    /**
     * Method to remove a value from the store, closing the value if it implements AutoClosable
     *
     * @param key key
     * @return an Optional containing the existing Object, or an empty Optional if an Object doesn't
     *     exist
     */
    public Optional<Object> remove(String key) {
        return store.remove(key);
    }

    /**
     * Method to remove a value from the store
     *
     * @param key key
     * @param clazz clazz
     * @return an Optional containing the existing value, or an empty Optional if a value doesn't
     *     exist
     * @param <T> the return type
     */
    public <T> Optional<T> remove(String key, Class<T> clazz) {
        return store.remove(key, clazz);
    }

    /**
     * Method to remove a value from the store, calling the Consumer if a value exists
     *
     * @param key key
     * @param consumer consumer
     * @param <T> the consumer type
     */
    public <T> void remove(String key, Consumer<T> consumer) {
        store.remove(key, consumer);
    }

    /**
     * Method to remove a value from the store, closing it if it exists and is an instance of
     * AutoCloseable
     *
     * @param key key
     * @throws StoreException exception if AutoCloseable close throws an Exception
     */
    public void removeAndClose(String key) throws StoreException {
        store.removeAndClose(key);
    }

    /** Class to hold the singleton instance */
    private static final class SingletonHolder {

        /** The singleton instance */
        private static final Store INSTANCE = new Store();
    }
}
