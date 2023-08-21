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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

    private static final Object[] NO_OBJECT_ARGS = null;

    private static final TestEngineReflectionUtils SINGLETON = new TestEngineReflectionUtils();

    private final Map<Class<?>, Method> ARGUMENT_SUPPLIER_METHOD_CACHE = new HashMap<>();
    private final Map<Class<?>, Optional<Field>> ARGUMENT_FIELD_CACHE = new HashMap<>();
    private final Map<Class<?>, List<Field>> AUTO_CLOSE_FIELD_CACHE = new HashMap<>();
    private final Map<Class<?>, List<Method>> PREPARE_METHOD_CACHE = new HashMap<>();
    private final Map<Class<?>, List<Method>> BEFORE_ALL_METHOD_CACHE = new HashMap<>();
    private final Map<Class<?>, List<Method>> BEFORE_EACH_METHOD_CACHE = new HashMap<>();
    private final Map<Class<?>, List<Method>> TEST_METHOD_CACHE = new HashMap<>();
    private final Map<Class<?>, List<Method>> AFTER_EACH_METHOD_CACHE = new HashMap<>();
    private final Map<Class<?>, List<Method>> AFTER_ALL_METHOD_CACHE = new HashMap<>();
    private final Map<Class<?>, List<Method>> CONCLUDE_METHOD_CACHE = new HashMap<>();

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
     * Method to find all classes for a URI
     *
     * @param uri uri
     * @return the return value
     */
    public List<Class<?>> findAllClasses(URI uri) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("findAllClasses uri [%s]", uri.toASCIIString());
        }

        List<Class<?>> classes = ReflectionUtils.singleton().findAllClasses(uri);
        sortClasses(classes);
        return classes;
    }

    /**
     * Method to find all classes with a package name
     *
     * @param packageName packageName
     * @return the return value
     */
    public List<Class<?>> findAllClasses(String packageName) {
        LOGGER.trace("findAllClasses package name [%s]", packageName);

        List<Class<?>> classes = ReflectionUtils.singleton().findAllClasses(packageName);
        sortClasses(classes);
        return classes;
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
     * Method to get a @TestEngine.ArgumentSupplier Method
     *
     * @param clazz class to inspect
     * @return Method the return value
     */
    public Method getArgumentSupplierMethod(Class<?> clazz) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("getArgumentSupplierMethod class [%s]", clazz.getName());
        }

        Method argumentSupplierMethod;

        synchronized (ARGUMENT_SUPPLIER_METHOD_CACHE) {
            argumentSupplierMethod = ARGUMENT_SUPPLIER_METHOD_CACHE.get(clazz);

            if (argumentSupplierMethod == null) {
                ReflectionUtils reflectionUtils = ReflectionUtils.singleton();

                List<Method> methods =
                        reflectionUtils
                                .getMethods(clazz, ReflectionUtils.Order.SUB_CLASS_FIRST)
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
                                    "Test class [%s] must define exactly [1]"
                                            + " @TestEngine.ArgumentSupplier method, [%d] found",
                                    clazz, methods.size()));
                }

                argumentSupplierMethod = methods.get(0);

                ARGUMENT_SUPPLIER_METHOD_CACHE.put(clazz, argumentSupplierMethod);
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

        synchronized (PREPARE_METHOD_CACHE) {
            methods = PREPARE_METHOD_CACHE.get(clazz);

            if (methods == null) {
                ReflectionUtils reflectionUtils = ReflectionUtils.singleton();

                methods =
                        reflectionUtils
                                .getMethods(clazz, ReflectionUtils.Order.SUPER_CLASS_FIRST)
                                .stream()
                                .filter(
                                        method ->
                                                method.isAnnotationPresent(
                                                        TestEngine.Prepare.class))
                                .filter(
                                        method ->
                                                !method.isAnnotationPresent(
                                                        TestEngine.Disabled.class))
                                .filter(reflectionUtils::isNotStatic)
                                .filter(
                                        method ->
                                                reflectionUtils.isPublic(method)
                                                        || reflectionUtils.isProtected(method))
                                .filter(
                                        method ->
                                                reflectionUtils.hasParameterCount(method, 0)
                                                        || reflectionUtils.acceptsParameters(
                                                                method, Argument.class))
                                .filter(method -> reflectionUtils.hasReturnType(method, Void.class))
                                .peek(method -> method.setAccessible(true))
                                .collect(Collectors.toList());

                sortMethods(methods);
                validateDistinctOrder(clazz, methods);

                PREPARE_METHOD_CACHE.put(clazz, methods);
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
     * @return Optional that contains an @TestEngine.Argument annotated Field if it exists or is
     *     empty
     */
    public Optional<Field> getArgumentField(Class<?> clazz) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("getArgumentField class [%s]", clazz.getName());
        }

        Optional<Field> optionalField;

        synchronized (ARGUMENT_FIELD_CACHE) {
            optionalField = ARGUMENT_FIELD_CACHE.get(clazz);

            if (optionalField == null) {
                List<Field> argumentFields =
                        getFields(clazz, TestEngine.Argument.class, Argument.class);
                if (argumentFields.isEmpty()) {
                    optionalField = Optional.empty();
                    ARGUMENT_FIELD_CACHE.put(clazz, optionalField);
                } else {
                    Field field = argumentFields.get(0);
                    optionalField = Optional.of(field);
                    ARGUMENT_FIELD_CACHE.put(clazz, optionalField);
                }
            }
        }

        if (LOGGER.isTraceEnabled()) {
            synchronized (LOGGER) {
                LOGGER.trace(
                        " class [%s] @TestEngine.Argument field count [%d]",
                        clazz.getName(), optionalField.isPresent() ? 1 : 0);

                LOGGER.trace(
                        " class [%s] @TestEngine.Argument argument field [%s]",
                        clazz.getName(), optionalField.map(Field::getName).orElse("ERROR"));
            }
        }

        return optionalField;
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

        synchronized (AUTO_CLOSE_FIELD_CACHE) {
            fields = AUTO_CLOSE_FIELD_CACHE.get(clazz);

            if (fields == null) {
                fields = getFields(clazz, TestEngine.AutoClose.class, Object.class);

                AUTO_CLOSE_FIELD_CACHE.put(clazz, fields);
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

        synchronized (BEFORE_ALL_METHOD_CACHE) {
            methods = BEFORE_ALL_METHOD_CACHE.get(clazz);

            if (methods == null) {
                ReflectionUtils reflectionUtils = ReflectionUtils.singleton();

                methods =
                        reflectionUtils
                                .getMethods(clazz, ReflectionUtils.Order.SUPER_CLASS_FIRST)
                                .stream()
                                .filter(
                                        method ->
                                                method.isAnnotationPresent(
                                                        TestEngine.BeforeAll.class))
                                .filter(
                                        method ->
                                                !method.isAnnotationPresent(
                                                        TestEngine.Disabled.class))
                                .filter(reflectionUtils::isNotStatic)
                                .filter(
                                        method ->
                                                reflectionUtils.isPublic(method)
                                                        || reflectionUtils.isProtected(method))
                                .filter(
                                        method ->
                                                reflectionUtils.hasParameterCount(method, 0)
                                                        || reflectionUtils.acceptsParameters(
                                                                method, Argument.class))
                                .filter(method -> reflectionUtils.hasReturnType(method, Void.class))
                                .peek(method -> method.setAccessible(true))
                                .collect(Collectors.toList());

                sortMethods(methods);
                validateDistinctOrder(clazz, methods);

                BEFORE_ALL_METHOD_CACHE.put(clazz, methods);
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

        synchronized (BEFORE_EACH_METHOD_CACHE) {
            methods = BEFORE_EACH_METHOD_CACHE.get(clazz);

            if (methods == null) {
                ReflectionUtils reflectionUtils = ReflectionUtils.singleton();

                methods =
                        reflectionUtils
                                .getMethods(clazz, ReflectionUtils.Order.SUPER_CLASS_FIRST)
                                .stream()
                                .filter(
                                        method ->
                                                method.isAnnotationPresent(
                                                        TestEngine.BeforeEach.class))
                                .filter(
                                        method ->
                                                !method.isAnnotationPresent(
                                                        TestEngine.Disabled.class))
                                .filter(reflectionUtils::isNotStatic)
                                .filter(
                                        method ->
                                                reflectionUtils.isPublic(method)
                                                        || reflectionUtils.isProtected(method))
                                .filter(
                                        method ->
                                                reflectionUtils.hasParameterCount(method, 0)
                                                        || reflectionUtils.acceptsParameters(
                                                                method, Argument.class))
                                .filter(method -> reflectionUtils.hasReturnType(method, Void.class))
                                .peek(method -> method.setAccessible(true))
                                .collect(Collectors.toList());

                sortMethods(methods);
                validateDistinctOrder(clazz, methods);

                BEFORE_EACH_METHOD_CACHE.put(clazz, methods);
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

        synchronized (TEST_METHOD_CACHE) {
            methods = TEST_METHOD_CACHE.get(clazz);

            if (methods == null) {
                ReflectionUtils reflectionUtils = ReflectionUtils.singleton();

                methods =
                        reflectionUtils
                                .getMethods(clazz, ReflectionUtils.Order.SUPER_CLASS_FIRST)
                                .stream()
                                .filter(method -> method.isAnnotationPresent(TestEngine.Test.class))
                                .filter(
                                        method ->
                                                !method.isAnnotationPresent(
                                                        TestEngine.Disabled.class))
                                .filter(reflectionUtils::isNotStatic)
                                .filter(
                                        method ->
                                                reflectionUtils.isPublic(method)
                                                        || reflectionUtils.isProtected(method))
                                .filter(
                                        method ->
                                                reflectionUtils.hasParameterCount(method, 0)
                                                        || reflectionUtils.acceptsParameters(
                                                                method, Argument.class))
                                .filter(method -> reflectionUtils.hasReturnType(method, Void.class))
                                .peek(method -> method.setAccessible(true))
                                .collect(Collectors.toList());

                sortMethods(methods);
                validateDistinctOrder(clazz, methods);

                TEST_METHOD_CACHE.put(clazz, methods);
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

        synchronized (AFTER_EACH_METHOD_CACHE) {
            methods = AFTER_EACH_METHOD_CACHE.get(clazz);

            if (methods == null) {
                ReflectionUtils reflectionUtils = ReflectionUtils.singleton();

                methods =
                        reflectionUtils
                                .getMethods(clazz, ReflectionUtils.Order.SUPER_CLASS_FIRST)
                                .stream()
                                .filter(
                                        method ->
                                                method.isAnnotationPresent(
                                                        TestEngine.AfterEach.class))
                                .filter(
                                        method ->
                                                !method.isAnnotationPresent(
                                                        TestEngine.Disabled.class))
                                .filter(reflectionUtils::isNotStatic)
                                .filter(
                                        method ->
                                                reflectionUtils.isPublic(method)
                                                        || reflectionUtils.isProtected(method))
                                .filter(
                                        method ->
                                                reflectionUtils.hasParameterCount(method, 0)
                                                        || reflectionUtils.acceptsParameters(
                                                                method, Argument.class))
                                .filter(method -> reflectionUtils.hasReturnType(method, Void.class))
                                .peek(method -> method.setAccessible(true))
                                .collect(Collectors.toList());

                sortMethods(methods);
                validateDistinctOrder(clazz, methods);

                AFTER_EACH_METHOD_CACHE.put(clazz, methods);
            }
        }

        if (LOGGER.isTraceEnabled()) {
            synchronized (LOGGER) {
                LOGGER.trace(
                        " class [%s] @TestEngine.AfterEach method count [%d]",
                        clazz.getName(), methods.size());

                for (Method method : methods) {
                    LOGGER.trace(
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

        synchronized (AFTER_ALL_METHOD_CACHE) {
            methods = AFTER_ALL_METHOD_CACHE.get(clazz);

            if (methods == null) {
                ReflectionUtils reflectionUtils = ReflectionUtils.singleton();

                methods =
                        reflectionUtils
                                .getMethods(clazz, ReflectionUtils.Order.SUPER_CLASS_FIRST)
                                .stream()
                                .filter(
                                        method ->
                                                method.isAnnotationPresent(
                                                        TestEngine.AfterAll.class))
                                .filter(
                                        method ->
                                                !method.isAnnotationPresent(
                                                        TestEngine.Disabled.class))
                                .filter(reflectionUtils::isNotStatic)
                                .filter(
                                        method ->
                                                reflectionUtils.isPublic(method)
                                                        || reflectionUtils.isProtected(method))
                                .filter(
                                        method ->
                                                reflectionUtils.hasParameterCount(method, 0)
                                                        || reflectionUtils.acceptsParameters(
                                                                method, Argument.class))
                                .filter(method -> reflectionUtils.hasReturnType(method, Void.class))
                                .peek(method -> method.setAccessible(true))
                                .collect(Collectors.toList());

                sortMethods(methods);
                validateDistinctOrder(clazz, methods);

                AFTER_ALL_METHOD_CACHE.put(clazz, methods);
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

        synchronized (CONCLUDE_METHOD_CACHE) {
            methods = CONCLUDE_METHOD_CACHE.get(clazz);

            if (methods == null) {
                ReflectionUtils reflectionUtils = ReflectionUtils.singleton();

                methods =
                        reflectionUtils
                                .getMethods(clazz, ReflectionUtils.Order.SUPER_CLASS_FIRST)
                                .stream()
                                .filter(
                                        method ->
                                                method.isAnnotationPresent(
                                                        TestEngine.Conclude.class))
                                .filter(
                                        method ->
                                                !method.isAnnotationPresent(
                                                        TestEngine.Disabled.class))
                                .filter(reflectionUtils::isNotStatic)
                                .filter(
                                        method ->
                                                reflectionUtils.isPublic(method)
                                                        || reflectionUtils.isProtected(method))
                                .filter(
                                        method ->
                                                reflectionUtils.hasParameterCount(method, 0)
                                                        || reflectionUtils.acceptsParameters(
                                                                method, Argument.class))
                                .filter(method -> reflectionUtils.hasReturnType(method, Void.class))
                                .peek(method -> method.setAccessible(true))
                                .collect(Collectors.toList());

                sortMethods(methods);
                validateDistinctOrder(clazz, methods);

                CONCLUDE_METHOD_CACHE.put(clazz, methods);
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
     * Method to get a List of all fields from a Class and super Classes
     *
     * @param clazz class to inspect
     * @param annotation annotation that is required
     * @param fieldType field type that is required
     * @return list of Fields
     */
    private List<Field> getFields(
            Class<?> clazz, Class<? extends Annotation> annotation, Class<?> fieldType) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "getFields class [%s] annotation [%s] fieldType [%s]",
                    clazz.getName(), annotation.getName(), fieldType.getName());
        }

        Set<Field> fieldSet = new LinkedHashSet<>();
        resolveFields(clazz, annotation, fieldType, fieldSet);
        List<Field> fields = new ArrayList<>(fieldSet);
        fields.sort(Comparator.comparing(Field::getName));

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("class [%s] argument field count [%d]", clazz.getName(), fieldSet.size());
        }

        return fields;
    }

    /**
     * Method to recursively resolve Fields
     *
     * @param clazz class to inspect
     * @param annotation annotation that is required
     * @param fieldType field type that is required
     * @param fieldSet set of Fields
     */
    private void resolveFields(
            Class<?> clazz,
            Class<? extends Annotation> annotation,
            Class<?> fieldType,
            Set<Field> fieldSet) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "resolveFields class [%s] annotation [%s] fieldType [%s]",
                    clazz.getName(), annotation.getName(), fieldType.getName());
        }

        List<Field> fields =
                Stream.of(clazz.getDeclaredFields())
                        .filter(
                                field -> {
                                    int modifiers = field.getModifiers();
                                    return !Modifier.isFinal(modifiers)
                                            && !Modifier.isStatic(modifiers)
                                            && field.isAnnotationPresent(annotation)
                                            && fieldType.isAssignableFrom(field.getType());
                                })
                        .collect(Collectors.toList());

        for (Field field : fields) {
            field.setAccessible(true);
            fieldSet.add(field);
        }

        Class<?> declaringClass = clazz.getSuperclass();
        if (declaringClass != null && !declaringClass.equals(Object.class)) {
            resolveFields(declaringClass, annotation, fieldType, fieldSet);
        }
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
    /*
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
    */

    /**
     * Method to sort a List of methods first by @TestEngine.Order annotation, then alphabetically
     *
     * @param methods list of Methods to sort
     */
    private void sortMethods(List<Method> methods) {
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

                    return o1DisplayName.compareTo(o2DisplayName);
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
