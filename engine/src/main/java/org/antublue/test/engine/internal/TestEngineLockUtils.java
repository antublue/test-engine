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

import org.antublue.test.engine.api.Store;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class to process @TestEngine.Lock and @TestEngine.Unlock annotations
 */
public class TestEngineLockUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEngineLockUtils.class);

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
            String name = annotation.value();
            if (name != null && !name.trim().isEmpty()) {
                name = name.trim();
                Store.getOrElse(name, n -> new ReentrantLock(true)).lock();
                LOGGER.trace(
                        String.format(
                                "Lock class [%s] name [%s] locked",
                                method.getDeclaringClass().getName(), name));
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
            String name = annotation.value();
            if (name != null && !name.trim().isEmpty()) {
                name = name.trim();
                ReentrantLock reentrantLock = Store.get(name);
                if (reentrantLock != null) {
                    LOGGER.trace(
                            String.format(
                                    "Lock class [%s] name [%s] unlocked",
                                    method.getDeclaringClass().getName(), name));
                    reentrantLock.unlock();
                } else {
                    throw new TestClassConfigurationException(
                            String.format(
                                    "@TestEngine.Unlock with out @TestEngine.Lock, class [%s]",
                                    method.getDeclaringClass().getName()));
                }
            }
        }
    }
}
