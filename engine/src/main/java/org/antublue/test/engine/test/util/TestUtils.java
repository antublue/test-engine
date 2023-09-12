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

package org.antublue.test.engine.test.util;

import static org.antublue.test.engine.util.ReflectionUtils.NO_OBJECT_ARGS;

import java.lang.reflect.Method;
import java.util.List;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.MethodOrderer;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.test.ThrowableContext;
import org.antublue.test.engine.util.ReflectionUtils;
import org.antublue.test.engine.util.StandardStreams;

public class TestUtils {

    private static final TestUtils SINGLETON = new TestUtils();

    private static final ReflectionUtils REFLECTION_UTILS = ReflectionUtils.getSingleton();

    public enum Sort {
        FORWARD,
        REVERSE,
    }

    private TestUtils() {
        // DO NOTHING
    }

    public static TestUtils getSingleton() {
        return SINGLETON;
    }

    public void invoke(
            Method method,
            Object testInstance,
            Object testArgument,
            ThrowableContext throwableContext) {
        try {
            if (REFLECTION_UTILS.acceptsArguments(method, Argument.class)) {
                method.invoke(testInstance, testArgument);
            } else {
                method.invoke(testInstance, (Object[]) null);
            }
        } catch (Throwable t) {
            throwableContext.add(testInstance.getClass(), t);
        } finally {
            StandardStreams.flush();
        }
    }

    /**
     * Method to get a test method display name
     *
     * @param testClass testClass
     * @return the display name
     */
    public String getDisplayName(Class<?> testClass) {
        String displayName = testClass.getName();

        TestEngine.DisplayName annotation = testClass.getAnnotation(TestEngine.DisplayName.class);
        if (annotation != null) {
            String name = annotation.name();
            if (name != null && !name.trim().isEmpty()) {
                displayName = name.trim();
            }
        }

        return displayName;
    }

    /**
     * Method to get a test method display name
     *
     * @param testMethod testMethod
     * @return the display name
     */
    public String getDisplayName(Method testMethod) {
        String displayName = testMethod.getName();

        TestEngine.DisplayName annotation = testMethod.getAnnotation(TestEngine.DisplayName.class);
        if (annotation != null) {
            String name = annotation.name();
            if (name != null && !name.trim().isEmpty()) {
                displayName = name.trim();
            }
        }

        return displayName;
    }

    /**
     * Method to get a test class tag value
     *
     * @param testClass testClass
     * @return the tag value
     */
    public String getTag(Class<?> testClass) {
        String tagValue = null;

        TestEngine.Tag annotation = testClass.getAnnotation(TestEngine.Tag.class);
        if (annotation != null) {
            String tag = annotation.tag();
            if (tag != null && !tag.trim().isEmpty()) {
                tagValue = tag.trim();
            }
        }

        return tagValue;
    }

    /**
     * Method to get a test method tag value
     *
     * @param testMethod testMethod
     * @return the tag value
     */
    public String getTag(Method testMethod) {
        String tagValue = null;

        TestEngine.Tag annotation = testMethod.getAnnotation(TestEngine.Tag.class);
        if (annotation != null) {
            String tag = annotation.tag();
            if (tag != null && !tag.trim().isEmpty()) {
                tagValue = tag.trim();
            }
        }

        return tagValue;
    }

    /**
     * Method to sort test methods first by @TestEngine.Order annotation, then by name
     *
     * @param testMethods testMethods
     * @param sort sort
     */
    public void orderTestMethods(List<Method> testMethods, Sort sort) {
        testMethods.sort(
                (o1, o2) -> {
                    int o1Order = Integer.MAX_VALUE;
                    TestEngine.Order o1Annotation = o1.getAnnotation(TestEngine.Order.class);
                    if (o1Annotation != null) {
                        o1Order = o1Annotation.order();
                    }

                    int o2Order = Integer.MAX_VALUE;
                    TestEngine.Order o2Annotation = o2.getAnnotation(TestEngine.Order.class);
                    if (o2Annotation != null) {
                        o2Order = o2Annotation.order();
                    }

                    if (o1Order != o2Order) {
                        return Integer.compare(o1Order, o2Order);
                    }

                    // Order by display name which is either
                    // the name declared by @TestEngine.DisplayName
                    // or the real method name
                    String o1DisplayName = getDisplayName(o1);
                    String o2DisplayName = getDisplayName(o2);

                    switch (sort) {
                        case FORWARD:
                            {
                                return o1DisplayName.compareTo(o2DisplayName);
                            }
                        case REVERSE:
                            {
                                return -o1DisplayName.compareTo(o2DisplayName);
                            }
                        default:
                            {
                                return 0;
                            }
                    }
                });
    }

    /**
     * Method to order test methods based on a test class @TestEngine.MethodOrderSupplier
     *
     * @param testClass testClass
     * @param testMethods testMethods
     * @throws Throwable Throwble
     */
    public void orderTestMethods(Class<?> testClass, List<Method> testMethods) throws Throwable {
        List<Method> methodOrderSupplierMethods =
                REFLECTION_UTILS.findMethods(testClass, TestFilters.METHOD_ORDERER_SUPPLIER);

        if (!methodOrderSupplierMethods.isEmpty()) {
            MethodOrderer methodOrderer =
                    (MethodOrderer)
                            methodOrderSupplierMethods.get(0).invoke(testClass, NO_OBJECT_ARGS);
            methodOrderer.order(testMethods);
        }
    }
}
