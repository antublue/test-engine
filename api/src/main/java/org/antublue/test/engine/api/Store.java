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

import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;

/** Interface to implement a Store */
public interface Store {

    /** Global Store namespace */
    String GLOBAL = "__GLOBAL__";

    /**
     * Method to get the Store namespace
     *
     * @return the store namespace
     */
    String getNamespace();

    /**
     * Method to lock the Store, returning the Store's Lock
     *
     * @return the Lock
     */
    Lock lock();

    /**
     * Method to unlock the Store, returning the Store's Lock
     *
     * @return the Store Lock
     */
    Lock unlock();

    /**
     * Method to get the Store's Lock
     *
     * @return the Store Lock
     */
    Lock getLock();

    /**
     * Method to get the Store's keys. Keys that start with "." are considered hidden and will not
     * be returned.
     *
     * @return the Store keys
     */
    Set<String> keySet();

    /**
     * Method to put a value into the Store. Accepts a null value
     *
     * @param key key
     * @param value value
     * @return the existing value
     */
    Object put(String key, Object value);

    /**
     * Method to put a value into the store. If a value doesn't exist, execute the function to
     * create a value and add it
     *
     * @param key key
     * @param function function
     * @return the existing value, if not found, the Object returned by the Function
     */
    Object computeIfAbsent(String key, Function<String, Object> function);

    /**
     * Method to get a value from the store
     *
     * @param key key
     * @return an Optional containing the existing Object, or an empty Optional if an Object doesn't
     *     exist
     */
    Object get(String key);

    /**
     * Method to get a value from the store, casting it to a specific type
     *
     * @param key key
     * @param clazz clazz
     * @param <T> the return type
     * @return an Optional containing the existing value, or an empty Optional if a value doesn't
     *     exist
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * Method to remove a value from the store, closing the value if it implements AutoClosable
     *
     * @param key key
     * @return an Optional containing the existing Object, or an empty Optional if an Object doesn't
     *     exist
     */
    Object remove(String key);

    /**
     * Method to remove a value from the store
     *
     * @param key key
     * @param clazz clazz
     * @param <T> the return type
     * @return an Optional containing the existing value, or an empty Optional if a value doesn't
     *     exist
     */
    <T> T remove(String key, Class<T> clazz);
}
