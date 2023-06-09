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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

/**
 * Class to process @TestEngine.Lock, @TestEngine.Unlock, @TestEngine.ResourceLock annotations
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
     * Method to perform locking on a Method, if annotated
     *
     * @param method
     */
    public static void processLock(Method method) {
        Annotation[] annotations = method.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().isAssignableFrom(TestEngine.Lock.class)) {
                TestEngine.Lock lockAnnotation = (TestEngine.Lock) annotation;
                lock(method, lockAnnotation.value(), lockAnnotation.mode());
            } else if (annotation.annotationType().isAssignableFrom(TestEngine.Lock.List.class)) {
                TestEngine.Lock.List lockListAnnotation = (TestEngine.Lock.List) annotation;
                Stream.of(lockListAnnotation.value()).forEach(lock -> lock(method, lock.value(), lock.mode()));
            } else if (annotation.annotationType().isAssignableFrom(TestEngine.ResourceLock.class)) {
                TestEngine.ResourceLock lockAnnotation = (TestEngine.ResourceLock) annotation;
                lock(method, lockAnnotation.value(), lockAnnotation.mode());
            } else if (annotation.annotationType().isAssignableFrom(TestEngine.ResourceLock.List.class)) {
                TestEngine.ResourceLock.List lockListAnnotation = (TestEngine.ResourceLock.List) annotation;
                Stream.of(lockListAnnotation.value()).forEach(lock -> lock(method, lock.value(), lock.mode()));
            }
        }
    }

    /**
     * Method to perform locking
     *
     * @param method method
     * @param name name
     * @param mode mode
     */
    private static void lock(Method method, String name, TestEngine.LockMode mode) {
        if (name != null && !name.trim().isEmpty()) {
            name = name.trim();
            if (mode == TestEngine.LockMode.READ_WRITE) {
                LOCK_MAP.computeIfAbsent(name, n -> new ReentrantReadWriteLock(true)).writeLock().lock();
            } else {
                LOCK_MAP.computeIfAbsent(name, n -> new ReentrantReadWriteLock(true)).readLock().lock();
            }

            LOGGER.trace(
                    String.format(
                            "Locking [%s] mode [%s] for class [%s] method [%s]",
                            name,
                            mode,
                            method.getDeclaringClass().getName(),
                            method.getName()));
        }
    }

    /**
     * Method to perform unlocking on a Method, if annotated
     *
     * @param method
     */
    public static void processUnlock(Method method) {
        Annotation[] annotations = method.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().isAssignableFrom(TestEngine.Lock.class)) {
                TestEngine.Lock lockAnnotation = (TestEngine.Lock) annotation;
                unlock(method, lockAnnotation.value(), lockAnnotation.mode());
            } else if (annotation.annotationType().isAssignableFrom(TestEngine.Lock.List.class)) {
                TestEngine.Lock.List lockListAnnotation = (TestEngine.Lock.List) annotation;
                Stream.of(lockListAnnotation.value()).forEach(lock -> unlock(method, lock.value(), lock.mode()));
            } else if (annotation.annotationType().isAssignableFrom(TestEngine.ResourceLock.class)) {
                TestEngine.ResourceLock lockAnnotation = (TestEngine.ResourceLock) annotation;
                unlock(method, lockAnnotation.value(), lockAnnotation.mode());
            } else if (annotation.annotationType().isAssignableFrom(TestEngine.ResourceLock.List.class)) {
                TestEngine.ResourceLock.List lockListAnnotation = (TestEngine.ResourceLock.List) annotation;
                List<TestEngine.ResourceLock> list = Arrays.asList(lockListAnnotation.value());
                Collections.reverse(list);
                list.stream().forEach(lock -> unlock(method, lock.value(), lock.mode()));
            }
        }
    }

    /**
     * Method to perform unlocking
     *
     * @param method method
     * @param name name
     * @param mode mode
     */
    private static void unlock(Method method, String name, TestEngine.LockMode mode) {
        if (name != null && !name.trim().isEmpty()) {
            name = name.trim();
            ReentrantReadWriteLock reentrantReadWriteLock = LOCK_MAP.get(name);
            if (reentrantReadWriteLock != null) {
                LOGGER.trace(
                        String.format(
                                "Unlocking [%s] mode [%s] for class [%s] method [%s]",
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
