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
public final class TestEngineReflectionUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEngineReflectionUtils.class);

    private static final TestEngineReflectionUtils SINGLETON = new TestEngineReflectionUtils();

    private static final Object[] NO_OBJECT_ARGS = null;

    private enum Sort {
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

    private static final Predicate<Field> RANDOM_FIELD_FILTER =
            field ->
                    field.isAnnotationPresent(TestEngine.Random.Boolean.class)
                            || field.isAnnotationPresent(TestEngine.Random.Integer.class)
                            || field.isAnnotationPresent(TestEngine.Random.Long.class)
                            || field.isAnnotationPresent(TestEngine.Random.Float.class)
                            || field.isAnnotationPresent(TestEngine.Random.Double.class);

    private final Predicate<Field> AUTO_CLOSE_FIELD_FILTER =
            field -> field.isAnnotationPresent(TestEngine.AutoClose.class);

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
    private final Map<Class<?>, List<Field>> argumentFieldCache = new HashMap<>();
    private final Map<Class<?>, List<Field>> autoCloseFieldCache = new HashMap<>();
    private final Map<Class<?>, List<Method>> prepareMethodCache = new HashMap<>();
    private final Map<Class<?>, List<Method>> beforeAllMethodCache = new HashMap<>();
    private final Map<Class<?>, List<Method>> beforeEachMethodCache = new HashMap<>();
    private final Map<Class<?>, List<Method>> testMethodCache = new HashMap<>();
    private final Map<Class<?>, List<Method>> afterEachMethodCache = new HashMap<>();
    private final Map<Class<?>, List<Method>> afterAllMethodCache = new HashMap<>();
    private final Map<Class<?>, List<Method>> concludeMethodCache = new HashMap<>();

    /** Constructor */
    private TestEngineReflectionUtils() {
        // DO NOTHING
    }

    /**
     * Method to get the singleton instance
     *
     * @return the singleton instance
     */
    public static TestEngineReflectionUtils singleton() {
        return SINGLETON;
    }

    /**
     * Method to find all test classes for a URI
     *
     * @param uri uri
     * @return the return value
     */
    public List<Class<?>> findAllTestClasses(URI uri) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("findAllTestClasses uri [%s]", uri.toASCIIString());
        }

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
     * Method to test if a Class is a test class
     *
     * @param clazz clazz
     * @return true if the Class is a test class, otherwise false
     */
    public boolean isTestClass(Class<?> clazz) {
        boolean isTestClass;

        TestEngineReflectionUtils testEngineReflectionUtils = TestEngineReflectionUtils.singleton();

        isTestClass =
                !clazz.isAnnotationPresent(TestEngine.BaseClass.class)
                        && !clazz.isAnnotationPresent(TestEngine.Disabled.class)
                        && !Modifier.isAbstract(clazz.getModifiers())
                        && !testEngineReflectionUtils.getTestMethods(clazz).isEmpty();

        LOGGER.trace("isTestClass class [%s] result [%b] ", clazz.getName(), isTestClass);

        return isTestClass;
    }

    /**
     * Method to test if a Method is a test method
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

    /**
     * Method to get the @TestEngine.ArgumentSupplier method
     *
     * @param clazz clazz
     * @return the argument supplier method
     */
    public Method getArgumentSupplierMethod(Class<?> clazz) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("getArgumentSupplierMethod class [%s]", clazz.getName());
        }

        Method argumentSupplierMethod;

        synchronized (argumentSupplierMethodCache) {
            argumentSupplierMethod = argumentSupplierMethodCache.get(clazz);

            if (argumentSupplierMethod == null) {
                ReflectionUtils reflectionUtils = ReflectionUtils.singleton();

                List<Method> methods =
                        reflectionUtils
                                .getMethods(clazz, ReflectionUtils.Order.CLASS_FIRST)
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
                                    clazz, methods.size()));
                }

                argumentSupplierMethod = methods.get(0);

                argumentSupplierMethodCache.put(clazz, argumentSupplierMethod);
            }
        }

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "class [%s] @TestEngine.ArgumentSupplier method [%s]",
                    clazz.getName(), argumentSupplierMethod.getName());
        }

        return argumentSupplierMethod;
    }

    /**
     * Method to get a List of Arguments for a Class
     *
     * @param clazz class to inspect
     * @return list of Arguments
     */
    public List<Argument> getArgumentsList(Class<?> clazz) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("getArgumentsList class [%s]", clazz.getName());
        }

        try {
            Method method = getArgumentSupplierMethod(clazz);
            Object object = method.invoke(null, NO_OBJECT_ARGS);
            if (object instanceof Stream) {
                List<Argument> arguments = ((Stream<Argument>) object).collect(Collectors.toList());
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace(
                            "class [%s] argument count [%d]", clazz.getName(), arguments.size());
                }
                return arguments;
            } else if (object instanceof Iterable) {
                List<Argument> arguments = new ArrayList<>();
                ((Iterable<Argument>) object).forEach(arguments::add);
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace(
                            "class [%s] argument count [%d]", clazz.getName(), arguments.size());
                }
                return arguments;
            } else {
                throw new TestClassConfigurationException(
                        String.format(
                                "Test class [%s] @TestEngine.ArgumentSupplier method must return"
                                        + " Stream<Argument> or Iterable<Argument>",
                                clazz.getName()));
            }
        } catch (TestClassConfigurationException e) {
            throw e;
        } catch (Throwable t) {
            t.printStackTrace();
            throw new TestClassConfigurationException(
                    String.format(
                            "Can't get Stream<Argument> or Iterable<Argument> from test class [%s]",
                            clazz.getName()),
                    t);
        }
    }

    /**
     * Method to get a List of @TestEngine.Prepare Methods
     *
     * @param clazz class to inspect
     * @return list of Methods
     */
    public List<Method> getPrepareMethods(Class<?> clazz) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("getPrepareMethods class [%s]", clazz.getName());
        }

        List<Method> methods;

        synchronized (prepareMethodCache) {
            methods = prepareMethodCache.get(clazz);

            if (methods == null) {
                ReflectionUtils reflectionUtils = ReflectionUtils.singleton();

                methods =
                        reflectionUtils
                                .getMethods(clazz, ReflectionUtils.Order.SUPERCLASS_FIRST)
                                .stream()
                                .filter(PREPARE_METHOD_FILTER)
                                .collect(Collectors.toList());

                sortMethods(methods, Sort.NORMAL);
                validateDistinctOrder(clazz, methods);

                prepareMethodCache.put(clazz, methods);
            }
        }

        if (LOGGER.isTraceEnabled()) {
            synchronized (LOGGER) {
                LOGGER.trace(
                        " class [%s] @TestEngine.Prepare method count [%d]",
                        clazz.getName(), methods.size());

                for (Method method : methods) {
                    LOGGER.trace(
                            " class [%s] @TestEngine.Prepare method [%s]",
                            clazz.getName(), method.getName());
                }
            }
        }

        return methods;
    }

    /**
     * Method to get @TestEngine.Argument Field
     *
     * @param clazz class to inspect
     * @return List of Fields
     */
    public List<Field> getArgumentFields(Class<?> clazz) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("getArgumentFields class [%s]", clazz.getName());
        }

        List<Field> fields;

        synchronized (argumentFieldCache) {
            fields = argumentFieldCache.get(clazz);

            if (fields == null) {
                fields =
                        ReflectionUtils.singleton().getFields(clazz).stream()
                                .filter(ARGUMENT_FIELD_FILTER)
                                .peek(field -> field.setAccessible(true))
                                .collect(Collectors.toList());

                argumentFieldCache.put(clazz, fields);
            }
        }

        if (LOGGER.isTraceEnabled()) {
            synchronized (LOGGER) {
                LOGGER.trace(
                        " class [%s] @TestEngine.Argument field count [%d]",
                        clazz.getName(), fields.size());

                for (Field field : fields) {
                    LOGGER.trace(
                            " class [%s] @TestEngine.Argument argument field [%s]",
                            clazz.getName(), field.getName());
                }
            }
        }

        return fields;
    }

    /**
     * Method to get @TestEngine.Random.X Fields
     *
     * @param clazz class to inspect
     * @return List of Fields
     */
    public List<Field> getRandomFields(Class<?> clazz) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("getRandomFields class [%s]", clazz.getName());
        }

        List<Field> fields =
                ReflectionUtils.singleton().getFields(clazz).stream()
                        .filter(RANDOM_FIELD_FILTER)
                        .peek(field -> field.setAccessible(true))
                        .collect(Collectors.toList());

        if (LOGGER.isTraceEnabled()) {
            synchronized (LOGGER) {
                LOGGER.trace(
                        " class [%s] @TestEngine.Random.X field count [%d]",
                        clazz.getName(), fields.size());

                for (Field field : fields) {
                    LOGGER.trace(
                            " class [%s] @TestEngine.Random.X field [%s] type [%s]",
                            clazz.getName(), field.getName(), field.getType().getName());
                }
            }
        }

        return fields;
    }

    /**
     * Method to get a List of @TestEngine.AutoClose Fields
     *
     * @param clazz class to inspect
     * @return List of Fields
     */
    public List<Field> getAutoCloseFields(Class<?> clazz) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("getAutoCloseFields class [%s]", clazz.getName());
        }

        List<Field> fields;

        synchronized (autoCloseFieldCache) {
            fields = autoCloseFieldCache.get(clazz);

            if (fields == null) {
                fields =
                        ReflectionUtils.singleton().getFields(clazz).stream()
                                .filter(AUTO_CLOSE_FIELD_FILTER)
                                .peek(field -> field.setAccessible(true))
                                .collect(Collectors.toList());

                autoCloseFieldCache.put(clazz, fields);
            }
        }

        if (LOGGER.isTraceEnabled()) {
            synchronized (LOGGER) {
                LOGGER.trace(
                        " class [%s] @TestEngine.AutoClose field count [%d]",
                        clazz.getName(), fields.size());

                for (Field field : fields) {
                    LOGGER.trace(
                            " class [%s] @TestEngine.AutoClose field [%s]",
                            clazz.getName(), field.getName());
                }
            }
        }

        return fields;
    }

    /**
     * Method to get a List of @TestEngine.BeforeAll Methods
     *
     * @param clazz class to inspect
     * @return list of Methods
     */
    public List<Method> getBeforeAllMethods(Class<?> clazz) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("getBeforeAllMethods class [%s]", clazz.getName());
        }

        List<Method> methods;

        synchronized (beforeAllMethodCache) {
            methods = beforeAllMethodCache.get(clazz);

            if (methods == null) {
                ReflectionUtils reflectionUtils = ReflectionUtils.singleton();

                methods =
                        reflectionUtils
                                .getMethods(clazz, ReflectionUtils.Order.SUPERCLASS_FIRST)
                                .stream()
                                .filter(BEFORE_ALL_METHOD_FILTER)
                                .collect(Collectors.toList());

                sortMethods(methods, Sort.NORMAL);
                validateDistinctOrder(clazz, methods);

                beforeAllMethodCache.put(clazz, methods);
            }
        }

        if (LOGGER.isTraceEnabled()) {
            synchronized (LOGGER) {
                LOGGER.trace(
                        " class [%s] @TestEngine.BeforeAll method count [%d]",
                        clazz.getName(), methods.size());

                for (Method method : methods) {
                    LOGGER.trace(
                            " class [%s] @TestEngine.BeforeAll method [%s]",
                            clazz.getName(), method.getName());
                }
            }
        }

        return methods;
    }

    /**
     * Method to get a List of @TestEngine.BeforeEach Methods
     *
     * @param clazz class to inspect
     * @return list of Methods
     */
    public List<Method> getBeforeEachMethods(Class<?> clazz) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("getBeforeEachMethods class [%s]", clazz.getName());
        }

        List<Method> methods;

        synchronized (beforeEachMethodCache) {
            methods = beforeEachMethodCache.get(clazz);

            if (methods == null) {
                ReflectionUtils reflectionUtils = ReflectionUtils.singleton();

                methods =
                        reflectionUtils
                                .getMethods(clazz, ReflectionUtils.Order.SUPERCLASS_FIRST)
                                .stream()
                                .filter(BEFORE_EACH_METHOD_FILTER)
                                .collect(Collectors.toList());

                sortMethods(methods, Sort.REVERSE);
                validateDistinctOrder(clazz, methods);

                beforeEachMethodCache.put(clazz, methods);
            }
        }

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    " class [%s] @TestEngine.BeforeEach method count [%d]",
                    clazz.getName(), methods.size());

            for (Method method : methods) {
                LOGGER.trace(
                        " class [%s] @TestEngine.BeforeEach method [%s]",
                        clazz.getName(), method.getName());
            }
        }

        return methods;
    }

    /**
     * Method to get a List of @TestEngine.Test Methods
     *
     * @param clazz class to inspect
     * @return list of Methods
     */
    public List<Method> getTestMethods(Class<?> clazz) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("getTestMethods class [%s]", clazz.getName());
        }

        List<Method> methods;

        synchronized (testMethodCache) {
            methods = testMethodCache.get(clazz);

            if (methods == null) {
                ReflectionUtils reflectionUtils = ReflectionUtils.singleton();

                methods =
                        reflectionUtils
                                .getMethods(clazz, ReflectionUtils.Order.CLASS_FIRST)
                                .stream()
                                .filter(TEST_METHOD_FILTER)
                                .collect(Collectors.toList());

                sortMethods(methods, Sort.NORMAL);
                validateDistinctOrder(clazz, methods);

                testMethodCache.put(clazz, methods);
            }
        }

        if (LOGGER.isTraceEnabled()) {
            synchronized (LOGGER) {
                LOGGER.trace(
                        " class [%s] @TestEngine.Test method count [%d]",
                        clazz.getName(), methods.size());

                for (Method method : methods) {
                    LOGGER.trace(
                            " class [%s] @TestEngine.Test method [%s]",
                            clazz.getName(), method.getName());
                }
            }
        }

        return methods;
    }

    /**
     * Method to get a List of @TestEngine.AfterEach Methods
     *
     * @param clazz class to inspect
     * @return list of Methods
     */
    public List<Method> getAfterEachMethods(Class<?> clazz) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("getAfterEachMethods class [%s]", clazz.getName());
        }

        List<Method> methods;

        synchronized (afterEachMethodCache) {
            methods = afterEachMethodCache.get(clazz);

            if (methods == null) {
                ReflectionUtils reflectionUtils = ReflectionUtils.singleton();

                methods =
                        reflectionUtils
                                .getMethods(clazz, ReflectionUtils.Order.CLASS_FIRST)
                                .stream()
                                .filter(AFTER_EACH_METHOD_FILTER)
                                .collect(Collectors.toList());

                sortMethods(methods, Sort.REVERSE);
                validateDistinctOrder(clazz, methods);

                afterEachMethodCache.put(clazz, methods);
            }
        }

        if (LOGGER.isTraceEnabled()) {
            synchronized (LOGGER) {
                LOGGER.trace(
                        " class [%s] @TestEngine.AfterEach method count [%d]",
                        clazz.getName(), methods.size());

                for (Method method : methods) {
                    LOGGER.info(
                            " class [%s] @TestEngine.AfterEach method [%s]",
                            clazz.getName(), method.getName());
                }
            }
        }

        return methods;
    }

    /**
     * Method to get a List of @TestEngine.AfterAll Methods
     *
     * @param clazz class to inspect
     * @return list of Methods
     */
    public List<Method> getAfterAllMethods(Class<?> clazz) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("getAfterAllMethods class [%s]", clazz.getName());
        }

        List<Method> methods;

        synchronized (afterAllMethodCache) {
            methods = afterAllMethodCache.get(clazz);

            if (methods == null) {
                ReflectionUtils reflectionUtils = ReflectionUtils.singleton();

                methods =
                        reflectionUtils
                                .getMethods(clazz, ReflectionUtils.Order.CLASS_FIRST)
                                .stream()
                                .filter(AFTER_ALL_METHOD_FILTER)
                                .collect(Collectors.toList());

                sortMethods(methods, Sort.REVERSE);
                validateDistinctOrder(clazz, methods);

                afterAllMethodCache.put(clazz, methods);
            }
        }

        if (LOGGER.isTraceEnabled()) {
            synchronized (LOGGER) {
                LOGGER.trace(
                        " class [%s] @TestEngine.AfterAll method count [%d]",
                        clazz.getName(), methods.size());

                for (Method method : methods) {
                    LOGGER.trace(
                            " class [%s] @TestEngine.AfterAll method [%s]",
                            clazz.getName(), method.getName());
                }
            }
        }

        return methods;
    }

    /**
     * Method to get a @TestEngine.Conclude Methods
     *
     * @param clazz class to inspect
     * @return Method the return value
     */
    public List<Method> getConcludeMethods(Class<?> clazz) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("getConcludeMethods class [%s]", clazz.getName());
        }

        List<Method> methods;

        synchronized (concludeMethodCache) {
            methods = concludeMethodCache.get(clazz);

            if (methods == null) {
                ReflectionUtils reflectionUtils = ReflectionUtils.singleton();

                methods =
                        reflectionUtils
                                .getMethods(clazz, ReflectionUtils.Order.CLASS_FIRST)
                                .stream()
                                .filter(CONCLUDE_METHOD_FILTER)
                                .collect(Collectors.toList());

                sortMethods(methods, Sort.REVERSE);
                validateDistinctOrder(clazz, methods);

                concludeMethodCache.put(clazz, methods);
            }
        }

        if (LOGGER.isTraceEnabled()) {
            synchronized (LOGGER) {
                LOGGER.trace(
                        "class [%s] @TestEngine.Conclude method count [%d]",
                        clazz.getName(), methods.size());

                for (Method method : methods) {
                    LOGGER.trace(
                            "class [%s] @TestEngine.Conclude method [%s]",
                            clazz.getName(), method.getName());
                }
            }
        }

        return methods;
    }

    /**
     * Method to get a test method display name
     *
     * @param method method to get the display name for
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

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "getDisplayName method [%s] display name [%s]", method.getName(), displayName);
        }

        return displayName;
    }

    /**
     * Method to get a test method display name
     *
     * @param clazz class to get the display name for
     * @return the display name
     */
    public String getDisplayName(Class<?> clazz) {
        String displayName = clazz.getName();

        TestEngine.DisplayName annotation = clazz.getAnnotation(TestEngine.DisplayName.class);
        if (annotation != null) {
            String name = annotation.name();
            if (name != null && !name.trim().isEmpty()) {
                displayName = name.trim();
            }
        }

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "getDisplayName class [%s] display name [%s]", clazz.getName(), displayName);
        }

        return displayName;
    }

    /**
     * Method to sort a List of classes first by @TestEngine.Order annotation, then by display name
     * / class name
     *
     * @param classes list of Classes to sort
     */
    private void sortClasses(List<Class<?>> classes) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("sortClasses count [%s]", classes.size());
        }

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
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("validateDistinctOrder count [%s]", classes.size());
        }

        Map<Integer, Class<?>> orderToClassMap = new LinkedHashMap<>();

        for (Class<?> clazz : classes) {
            if (!clazz.isAnnotationPresent(TestEngine.BaseClass.class)
                    && !Modifier.isAbstract(clazz.getModifiers())
                    && clazz.isAnnotationPresent(TestEngine.Order.class)) {
                int order = clazz.getAnnotation(TestEngine.Order.class).order();
                LOGGER.trace("clazz [%s] order [%d]", clazz.getName(), order);
                if (orderToClassMap.containsKey(order)) {
                    Class<?> existingClass = orderToClassMap.get(order);
                    throw new TestClassConfigurationException(
                            String.format(
                                    "Test class [%s] (or superclass) and test class [%s]"
                                            + " (or superclass) contain duplicate"
                                            + " @TestEngine.Order(%d) class annotation",
                                    existingClass.getName(), clazz.getName(), order));
                } else {
                    orderToClassMap.put(order, clazz);
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
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("sortMethods count [%s]", methods.size());
        }

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
     * @param clazz clazz
     * @param methods methods
     */
    private void validateDistinctOrder(Class<?> clazz, List<Method> methods) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "validateDistinctOrder class [%s] count [%s]", clazz.getName(), methods.size());
        }

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
                                    clazz.getName(), method.getName(), value));
                } else {
                    integerSet.add(value);
                }
            }
        }
    }
}
