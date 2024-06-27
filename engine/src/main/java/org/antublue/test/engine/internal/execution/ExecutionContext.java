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

package org.antublue.test.engine.internal.execution;

import java.util.HashMap;
import java.util.Map;
import org.antublue.test.engine.internal.configuration.Configuration;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.ExecutionRequest;

/** Class to implement ExecutionContext */
@SuppressWarnings("unchecked")
public class ExecutionContext {

    private final Map<Object, Object> map;
    private final ExecutionRequest executionRequest;

    /**
     * Constructor
     *
     * @param executionRequest executionContext
     */
    public ExecutionContext(ExecutionRequest executionRequest) {
        Preconditions.notNull(executionRequest, "executionRequest is null");

        this.map = new HashMap<>();
        this.executionRequest =
                ExecutionRequest.create(
                        executionRequest.getRootTestDescriptor(),
                        executionRequest.getEngineExecutionListener(),
                        Configuration.getInstance());
    }

    /**
     * Copy constructor
     *
     * @param executionContext executionContext
     */
    public ExecutionContext(ExecutionContext executionContext) {
        Preconditions.notNull(executionContext, "executionContext is null");

        this.map = new HashMap<>();
        this.executionRequest = executionContext.executionRequest;
    }

    /**
     * Method to get the execution request
     *
     * @return the execution request
     */
    public ExecutionRequest getExecutionRequest() {
        return executionRequest;
    }

    /**
     * Method to store an Object in the execution context
     *
     * @param key key
     * @param value value
     * @return the existing value, or null if there is no existing value
     */
    public <T> T put(Object key, Object value) {
        Preconditions.notNull(key, "key is null");
        Preconditions.notNull(value, "value is null");

        return (T) map.put(key, value);
    }

    /**
     * Method to get an Object from the execution context
     *
     * @param key key
     * @return an Object
     * @param <T> the return type
     */
    public <T> T get(Object key) {
        Preconditions.notNull(key, "key is null");
        return (T) map.get(key);
    }

    /**
     * Method to get an Object from the execution context
     *
     * @param key key
     * @param clazz clazz
     * @return the value
     * @param <T> the return type
     */
    public <T> T get(Object key, Class<T> clazz) {
        Preconditions.notNull(key, "key is null");
        Preconditions.notNull(clazz, "clazz is null");
        return clazz.cast(map.get(key));
    }

    /**
     * Method to return if the execution context contains a value for a key
     *
     * @param key key
     * @return true if a value exists, else false
     */
    public boolean containsKey(Object key) {
        Preconditions.notNull(key, "key is null");
        return map.containsKey(key);
    }

    /**
     * Method to remove a value for a key
     *
     * @param key key
     * @return the value, or null if a value for the key doesn't exist
     * @param <T> the return type
     */
    public <T> T remove(Object key) {
        Preconditions.notNull(key, "key is null");
        return (T) map.remove(key);
    }

    /**
     * Method to remove a value for a key
     *
     * @param key key
     * @param clazz clazz
     * @return an Object, or null if a value for the key doesn't exist
     * @param <T> the return type
     */
    public <T> T remove(Object key, Class<T> clazz) {
        Preconditions.notNull(key, "key is null");
        Preconditions.notNull(clazz, "clazz is null");
        return clazz.cast(map.remove(key));
    }
}
