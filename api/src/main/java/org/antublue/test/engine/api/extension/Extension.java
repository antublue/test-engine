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

package org.antublue.test.engine.api.extension;

import java.lang.reflect.Method;
import org.antublue.test.engine.api.Argument;

/** Interface to implement an Extension */
public interface Extension {

    /**
     * Method to call after test object creation
     *
     * @param testInstance testInstance
     * @throws Throwable Throwable
     */
    default void postCreateTestInstance(Object testInstance) throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to call after all @TestEngine.Prepare methods
     *
     * @param testInstance testInstance
     * @throws Throwable Throwable
     */
    default void prepare(Object testInstance) throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to call after all @TestEngine.BeforeAll methods
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @throws Throwable Throwable
     */
    default void beforeAll(Object testInstance, Argument testArgument) throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to call after all @TestEngine.BeforeEach methods
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @throws Throwable Throwable
     */
    default void beforeEach(Object testInstance, Argument testArgument) throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to execute before a single @TestEngine.Test method
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @param testMethod testMethod
     * @throws Throwable Throwable
     */
    default void beforeTest(Object testInstance, Argument testArgument, Method testMethod)
            throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to execute after a single @TestEngine.Test method
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @param testMethod testMethod
     * @throws Throwable Throwable
     */
    default void afterTest(Object testInstance, Argument testArgument, Method testMethod)
            throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to execute after all @TestEngine.AfterEach methods
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @throws Throwable Throwable
     */
    default void afterEach(Object testInstance, Argument testArgument) throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to execute after all @TestEngine.AfterAll methods
     *
     * @param testInstance testInstance
     * @throws Throwable Throwable
     */
    default void afterAll(Object testInstance, Argument testArgument) throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to execute after all @TestEngine.Conclude methods
     *
     * @param testInstance testInstance
     * @throws Throwable Throwable
     */
    default void conclude(Object testInstance) throws Throwable {
        // DO NOTHING
    }
}
