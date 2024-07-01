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

package org.antublue.test.engine.testing.extension;

import java.lang.reflect.Method;
import org.antublue.test.engine.api.extension.TestExtension;

public class ExampleTestExtension implements TestExtension {

    /**
     * Method to call before instantiating a test instance
     *
     * @param testClass testClass
     * @throws Throwable Throwable
     */
    @Override
    public void preInstantiateCallback(Class<?> testClass) throws Throwable {
        System.out.println(
                getClass().getName()
                        + " preInstantiateCallback() testClass ["
                        + testClass.getSimpleName()
                        + "]");
    }

    /**
     * Method to call after instantiating a test instance
     *
     * @param testClass testClass
     * @param testInstance testInstance
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    @Override
    public void postInstantiateCallback(
            Class<?> testClass, Object testInstance, Throwable throwable) throws Throwable {
        System.out.println(
                getClass().getName()
                        + " postInstantiateCallback() testClass ["
                        + testClass.getSimpleName()
                        + "] testInstance ["
                        + testInstance
                        + "] throwable ["
                        + throwable
                        + "]");

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
    @Override
    public void prePrepareCallback(Object testInstance) throws Throwable {
        System.out.println(
                getClass().getName()
                        + " prePrepareCallback() testAnnotationClass ["
                        + testInstance.getClass().getName()
                        + "] testInstance ["
                        + testInstance.getClass().getName()
                        + "]");
    }

    /**
     * TODO
     *
     * @param testInstance
     * @param throwable
     * @throws Throwable
     */
    @Override
    public void postPrepareCallback(Object testInstance, Throwable throwable) throws Throwable {
        System.out.println(
                getClass().getName()
                        + " postPrepareCallback() testInstance ["
                        + testInstance.getClass().getName()
                        + "] throwable ["
                        + throwable
                        + "]");

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
    public void preBeforeAllCallback(Object testInstance) throws Throwable {
        System.out.println(
                getClass().getName()
                        + " preBeforeAllCallback() testAnnotationClass ["
                        + testInstance.getClass().getName()
                        + "] testInstance ["
                        + testInstance.getClass().getName()
                        + "]");
    }

    /**
     * TODO
     *
     * @param testInstance
     * @param throwable
     * @throws Throwable
     */
    public void postBeforeAllCallback(Object testInstance, Throwable throwable) throws Throwable {
        System.out.println(
                getClass().getName()
                        + " postBeforeAllCallback() testInstance ["
                        + testInstance.getClass().getName()
                        + "] throwable ["
                        + throwable
                        + "]");

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
    public void preBeforeEachCallback(Object testInstance) throws Throwable {
        System.out.println(
                getClass().getName()
                        + " preBeforeEachCallback() testAnnotationClass ["
                        + testInstance.getClass().getName()
                        + "] testInstance ["
                        + testInstance.getClass().getName()
                        + "]");
    }

    /**
     * TODO
     *
     * @param testInstance
     * @param throwable
     * @throws Throwable
     */
    public void postBeforeEachCallback(Object testInstance, Throwable throwable) throws Throwable {
        System.out.println(
                getClass().getName()
                        + " postBeforeEachCallback() testInstance ["
                        + testInstance.getClass().getName()
                        + "] throwable ["
                        + throwable
                        + "]");

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
    public void preAfterEachCallback(Object testInstance) throws Throwable {
        System.out.println(
                getClass().getName()
                        + " preAfterEachCallback() testAnnotationClass ["
                        + testInstance.getClass().getName()
                        + "] testInstance ["
                        + testInstance.getClass().getName()
                        + "]");
    }

    /**
     * TODO
     *
     * @param testInstance
     * @param throwable
     * @throws Throwable
     */
    public void postAfterEachCallback(Object testInstance, Throwable throwable) throws Throwable {
        System.out.println(
                getClass().getName()
                        + " postAfterEachCallback() testInstance ["
                        + testInstance.getClass().getName()
                        + "] throwable ["
                        + throwable
                        + "]");

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
    public void beforeTestCallback(Object testInstance, Method testMethod) throws Throwable {
        System.out.println(
                getClass().getName()
                        + " beforeTestCallback() testAnnotationClass ["
                        + testInstance.getClass().getName()
                        + "] testInstance ["
                        + testInstance.getClass().getName()
                        + "] testMethod ["
                        + testMethod.getName()
                        + "]");
    }

    /**
     * TODO
     *
     * @param testInstance
     * @param testMethod
     * @param throwable
     * @throws Throwable
     */
    public void postTestCallback(Object testInstance, Method testMethod, Throwable throwable)
            throws Throwable {
        System.out.println(
                getClass().getName()
                        + " postTestCallback() testInstance ["
                        + testInstance.getClass().getName()
                        + "] testMethod ["
                        + testMethod.getName()
                        + "] throwable ["
                        + throwable
                        + "]");

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
    public void preAfterAllCallback(Object testInstance) throws Throwable {
        System.out.println(
                getClass().getName()
                        + " preAfterAllCallback() testAnnotationClass ["
                        + testInstance.getClass().getName()
                        + "] testInstance ["
                        + testInstance.getClass().getName()
                        + "]");
    }

    /**
     * TODO
     *
     * @param testInstance
     * @param throwable
     * @throws Throwable
     */
    public void postAfterAllCallback(Object testInstance, Throwable throwable) throws Throwable {
        System.out.println(
                getClass().getName()
                        + " postAfterAllCallback() testInstance ["
                        + testInstance.getClass().getName()
                        + "] throwable ["
                        + throwable
                        + "]");

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
    @Override
    public void preConcludeCallback(Object testInstance) throws Throwable {
        System.out.println(
                getClass().getName()
                        + " preConcludeCallback() testAnnotationClass ["
                        + testInstance.getClass().getName()
                        + "] testInstance ["
                        + testInstance.getClass().getName()
                        + "]");
    }

    /**
     * TODO
     *
     * @param testInstance
     * @param throwable
     * @throws Throwable
     */
    @Override
    public void postConcludeCallback(Object testInstance, Throwable throwable) throws Throwable {
        System.out.println(
                getClass().getName()
                        + " postConcludeCallback() testInstance ["
                        + testInstance.getClass().getName()
                        + "] throwable ["
                        + throwable
                        + "]");

        if (throwable != null) {
            throw throwable;
        }
    }

    /**
     * Method to call before instantiating a test instance
     *
     * @param testClass testClass
     * @throws Throwable Throwable
     */
    @Override
    public void preDestroyCallback(Class<?> testClass, Object testInstance) throws Throwable {
        System.out.println(
                getClass().getName()
                        + " preDestroyCallback() testClass ["
                        + testClass.getSimpleName()
                        + "] testInstance ["
                        + testInstance
                        + "]");
    }

    /**
     * Method to call after instantiating a test instance
     *
     * @param testClass testClass
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    @Override
    public void postDestroyCallback(Class<?> testClass, Throwable throwable) {
        System.out.println(
                getClass().getName()
                        + " postDestroyCallback() testClass ["
                        + testClass.getSimpleName()
                        + "] throwable ["
                        + throwable
                        + "]");
    }
}
