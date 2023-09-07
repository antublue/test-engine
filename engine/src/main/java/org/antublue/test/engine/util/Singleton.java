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

package org.antublue.test.engine.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/** Class to implement a Singleton */
@SuppressWarnings("unchecked")
public class Singleton {

    private static final ReentrantReadWriteLock reentrantReadWriteLock;
    private static final Map<String, Object> map;

    static {
        reentrantReadWriteLock = new ReentrantReadWriteLock(true);
        map = new HashMap<>();
    }

    private Singleton() {
        // DO NOTHING
    }

    public static void register(Class<?> clazz, Function<Class<?>, Object> function) {
        try {
            reentrantReadWriteLock.writeLock().lock();
            if (!map.containsKey(clazz.getName())) {
                map.put(clazz.getName(), function.apply(clazz));
            }
        } finally {
            reentrantReadWriteLock.writeLock().unlock();
        }
    }

    public static void register(String name, Function<String, Object> function) {
        try {
            reentrantReadWriteLock.writeLock().lock();
            if (!map.containsKey(name)) {
                map.put(name, function.apply(name));
            }
        } finally {
            reentrantReadWriteLock.writeLock().unlock();
        }
    }

    public static <T> T get(Class<T> clazz) {
        return get(clazz.getName());
    }

    public static <T> T get(String name) {
        Object object = map.get(name);
        if (object == null) {
            throw new RuntimeException(
                    String.format("No singleton registered for name [%s]", name));
        }
        return (T) object;
    }
}
