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

public class TestEngineLockUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEngineLockUtils.class);

    private TestEngineLockUtils() {
        // DO NOTHING
    }

    /**
     * Method to process a TestEngine.Lock annotation for a Method
     *
     * @param method
     */
    public static void processLock(Method method) {
        if (method.isAnnotationPresent(TestEngine.Lock.class)) {
            String value = method.getAnnotation(TestEngine.Lock.class).value();
            if (value != null && !value.trim().isEmpty()) {
                Store.getOrCreate(value, name -> new ReentrantLock(true)).lock();
                LOGGER.trace(String.format("Lock name = [%s] locked", value));
            }
        }
    }

    /**
     * Method to process a TestEngine.Unlock annotation for a Method
     *
     * @param method
     */
    public static void processUnlock(Method method) {
        if (method.isAnnotationPresent(TestEngine.Unlock.class)) {
            String value = method.getAnnotation(TestEngine.Unlock.class).value();
            if (value != null && !value.trim().isEmpty()) {
                ReentrantLock reentrantLock = Store.get(value);
                if (reentrantLock != null) {
                    LOGGER.trace(String.format("Lock name = [%s] unlocked", value));
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
