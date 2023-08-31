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

package org.antublue.test.engine.api;

/** Interface to implement an Extension */
public interface Extension {

    /**
     * Method to execute before all @TestEngine.Prepare methods
     *
     * @param testInstance testInstance
     * @throws Throwable Throwable
     */
    default void beforePrepare(Object testInstance) throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to execute after all @TestEngine.Prepare methods
     *
     * @param testInstance testInstance
     * @throws Throwable Throwable
     */
    default void prepareCallback(Object testInstance) throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to execute before all @TestEngine.BeforeAll methods
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @throws Throwable Throwable
     */
    default void beforeBeforeAll(Object testInstance, Argument testArgument) throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to execute after all @TestEngine.BeforeAll methods
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @throws Throwable Throwable
     */
    default void beforeAllCallback(Object testInstance, Argument testArgument) throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to execute before all @TestEngine.BeforeEach methods
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @throws Throwable Throwable
     */
    default void beforeBeforeEach(Object testInstance, Argument testArgument) throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to execute after all @TestEngine.BeforeEach methods
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @throws Throwable Throwable
     */
    default void beforeEachCallback(Object testInstance, Argument testArgument) throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to execute before a single @TestEngine.Test method
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @throws Throwable Throwable
     */
    default void beforeTest(Object testInstance, Argument testArgument) throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to execute after a single @TestEngine.Test method
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @throws Throwable Throwable
     */
    default void testCallback(Object testInstance, Argument testArgument) throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to execute before all @TestEngine.AfterEach methods
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @throws Throwable Throwable
     */
    default void beforeAfterEach(Object testInstance, Argument testArgument) throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to execute after all @TestEngine.AfterEach methods
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @throws Throwable Throwable
     */
    default void afterEachCallback(Object testInstance, Argument testArgument) throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to execute before all @TestEngine.AfterAll methods
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @throws Throwable Throwable
     */
    default void beforeAfterAll(Object testInstance, Argument testArgument) throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to execute after all @TestEngine.AfterAll methods
     *
     * @param testInstance testInstance
     * @throws Throwable Throwable
     */
    default void afterAllCallback(Object testInstance, Argument testArgument) throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to execute before all @TestEngine.Conclude methods
     *
     * @param testInstance testInstance
     * @throws Throwable Throwable
     */
    default void beforeConclude(Object testInstance) throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to execute after all @TestEngine.Conclude methods
     *
     * @param testInstance testInstance
     * @throws Throwable Throwable
     */
    default void afterConcludeCallback(Object testInstance) throws Throwable {
        // DO NOTHING
    }
}
