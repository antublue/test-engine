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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.junit.platform.commons.support.ReflectionSupport;

/** Class to implement methods to get test class fields / methods, caching the results */
@SuppressWarnings({
    "unchecked",
    "PMD.NPathComplexity",
    "PMD.AvoidAccessibilityAlteration",
    "PMD.EmptyCatchBlock"
})
public final class ReflectionUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionUtils.class);

    private enum Scope {
        STATIC,
        NON_STATIC
    }

    private static final Map<Class<?>, Method> ARGUMENT_SUPPLIER_METHOD_CACHE = new HashMap<>();
    private static final Map<Class<?>, Optional<Field>> ARGUMENT_FIELD_CACHE = new HashMap<>();
    // private static final Map<Class<?>, Optional<Method>> EXCEPTION_HANDLER_METHOD_CACHE = new
    // HashMap<>();
    private static final Map<Class<?>, List<Field>> AUTO_CLOSE_FIELD_CACHE = new HashMap<>();
    private static final Map<Class<?>, List<Method>> PREPARE_METHOD_CACHE = new HashMap<>();
    private static final Map<Class<?>, List<Method>> BEFORE_ALL_METHOD_CACHE = new HashMap<>();
    private static final Map<Class<?>, List<Method>> BEFORE_EACH_METHOD_CACHE = new HashMap<>();
    private static final Map<Class<?>, List<Method>> TEST_METHOD_CACHE = new HashMap<>();
    private static final Map<Class<?>, List<Method>> AFTER_EACH_METHOD_CACHE = new HashMap<>();
    private static final Map<Class<?>, List<Method>> AFTER_ALL_METHOD_CACHE = new HashMap<>();
    private static final Map<Class<?>, List<Method>> CONCLUDE_METHOD_CACHE = new HashMap<>();

    /** Constructor */
    private ReflectionUtils() {
        // DO NOTHING
    }

    /**
     * Method to find all classes for a URI
     *
     * @param uri uri
     * @return the return value
     */
    public static List<Class<?>> findAllClasses(URI uri) {
        List<Class<?>> classes =
                org.junit.platform.commons.util.ReflectionUtils.findAllClassesInClasspathRoot(
                        uri, classFilter -> true, classNameFilter -> true);

        classes = new ArrayList<>(classes);
        sortClasses(classes);
        validateDistinctOrder(classes);

        return classes;
    }

    /**
     * Method to find all classes with a package name
     *
     * @param packageName packageName
     * @return the return value
     */
    public static List<Class<?>> findAllClasses(String packageName) {
        List<Class<?>> classes =
                ReflectionSupport.findAllClassesInPackage(
                        packageName, classFilter -> true, classNameFilter -> true);

        classes = new ArrayList<>(classes);
        sortClasses(classes);
        validateDistinctOrder(classes);

        return classes;
    }

    /**
     * Method to return if a Method accepts an Argument
     *
     * @param method method
     * @param argument argument
     * @return the return value
     */
    public static boolean acceptsArgument(Method method, Argument argument) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        return parameterTypes != null
                && parameterTypes.length == 1
                && parameterTypes[0].isAssignableFrom(argument.getClass());
    }

    /**
     * Method to get a @TestEngine.ArgumentSupplier Method
     *
     * @param clazz class to inspect
     * @return Method the return value
     */
    public static Method getArgumentSupplierMethod(Class<?> clazz) {
        synchronized (ARGUMENT_SUPPLIER_METHOD_CACHE) {
            LOGGER.trace("getArgumentSupplierMethod(%s)", clazz.getName());

            if (ARGUMENT_SUPPLIER_METHOD_CACHE.containsKey(clazz)) {
                return ARGUMENT_SUPPLIER_METHOD_CACHE.get(clazz);
            }

            List<Method> methodList =
                    getMethods(
                            clazz,
                            TestEngine.ArgumentSupplier.class,
                            Scope.STATIC,
                            Stream.class,
                            (Class<?>[]) null);

            if (methodList.size() == 0) {
                methodList =
                        getMethods(
                                clazz,
                                TestEngine.ArgumentSupplier.class,
                                Scope.STATIC,
                                Iterable.class,
                                (Class<?>[]) null);
            }

            LOGGER.trace(
                    "class [%s] @TestEngine.ArgumentSupplier method count [%d]",
                    clazz.getName(), methodList.size());

            if (methodList.size() != 1) {
                throw new TestClassConfigurationException(
                        String.format(
                                "Test class [%s] must define one @TestEngine.ArgumentSupplier"
                                        + " method",
                                clazz.getName()));
            }

            Method method = methodList.get(0);
            ARGUMENT_SUPPLIER_METHOD_CACHE.put(clazz, method);

            return method;
        }
    }

    /**
     * Method to get a List of Arguments for a Class
     *
     * @param clazz class to inspect
     * @return list of Arguments
     */
    public static List<Argument> getArgumentsList(Class<?> clazz) {
        LOGGER.trace("getArgumentsList(%s)", clazz.getName());

        try {
            Method method = getArgumentSupplierMethod(clazz);
            Object object = method.invoke(null, (Object[]) null);
            if (object instanceof Stream) {
                List<Argument> arguments = ((Stream<Argument>) object).collect(Collectors.toList());
                LOGGER.trace("class [%s] argument count [%d]", clazz.getName(), arguments.size());
                return arguments;
            } else if (object instanceof Iterable) {
                List<Argument> arguments = new ArrayList<>();
                ((Iterable<Argument>) object).forEach(arguments::add);
                LOGGER.trace("class [%s] argument count [%d]", clazz.getName(), arguments.size());
                return arguments;
            } else {
                throw new TestClassConfigurationException(
                        String.format(
                                "Test class [%s] must define one @TestEngine.ArgumentSupplier"
                                        + " method",
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

    /*
    public static Optional<Method> getExceptionHandlerMethod(Class<?> clazz) {
        synchronized (EXCEPTION_HANDLER_METHOD_CACHE) {
            LOGGER.trace("getExceptionHandlerMethod(%s)", clazz.getName());

            if (EXCEPTION_HANDLER_METHOD_CACHE.containsKey(clazz)) {
                return EXCEPTION_HANDLER_METHOD_CACHE.get(clazz);
            }

            List<Method> methodList =
                    getMethods(
                            clazz,
                            TestEngine.ExceptionHandler.class,
                            Scope.NON_STATIC,
                            Void.class,
                            (Class<?>[]) null);

            LOGGER.trace(
                    "class [%s] @TestEngine.ExceptionHandler method count [%d]",
                    clazz.getName(),
                    methodList.size());

            if (methodList.size() > 1) {
                throw new TestClassConfigurationException(
                        String.format(
                                "Test class [%s] must define one @TestEngine.ExceptionHandler method",
                                clazz.getName()));
            }

            Optional<Method> optionalMethod;

            if (methodList.size() == 1) {
                optionalMethod = Optional.of(methodList.get(0));
            } else {
                optionalMethod = Optional.empty();
            }

            EXCEPTION_HANDLER_METHOD_CACHE.put(clazz, optionalMethod);

            return optionalMethod;
        }
    }
    */

    /**
     * Method to get a List of @TestEngine.Prepare Methods
     *
     * @param clazz class to inspect
     * @return list of Methods
     */
    public static List<Method> getPrepareMethods(Class<?> clazz) {
        synchronized (PREPARE_METHOD_CACHE) {
            LOGGER.trace("getPrepareMethods(%s)", clazz.getName());

            if (PREPARE_METHOD_CACHE.containsKey(clazz)) {
                return PREPARE_METHOD_CACHE.get(clazz);
            }

            List<Method> methods =
                    getMethods(
                                    clazz,
                                    TestEngine.Prepare.class,
                                    Scope.NON_STATIC,
                                    Void.class,
                                    (Class<?>[]) null)
                            .stream()
                            .filter(
                                    method ->
                                            !method.isAnnotationPresent(TestEngine.Disabled.class))
                            .collect(Collectors.toList());

            LOGGER.trace(
                    "class [%s] @TestEngine.Prepare method count [%d]",
                    clazz.getName(), methods.size());

            if (!methods.isEmpty()) {
                sortMethods(methods);
                validateDistinctOrder(clazz, methods);
            }

            methods = Collections.unmodifiableList(methods);
            PREPARE_METHOD_CACHE.put(clazz, methods);

            return methods;
        }
    }

    /**
     * Method to get @TestEngine.Argument Field
     *
     * @param clazz class to inspect
     * @return Optional that contains an @TestEngine.Argument annotated Field if it exists or is
     *     empty
     */
    public static Optional<Field> getArgumentField(Class<?> clazz) {
        synchronized (ARGUMENT_FIELD_CACHE) {
            LOGGER.trace("getArgumentField(%s)", clazz.getName());

            if (ARGUMENT_FIELD_CACHE.containsKey(clazz)) {
                return ARGUMENT_FIELD_CACHE.get(clazz);
            }

            List<Field> argumentFields =
                    getFields(clazz, TestEngine.Argument.class, Argument.class);

            LOGGER.trace(
                    "class [%s] @TestEngine.Argument field count [%d]",
                    clazz.getName(), argumentFields.size());

            Field field;
            Optional<Field> optionalField;

            if (argumentFields.size() == 0) {
                optionalField = Optional.empty();
                ARGUMENT_FIELD_CACHE.put(clazz, optionalField);
            } else if (argumentFields.size() == 1) {
                field = argumentFields.get(0);
                optionalField = Optional.of(field);
                ARGUMENT_FIELD_CACHE.put(clazz, optionalField);
            } else {
                throw new TestClassConfigurationException(
                        String.format(
                                "Test class [%s] must define one @TestEngine.Argument field",
                                clazz.getName()));
            }

            return optionalField;
        }
    }

    /**
     * Method to get a List of @TestEngine.AutoClose Fields
     *
     * @param clazz class to inspect
     * @return List of Fields
     */
    public static List<Field> getAutoCloseFields(Class<?> clazz) {
        synchronized (AUTO_CLOSE_FIELD_CACHE) {
            LOGGER.trace("getAutoCloseFields(%s)", clazz.getName());

            if (AUTO_CLOSE_FIELD_CACHE.containsKey(clazz)) {
                return AUTO_CLOSE_FIELD_CACHE.get(clazz);
            }

            List<Field> autoCloseFields =
                    getFields(clazz, TestEngine.AutoClose.class, Object.class);

            LOGGER.trace(
                    "class [%s] @TestEngine.AutoClose field count [%d]",
                    clazz.getName(), autoCloseFields.size());

            AUTO_CLOSE_FIELD_CACHE.put(clazz, autoCloseFields);

            return autoCloseFields;
        }
    }

    /**
     * Method to get a List of @TestEngine.BeforeAll Methods
     *
     * @param clazz class to inspect
     * @return list of Methods
     */
    public static List<Method> getBeforeAllMethods(Class<?> clazz) {
        synchronized (BEFORE_ALL_METHOD_CACHE) {
            LOGGER.trace("getBeforeAllMethods(%s)", clazz.getName());

            if (BEFORE_ALL_METHOD_CACHE.containsKey(clazz)) {
                return BEFORE_ALL_METHOD_CACHE.get(clazz);
            }

            List<Method> methods =
                    getMethods(
                                    clazz,
                                    TestEngine.BeforeAll.class,
                                    Scope.NON_STATIC,
                                    Void.class,
                                    (Class<?>[]) null)
                            .stream()
                            .filter(
                                    method ->
                                            !method.isAnnotationPresent(TestEngine.Disabled.class))
                            .collect(Collectors.toList());

            methods.addAll(
                    getMethods(
                                    clazz,
                                    TestEngine.BeforeAll.class,
                                    Scope.NON_STATIC,
                                    Void.class,
                                    Argument.class)
                            .stream()
                            .filter(
                                    method ->
                                            !method.isAnnotationPresent(TestEngine.Disabled.class))
                            .collect(Collectors.toList()));

            LOGGER.trace(
                    "class [%s] @TestEngine.BeforeAll method count [%d]",
                    clazz.getName(), methods.size());

            if (!methods.isEmpty()) {
                sortMethods(methods);
                validateDistinctOrder(clazz, methods);
            }

            methods = Collections.unmodifiableList(methods);
            BEFORE_ALL_METHOD_CACHE.put(clazz, methods);

            return methods;
        }
    }

    /**
     * Method to get a List of @TestEngine.BeforeEach Methods
     *
     * @param clazz class to inspect
     * @return list of Methods
     */
    public static List<Method> getBeforeEachMethods(Class<?> clazz) {
        synchronized (BEFORE_EACH_METHOD_CACHE) {
            LOGGER.trace("getBeforeEachMethods(%s)", clazz.getName());

            if (BEFORE_EACH_METHOD_CACHE.containsKey(clazz)) {
                return BEFORE_EACH_METHOD_CACHE.get(clazz);
            }

            List<Method> methods =
                    getMethods(
                                    clazz,
                                    TestEngine.BeforeEach.class,
                                    Scope.NON_STATIC,
                                    Void.class,
                                    (Class<?>[]) null)
                            .stream()
                            .filter(
                                    method ->
                                            !method.isAnnotationPresent(TestEngine.Disabled.class))
                            .collect(Collectors.toList());

            methods.addAll(
                    getMethods(
                                    clazz,
                                    TestEngine.BeforeEach.class,
                                    Scope.NON_STATIC,
                                    Void.class,
                                    Argument.class)
                            .stream()
                            .filter(
                                    method ->
                                            !method.isAnnotationPresent(TestEngine.Disabled.class))
                            .collect(Collectors.toList()));

            LOGGER.trace(
                    "class [%s] @TestEngine.BeforeEach method count [%d]",
                    clazz.getName(), methods.size());

            if (!methods.isEmpty()) {
                sortMethods(methods);
                validateDistinctOrder(clazz, methods);
            }

            methods = Collections.unmodifiableList(methods);
            BEFORE_EACH_METHOD_CACHE.put(clazz, methods);

            return methods;
        }
    }

    /**
     * Method to get a List of @TestEngine.Test Methods
     *
     * @param clazz class to inspect
     * @return list of Methods
     */
    public static List<Method> getTestMethods(Class<?> clazz) {
        synchronized (TEST_METHOD_CACHE) {
            LOGGER.trace("getTestMethods(%s)", clazz.getName());

            if (TEST_METHOD_CACHE.containsKey(clazz)) {
                return new ArrayList<>(TEST_METHOD_CACHE.get(clazz));
            }

            List<Method> methods =
                    getMethods(
                                    clazz,
                                    TestEngine.Test.class,
                                    Scope.NON_STATIC,
                                    Void.class,
                                    (Class<?>[]) null)
                            .stream()
                            .filter(
                                    method ->
                                            !method.isAnnotationPresent(TestEngine.Disabled.class))
                            .collect(Collectors.toList());

            methods.addAll(
                    getMethods(
                                    clazz,
                                    TestEngine.Test.class,
                                    Scope.NON_STATIC,
                                    Void.class,
                                    Argument.class)
                            .stream()
                            .filter(
                                    method ->
                                            !method.isAnnotationPresent(TestEngine.Disabled.class))
                            .collect(Collectors.toList()));

            LOGGER.trace(
                    "class [%s] @TestEngine.Test method count [%d]",
                    clazz.getName(), methods.size());

            if (!methods.isEmpty()) {
                sortMethods(methods);
                validateDistinctOrder(clazz, methods);
                methods = Collections.unmodifiableList(methods);
            }

            TEST_METHOD_CACHE.put(clazz, methods);

            return new ArrayList<>(methods);
        }
    }

    /**
     * Method to get a List of @TestEngine.AfterEach Methods
     *
     * @param clazz class to inspect
     * @return list of Methods
     */
    public static List<Method> getAfterEachMethods(Class<?> clazz) {
        synchronized (AFTER_EACH_METHOD_CACHE) {
            LOGGER.trace("getAfterEachMethods(%s)", clazz.getName());

            if (AFTER_EACH_METHOD_CACHE.containsKey(clazz)) {
                return AFTER_EACH_METHOD_CACHE.get(clazz);
            }

            List<Method> methods =
                    getMethods(
                                    clazz,
                                    TestEngine.AfterEach.class,
                                    Scope.NON_STATIC,
                                    Void.class,
                                    (Class<?>[]) null)
                            .stream()
                            .filter(
                                    method ->
                                            !method.isAnnotationPresent(TestEngine.Disabled.class))
                            .collect(Collectors.toList());

            methods.addAll(
                    getMethods(
                                    clazz,
                                    TestEngine.AfterEach.class,
                                    Scope.NON_STATIC,
                                    Void.class,
                                    Argument.class)
                            .stream()
                            .filter(
                                    method ->
                                            !method.isAnnotationPresent(TestEngine.Disabled.class))
                            .collect(Collectors.toList()));

            LOGGER.trace(
                    "class [%s] @TestEngine.AfterEach method count [%d]",
                    clazz.getName(), methods.size());

            if (!methods.isEmpty()) {
                sortMethods(methods);
                validateDistinctOrder(clazz, methods);
            }

            methods = Collections.unmodifiableList(methods);
            AFTER_EACH_METHOD_CACHE.put(clazz, methods);

            return methods;
        }
    }

    /**
     * Method to get a List of @TestEngine.AfterAll Methods
     *
     * @param clazz class to inspect
     * @return list of Methods
     */
    public static List<Method> getAfterAllMethods(Class<?> clazz) {
        synchronized (AFTER_ALL_METHOD_CACHE) {
            LOGGER.trace("getAfterAllMethods(%s)", clazz.getName());

            if (AFTER_ALL_METHOD_CACHE.containsKey(clazz)) {
                return AFTER_ALL_METHOD_CACHE.get(clazz);
            }

            List<Method> methods =
                    getMethods(
                                    clazz,
                                    TestEngine.AfterAll.class,
                                    Scope.NON_STATIC,
                                    Void.class,
                                    (Class<?>[]) null)
                            .stream()
                            .filter(
                                    method ->
                                            !method.isAnnotationPresent(TestEngine.Disabled.class))
                            .collect(Collectors.toList());

            methods.addAll(
                    getMethods(
                                    clazz,
                                    TestEngine.AfterAll.class,
                                    Scope.NON_STATIC,
                                    Void.class,
                                    Argument.class)
                            .stream()
                            .filter(
                                    method ->
                                            !method.isAnnotationPresent(TestEngine.Disabled.class))
                            .collect(Collectors.toList()));

            LOGGER.trace(
                    "class [%s] @TestEngine.AfterAll method count [%d]",
                    clazz.getName(), methods.size());

            if (!methods.isEmpty()) {
                sortMethods(methods);
                validateDistinctOrder(clazz, methods);
            }

            methods = Collections.unmodifiableList(methods);
            AFTER_ALL_METHOD_CACHE.put(clazz, methods);

            return methods;
        }
    }

    /**
     * Method to get a @TestEngine.Conclude Methods
     *
     * @param clazz class to inspect
     * @return Method the return value
     */
    public static List<Method> getConcludeMethods(Class<?> clazz) {
        synchronized (CONCLUDE_METHOD_CACHE) {
            LOGGER.trace("getConcludeMethods(%s)", clazz.getName());

            if (CONCLUDE_METHOD_CACHE.containsKey(clazz)) {
                return CONCLUDE_METHOD_CACHE.get(clazz);
            }

            List<Method> methods =
                    getMethods(
                                    clazz,
                                    TestEngine.Conclude.class,
                                    Scope.NON_STATIC,
                                    Void.class,
                                    (Class<?>[]) null)
                            .stream()
                            .filter(
                                    method ->
                                            !method.isAnnotationPresent(TestEngine.Disabled.class))
                            .collect(Collectors.toList());

            LOGGER.trace(
                    "class [%s] @TestEngine.Conclude method count [%d]",
                    clazz.getName(), methods.size());

            if (!methods.isEmpty()) {
                sortMethods(methods);
                validateDistinctOrder(clazz, methods);
            }

            methods = Collections.unmodifiableList(methods);
            CONCLUDE_METHOD_CACHE.put(clazz, methods);

            return methods;
        }
    }

    /**
     * Method to get a test method display name
     *
     * @param method method to get the display name for
     * @return the display name
     */
    public static String getDisplayName(Method method) {
        String displayName = method.getName();

        TestEngine.DisplayName annotation = method.getAnnotation(TestEngine.DisplayName.class);
        if (annotation != null) {
            String name = annotation.name();
            if (name != null && !name.trim().isEmpty()) {
                displayName = name.trim();
            }
        }

        LOGGER.trace("method [%s] display name [%s]", method.getName(), displayName);

        return displayName;
    }

    /**
     * Method to get a test method display name
     *
     * @param clazz class to get the display name for
     * @return the display name
     */
    public static String getDisplayName(Class<?> clazz) {
        String displayName = clazz.getName();

        TestEngine.DisplayName annotation = clazz.getAnnotation(TestEngine.DisplayName.class);
        if (annotation != null) {
            String name = annotation.name();
            if (name != null && !name.trim().isEmpty()) {
                displayName = name.trim();
            }
        }

        LOGGER.trace("class [%s] display name [%s]", clazz.getName(), displayName);

        return displayName;
    }

    /**
     * Method to instantiate an Object
     *
     * @param clazz clazz
     * @param objectConsumer objectConsumer
     * @param throwableConsumer throwableConsumer
     */
    public static void instantiate(
            Class<?> clazz,
            Consumer<Object> objectConsumer,
            Consumer<Throwable> throwableConsumer) {
        try {
            Object object =
                    clazz.getDeclaredConstructor((Class<?>[]) null).newInstance((Object[]) null);
            objectConsumer.accept(object);
        } catch (Throwable t) {
            throwableConsumer.accept(t.getCause());
        }
    }

    /**
     * Method to set a Field
     *
     * @param object object
     * @param field field
     * @param value value
     * @param throwableConsumer throwableConsumer
     */
    public static void setField(
            Object object, Field field, Object value, Consumer<Throwable> throwableConsumer) {
        try {
            field.setAccessible(true);
            field.set(object, value);
        } catch (Throwable t) {
            t.printStackTrace();
            throwableConsumer.accept(t);
        }
    }

    /**
     * Method to invoke a method
     *
     * @param object object
     * @param method method
     * @param throwableConsumer throwableConsumer
     */
    public static void invoke(Object object, Method method, Consumer<Throwable> throwableConsumer) {
        invoke(object, method, null, throwableConsumer);
    }

    /**
     * Method to invoke a method
     *
     * @param object object
     * @param method method
     * @param arguments arguments
     * @param throwableConsumer throwableConsumer
     */
    public static void invoke(
            Object object,
            Method method,
            Object[] arguments,
            Consumer<Throwable> throwableConsumer) {
        try {
            method.invoke(object, arguments);
        } catch (Throwable t) {
            throwableConsumer.accept(t.getCause());
        }
    }

    /**
     * Method to get a List of all fields from a Class and super Classes
     *
     * @param clazz class to inspect
     * @param annotation annotation that is required
     * @param fieldType field type that is required
     * @return list of Fields
     */
    private static List<Field> getFields(
            Class<?> clazz, Class<? extends Annotation> annotation, Class<?> fieldType) {
        LOGGER.trace(
                "getFields(%s, %s, %s)",
                clazz.getName(), annotation.getName(), fieldType.getName());

        Set<Field> fieldSet = new LinkedHashSet<>();
        resolveFields(clazz, annotation, fieldType, fieldSet);
        List<Field> fields = new ArrayList<>(fieldSet);
        fields.sort(Comparator.comparing(Field::getName));

        LOGGER.trace("class [%s] argument field count [%d]", clazz.getName(), fieldSet.size());

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
    private static void resolveFields(
            Class<?> clazz,
            Class<? extends Annotation> annotation,
            Class<?> fieldType,
            Set<Field> fieldSet) {
        LOGGER.trace(
                "resolveFields(%s, %s, %s)",
                clazz.getName(), annotation.getName(), fieldType.getName());

        Stream.of(clazz.getDeclaredFields())
                .filter(
                        field -> {
                            int modifiers = field.getModifiers();
                            return !Modifier.isFinal(modifiers)
                                    && !Modifier.isStatic(modifiers)
                                    && field.isAnnotationPresent(annotation)
                                    && fieldType.isAssignableFrom(field.getType());
                        })
                .forEach(
                        field -> {
                            field.setAccessible(true);
                            fieldSet.add(field);
                        });

        Class<?> declaringClass = clazz.getSuperclass();
        if (declaringClass != null && !declaringClass.equals(Object.class)) {
            resolveFields(declaringClass, annotation, fieldType, fieldSet);
        }
    }

    /**
     * Method to get a List of all methods from a Class and super Classes
     *
     * @param clazz class to inspect
     * @return list of Methods
     */
    private static List<Method> getMethods(
            Class<?> clazz,
            Class<? extends Annotation> annotation,
            Scope scope,
            Class<?> returnType,
            Class<?>... parameterTypes) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(
                String.format(
                        "getMethods(%s, %s, %s, %s",
                        clazz.getName(), annotation.getName(), scope, returnType.getName()));

        if (parameterTypes != null) {
            for (Class<?> parameterTypeClass : parameterTypes) {
                stringBuilder.append(", ").append(parameterTypeClass.getName());
            }
        }

        stringBuilder.append(")");
        LOGGER.trace(stringBuilder.toString());

        Map<String, Method> methodMap = new HashMap<>();
        resolveMethods(clazz, annotation, scope, returnType, parameterTypes, methodMap);
        List<Method> methodList = new ArrayList<>(methodMap.values());
        methodList.sort(Comparator.comparing(Method::getName));

        return methodList;
    }

    /**
     * Method to recursively resolve Methods
     *
     * @param clazz class to inspect
     * @param annotation annotation that is required
     * @param scope method scope that is required
     * @param returnType method return type that is required
     * @param parameterTypes parameter types that are required
     * @param methodMap map of Methods
     */
    private static void resolveMethods(
            Class<?> clazz,
            Class<? extends Annotation> annotation,
            Scope scope,
            Class<?> returnType,
            Class<?>[] parameterTypes,
            Map<String, Method> methodMap) {

        if (LOGGER.isTraceEnabled()) {
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append(
                    String.format(
                            "resolveMethods(%s, %s, %s, %s",
                            clazz.getName(), annotation.getName(), scope, returnType.getName()));

            if (parameterTypes != null) {
                for (Class<?> parameterTypeClass : parameterTypes) {
                    stringBuilder.append(", ").append(parameterTypeClass.getName());
                }
            }

            stringBuilder.append(")");
            LOGGER.trace(stringBuilder.toString());
        }

        try {
            Stream.of(clazz.getDeclaredMethods())
                    .filter(method -> method.isAnnotationPresent(annotation))
                    .filter(
                            method -> {
                                int modifiers = method.getModifiers();
                                return Modifier.isPublic(modifiers)
                                        || Modifier.isProtected(modifiers);
                            })
                    .filter(
                            method -> {
                                int modifiers = method.getModifiers();
                                if (scope == Scope.STATIC && !Modifier.isStatic(modifiers)) {
                                    throw new TestClassConfigurationException(
                                            String.format(
                                                    "%s method [%s] must be declared static",
                                                    getAnnotationDisplayName(annotation),
                                                    method.getName()));
                                } else if (scope != Scope.STATIC && Modifier.isStatic(modifiers)) {
                                    throw new TestClassConfigurationException(
                                            String.format(
                                                    "%s method [%s] must be not be declared static",
                                                    getAnnotationDisplayName(annotation),
                                                    method.getName()));
                                } else {
                                    return true;
                                }
                            })
                    .filter(
                            method -> {
                                if (parameterTypes == null) {
                                    return method.getParameterTypes().length == 0;
                                }
                                if (parameterTypes.length != method.getParameterCount()) {
                                    return false;
                                }
                                Class<?>[] methodParameterTypes = method.getParameterTypes();
                                for (int i = 0; i < parameterTypes.length; i++) {
                                    if (!parameterTypes[i].isAssignableFrom(
                                            methodParameterTypes[i])) {
                                        return false;
                                    }
                                }
                                return true;
                            })
                    .filter(
                            method -> {
                                if (returnType == Void.class) {
                                    return method.getReturnType().getName().equals("void");
                                } else {
                                    return returnType.isAssignableFrom(method.getReturnType());
                                }
                            })
                    .forEach(
                            method -> {
                                if (methodMap.putIfAbsent(method.getName(), method) == null) {
                                    method.setAccessible(true);
                                }
                            });
        } catch (NoClassDefFoundError e) {
            // DO NOTHING
        } catch (TestEngineException e) {
            throw e;
        } catch (Throwable t) {
            throw new TestEngineException(
                    String.format("Exception resolving methods class [%s]", clazz.getName()), t);
        }

        Class<?> declaringClass = clazz.getSuperclass();
        if (declaringClass != null && !declaringClass.equals(Object.class)) {
            resolveMethods(
                    declaringClass, annotation, scope, returnType, parameterTypes, methodMap);
        }
    }

    /**
     * Method to sort a List of classes first by @TestEngine.Order annotation, then by display name
     * / class name
     *
     * @param classes list of Classes to sort
     */
    public static void sortClasses(List<Class<?>> classes) {
        classes.sort(
                (o1, o2) -> {
                    boolean o1AnnotationPresent = o1.isAnnotationPresent(TestEngine.Order.class);
                    boolean o2AnnotationPresent = o2.isAnnotationPresent(TestEngine.Order.class);
                    if (o1AnnotationPresent) {
                        if (o2AnnotationPresent) {
                            // Sort based on @TestEngine.Order value
                            int o1Order = o1.getAnnotation(TestEngine.Order.class).order();
                            int o2Order = o2.getAnnotation(TestEngine.Order.class).order();
                            return Integer.compare(o1Order, o2Order);
                        } else {
                            return -1;
                        }
                    } else if (o2AnnotationPresent) {
                        return 1;
                    } else {
                        // Order by display name which is either
                        // the name declared by @TestEngine.DisplayName
                        // or the real class name
                        String o1DisplayName = getDisplayName(o1);
                        String o2DisplayName = getDisplayName(o2);
                        return o1DisplayName.compareTo(o2DisplayName);
                    }
                });
    }

    /**
     * Method to validate @TestEngine.Order is unique on classes
     *
     * @param classes classes
     */
    private static void validateDistinctOrder(List<Class<?>> classes) {
        Map<Integer, Class<?>> orderToClassMap = new LinkedHashMap<>();

        classes.forEach(
                clazz -> {
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
                });
    }

    /**
     * Method to sort a List of methods first by @TestEngine.Order annotation, then alphabetically
     *
     * @param methods list of Methods to sort
     */
    public static void sortMethods(List<Method> methods) {
        methods.sort(
                (o1, o2) -> {
                    TestEngine.Order o1Annotation = o1.getAnnotation(TestEngine.Order.class);
                    TestEngine.Order o2Annotation = o2.getAnnotation(TestEngine.Order.class);
                    if (o1Annotation != null) {
                        if (o2Annotation != null) {
                            // Sort based on @TestEngine.Order value
                            int o1Order = o1Annotation.order();
                            int o2Order = o2Annotation.order();
                            return Integer.compare(o1Order, o2Order);
                        } else {
                            return -1;
                        }
                    } else if (o2Annotation != null) {
                        return 1;
                    } else {
                        // Order by display name which is either
                        // the name declared by @TestEngine.DisplayName
                        // or the real method name
                        String o1DisplayName = getDisplayName(o1);
                        String o2DisplayName = getDisplayName(o2);
                        return o1DisplayName.compareTo(o2DisplayName);
                    }
                });
    }

    /**
     * Method to validate @TestEngine.Order is unique on methods with the same annotation
     *
     * @param clazz clazz
     * @param methods methods
     */
    private static void validateDistinctOrder(Class<?> clazz, List<Method> methods) {
        Set<Integer> integers = new HashSet<>();

        methods.forEach(
                method -> {
                    TestEngine.Order annotation = method.getAnnotation(TestEngine.Order.class);
                    if (annotation != null) {
                        int value = annotation.order();
                        if (integers.contains(value)) {
                            throw new TestClassConfigurationException(
                                    String.format(
                                            "Test class [%s] (or superclass) contains a duplicate"
                                                    + " @TestEngine.Order(%d) method annotation",
                                            clazz.getName(), value));
                        } else {
                            integers.add(value);
                        }
                    }
                });
    }

    /**
     * Method to get a display name for an Annotation
     *
     * @param annotation to look for
     * @return the display name
     */
    private static String getAnnotationDisplayName(Class<? extends Annotation> annotation) {
        return String.format(
                "@%s.%s",
                annotation.getDeclaringClass().getSimpleName(), annotation.getSimpleName());
    }
}
