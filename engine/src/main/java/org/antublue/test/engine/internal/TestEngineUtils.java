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

package org.antublue.test.engine.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.Extension;
import org.antublue.test.engine.api.Key;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.util.ReflectionUtils;

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

    private static final Object[] NO_OBJECT_ARGS = null;

    public enum Sort {
        NORMAL,
        REVERSE
    }

    private static final Predicate<Field> ARGUMENT_FIELD_FILTER =
            field -> {
                if (!field.isAnnotationPresent(TestEngine.Argument.class)) {
                    return false;
                }
                return Argument.class.isAssignableFrom(field.getType());
            };

    private static final Predicate<Field> ANNOTATED_FIELD_FILTER =
            field ->
                    field.isAnnotationPresent(TestEngine.Argument.class)
                            || field.isAnnotationPresent(TestEngine.AutoClose.class)
                            || field.isAnnotationPresent(TestEngine.RandomBoolean.class)
                            || field.isAnnotationPresent(TestEngine.RandomInteger.class)
                            || field.isAnnotationPresent(TestEngine.RandomLong.class)
                            || field.isAnnotationPresent(TestEngine.RandomFloat.class)
                            || field.isAnnotationPresent(TestEngine.RandomDouble.class)
                            || field.isAnnotationPresent(TestEngine.RandomBigInteger.class)
                            || field.isAnnotationPresent(TestEngine.RandomBigDecimal.class)
                            || field.isAnnotationPresent(TestEngine.UUID.class);

    private static final Predicate<Method> PREPARE_METHOD_FILTER =
            method -> {
                if (!method.isAnnotationPresent(TestEngine.Prepare.class)) {
                    return false;
                }
                if (method.isAnnotationPresent(TestEngine.Disabled.class)) {
                    return false;
                }
                ReflectionUtils reflectionUtils = ReflectionUtils.singleton();
                if (reflectionUtils.isStatic(method)) {
                    return false;
                }
                if (!(reflectionUtils.isPublic(method) || reflectionUtils.isProtected(method))) {
                    return false;
                }
                if (!(reflectionUtils.hasParameterCount(method, 0)
                        || reflectionUtils.acceptsParameters(method, Argument.class))) {
                    return false;
                }
                if (!reflectionUtils.hasReturnType(method, Void.class)) {
                    return false;
                }
                method.setAccessible(true);
                return true;
            };

    private static final Predicate<Method> BEFORE_ALL_METHOD_FILTER =
            method -> {
                if (!method.isAnnotationPresent(TestEngine.BeforeAll.class)) {
                    return false;
                }
                if (method.isAnnotationPresent(TestEngine.Disabled.class)) {
                    return false;
                }
                ReflectionUtils reflectionUtils = ReflectionUtils.singleton();
                if (reflectionUtils.isStatic(method)) {
                    return false;
                }
                if (!(reflectionUtils.isPublic(method) || reflectionUtils.isProtected(method))) {
                    return false;
                }
                if (!(reflectionUtils.hasParameterCount(method, 0)
                        || reflectionUtils.acceptsParameters(method, Argument.class))) {
                    return false;
                }
                if (!reflectionUtils.hasReturnType(method, Void.class)) {
                    return false;
                }
                method.setAccessible(true);
                return true;
            };

    private static final Predicate<Method> BEFORE_EACH_METHOD_FILTER =
            method -> {
                if (!method.isAnnotationPresent(TestEngine.BeforeEach.class)) {
                    return false;
                }
                if (method.isAnnotationPresent(TestEngine.Disabled.class)) {
                    return false;
                }
                ReflectionUtils reflectionUtils = ReflectionUtils.singleton();
                if (reflectionUtils.isStatic(method)) {
                    return false;
                }
                if (!(reflectionUtils.isPublic(method) || reflectionUtils.isProtected(method))) {
                    return false;
                }
                if (!(reflectionUtils.hasParameterCount(method, 0)
                        || reflectionUtils.acceptsParameters(method, Argument.class))) {
                    return false;
                }
                if (!reflectionUtils.hasReturnType(method, Void.class)) {
                    return false;
                }
                method.setAccessible(true);
                return true;
            };

    private static final Predicate<Method> TEST_METHOD_FILTER =
            method -> {
                if (!method.isAnnotationPresent(TestEngine.Test.class)) {
                    return false;
                }
                if (method.isAnnotationPresent(TestEngine.Disabled.class)) {
                    return false;
                }
                ReflectionUtils reflectionUtils = ReflectionUtils.singleton();
                if (reflectionUtils.isStatic(method)) {
                    return false;
                }
                if (!(reflectionUtils.isPublic(method) || reflectionUtils.isProtected(method))) {
                    return false;
                }
                if (!(reflectionUtils.hasParameterCount(method, 0)
                        || reflectionUtils.acceptsParameters(method, Argument.class))) {
                    return false;
                }
                if (!reflectionUtils.hasReturnType(method, Void.class)) {
                    return false;
                }
                method.setAccessible(true);
                return true;
            };

    private static final Predicate<Method> AFTER_EACH_METHOD_FILTER =
            method -> {
                if (!method.isAnnotationPresent(TestEngine.AfterEach.class)) {
                    return false;
                }
                if (method.isAnnotationPresent(TestEngine.Disabled.class)) {
                    return false;
                }
                ReflectionUtils reflectionUtils = ReflectionUtils.singleton();
                if (reflectionUtils.isStatic(method)) {
                    return false;
                }
                if (!(reflectionUtils.isPublic(method) || reflectionUtils.isProtected(method))) {
                    return false;
                }
                if (!(reflectionUtils.hasParameterCount(method, 0)
                        || reflectionUtils.acceptsParameters(method, Argument.class))) {
                    return false;
                }
                if (!reflectionUtils.hasReturnType(method, Void.class)) {
                    return false;
                }
                method.setAccessible(true);
                return true;
            };

    private static final Predicate<Method> AFTER_ALL_METHOD_FILTER =
            method -> {
                if (!method.isAnnotationPresent(TestEngine.AfterAll.class)) {
                    return false;
                }
                if (method.isAnnotationPresent(TestEngine.Disabled.class)) {
                    return false;
                }
                ReflectionUtils reflectionUtils = ReflectionUtils.singleton();
                if (reflectionUtils.isStatic(method)) {
                    return false;
                }
                if (!(reflectionUtils.isPublic(method) || reflectionUtils.isProtected(method))) {
                    return false;
                }
                if (!(reflectionUtils.hasParameterCount(method, 0)
                        || reflectionUtils.acceptsParameters(method, Argument.class))) {
                    return false;
                }
                if (!reflectionUtils.hasReturnType(method, Void.class)) {
                    return false;
                }
                method.setAccessible(true);
                return true;
            };

    private static final Predicate<Method> CONCLUDE_METHOD_FILTER =
            method -> {
                if (!method.isAnnotationPresent(TestEngine.Conclude.class)) {
                    return false;
                }
                if (method.isAnnotationPresent(TestEngine.Disabled.class)) {
                    return false;
                }
                ReflectionUtils reflectionUtils = ReflectionUtils.singleton();
                if (reflectionUtils.isStatic(method)) {
                    return false;
                }
                if (!(reflectionUtils.isPublic(method) || reflectionUtils.isProtected(method))) {
                    return false;
                }
                if (!(reflectionUtils.hasParameterCount(method, 0)
                        || reflectionUtils.acceptsParameters(method, Argument.class))) {
                    return false;
                }
                if (!reflectionUtils.hasReturnType(method, Void.class)) {
                    return false;
                }
                method.setAccessible(true);
                return true;
            };

    private final Map<Class<?>, Method> argumentSupplierMethodCache = new HashMap<>();
    private final Map<Class<?>, List<Method>> extensionSupplierMethodsCache = new HashMap<>();
    private final Map<Class<?>, List<Field>> argumentFieldCache = new HashMap<>();
    private final Map<Class<?>, List<Method>> prepareMethodCache = new HashMap<>();
    private final Map<Class<?>, List<Method>> beforeAllMethodCache = new HashMap<>();
    private final Map<Class<?>, List<Method>> beforeEachMethodCache = new HashMap<>();
    private final Map<Class<?>, List<Method>> testMethodCache = new HashMap<>();
    private final Map<Class<?>, List<Method>> afterEachMethodCache = new HashMap<>();
    private final Map<Class<?>, List<Method>> afterAllMethodCache = new HashMap<>();
    private final Map<Class<?>, List<Method>> concludeMethodCache = new HashMap<>();
    private final Map<String, List<Extension>> extensionsCache = new HashMap<>();

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
        LOGGER.trace("findAllTestClasses uri [%s]", uri.toASCIIString());

        List<Class<?>> testClasses = new ArrayList<>();

        List<Class<?>> classes = ReflectionUtils.singleton().findAllClasses(uri);
        for (Class<?> clazz : classes) {
            if (isTestClass(clazz)) {
                testClasses.add(clazz);
            }
        }

        sortClasses(testClasses);
        validateDistinctOrder(testClasses);

        return testClasses;
    }

    /**
     * Method to find all test classes with a package name
     *
     * @param packageName packageName
     * @return the return value
     */
    public List<Class<?>> findAllTestClasses(String packageName) {
        LOGGER.trace("findAllTestClasses package name [%s]", packageName);

        List<Class<?>> testClasses = new ArrayList<>();

        List<Class<?>> classes = ReflectionUtils.singleton().findAllClasses(packageName);
        for (Class<?> clazz : classes) {
            if (isTestClass(clazz)) {
                testClasses.add(clazz);
            }
        }

        sortClasses(testClasses);
        validateDistinctOrder(testClasses);

        return testClasses;
    }

    /**
     * Method to test if a class is a test class
     *
     * @param testClass testClass
     * @return true if the Class is a test class, otherwise false
     */
    public boolean isTestClass(Class<?> testClass) {
        boolean isTestClass;

        TestEngineUtils testEngineUtils = TestEngineUtils.singleton();

        isTestClass =
                !testClass.isAnnotationPresent(TestEngine.BaseClass.class)
                        && !testClass.isAnnotationPresent(TestEngine.Disabled.class)
                        && !Modifier.isAbstract(testClass.getModifiers())
                        && !testEngineUtils.getTestMethods(testClass).isEmpty();

        LOGGER.trace("isTestClass testClass [%s] result [%b] ", testClass.getName(), isTestClass);

        return isTestClass;
    }

    /**
     * Method to test if a method is a test method
     *
     * @param method method
     * @return true if the Method is a test method, otherwise false
     */
    public boolean isTestMethod(Method method) {
        boolean result =
                !method.isAnnotationPresent(TestEngine.Disabled.class)
                        && getTestMethods(method.getDeclaringClass()).contains(method);

        LOGGER.trace(
                "isTestMethod method [%s] result [%b]",
                method.getDeclaringClass().getName(), result);

        return result;
    }

    /**
     * Method to get the @TestEngine.ArgumentSupplier method for a test class
     *
     * @param testClass testClass
     * @return the argument supplier method
     */
    public Method getArgumentSupplierMethod(Class<?> testClass) {
        LOGGER.trace("getArgumentSupplierMethod testClass [%s]", testClass.getName());

        Method argumentSupplierMethod;

        synchronized (argumentSupplierMethodCache) {
            argumentSupplierMethod = argumentSupplierMethodCache.get(testClass);

            if (argumentSupplierMethod == null) {
                ReflectionUtils reflectionUtils = ReflectionUtils.singleton();

                List<Method> methods =
                        reflectionUtils
                                .getMethods(testClass, ReflectionUtils.Order.CLASS_FIRST)
                                .stream()
                                .filter(
                                        method ->
                                                method.isAnnotationPresent(
                                                        TestEngine.ArgumentSupplier.class))
                                .filter(reflectionUtils::isStatic)
                                .filter(
                                        method ->
                                                reflectionUtils.isPublic(method)
                                                        || reflectionUtils.isProtected(method))
                                .filter(method -> reflectionUtils.hasParameterCount(method, 0))
                                .filter(
                                        method ->
                                                reflectionUtils.hasReturnType(
                                                                method, Iterable.class)
                                                        || reflectionUtils.hasReturnType(
                                                                method, Stream.class))
                                .peek(method -> method.setAccessible(true))
                                .collect(Collectors.toList());

                if (methods.size() != 1) {
                    throw new TestClassConfigurationException(
                            String.format(
                                    "Test class [%s] must define a single"
                                        + " @TestEngineArgument.Supplier method, %d methods found",
                                    testClass, methods.size()));
                }

                argumentSupplierMethod = methods.get(0);

                argumentSupplierMethodCache.put(testClass, argumentSupplierMethod);
            }
        }

        LOGGER.trace(
                "testClass [%s] @TestEngine.ArgumentSupplier method [%s]",
                testClass.getName(), argumentSupplierMethod);

        return argumentSupplierMethod;
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
            Method method = getArgumentSupplierMethod(testClass);
            Object object = method.invoke(null, NO_OBJECT_ARGS);
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
     * Method to get a list of extensions for a test class
     *
     * @param testClass class to inspect
     * @return list of Extensions
     */
    public List<Extension> getExtensions(Class<?> testClass, Sort sort) {
        LOGGER.trace("getExtensions testClass [%s] sort [%s]", testClass.getName(), sort);

        try {
            synchronized (extensionsCache) {
                String key = Key.of(testClass, sort);

                List<Extension> allExtensions = extensionsCache.get(key);
                if (allExtensions != null) {
                    return allExtensions;
                }

                allExtensions = new ArrayList<>();
                List<Method> methods = getExtensionSupplierMethods(testClass);
                for (Method method : methods) {
                    Object object = method.invoke(null, NO_OBJECT_ARGS);
                    if (object instanceof Stream) {
                        List<Extension> extensions =
                                ((Stream<Extension>) object).collect(Collectors.toList());
                        LOGGER.trace(
                                "testClass [%s] extension count [%d]",
                                testClass.getName(), extensions.size());
                        allExtensions.addAll(extensions);
                    } else if (object instanceof Iterable) {
                        List<Extension> extensions = new ArrayList<>();
                        ((Iterable<Extension>) object).forEach(extensions::add);
                        LOGGER.trace(
                                "testClass [%s] extension count [%d]",
                                testClass.getName(), extensions.size());
                        allExtensions.addAll(extensions);
                    } else {
                        throw new TestClassConfigurationException(
                                String.format(
                                        "Test class [%s] @TestEngine.ExtensionSupplier method must"
                                            + " return Stream<Extension> or Iterable<Extension>",
                                        testClass.getName()));
                    }
                }

                List<Extension> allExtensionsReverse = new ArrayList<>(allExtensions);
                Collections.reverse(allExtensionsReverse);

                extensionsCache.put(Key.of(testClass, Sort.NORMAL), allExtensions);
                extensionsCache.put(Key.of(testClass, Sort.REVERSE), allExtensionsReverse);

                if (sort == Sort.NORMAL) {
                    return allExtensions;
                } else {
                    return allExtensionsReverse;
                }
            }
        } catch (TestClassConfigurationException e) {
            throw e;
        } catch (Throwable t) {
            throw new TestClassConfigurationException(
                    String.format(
                            "Can't get Stream<Extension> or Iterable<Extension> from test class"
                                    + " [%s]",
                            testClass.getName()),
                    t);
        }
    }

    /**
     * Method to get the @TestEngine.ExtensionSupplier methods for a test class
     *
     * @param testClass testClass
     * @return the extension supplier method
     */
    private List<Method> getExtensionSupplierMethods(Class<?> testClass) {
        LOGGER.trace("getExtensionSupplierMethods class [%s]", testClass.getName());

        List<Method> extensionSupplierMethods;

        synchronized (extensionSupplierMethodsCache) {
            extensionSupplierMethods = extensionSupplierMethodsCache.get(testClass);

            if (extensionSupplierMethods == null) {
                ReflectionUtils reflectionUtils = ReflectionUtils.singleton();

                extensionSupplierMethods =
                        reflectionUtils
                                .getMethods(testClass, ReflectionUtils.Order.CLASS_FIRST)
                                .stream()
                                .filter(
                                        method ->
                                                method.isAnnotationPresent(
                                                        TestEngine.ExtensionSupplier.class))
                                .filter(reflectionUtils::isStatic)
                                .filter(
                                        method ->
                                                reflectionUtils.isPublic(method)
                                                        || reflectionUtils.isProtected(method))
                                .filter(method -> reflectionUtils.hasParameterCount(method, 0))
                                .filter(
                                        method ->
                                                reflectionUtils.hasReturnType(
                                                                method, Iterable.class)
                                                        || reflectionUtils.hasReturnType(
                                                                method, Stream.class))
                                .peek(method -> method.setAccessible(true))
                                .collect(Collectors.toList());

                extensionSupplierMethodsCache.put(testClass, extensionSupplierMethods);
            }
        }

        if (LOGGER.isTraceEnabled()) {
            synchronized (LOGGER) {
                LOGGER.trace(
                        " testClass [%s] @TestEngine.ExtensionSupplier method count [%d]",
                        testClass.getName(), extensionSupplierMethods.size());

                for (Method method : extensionSupplierMethods) {
                    LOGGER.trace(
                            " testClass [%s] @TestEngine.ExtensionSupplier method [%s]",
                            testClass.getName(), method);
                }
            }
        }

        return extensionSupplierMethods;
    }

    /**
     * Method to get a list of @TestEngine.Prepare method for a test class
     *
     * @param testClass class to inspect
     * @return list of Methods
     */
    public List<Method> getPrepareMethods(Class<?> testClass) {
        LOGGER.trace("getPrepareMethods class [%s]", testClass.getName());

        List<Method> methods;

        synchronized (prepareMethodCache) {
            methods = prepareMethodCache.get(testClass);

            if (methods == null) {
                ReflectionUtils reflectionUtils = ReflectionUtils.singleton();

                methods =
                        reflectionUtils
                                .getMethods(testClass, ReflectionUtils.Order.SUPERCLASS_FIRST)
                                .stream()
                                .filter(PREPARE_METHOD_FILTER)
                                .collect(Collectors.toList());

                sortMethods(methods, Sort.NORMAL);
                validateDistinctOrder(testClass, methods);

                prepareMethodCache.put(testClass, methods);
            }
        }

        if (LOGGER.isTraceEnabled()) {
            synchronized (LOGGER) {
                LOGGER.trace(
                        " testClass [%s] @TestEngine.Prepare method count [%d]",
                        testClass.getName(), methods.size());

                for (Method method : methods) {
                    LOGGER.trace(
                            " testClass [%s] @TestEngine.Prepare method [%s]",
                            testClass.getName(), method);
                }
            }
        }

        return methods;
    }

    /**
     * Method to get @TestEngine.Argument fields for a test class
     *
     * @param testClass class to inspect
     * @return List of Fields
     */
    public List<Field> getArgumentFields(Class<?> testClass) {
        LOGGER.trace("getArgumentFields class [%s]", testClass.getName());

        List<Field> fields;

        synchronized (argumentFieldCache) {
            fields = argumentFieldCache.get(testClass);

            if (fields == null) {
                fields =
                        ReflectionUtils.singleton().getFields(testClass).stream()
                                .filter(ARGUMENT_FIELD_FILTER)
                                .peek(field -> field.setAccessible(true))
                                .collect(Collectors.toList());

                argumentFieldCache.put(testClass, fields);
            }
        }

        if (LOGGER.isTraceEnabled()) {
            synchronized (LOGGER) {
                LOGGER.trace(
                        " testClass [%s] @TestEngine.Argument field count [%d]",
                        testClass.getName(), fields.size());

                for (Field field : fields) {
                    LOGGER.trace(
                            " testClass [%s] @TestEngine.Argument argument field [%s]",
                            testClass.getName(), field.getName());
                }
            }
        }

        return fields;
    }

    /**
     * Method to get @TestEngine.X annotated fields for a test class
     *
     * @param testClass class to inspect
     * @return List of Fields
     */
    public List<Field> getAnnotatedFields(Class<?> testClass) {
        LOGGER.trace("getAnnotatedFields testClass [%s]", testClass.getName());

        List<Field> fields =
                ReflectionUtils.singleton().getFields(testClass).stream()
                        .filter(ANNOTATED_FIELD_FILTER)
                        .peek(field -> field.setAccessible(true))
                        .collect(Collectors.toList());

        if (LOGGER.isTraceEnabled()) {
            synchronized (LOGGER) {
                LOGGER.trace(
                        " testClass [%s] @TestEngine.X field count [%d]",
                        testClass.getName(), fields.size());

                for (Field field : fields) {
                    LOGGER.trace(
                            " testClass [%s] @TestEngine.X field [%s] type [%s]",
                            testClass.getName(), field.getName(), field.getType().getName());
                }
            }
        }

        return fields;
    }

    /**
     * Method to get a list of @TestEngine.BeforeAll methods for a test class
     *
     * @param testClass class to inspect
     * @return list of Methods
     */
    public List<Method> getBeforeAllMethods(Class<?> testClass) {
        LOGGER.trace("getBeforeAllMethods testClass [%s]", testClass.getName());

        List<Method> methods;

        synchronized (beforeAllMethodCache) {
            methods = beforeAllMethodCache.get(testClass);

            if (methods == null) {
                ReflectionUtils reflectionUtils = ReflectionUtils.singleton();

                methods =
                        reflectionUtils
                                .getMethods(testClass, ReflectionUtils.Order.SUPERCLASS_FIRST)
                                .stream()
                                .filter(BEFORE_ALL_METHOD_FILTER)
                                .collect(Collectors.toList());

                sortMethods(methods, Sort.NORMAL);
                validateDistinctOrder(testClass, methods);

                beforeAllMethodCache.put(testClass, methods);
            }
        }

        if (LOGGER.isTraceEnabled()) {
            synchronized (LOGGER) {
                LOGGER.trace(
                        " testClass [%s] @TestEngine.BeforeAll method count [%d]",
                        testClass.getName(), methods.size());

                for (Method method : methods) {
                    LOGGER.trace(
                            " testClass [%s] @TestEngine.BeforeAll method [%s]",
                            testClass.getName(), method);
                }
            }
        }

        return methods;
    }

    /**
     * Method to get a list of @TestEngine.BeforeEach methods for a test class
     *
     * @param testClass class to inspect
     * @return list of Methods
     */
    public List<Method> getBeforeEachMethods(Class<?> testClass) {
        LOGGER.trace("getBeforeEachMethods testClass [%s]", testClass.getName());

        List<Method> methods;

        synchronized (beforeEachMethodCache) {
            methods = beforeEachMethodCache.get(testClass);

            if (methods == null) {
                ReflectionUtils reflectionUtils = ReflectionUtils.singleton();

                methods =
                        reflectionUtils
                                .getMethods(testClass, ReflectionUtils.Order.SUPERCLASS_FIRST)
                                .stream()
                                .filter(BEFORE_EACH_METHOD_FILTER)
                                .collect(Collectors.toList());

                sortMethods(methods, Sort.REVERSE);
                validateDistinctOrder(testClass, methods);

                beforeEachMethodCache.put(testClass, methods);
            }
        }

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    " testClass [%s] @TestEngine.BeforeEach method count [%d]",
                    testClass.getName(), methods.size());

            for (Method method : methods) {
                LOGGER.trace(
                        " testClass [%s] @TestEngine.BeforeEach method [%s]",
                        testClass.getName(), method);
            }
        }

        return methods;
    }

    /**
     * Method to get a list of @TestEngine.Test methods for a test class
     *
     * @param testClass class to inspect
     * @return list of Methods
     */
    public List<Method> getTestMethods(Class<?> testClass) {
        LOGGER.trace("getTestMethods testClass [%s]", testClass.getName());

        List<Method> methods;

        synchronized (testMethodCache) {
            methods = testMethodCache.get(testClass);

            if (methods == null) {
                ReflectionUtils reflectionUtils = ReflectionUtils.singleton();

                methods =
                        reflectionUtils
                                .getMethods(testClass, ReflectionUtils.Order.CLASS_FIRST)
                                .stream()
                                .filter(TEST_METHOD_FILTER)
                                .collect(Collectors.toList());

                sortMethods(methods, Sort.NORMAL);
                validateDistinctOrder(testClass, methods);

                testMethodCache.put(testClass, methods);
            }
        }

        if (LOGGER.isTraceEnabled()) {
            synchronized (LOGGER) {
                LOGGER.trace(
                        " testClass [%s] @TestEngine.Test method count [%d]",
                        testClass.getName(), methods.size());

                for (Method method : methods) {
                    LOGGER.trace(
                            " testClass [%s] @TestEngine.Test method [%s]",
                            testClass.getName(), method);
                }
            }
        }

        return methods;
    }

    /**
     * Method to get a list of @TestEngine.AfterEach methods for a test class
     *
     * @param testClass class to inspect
     * @return list of Methods
     */
    public List<Method> getAfterEachMethods(Class<?> testClass) {
        LOGGER.trace("getAfterEachMethods testClass [%s]", testClass.getName());

        List<Method> methods;

        synchronized (afterEachMethodCache) {
            methods = afterEachMethodCache.get(testClass);

            if (methods == null) {
                ReflectionUtils reflectionUtils = ReflectionUtils.singleton();

                methods =
                        reflectionUtils
                                .getMethods(testClass, ReflectionUtils.Order.CLASS_FIRST)
                                .stream()
                                .filter(AFTER_EACH_METHOD_FILTER)
                                .collect(Collectors.toList());

                sortMethods(methods, Sort.REVERSE);
                validateDistinctOrder(testClass, methods);

                afterEachMethodCache.put(testClass, methods);
            }
        }

        if (LOGGER.isTraceEnabled()) {
            synchronized (LOGGER) {
                LOGGER.trace(
                        " testClass [%s] @TestEngine.AfterEach method count [%d]",
                        testClass.getName(), methods.size());

                for (Method method : methods) {
                    LOGGER.trace(
                            " testClass [%s] @TestEngine.AfterEach method [%s]",
                            testClass.getName(), method);
                }
            }
        }

        return methods;
    }

    /**
     * Method to get a list of @TestEngine.AfterAll methods for a test class
     *
     * @param testClass class to inspect
     * @return list of Methods
     */
    public List<Method> getAfterAllMethods(Class<?> testClass) {
        LOGGER.trace("getAfterAllMethods testClass [%s]", testClass.getName());

        List<Method> methods;

        synchronized (afterAllMethodCache) {
            methods = afterAllMethodCache.get(testClass);

            if (methods == null) {
                ReflectionUtils reflectionUtils = ReflectionUtils.singleton();

                methods =
                        reflectionUtils
                                .getMethods(testClass, ReflectionUtils.Order.CLASS_FIRST)
                                .stream()
                                .filter(AFTER_ALL_METHOD_FILTER)
                                .collect(Collectors.toList());

                sortMethods(methods, Sort.REVERSE);
                validateDistinctOrder(testClass, methods);

                afterAllMethodCache.put(testClass, methods);
            }
        }

        if (LOGGER.isTraceEnabled()) {
            synchronized (LOGGER) {
                LOGGER.trace(
                        " testClass [%s] @TestEngine.AfterAll method count [%d]",
                        testClass.getName(), methods.size());

                for (Method method : methods) {
                    LOGGER.trace(
                            " testClass [%s] @TestEngine.AfterAll method [%s]",
                            testClass.getName(), method);
                }
            }
        }

        return methods;
    }

    /**
     * Method to get a list of @TestEngine.Conclude methods for a test class
     *
     * @param testClass class to inspect
     * @return Method the return value
     */
    public List<Method> getConcludeMethods(Class<?> testClass) {
        LOGGER.trace("getConcludeMethods class [%s]", testClass.getName());

        List<Method> methods;

        synchronized (concludeMethodCache) {
            methods = concludeMethodCache.get(testClass);

            if (methods == null) {
                ReflectionUtils reflectionUtils = ReflectionUtils.singleton();

                methods =
                        reflectionUtils
                                .getMethods(testClass, ReflectionUtils.Order.CLASS_FIRST)
                                .stream()
                                .filter(CONCLUDE_METHOD_FILTER)
                                .collect(Collectors.toList());

                sortMethods(methods, Sort.REVERSE);
                validateDistinctOrder(testClass, methods);

                concludeMethodCache.put(testClass, methods);
            }
        }

        if (LOGGER.isTraceEnabled()) {
            synchronized (LOGGER) {
                LOGGER.trace(
                        " testClass [%s] @TestEngine.Conclude method count [%d]",
                        testClass.getName(), methods.size());

                for (Method method : methods) {
                    LOGGER.trace(
                            " testClass [%s] @TestEngine.Conclude method [%s]",
                            testClass.getName(), method);
                }
            }
        }

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
    private void sortMethods(List<Method> methods, Sort sort) {
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
    private void validateDistinctOrder(Class<?> testClass, List<Method> methods) {
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

    /**
     * Method to return if a Method accepts an Argument
     *
     * @param method method
     * @param argument argument
     * @return the return value
     */
    public boolean acceptsArgument(Method method, Argument argument) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        return parameterTypes.length == 1
                && parameterTypes[0].isAssignableFrom(argument.getClass());
    }
}
