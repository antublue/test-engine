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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Class to implement InvocationExtension */
public class ChainedInvocationExtension implements InvocationExtension {

    private final List<InvocationExtension> invocationExtensions;
    private final List<InvocationExtension> invocationExtensionsReversed;

    public ChainedInvocationExtension(InvocationExtension... invocationExtensions) {
        this.invocationExtensions = new ArrayList<>();
        this.invocationExtensions.addAll(Arrays.asList(invocationExtensions));
        this.invocationExtensionsReversed = new ArrayList<>(this.invocationExtensions);
        Collections.reverse(invocationExtensionsReversed);
    }

    /**
     * Method to call before instantiating a test instance
     *
     * @param testClass testClass
     * @throws Throwable Throwable
     */
    public void beforeInstantiateCallback(Class<?> testClass) throws Throwable {
        for (InvocationExtension invocationExtension : invocationExtensions) {
            invocationExtension.beforeInstantiateCallback(testClass);
        }
    }

    /**
     * Method to call after instantiating a test instance
     *
     * @param testClass testClass
     * @param testInstance testInstance
     * @param throwable throwable
     * @throws Throwable Throwable
     */
    public void afterInstantiateCallback(
            Class<?> testClass, Object testInstance, Throwable throwable) throws Throwable {
        for (InvocationExtension invocationExtension : invocationExtensionsReversed) {
            invocationExtension.afterInstantiateCallback(testClass, testInstance, throwable);
        }

        if (throwable != null) {
            throw throwable;
        }
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
        for (InvocationExtension invocationExtension : invocationExtensions) {
            invocationExtension.beforeInvocationCallback(
                    testAnnotationClass, testInstance, testMethod);
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
        for (InvocationExtension invocationExtension : invocationExtensionsReversed) {
            invocationExtension.afterInvocationCallback(
                    testAnnotationClass, testInstance, testMethod, throwable);
        }

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
    public void beforeDestroy(Class<?> testClass, Object testInstance) throws Throwable {
        for (InvocationExtension invocationExtension : invocationExtensions) {
            invocationExtension.beforeDestroy(testClass, testInstance);
        }
    }

    /**
     * TODO
     *
     * @param testClass
     * @param testInstance
     * @param throwable
     */
    public void afterDestroy(Class<?> testClass, Object testInstance, Throwable throwable) {
        for (InvocationExtension invocationExtension : invocationExtensionsReversed) {
            invocationExtension.afterDestroy(testClass, testInstance, throwable);
        }
    }

    public static ChainedInvocationExtension of(InvocationExtension... invocationExtensions) {
        return new ChainedInvocationExtension(invocationExtensions);
    }
}
