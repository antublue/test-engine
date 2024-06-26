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

package org.antublue.test.engine.internal.support;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.antublue.test.engine.api.TestEngine;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.util.ClassUtils;

/** Class to implement OrderSupport */
public class OrdererSupport {

    /** DefaultMethodOrderTopDownComparator */
    private static final DefaultMethodOrderTopDownComparator
            DEFAULT_METHOD_ORDER_TOP_DOWN_COMPARATOR = new DefaultMethodOrderTopDownComparator();

    /** DefaultMethodOrderBottomUpComparator */
    private static final DefaultMethodOrderBottomUpComparator
            DEFAULT_METHOD_ORDER_BOTTOM_UP_COMPARATOR = new DefaultMethodOrderBottomUpComparator();

    /** TestEngineOrderAnnotationMethodComparator */
    private static final TestEngineOrderAnnotationMethodComparator
            TEST_ENGINE_ORDER_ANNOTATION_COMPARATOR =
                    new TestEngineOrderAnnotationMethodComparator();

    /** MethodNameComparator */
    private static final MethodNameComparator METHOD_NAME_COMPARATOR = new MethodNameComparator();

    /** Constructor */
    private OrdererSupport() {
        // DO NOTHING
    }

    /**
     * Method to order a List of Classes
     *
     * @param testClasses testClasses
     */
    public static void orderTestClasses(List<Class<?>> testClasses) {
        testClasses.sort(Comparator.comparing(Class::getName));
        testClasses.sort(Comparator.comparing(DisplayNameSupport::getDisplayName));
        testClasses.sort(Comparator.comparingInt(OrdererSupport::getOrderAnnotation));
    }

    /**
     * Method to get the order annotation value
     *
     * @param clazz clazz
     * @return the order annotation value
     */
    public static int getOrderAnnotation(Class<?> clazz) {
        int order = Integer.MAX_VALUE;

        TestEngine.Order annotation = clazz.getAnnotation(TestEngine.Order.class);
        if (annotation != null) {
            order = annotation.order();
        }

        return order;
    }

    /**
     * Method to order test methods within the hierarchy, keeping the groups by component type /
     * declaring class
     *
     * @param testMethods testMethods
     * @param hierarchyTraversalMode hierarchyTraversalMode
     * @return the list of methods ordered
     */
    public static List<Method> orderTestMethods(
            List<Method> testMethods, HierarchyTraversalMode hierarchyTraversalMode) {
        // The code assumes that the list of already ordered by component type / declaring class

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

        Comparator<Method> methodComparator = DEFAULT_METHOD_ORDER_TOP_DOWN_COMPARATOR;
        if (hierarchyTraversalMode == HierarchyTraversalMode.BOTTOM_UP) {
            methodComparator = DEFAULT_METHOD_ORDER_BOTTOM_UP_COMPARATOR;
        }

        // Sort methods for each group and add them to the list
        for (Map.Entry<Class<?>, List<Method>> entry : methodMap.entrySet()) {
            entry.getValue().sort(methodComparator);
        }

        List<Method> orderedMethods = new ArrayList<>();
        for (Class<?> key : methodMap.keySet()) {
            List<Method> methods = methodMap.get(key);
            orderedMethods.addAll(methods);
        }

        return orderedMethods;
    }

    /** Class to order methods first by order annotation then by method name */
    private static class DefaultMethodOrderTopDownComparator implements Comparator<Method> {

        @Override
        public int compare(Method method1, Method method2) {
            int comparison = TEST_ENGINE_ORDER_ANNOTATION_COMPARATOR.compare(method1, method2);
            if (comparison == 0) {
                comparison = METHOD_NAME_COMPARATOR.compare(method1, method2);
            }
            return comparison;
        }
    }

    /** Class to order methods based on @TestEngine.Order annotation */
    private static class DefaultMethodOrderBottomUpComparator implements Comparator<Method> {

        @Override
        public int compare(Method method1, Method method2) {
            int comparison = TEST_ENGINE_ORDER_ANNOTATION_COMPARATOR.compare(method1, method2);
            if (comparison == 0) {
                comparison = -METHOD_NAME_COMPARATOR.compare(method1, method2);
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
