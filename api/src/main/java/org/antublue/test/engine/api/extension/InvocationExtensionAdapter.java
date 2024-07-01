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
import org.antublue.test.engine.api.TestEngine;
import org.junit.platform.commons.util.Preconditions;

public final class InvocationExtensionAdapter implements InvocationExtension {

    private final TestExtension testExtension;

    /**
     * Constructor
     *
     * @param testExtension testExtensions
     */
    private InvocationExtensionAdapter(TestExtension testExtension) {
        this.testExtension = testExtension;
    }

    /**
     * Method to call before instantiating a test instance
     *
     * @param testClass testClass
     * @throws Throwable Throwable
     */
    @Override
    public void beforeInstantiateCallback(Class<?> testClass) throws Throwable {
        testExtension.preInstantiateCallback(testClass);
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
    public void afterInstantiateCallback(
            Class<?> testClass, Object testInstance, Throwable throwable) throws Throwable {
        testExtension.postInstantiateCallback(testClass, testInstance, throwable);
    }

    /**
     * TODO
     *
     * @param testAnnotationClass
     * @param testInstance
     * @throws Throwable
     */
    @Override
    public void beforeInvocationCallback(
            Class<?> testAnnotationClass, Object testInstance, Method testMethod) throws Throwable {
        if (testAnnotationClass.equals(TestEngine.Prepare.class)) {
            testExtension.prePrepareCallback(testInstance);
        } else if (testAnnotationClass.equals(TestEngine.BeforeAll.class)) {
            testExtension.preBeforeAllCallback(testInstance);
        } else if (testAnnotationClass.equals(TestEngine.BeforeEach.class)) {
            testExtension.preBeforeEachCallback(testInstance);
        } else if (testAnnotationClass.equals(TestEngine.Test.class)) {
            testExtension.beforeTestCallback(testInstance, testMethod);
        } else if (testAnnotationClass.equals(TestEngine.AfterEach.class)) {
            testExtension.preAfterEachCallback(testInstance);
        } else if (testAnnotationClass.equals(TestEngine.AfterAll.class)) {
            testExtension.preAfterAllCallback(testInstance);
        } else if (testAnnotationClass.equals(TestEngine.Conclude.class)) {
            testExtension.preConcludeCallback(testInstance);
        }
    }

    /**
     * TODO
     *
     * @param testAnnotationClass
     * @param testInstance
     * @param throwable
     * @throws Throwable
     */
    @Override
    public void afterInvocationCallback(
            Class<?> testAnnotationClass,
            Object testInstance,
            Method testMethod,
            Throwable throwable)
            throws Throwable {
        if (testAnnotationClass.equals(TestEngine.Prepare.class)) {
            testExtension.postPrepareCallback(testInstance, throwable);
        } else if (testAnnotationClass.equals(TestEngine.BeforeAll.class)) {
            testExtension.postBeforeAllCallback(testInstance, throwable);
        } else if (testAnnotationClass.equals(TestEngine.BeforeEach.class)) {
            testExtension.postBeforeEachCallback(testInstance, throwable);
        } else if (testAnnotationClass.equals(TestEngine.Test.class)) {
            testExtension.postTestCallback(testInstance, testMethod, throwable);
        } else if (testAnnotationClass.equals(TestEngine.AfterEach.class)) {
            testExtension.postAfterEachCallback(testInstance, throwable);
        } else if (testAnnotationClass.equals(TestEngine.AfterAll.class)) {
            testExtension.postAfterAllCallback(testInstance, throwable);
        } else if (testAnnotationClass.equals(TestEngine.Conclude.class)) {
            testExtension.postConcludeCallback(testInstance, throwable);
        }
    }

    /**
     * TODO
     *
     * @param testClass
     * @param testInstance
     * @throws Throwable
     */
    @Override
    public void preDestroyCallback(Class<?> testClass, Object testInstance) throws Throwable {
        testExtension.preDestroyCallback(testClass, testInstance);
    }

    /**
     * TODO
     *
     * @param testClass
     * @param throwable
     */
    @Override
    public void postDestroyCallback(Class<?> testClass, Throwable throwable) {
        testExtension.postDestroyCallback(testClass, throwable);
    }

    public static InvocationExtension of(TestExtension testExtension) {
        Preconditions.notNull(testExtension, "testExtensions is null");
        return new InvocationExtensionAdapter(testExtension);
    }
}
