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

package org.antublue.test.engine.internal;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Class to process @TestEngine.Lock and @TestEngine.Unlock annotations
 */
public class TestEngineLockUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEngineLockUtils.class);

    private static final Map<String, ReentrantReadWriteLock> LOCK_MAP = Collections.synchronizedMap(new HashMap<>());

    /**
     * Constructor
     */
    private TestEngineLockUtils() {
        // DO NOTHING
    }

    /**
     * Method to process a @TestEngine.Lock annotation for a Method if it exists
     *
     * @param method
     */
    public static void processLock(Method method) {
        TestEngine.Lock annotation = method.getAnnotation(TestEngine.Lock.class);
        if (annotation != null) {
            LOGGER.trace("processLock(%s, %s)", method.getDeclaringClass().getName(), method.getName());
            String name = annotation.value();
            TestEngine.LockMode mode = annotation.mode();
            if (name != null && !name.trim().isEmpty()) {
                name = name.trim();
                if (mode == TestEngine.LockMode.READ_WRITE) {
                    LOCK_MAP.computeIfAbsent(name, n -> new ReentrantReadWriteLock(true)).writeLock().lock();
                } else {
                    LOCK_MAP.computeIfAbsent(name, n -> new ReentrantReadWriteLock(true)).readLock().lock();
                }
                LOGGER.trace(
                        String.format(
                                "Lock [%s] [%s] acquired for class [%s] method [%s]",
                                name,
                                mode,
                                method.getDeclaringClass().getName(),
                                method.getName()));
            }
        }
    }

    /**
     * Method to process a @TestEngine.Unlock annotation for a Method if it exists
     *
     * @param method
     */
    public static void processUnlock(Method method) {
        TestEngine.Unlock annotation = method.getAnnotation(TestEngine.Unlock.class);
        if (annotation != null) {
            LOGGER.trace("processUnlock(%s, %s)", method.getDeclaringClass().getName(), method.getName());
            String name = annotation.value();
            TestEngine.LockMode mode = annotation.mode();
            if (name != null && !name.trim().isEmpty()) {
                name = name.trim();
                ReentrantReadWriteLock reentrantReadWriteLock = LOCK_MAP.get(name);
                if (reentrantReadWriteLock != null) {
                    LOGGER.trace(
                            String.format(
                                    "Lock [%s] mode [%s] released for class [%s] method [%s]",
                                    name,
                                    mode,
                                    method.getDeclaringClass().getName(),
                                    method.getName()));
                    if (mode == TestEngine.LockMode.READ_WRITE) {
                        reentrantReadWriteLock.writeLock().unlock();
                    } else {
                        reentrantReadWriteLock.readLock().unlock();
                    }
                } else {
                    throw new TestClassConfigurationException(
                            String.format(
                                    "@TestEngine.Unlock without @TestEngine.Lock, class [%s] method [%s]",
                                    method.getDeclaringClass().getName(),
                                    method.getName()));
                }
            }
        }
    }
}
