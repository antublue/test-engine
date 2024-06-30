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

import org.antublue.test.engine.api.extension.InvocationExtension;

public class ExampleInvocationExtension implements InvocationExtension {

    public void beforeInstantiateCallback(Class<?> testClass) throws Throwable {
        System.out.println("beforeInstantiateCallback() testClass [" + testClass + "]");
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
        System.out.println(
                "afterInstantiateCallback() testClass ["
                        + testClass
                        + "] testInstance ["
                        + testInstance
                        + "] throwable ["
                        + throwable
                        + "]");
    }

    /**
     * TODO
     *
     * @param testAnnotationClass
     * @param testInstance
     * @throws Throwable
     */
    public void beforeInvocationCallback(Class<?> testAnnotationClass, Object testInstance)
            throws Throwable {
        System.out.println(
                "beforeInvocationCallback() testAnnotationClass ["
                        + testAnnotationClass.getName()
                        + "] testInstance ["
                        + testInstance.getClass().getName()
                        + "]");
    }

    /**
     * TODO
     *
     * @param testAnnotationClass
     * @param testInstance
     * @param throwable
     * @throws Throwable
     */
    public void afterInvocationCallback(
            Class<?> testAnnotationClass, Object testInstance, Throwable throwable)
            throws Throwable {
        System.out.println(
                "afterInvocationCallback() testAnnotationClass ["
                        + testAnnotationClass.getName()
                        + "] testInstance ["
                        + testInstance.getClass().getName()
                        + "] throwable ["
                        + throwable
                        + "]");

        if (throwable != null) {
            throw throwable;
        }
    }
}
