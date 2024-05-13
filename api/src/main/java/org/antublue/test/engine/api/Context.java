/*
 * Copyright (C) 2024 The AntuBLUE test-engine project authors
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

import static java.lang.String.format;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class Context {

    private Store store;
    private Map<String, Store> namespacedStores;

    private Context() {
        store = new StoreImpl();
        namespacedStores = new HashMap<>();
    }

    public static Context getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public Store getStore() {
        return store;
    }

    public Store getStore(Object namespace) {
        checkNotNull(namespace, "namespace is null");

        String validNamespace;
        if (namespace instanceof Class) {
            validNamespace = ((Class<?>) namespace).getName();
        } else {
            validNamespace = checkKey(namespace.toString());
        }

        synchronized (namespacedStores) {
            Store store = namespacedStores.get(validNamespace);
            if (store == null) {
                store = new StoreImpl();
                namespacedStores.put(validNamespace, store);
            }
        }
        return store;
    }

    /** Class to hold the singleton instance */
    private static final class SingletonHolder {

        /** The singleton instance */
        private static final Context INSTANCE = new Context();
    }

    /**
     * Method to validate a key is not null and not blank
     *
     * @param key key
     * @return the key trimmed
     */
    private static String checkKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }

        if (key.trim().isEmpty()) {
            throw new IllegalArgumentException("key is empty");
        }

        return key.trim();
    }

    /**
     * Method to validate a value is not null
     *
     * @param object object
     * @param message message
     */
    private static void checkNotNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Class to implement a Store
     *
     * <p>Store methods are atomic and thread-safe. Complex usage should lock the Store using either
     * lock() / unlock() or use the Lock returned from getLock()
     *
     * <p>Locking of Objects in the Store is the responsibility of the calling code
     */
    @SuppressWarnings({"unchecked", "PMD.EmptyCatchBlock", "UnusedMethod"})
    private static final class StoreImpl implements Store {

        private final Lock lock;
        private final Map<String, Object> map;

        /** Constructor */
        private StoreImpl() {
            lock = new ReentrantLock(true);
            map = new LinkedHashMap<>();
        }

        /**
         * Method to lock the Store, returning the Store's Lock
         *
         * @return the Lock
         */
        @Override
        public Lock lock() {
            lock.lock();
            return lock;
        }

        /**
         * Method to unlock the Store, returning the Store's Lock
         *
         * @return the Store Lock
         */
        @Override
        public Lock unlock() {
            lock.unlock();
            return lock;
        }

        /**
         * Method to get the Store's Lock
         *
         * @return the Store Lock
         */
        @Override
        public Lock getLock() {
            return lock;
        }

        /**
         * Method to get the Store's keys. Keys that start with "." are considered hidden and will
         * not be returned.
         *
         * @return the Store keys
         */
        @Override
        public Set<String> keySet() {
            try {
                lock();
                Set<String> treeSet = new TreeSet<>();
                for (String key : map.keySet()) {
                    if (!key.startsWith(".")) {
                        treeSet.add(key);
                    }
                }
                return treeSet;
            } finally {
                unlock();
            }
        }

        /**
         * Method to put a value into the Store. Accepts a null value
         *
         * @param key key
         * @param value value
         * @return the existing value
         */
        @Override
        public Object put(String key, Object value) {
            String validKey = checkKey(key);

            try {
                lock();
                return map.put(validKey, value);
            } finally {
                unlock();
            }
        }

        /**
         * Method to put a value into the store. If a value doesn't exist, execute the function to
         * create a value and add it
         *
         * @param key key
         * @param function function
         * @return the existing value, if not found, the Object returned by the Function
         */
        @Override
        public Object putIfAbsent(String key, Function<String, Object> function) {
            String validKey = checkKey(key);
            checkNotNull(function, "function is null");

            try {
                lock();
                return map.computeIfAbsent(validKey, function);
            } finally {
                unlock();
            }
        }

        /**
         * Method to get a value from the store
         *
         * @param key key
         * @return an Optional containing the existing Object, or an empty Optional if an Object
         *     doesn't exist
         */
        @Override
        public Object get(String key) {
            String validKey = checkKey(key);

            try {
                lock();
                return map.get(validKey);
            } finally {
                unlock();
            }
        }

        /**
         * Method to get a value from the store, casting it to a specific type
         *
         * @param key key
         * @param clazz clazz
         * @param <T> the return type
         * @return an Optional containing the existing value, or an empty Optional if a value
         *     doesn't exist
         */
        @Override
        public <T> T get(String key, Class<T> clazz) {
            String validKey = checkKey(key);
            checkNotNull(clazz, "class is null");

            try {
                lock();
                Object object = map.get(validKey);
                if (clazz.isInstance(object)) {
                    return clazz.cast(object);
                } else {
                    throw new StoreException(
                            format(
                                    "Object [%s] can't be cast to [%s]",
                                    object.getClass().getName(), clazz.getName()));
                }
            } finally {
                unlock();
            }
        }

        /**
         * Method to remove a value from the store, closing the value if it implements AutoClosable
         *
         * @param key key
         * @return an Optional containing the existing Object, or an empty Optional if an Object
         *     doesn't exist
         */
        @Override
        public Object remove(String key) {
            String validKey = checkKey(key);

            try {
                lock();
                return map.remove(validKey);
            } finally {
                unlock();
            }
        }

        /**
         * Method to remove a value from the store
         *
         * @param key key
         * @param clazz clazz
         * @param <T> the return type
         * @return an Optional containing the existing value, or an empty Optional if a value
         *     doesn't exist
         */
        @Override
        public <T> T remove(String key, Class<T> clazz) {
            String validKey = checkKey(key);
            checkNotNull(clazz, "class is null");

            try {
                lock();
                Object object = map.remove(validKey);
                if (clazz.isInstance(object)) {
                    return clazz.cast(object);
                } else {
                    throw new StoreException(
                            format(
                                    "Object [%s] can't be cast to [%s]",
                                    object.getClass().getName(), clazz.getName()));
                }
            } finally {
                unlock();
            }
        }
    }

    /** Class to implement a generic TestEngineException */
    public static class StoreException extends RuntimeException {

        /**
         * Constructor
         *
         * @param message message
         */
        public StoreException(String message) {
            super(message);
        }

        /**
         * Constructor
         *
         * @param message message
         * @param throwable throwable
         */
        public StoreException(String message, Throwable throwable) {
            super(message, throwable);
        }

        /**
         * Constructor
         *
         * @param throwable throwable
         */
        public StoreException(Throwable throwable) {
            super(throwable);
        }
    }
}
