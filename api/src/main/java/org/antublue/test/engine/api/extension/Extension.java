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
import java.util.Optional;
import org.antublue.test.engine.api.Argument;

/** Interface to implement an Extension */
public interface Extension {

    /**
     * Method to call before test object creation
     *
     * @param testClass testClass
     * @throws Throwable Throwable
     */
    default void preInstantiateCallback(Class<?> testClass) throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to call after test object creation
     *
     * @param testInstance testInstance
     * @throws Throwable Throwable
     */
    default void postInstantiateCallback(Object testInstance) throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to call after all @TestEngine.Prepare methods
     *
     * @param testInstance testInstance
     * @throws Throwable Throwable
     */
    default void postPrepareCallback(Object testInstance) throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to call after all @TestEngine.BeforeAll methods
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @throws Throwable Throwable
     */
    default void postBeforeAllCallback(Object testInstance, Argument testArgument)
            throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to call after all @TestEngine.BeforeEach methods
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @throws Throwable Throwable
     */
    default void postBeforeEachCallback(Object testInstance, Argument testArgument)
            throws Throwable {
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
    default void preTestCallback(Object testInstance, Argument testArgument, Method testMethod)
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
    default void postTestCallback(Object testInstance, Argument testArgument, Method testMethod)
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
    default void postAfterEachCallback(Object testInstance, Argument testArgument)
            throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to execute after all @TestEngine.AfterAll methods
     *
     * @param testInstance testInstance
     * @throws Throwable Throwable
     */
    default void postAfterAllCallback(Object testInstance, Argument testArgument) throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to execute after all @TestEngine.Conclude methods
     *
     * @param testInstance testInstance
     * @throws Throwable Throwable
     */
    default void postConcludeCallback(Object testInstance) throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to call after all testing
     *
     * @param testClass testClass
     * @param optionalTestInstance optionalTestInstance
     */
    default void preDestroyCallback(Class<?> testClass, Optional<Object> optionalTestInstance) {
        // DO NOTHING
    }
}
