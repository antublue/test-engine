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

/** Class to implement LifeCycleExtension */
public interface TestExtension {

    /**
     * Method to call before instantiating a test instance
     *
     * @param testClass testClass
     * @throws Throwable Throwable
     */
    default void beforeInstantiateCallback(Class<?> testClass) throws Throwable {
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
    default void afterInstantiateCallback(
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
    default void beforePrepareCallback(Object testInstance) throws Throwable {
        // DO NOTHING
    }

    /**
     * TODO
     *
     * @param testInstance
     * @param throwable
     * @throws Throwable
     */
    default void afterPrepareCallback(Object testInstance, Throwable throwable) throws Throwable {
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
    default void beforeBeforeAllCallback(Object testInstance) throws Throwable {
        // DO NOTHING
    }

    /**
     * TODO
     *
     * @param testInstance
     * @param throwable
     * @throws Throwable
     */
    default void afterBeforeAllCallback(Object testInstance, Throwable throwable) throws Throwable {
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
    default void beforeBeforeEachCallback(Object testInstance) throws Throwable {
        // DO NOTHING
    }

    /**
     * TODO
     *
     * @param testInstance
     * @param throwable
     * @throws Throwable
     */
    default void afterBeforeEachCallback(Object testInstance, Throwable throwable)
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
    default void beforeAfterEachCallback(Object testInstance) throws Throwable {
        // DO NOTHING
    }

    /**
     * TODO
     *
     * @param testInstance
     * @param throwable
     * @throws Throwable
     */
    default void afterAfterEachCallback(Object testInstance, Throwable throwable) throws Throwable {
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
    default void beforeAfterAllCallback(Object testInstance) throws Throwable {
        // DO NOTHING
    }

    /**
     * TODO
     *
     * @param testInstance
     * @param throwable
     * @throws Throwable
     */
    default void afterAfterAllCallback(Object testInstance, Throwable throwable) throws Throwable {
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
    default void beforeDestroy(Class<?> testClass, Object testInstance) throws Throwable {
        // DO NOTHING
    }

    /**
     * TODO
     *
     * @param testClass
     * @param testInstance
     * @param throwable
     */
    default void afterDestroy(Class<?> testClass, Object testInstance, Throwable throwable) {
        // DO NOTHING
    }
}
