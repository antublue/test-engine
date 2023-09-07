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

package org.antublue.test.engine.test;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.platform.engine.ExecutionRequest;

/** Class to implement a ExecutableContext */
@SuppressWarnings("unchecked")
public class ExecutableContext {

    private final ExecutionRequest executionRequest;
    private final Map<String, Object> map;
    private final ThrowableContext throwableContext;
    private Object testInstance;

    public ExecutableContext(ExecutionRequest executionRequest) {
        this.executionRequest = executionRequest;
        map = new ConcurrentHashMap<>();
        throwableContext = new ThrowableContext();
    }

    public ExecutionRequest getExecutionRequest() {
        return executionRequest;
    }

    public ThrowableContext getThrowableContext() {
        return throwableContext;
    }

    public void setTestInstance(Object testInstance) {
        this.testInstance = testInstance;
    }

    public Object getTestInstance() {
        return testInstance;
    }

    public void put(String key, Object value) {
        map.put(key, value);
    }

    public <T> T get(String key) {
        return (T) map.get(key);
    }

    public <T> T getOrDefault(String key, T defaultValue) {
        return (T) map.getOrDefault(key, defaultValue);
    }

    public void remove(String key) {
        map.remove(key);
    }

    public Set<String> keySet() {
        return map.keySet();
    }
}
