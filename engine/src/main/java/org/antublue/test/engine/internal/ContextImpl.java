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

package org.antublue.test.engine.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.antublue.test.engine.api.Configuration;
import org.antublue.test.engine.api.Context;
import org.antublue.test.engine.api.Store;
import org.antublue.test.engine.internal.configuration.ConfigurationImpl;

/** Class to implement a Context */
public class ContextImpl implements Context {

    private final Store store;
    private final Map<String, Store> namespacedStores;

    /** Constructor */
    private ContextImpl() {
        store = new StoreImpl(Store.GLOBAL);
        namespacedStores = new ConcurrentHashMap<>();
    }

    /**
     * Method to get the singleton instance
     *
     * @return the singleton instance
     */
    public static Context getInstance() {
        return SingletonHolder.INSTANCE;
    }

    @Override
    public Configuration getConfiguration() {
        return ConfigurationImpl.getInstance();
    }

    /**
     * Method to get the global Store
     *
     * @return the global Store
     */
    @Override
    public Store getStore() {
        return store;
    }

    /**
     * Method to get a namespaced Store. The value Store.GLOBAL will reference the global Store.
     *
     * @param namespace namespace
     * @return the namespaced Store
     */
    @Override
    public Store getStore(Object namespace) {
        checkNotNull(namespace, "namespace is null");

        if (Store.GLOBAL == namespace) {
            return store;
        }

        String validNamespace;
        if (namespace instanceof Class) {
            validNamespace = ((Class<?>) namespace).getName();
        } else {
            validNamespace = checkKey(namespace.toString());
        }

        if (!validNamespace.startsWith("/")) {
            validNamespace = "/" + validNamespace;
        }

        final String finalValidNamespace = validNamespace;

        return namespacedStores.computeIfAbsent(
                validNamespace, s -> new StoreImpl(finalValidNamespace));
    }

    /** Class to hold the singleton instance */
    private static final class SingletonHolder {

        /** The singleton instance */
        private static final ContextImpl INSTANCE = new ContextImpl();
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
}
