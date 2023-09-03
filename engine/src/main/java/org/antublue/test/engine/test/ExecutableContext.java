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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.antublue.test.engine.configuration.Constants;
import org.antublue.test.engine.util.StandardStreams;
import org.antublue.test.engine.util.ThrowableUtils;

/** Class to implement a ExecutableContext */
@SuppressWarnings("unchecked")
public class ExecutableContext {

    private static final boolean PRINT_STACKTRACE =
            Constants.TRUE.equals(System.getProperty(Constants.MAVEN_PLUGIN));

    private final Map<String, Object> map;
    private final List<Throwable> throwables;

    public ExecutableContext() {
        map = new ConcurrentHashMap<>();
        throwables = new ArrayList<>();
    }

    public void addAndProcessThrowable(Class<?> clazz, Throwable throwable) {
        Throwable prunedThrowable = ThrowableUtils.prune(clazz, throwable);
        if (PRINT_STACKTRACE) {
            prunedThrowable.printStackTrace();
            StandardStreams.flush();
        }
        throwables.add(prunedThrowable);
    }

    public boolean hasThrowables() {
        return !throwables.isEmpty();
    }

    public List<Throwable> getThrowables() {
        return throwables;
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
