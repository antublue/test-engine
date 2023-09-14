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

package org.antublue.test.engine.internal.test.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.utils.ReflectionUtils;

public class TestUtils {

    private static final TestUtils SINGLETON = new TestUtils();

    private static final ReflectionUtils REFLECTION_UTILS = ReflectionUtils.getSingleton();

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
     * Method to order test methods first by @TestEngine.Order annotation, then by name for a class
     * within the hierarchy
     *
     * @param testMethods testMethods
     */
    public void orderTestMethods(List<Method> testMethods) {
        // Group methods based on component type / declaring class
        Map<Class<?>, List<Method>> methodMap = new LinkedHashMap<>();
        for (Method method : testMethods) {
            Class<?> componentType = method.getDeclaringClass().getComponentType();
            if (componentType == null) {
                componentType = method.getDeclaringClass();
            }
            List<Method> methods = methodMap.get(componentType);
            if (methods == null) {
                methods = new ArrayList<>();
                methodMap.put(componentType, methods);
            }
            methods.add(method);
        }

        // Sort methods for each group
        for (Map.Entry<Class<?>, List<Method>> entry : methodMap.entrySet()) {
            entry.getValue()
                    .sort(
                            (o1, o2) -> {
                                int o1Order = Integer.MAX_VALUE;
                                TestEngine.Order o1Annotation =
                                        o1.getAnnotation(TestEngine.Order.class);
                                if (o1Annotation != null) {
                                    o1Order = o1Annotation.order();
                                }

                                int o2Order = Integer.MAX_VALUE;
                                TestEngine.Order o2Annotation =
                                        o2.getAnnotation(TestEngine.Order.class);
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

                                return o1DisplayName.compareTo(o2DisplayName);
                            });
        }

        // Clear the test method list
        testMethods.clear();

        // Rebuild the test method set
        for (Map.Entry<Class<?>, List<Method>> entry : methodMap.entrySet()) {
            testMethods.addAll(entry.getValue());
        }
    }

    /**
     * Method to order test methods in reverse first by @TestEngine.Order annotation, then by name
     * for a class within the hierarchy
     *
     * @param testMethods testMethods
     */
    public void orderTestMethodsReverse(List<Method> testMethods) {
        // Group methods based on component type / declaring class
        Map<Class<?>, List<Method>> methodMap = new LinkedHashMap<>();
        for (Method method : testMethods) {
            Class<?> componentType = method.getDeclaringClass().getComponentType();
            if (componentType == null) {
                componentType = method.getDeclaringClass();
            }
            List<Method> methods = methodMap.get(componentType);
            if (methods == null) {
                methods = new ArrayList<>();
                methodMap.put(componentType, methods);
            }
            methods.add(method);
        }

        // Sort methods for each group
        for (Map.Entry<Class<?>, List<Method>> entry : methodMap.entrySet()) {
            entry.getValue()
                    .sort(
                            (o1, o2) -> {
                                int o1Order = Integer.MAX_VALUE;
                                TestEngine.Order o1Annotation =
                                        o1.getAnnotation(TestEngine.Order.class);
                                if (o1Annotation != null) {
                                    o1Order = o1Annotation.order();
                                }

                                int o2Order = Integer.MAX_VALUE;
                                TestEngine.Order o2Annotation =
                                        o2.getAnnotation(TestEngine.Order.class);
                                if (o2Annotation != null) {
                                    o2Order = o2Annotation.order();
                                }

                                if (o1Order != o2Order) {
                                    return -Integer.compare(o1Order, o2Order);
                                }

                                // Order by display name which is either
                                // the name declared by @TestEngine.DisplayName
                                // or the real method name
                                String o1DisplayName = getDisplayName(o1);
                                String o2DisplayName = getDisplayName(o2);

                                return -o1DisplayName.compareTo(o2DisplayName);
                            });
        }

        // Clear the test method list
        testMethods.clear();

        // Rebuild the test method set
        for (Map.Entry<Class<?>, List<Method>> entry : methodMap.entrySet()) {
            testMethods.addAll(entry.getValue());
        }

        Collections.reverse(testMethods);
    }
}
