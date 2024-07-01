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

package org.antublue.test.engine.api.extension;

import java.lang.reflect.Method;

/** Class to implement LifeCycleExtension */
public interface TestExtension {

    /**
     * Method to call before instantiating a test instance
     *
     * @param testClass testClass
     * @throws Throwable Throwable
     */
    default void preInstantiateCallback(Class<?> testClass) throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to call after instantiating a test instance
     *
     * @param testClass testClass
     * @param testInstance testInstance
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    default void postInstantiateCallback(
            Class<?> testClass, Object testInstance, Throwable throwable) throws Throwable {
        if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * TODO
     *
     * @param testInstance
     * @throws Throwable
     */
    default void prePrepareCallback(Object testInstance) throws Throwable {
        // DO NOTHING
    }

    /**
     * TODO
     *
     * @param testInstance
     * @param throwable
     * @throws Throwable
     */
    default void postPrepareCallback(Object testInstance, Throwable throwable) throws Throwable {
        if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * TODO
     *
     * @param testInstance
     * @throws Throwable
     */
    default void preBeforeAllCallback(Object testInstance) throws Throwable {
        // DO NOTHING
    }

    /**
     * TODO
     *
     * @param testInstance
     * @param throwable
     * @throws Throwable
     */
    default void postBeforeAllCallback(Object testInstance, Throwable throwable) throws Throwable {
        if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * TODO
     *
     * @param testInstance
     * @throws Throwable
     */
    default void preBeforeEachCallback(Object testInstance) throws Throwable {
        // DO NOTHING
    }

    /**
     * TODO
     *
     * @param testInstance
     * @param throwable
     * @throws Throwable
     */
    default void postBeforeEachCallback(Object testInstance, Throwable throwable) throws Throwable {
        if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * TODO
     *
     * @param testInstance
     * @throws Throwable
     */
    default void preAfterEachCallback(Object testInstance) throws Throwable {
        // DO NOTHING
    }

    /**
     * TODO
     *
     * @param testInstance
     * @param throwable
     * @throws Throwable
     */
    default void postAfterEachCallback(Object testInstance, Throwable throwable) throws Throwable {
        if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * TODO
     *
     * @param testInstance
     * @param testMethod
     * @throws Throwable
     */
    default void beforeTestCallback(Object testInstance, Method testMethod) throws Throwable {
        // DO NOTHING
    }

    /**
     * TODO
     *
     * @param testInstance
     * @param testMethod
     * @param throwable
     * @throws Throwable
     */
    default void postTestCallback(Object testInstance, Method testMethod, Throwable throwable)
            throws Throwable {
        if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * TODO
     *
     * @param testInstance
     * @throws Throwable
     */
    default void preAfterAllCallback(Object testInstance) throws Throwable {
        // DO NOTHING
    }

    /**
     * TODO
     *
     * @param testInstance
     * @param throwable
     * @throws Throwable
     */
    default void postAfterAllCallback(Object testInstance, Throwable throwable) throws Throwable {
        if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * TODO
     *
     * @param testInstance
     * @throws Throwable
     */
    default void preConcludeCallback(Object testInstance) throws Throwable {
        // DO NOTHING
    }

    /**
     * TODO
     *
     * @param testInstance
     * @param throwable
     * @throws Throwable
     */
    default void postConcludeCallback(Object testInstance, Throwable throwable) throws Throwable {
        if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * TODO
     *
     * @param testClass
     * @param testInstance
     * @throws Throwable
     */
    default void preDestroyCallback(Class<?> testClass, Object testInstance) throws Throwable {
        // DO NOTHING
    }

    /**
     * TODO
     *
     * @param testClass
     * @param throwable
     */
    default void postDestroyCallback(Class<?> testClass, Throwable throwable) {
        // DO NOTHING
    }
}
