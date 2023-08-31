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

package org.antublue.test.engine;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.Extension;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.exception.TestClassConfigurationException;
import org.antublue.test.engine.extension.ExtensionManager;
import org.antublue.test.engine.logger.Logger;
import org.antublue.test.engine.logger.LoggerFactory;
import org.antublue.test.engine.util.ReflectionUtils;

/** Class to implement TestEngineReflectionUtils */
@SuppressWarnings({
    "unchecked",
    "PMD.NPathComplexity",
    "PMD.AvoidAccessibilityAlteration",
    "PMD.EmptyCatchBlock",
    "PMD.UnusedPrivateMethod"
})
public final class TestEngineUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEngineUtils.class);

    private static final TestEngineUtils SINGLETON = new TestEngineUtils();

    private static final ReflectionUtils reflectionUtils = ReflectionUtils.singleton();

    private static final Object[] NO_OBJECT_ARGS = null;

    private enum Sort {
        NORMAL,
        REVERSE
    }

    /** Constructor */
    private TestEngineUtils() {
        // DO NOTHING
    }

    /**
     * Method to get the singleton instance
     *
     * @return the singleton instance
     */
    public static TestEngineUtils singleton() {
        return SINGLETON;
    }

    /**
     * Method to find all test classes for a URI
     *
     * @param uri uri
     * @return the return value
     */
    public List<Class<?>> findAllTestClasses(URI uri) {
        return reflectionUtils
                .findAllClasses(uri, Predicates.TEST_CLASS)
                .collect(Collectors.toList());
    }

    /**
     * Method to find all test classes with a package name
     *
     * @param packageName packageName
     * @return the return value
     */
    public List<Class<?>> findAllTestClasses(String packageName) {
        List<Class<?>> classes =
                reflectionUtils
                        .findAllClasses(packageName, Predicates.TEST_CLASS)
                        .collect(Collectors.toList());

        sortClasses(classes);
        validateDistinctOrder(classes);

        return classes;
    }

    /**
     * Method to get a list of arguments for a test class
     *
     * @param testClass class to inspect
     * @return list of Arguments
     */
    public List<Argument> getArguments(Class<?> testClass) {
        LOGGER.trace("getArguments testClass [%s]", testClass.getName());

        try {
            Optional<Method> optional =
                    reflectionUtils
                            .findMethods(testClass, Predicates.ARGUMENT_SUPPLIER_METHOD)
                            .findFirst();
            if (!optional.isPresent()) {
                throw new TestClassConfigurationException(
                        String.format(
                                "Test class [%s] @TestEngine.ArgumentSupplier method must return"
                                        + " Stream<Argument> or Iterable<Argument>",
                                testClass.getName()));
            }
            Object object = optional.get().invoke(null, NO_OBJECT_ARGS);
            if (object instanceof Stream) {
                List<Argument> arguments = ((Stream<Argument>) object).collect(Collectors.toList());
                LOGGER.trace(
                        "testClass [%s] argument count [%d]",
                        testClass.getName(), arguments.size());
                return arguments;
            } else if (object instanceof Iterable) {
                List<Argument> arguments = new ArrayList<>();
                ((Iterable<Argument>) object).forEach(arguments::add);
                LOGGER.trace(
                        "testClass [%s] argument count [%d]",
                        testClass.getName(), arguments.size());
                return arguments;
            } else {
                throw new TestClassConfigurationException(
                        String.format(
                                "Test class [%s] @TestEngine.ArgumentSupplier method must return"
                                        + " Stream<Argument> or Iterable<Argument>",
                                testClass.getName()));
            }
        } catch (TestClassConfigurationException e) {
            throw e;
        } catch (Throwable t) {
            throw new TestClassConfigurationException(
                    String.format(
                            "Can't get Stream<Argument> or Iterable<Argument> from test class [%s]",
                            testClass.getName()),
                    t);
        }
    }

    /**
     * Method to get @TestEngine.X annotated fields for a test class
     *
     * @param clazz class to inspect
     * @return a List of Fields
     */
    public List<Field> getAnnotatedFields(Class<?> clazz) {
        LOGGER.trace("getAnnotatedFields testClass [%s]", clazz.getName());
        return reflectionUtils
                .findFields(clazz, Predicates.ANNOTATED_FIELD)
                .collect(Collectors.toList());
    }

    /**
     * Method to get the @TestEngine.ArgumentSupplier methods for a test class
     *
     * @param clazz class to inspect
     * @return the argument supplier method
     */
    public List<Method> getArgumentSupplierMethods(Class<?> clazz) {
        LOGGER.trace("getArgumentSupplierMethods class [%s]", clazz.getName());
        return reflectionUtils
                .findMethods(clazz, Predicates.ARGUMENT_SUPPLIER_METHOD)
                .collect(Collectors.toList());
    }

    /**
     * Method to get a list of extensions for a test class
     *
     * @param clazz class to inspect
     * @return a List of Extensions
     */
    public List<Extension> getExtensions(Class<?> clazz) {
        LOGGER.trace("getExtensions class [%s]", clazz);

        try {
            List<Extension> extensions = ExtensionManager.singleton().getExtensions();
            Optional<Method> optional =
                    reflectionUtils
                            .findMethods(clazz, Predicates.EXTENSION_SUPPLIER_METHOD)
                            .findFirst();
            if (optional.isPresent()) {
                Object object = optional.get().invoke(null, NO_OBJECT_ARGS);
                if (object instanceof Stream) {
                    ((Stream<Extension>) object).forEach(extensions::add);
                    return extensions;
                } else if (object instanceof Iterable) {
                    ((Iterable<Extension>) object).forEach(extensions::add);
                    return extensions;
                } else {
                    throw new TestClassConfigurationException(
                            String.format(
                                    "Test class [%s] @TestEngine.ExtensionSupplier method must return"
                                            + " Stream<Extension> or Iterable<Extension>",
                                    clazz.getName()));
                }
            }
            return extensions;
        } catch (TestClassConfigurationException e) {
            throw e;
        } catch (Throwable t) {
            throw new TestClassConfigurationException(
                    String.format(
                            "Can't get Stream<Extension> or Iterable<Extension> from test class"
                                    + " [%s]",
                            clazz.getName()),
                    t);
        }
    }

    /**
     * Method to get a list of @TestEngine.Prepare method for a test class
     *
     * @param clazz class to inspect
     * @return list of Methods
     */
    public List<Method> getPrepareMethods(Class<?> clazz) {
        LOGGER.trace("getPrepareMethods class [%s]", clazz.getName());

        List<Method> methods =
                reflectionUtils
                        .findMethods(clazz, Predicates.PREPARE_METHOD)
                        .collect(Collectors.toList());
        sortMethods(methods, Sort.NORMAL);
        validateDistinctOrder(clazz, methods);

        return methods;
    }

    /**
     * Method to get a list of @TestEngine.BeforeAll methods for a test class
     *
     * @param clazz class to inspect
     * @return a List of Methods
     */
    public List<Method> getBeforeAllMethods(Class<?> clazz) {
        LOGGER.trace("getBeforeAllMethods class [%s]", clazz.getName());

        List<Method> methods =
                reflectionUtils
                        .findMethods(clazz, Predicates.BEFORE_ALL_METHOD)
                        .collect(Collectors.toList());

        sortMethods(methods, Sort.NORMAL);
        validateDistinctOrder(clazz, methods);

        return methods;
    }

    /**
     * Method to get a list of @TestEngine.BeforeEach methods for a test class
     *
     * @param clazz class to inspect
     * @return a List of Methods
     */
    public List<Method> getBeforeEachMethods(Class<?> clazz) {
        LOGGER.trace("getBeforeEachMethods class [%s]", clazz.getName());

        List<Method> methods =
                reflectionUtils
                        .findMethods(clazz, Predicates.BEFORE_EACH_METHOD)
                        .collect(Collectors.toList());

        sortMethods(methods, Sort.NORMAL);
        validateDistinctOrder(clazz, methods);

        return methods;
    }

    /**
     * Method to get a list of @TestEngine.Test methods for a test class
     *
     * @param clazz class to inspect
     * @return a List of Methods
     */
    public List<Method> getTestMethods(Class<?> clazz) {
        LOGGER.trace("getTestMethods testClass [%s]", clazz.getName());

        List<Method> methods =
                reflectionUtils
                        .findMethods(clazz, Predicates.TEST_METHOD)
                        .collect(Collectors.toList());
        sortMethods(methods, Sort.NORMAL);
        validateDistinctOrder(clazz, methods);

        return methods;
    }

    /**
     * Method to get a list of @TestEngine.AfterEach methods for a test class
     *
     * @param clazz class to inspect
     * @return a List of Methods
     */
    public List<Method> getAfterEachMethods(Class<?> clazz) {
        LOGGER.trace("getAfterEachMethods class [%s]", clazz.getName());

        List<Method> methods =
                reflectionUtils
                        .findMethods(clazz, Predicates.AFTER_EACH_METHOD)
                        .collect(Collectors.toList());

        sortMethods(methods, Sort.REVERSE);
        validateDistinctOrder(clazz, methods);

        return methods;
    }

    /**
     * Method to get a list of @TestEngine.AfterAll methods for a test class
     *
     * @param clazz class to inspect
     * @return a List of Methods
     */
    public List<Method> getAfterAllMethods(Class<?> clazz) {
        LOGGER.trace("getAfterAllMethods class [%s]", clazz.getName());

        List<Method> methods =
                reflectionUtils
                        .findMethods(clazz, Predicates.AFTER_ALL_METHOD)
                        .collect(Collectors.toList());

        sortMethods(methods, Sort.REVERSE);
        validateDistinctOrder(clazz, methods);

        return methods;
    }

    /**
     * Method to get a list of @TestEngine.Conclude methods for a test class
     *
     * @param clazz class to inspect
     * @return a List of Methods
     */
    public List<Method> getConcludeMethods(Class<?> clazz) {
        LOGGER.trace("getAfterAllMethods class [%s]", clazz.getName());

        List<Method> methods =
                reflectionUtils
                        .findMethods(clazz, Predicates.CONCLUDE_METHOD)
                        .collect(Collectors.toList());

        sortMethods(methods, Sort.REVERSE);
        validateDistinctOrder(clazz, methods);

        return methods;
    }

    /**
     * Method to get a test method display name
     *
     * @param method method
     * @return the display name
     */
    public String getDisplayName(Method method) {
        String displayName = method.getName();

        TestEngine.DisplayName annotation = method.getAnnotation(TestEngine.DisplayName.class);
        if (annotation != null) {
            String name = annotation.name();
            if (name != null && !name.trim().isEmpty()) {
                displayName = name.trim();
            }
        }

        LOGGER.trace("getDisplayName method [%s] display name [%s]", method, displayName);

        return displayName;
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

        LOGGER.trace(
                "getDisplayName testClass [%s] display name [%s]",
                testClass.getName(), displayName);

        return displayName;
    }

    /**
     * Method to test if a class is a test class
     *
     * @param clazz class to inspect
     * @return true if the Class is a test class, otherwise false
     */
    public boolean isTestClass(Class<?> clazz) {
        boolean isTestClass = Predicates.TEST_CLASS.test(clazz);
        LOGGER.trace("isTestClass class [%s] result [%b]", clazz.getName(), isTestClass);
        return isTestClass;
    }

    public boolean isTestMethod(Method method) {
        boolean isTestMethod = Predicates.TEST_METHOD.test(method);
        LOGGER.trace("isTestMethod method [%s] result [%b]", method.getName(), isTestMethod);
        return isTestMethod;
    }

    /**
     * Method to return whether a method accepts a list of parameter types
     *
     * @param method method
     * @param parameterTypes parameterTypes
     * @return true if the method accepts the parameter types, otherwise false
     */
    public boolean acceptsParameterTypes(Method method, Class<?>... parameterTypes) {
        return reflectionUtils.acceptsParameters(method, parameterTypes);
    }

    /**
     * Method to sort a List of classes first by @TestEngine.Order annotation, then by display name
     * / class name
     *
     * @param classes list of classes to sort
     */
    private void sortClasses(List<Class<?>> classes) {
        LOGGER.trace("sortClasses count [%s]", classes.size());

        classes.sort(
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

                    return o1DisplayName.compareTo(o2DisplayName);
                });
    }

    /*
     * Method to validate @TestEngine.Order is unique on classes
     *
     * @param classes classes
     */
    private void validateDistinctOrder(List<Class<?>> classes) {
        LOGGER.trace("validateDistinctOrder count [%s]", classes.size());

        Map<Integer, Class<?>> orderToClassMap = new LinkedHashMap<>();

        for (Class<?> testClass : classes) {
            if (!testClass.isAnnotationPresent(TestEngine.BaseClass.class)
                    && !Modifier.isAbstract(testClass.getModifiers())
                    && testClass.isAnnotationPresent(TestEngine.Order.class)) {
                int order = testClass.getAnnotation(TestEngine.Order.class).order();
                LOGGER.trace(" testClass [%s] order [%d]", testClass.getName(), order);
                if (orderToClassMap.containsKey(order)) {
                    Class<?> existingClass = orderToClassMap.get(order);
                    throw new TestClassConfigurationException(
                            String.format(
                                    "Test class [%s] (or superclass) and test class [%s]"
                                            + " (or superclass) contain duplicate"
                                            + " @TestEngine.Order(%d) class annotation",
                                    existingClass.getName(), testClass.getName(), order));
                } else {
                    orderToClassMap.put(order, testClass);
                }
            }
        }
    }

    /**
     * Method to sort a List of methods first by @TestEngine.Order annotation, then alphabetically
     *
     * @param methods list of Methods to sort
     */
    public void sortMethods(List<Method> methods, Sort sort) {
        LOGGER.trace("sortMethods count [%s]", methods.size());

        methods.sort(
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
                        case NORMAL:
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
     * Method to validate @TestEngine.Order is unique on methods with the same annotation
     *
     * @param testClass testClass
     * @param methods methods
     */
    public void validateDistinctOrder(Class<?> testClass, List<Method> methods) {
        LOGGER.trace(
                "validateDistinctOrder testClass [%s] count [%s]",
                testClass.getName(), methods.size());

        Map<Class<?>, Set<Integer>> map = new HashMap<>();

        for (Method method : methods) {
            TestEngine.Order annotation = method.getAnnotation(TestEngine.Order.class);
            if (annotation != null) {
                int value = annotation.order();
                Set<Integer> integerSet =
                        map.computeIfAbsent(method.getDeclaringClass(), m -> new HashSet<>());
                if (integerSet.contains(value)) {
                    throw new TestClassConfigurationException(
                            String.format(
                                    "Test class [%s] method [%s] contains a duplicate"
                                            + " @TestEngine.Order(%d) method annotation",
                                    testClass.getName(), method, value));
                } else {
                    integerSet.add(value);
                }
            }
        }
    }
}
