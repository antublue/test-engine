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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.junit.platform.commons.util.ClassUtils;

public class TestUtils {

    private static final TestUtils SINGLETON = new TestUtils();

    private static final ReflectionUtils REFLECTION_UTILS = ReflectionUtils.getSingleton();

    private static final DefaultMethodOrderComparator DEFAULT_METHOD_ORDER_COMPARATOR =
            new DefaultMethodOrderComparator();

    private static final TestEngineOrderAnnotationMethodComparator
            TEST_ENGINE_ORDER_ANNOTATION_COMPARATOR = new TestEngineOrderAnnotationMethodComparator();

    private static final MethodNameComparator METHOD_NAME_COMPARATOR = new MethodNameComparator();

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
     * @param annotatedElement annotatedElement
     * @return the tag value
     */
    public String getTag(AnnotatedElement annotatedElement) {
        String tagValue = null;

        TestEngine.Tag annotation = annotatedElement.getAnnotation(TestEngine.Tag.class);
        if (annotation != null) {
            String tag = annotation.tag();
            if (tag != null && !tag.trim().isEmpty()) {
                tagValue = tag.trim();
            }
        }

        return tagValue;
    }

    /**
     * Method to order test methods within the hierarchy, keeping the groups by component type / declaring class
     *
     * @param testMethods testMethods
     * @return the list of methods ordered
     */
    public List<Method> orderTestMethods(List<Method> testMethods) {
        // Group methods based on component type / declaring class
        Map<Class<?>, List<Method>> methodMap = new LinkedHashMap<>();
        for (Method method : testMethods) {
            Class<?> componentType = method.getDeclaringClass().getComponentType();
            if (componentType == null) {
                componentType = method.getDeclaringClass();
            }
            List<Method> methods = methodMap.computeIfAbsent(componentType, k -> new ArrayList<>());
            methods.add(method);
        }

        List<Method> orderedMethods = new ArrayList<>();

        // Sort methods for each group and add them to the list
        for (Map.Entry<Class<?>, List<Method>> entry : methodMap.entrySet()) {
            entry.getValue().sort(DEFAULT_METHOD_ORDER_COMPARATOR);
            orderedMethods.addAll(entry.getValue());
        }

        return orderedMethods;
    }

    private static class DefaultMethodOrderComparator implements Comparator<Method> {

        @Override
        public int compare(Method method1, Method method2) {
            int comparison =
                    TEST_ENGINE_ORDER_ANNOTATION_COMPARATOR.compare(
                            method1, method2);
            if (comparison == 0) {
                comparison = METHOD_NAME_COMPARATOR.compare(method1, method2);
            }
            return comparison;
        }
    }

    /** Class to order methods based on @TestEngine.Order annotation */
    private static class TestEngineOrderAnnotationMethodComparator implements Comparator<Method> {

        private static final int DEFAULT_ORDER = Integer.MAX_VALUE;

        @Override
        public int compare(Method o1, Method o2) {
            int o1Order = DEFAULT_ORDER;
            TestEngine.Order o1Annotation = o1.getAnnotation(TestEngine.Order.class);
            if (o1Annotation != null) {
                o1Order = o1Annotation.order();
            }

            int o2Order = DEFAULT_ORDER;
            TestEngine.Order o2Annotation = o2.getAnnotation(TestEngine.Order.class);
            if (o2Annotation != null) {
                o2Order = o2Annotation.order();
            }

            return Integer.compare(o1Order, o2Order);
        }
    }

    /** Class to order methods based on method name, then parameter types */
    private static class MethodNameComparator implements Comparator<Method> {

        @Override
        public int compare(Method method1, Method method2) {
            int comparison = method1.getName().compareTo(method2.getName());
            if (comparison == 0) {
                comparison =
                        ClassUtils.nullSafeToString(method1.getParameterTypes())
                                .compareTo(
                                        ClassUtils.nullSafeToString(method2.getParameterTypes()));
            }
            return comparison;
        }
    }
}
