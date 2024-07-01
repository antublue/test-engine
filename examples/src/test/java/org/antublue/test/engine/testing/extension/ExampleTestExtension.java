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
     * TODO
     *
     * @param testInstance
     * @throws Throwable
     */
    @Override
    public void beforePrepareCallback(Object testInstance) throws Throwable {
        System.out.println(
                getClass().getName()
                        + " beforePrepareCallback() testAnnotationClass ["
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
    public void afterPrepareCallback(Object testInstance, Throwable throwable) throws Throwable {
        System.out.println(
                getClass().getName()
                        + " afterPrepareCallback() testInstance ["
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
    public void beforeBeforeAllCallback(Object testInstance) throws Throwable {
        System.out.println(
                getClass().getName()
                        + " beforeBeforeAllCallback() testAnnotationClass ["
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
    public void afterBeforeAllCallback(Object testInstance, Throwable throwable) throws Throwable {
        System.out.println(
                getClass().getName()
                        + " afterBeforeAllCallback() testInstance ["
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
    public void beforeBeforeEachCallback(Object testInstance) throws Throwable {
        System.out.println(
                getClass().getName()
                        + " beforeBeforeEachCallback() testAnnotationClass ["
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
    public void afterBeforeEachCallback(Object testInstance, Throwable throwable) throws Throwable {
        System.out.println(
                getClass().getName()
                        + " afterBeforeEachCallback() testInstance ["
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
    public void beforeAfterEachCallback(Object testInstance) throws Throwable {
        System.out.println(
                getClass().getName()
                        + " beforeAfterEachCallback() testAnnotationClass ["
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
    public void afterAfterEachCallback(Object testInstance, Throwable throwable) throws Throwable {
        System.out.println(
                getClass().getName()
                        + " afterAfterEachCallback() testInstance ["
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
    public void afterTestCallback(Object testInstance, Method testMethod, Throwable throwable)
            throws Throwable {
        System.out.println(
                getClass().getName()
                        + " afterTestCallback() testInstance ["
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
    public void beforeAfterAllCallback(Object testInstance) throws Throwable {
        System.out.println(
                getClass().getName()
                        + " beforeAfterAllCallback() testAnnotationClass ["
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
    public void afterAfterAllCallback(Object testInstance, Throwable throwable) throws Throwable {
        System.out.println(
                getClass().getName()
                        + " afterAfterAllCallback() testInstance ["
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
    public void beforeConcludeCallback(Object testInstance) throws Throwable {
        System.out.println(
                getClass().getName()
                        + " beforeConcludeCallback() testAnnotationClass ["
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
    public void afterConcludeCallback(Object testInstance, Throwable throwable) throws Throwable {
        System.out.println(
                getClass().getName()
                        + " afterConcludeCallback() testInstance ["
                        + testInstance.getClass().getName()
                        + "] throwable ["
                        + throwable
                        + "]");

        if (throwable != null) {
            throw throwable;
        }
    }
}
