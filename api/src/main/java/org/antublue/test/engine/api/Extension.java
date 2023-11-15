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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/** Interface to implement an Extension */
public interface Extension {

    /**
     * Method to call after test parameters have been discovered
     *
     * @param testClass testClass
     * @param testArguments testArguments
     * @throws Throwable Throwable
     */
    default void postTestArgumentDiscoveryCallback(Class<?> testClass, List<Argument> testArguments)
            throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to call after test methods have been discovered
     *
     * @param testClass testClass
     * @param testMethods testMethods
     * @throws Throwable Throwable
     */
    default void postTestMethodDiscoveryCallback(Class<?> testClass, List<Method> testMethods)
            throws Throwable {
        // DO NOTHING
    }

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
     * Method to call before processing all @TestEngine.Prepare methods
     *
     * @param testInstance testInstance
     * @throws Throwable Throwable
     */
    default void prePrepareMethodsCallback(Object testInstance) throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to call after processing all @TestEngine.Prepare methods
     *
     * @param testInstance testInstance
     * @throws Throwable Throwable
     */
    default void postPrepareMethodsCallback(Object testInstance) throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to call before processing all @TestEngine.BeforeAll methods
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @throws Throwable Throwable
     */
    default void preBeforeAllMethodsCallback(Object testInstance, Argument testArgument)
            throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to call after processing all @TestEngine.BeforeAll methods
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @throws Throwable Throwable
     */
    default void postBeforeAllMethodsCallback(Object testInstance, Argument testArgument)
            throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to call before processing all @TestEngine.BeforeEach methods
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @throws Throwable Throwable
     */
    default void preBeforeEachMethodsCallback(Object testInstance, Argument testArgument)
            throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to call after processing all @TestEngine.BeforeEach methods
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @throws Throwable Throwable
     */
    default void postBeforeEachMethodsCallback(Object testInstance, Argument testArgument)
            throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to call before processing a @TestEngine.Test method
     *
     * @param testMethod testMethod
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @throws Throwable Throwable
     */
    default void preTestMethodsCallback(
            Method testMethod, Object testInstance, Argument testArgument) throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to call after processing a @TestEngine.Test method
     *
     * @param testMethod testMethod
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @throws Throwable Throwable
     */
    default void postTestMethodsCallback(
            Method testMethod, Object testInstance, Argument testArgument) throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to call after processing all @TestEngine.AfterEach methods
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @throws Throwable Throwable
     */
    default void preAfterEachMethodsCallback(Object testInstance, Argument testArgument)
            throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to call before processing all @TestEngine.AfterEach methods
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @throws Throwable Throwable
     */
    default void postAfterEachMethodsCallback(Object testInstance, Argument testArgument)
            throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to call after processing all @TestEngine.AfterAll methods
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @throws Throwable Throwable
     */
    default void preAfterAllMethodsCallback(Object testInstance, Argument testArgument)
            throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to call before processing all @TestEngine.AfterAll methods
     *
     * @param testInstance testInstance
     * @param testArgument testArgument
     * @throws Throwable Throwable
     */
    default void postAfterAllMethodsCallback(Object testInstance, Argument testArgument)
            throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to call after processing all @TestEngine.Conclude methods
     *
     * @param testInstance testInstance
     * @throws Throwable Throwable
     */
    default void preConcludeMethodsCallback(Object testInstance) throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to call before processing all @TestEngine.Conclude methods
     *
     * @param testInstance testInstance
     * @throws Throwable Throwable
     */
    default void postConcludeMethodsCallback(Object testInstance) throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to call after all testing
     *
     * @param testClass testClass
     * @param optionalTestInstance optionalTestInstance
     */
    default void preDestroyCallback(Class<?> testClass, Optional<Object> optionalTestInstance)
            throws Throwable {
        // DO NOTHING
    }
}
