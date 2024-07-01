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

public final class InvocationExtensionAdapter implements InvocationExtension {

    private final TestExtension testExtension;

    /**
     * TODO
     *
     * @param testExtension
     */
    public InvocationExtensionAdapter(TestExtension testExtension) {
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
        testExtension.beforeInstantiateCallback(testClass);
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
        testExtension.afterInstantiateCallback(testClass, testInstance, throwable);
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
            testExtension.beforePrepareCallback(testInstance);
        } else if (testAnnotationClass.equals(TestEngine.BeforeAll.class)) {
            testExtension.beforeBeforeAllCallback(testInstance);
        } else if (testAnnotationClass.equals(TestEngine.BeforeEach.class)) {
            testExtension.beforeBeforeEachCallback(testInstance);
        } else if (testAnnotationClass.equals(TestEngine.Test.class)) {
            testExtension.beforeTestCallback(testInstance, testMethod);
        } else if (testAnnotationClass.equals(TestEngine.AfterEach.class)) {
            testExtension.beforeAfterEachCallback(testInstance);
        } else if (testAnnotationClass.equals(TestEngine.AfterAll.class)) {
            testExtension.beforeAfterAllCallback(testInstance);
        } else if (testAnnotationClass.equals(TestEngine.Conclude.class)) {
            testExtension.beforeConcludeCallback(testInstance);
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
            testExtension.afterPrepareCallback(testInstance, throwable);
        } else if (testAnnotationClass.equals(TestEngine.BeforeAll.class)) {
            testExtension.afterBeforeAllCallback(testInstance, throwable);
        } else if (testAnnotationClass.equals(TestEngine.BeforeEach.class)) {
            testExtension.afterBeforeEachCallback(testInstance, throwable);
        } else if (testAnnotationClass.equals(TestEngine.Test.class)) {
            testExtension.afterTestCallback(testInstance, testMethod, throwable);
        } else if (testAnnotationClass.equals(TestEngine.AfterEach.class)) {
            testExtension.afterAfterEachCallback(testInstance, throwable);
        } else if (testAnnotationClass.equals(TestEngine.AfterAll.class)) {
            testExtension.afterAfterAllCallback(testInstance, throwable);
        } else if (testAnnotationClass.equals(TestEngine.Conclude.class)) {
            testExtension.afterConcludeCallback(testInstance, throwable);
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
    public void beforeDestroy(Class<?> testClass, Object testInstance) throws Throwable {
        testExtension.beforeDestroy(testClass, testInstance);
    }

    /**
     * TODO
     *
     * @param testClass
     * @param testInstance
     * @param throwable
     */
    @Override
    public void afterDestroy(Class<?> testClass, Object testInstance, Throwable throwable) {
        testExtension.afterDestroy(testClass, testInstance, throwable);
    }
}
