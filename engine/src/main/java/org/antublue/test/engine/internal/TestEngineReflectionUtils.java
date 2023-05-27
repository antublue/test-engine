/*
 * Copyright (C) 2022-2023 The AntuBLUE test-engine project authors
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

import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.util.Throwables;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.commons.util.ReflectionUtils;

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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class to implement methods to get test class fields / methods, caching the information
 */
@SuppressWarnings({"unchecked", "PMD.EmptyControlStatement"})
public final class TestEngineReflectionUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEngineReflectionUtils.class);

    private enum Scope { STATIC, NON_STATIC }

    private static final Map<Class<?>, Method> argumentSupplierMethodCache;
    private static final Map<Class<?>, Field> argumentFieldCaches;
    private static final Map<Class<?>, List<Method>> prepareMethodCache;
    private static final Map<Class<?>, List<Method>> beforeAllMethodCache;
    private static final Map<Class<?>, List<Method>> beforeEachMethodCache;
    private static final Map<Class<?>, List<Method>> testMethodCache;
    private static final Map<Class<?>, List<Method>> afterEachMethodCache;
    private static final Map<Class<?>, List<Method>> afterAllMethodCache;
    private static final Map<Class<?>, List<Method>> concludeMethodCache;

    static {
        argumentSupplierMethodCache = new HashMap<>();
        prepareMethodCache = new HashMap<>();
        argumentFieldCaches = new HashMap<>();
        beforeAllMethodCache = new HashMap<>();
        beforeEachMethodCache = new HashMap<>();
        testMethodCache = new HashMap<>();
        afterEachMethodCache = new HashMap<>();
        afterAllMethodCache = new HashMap<>();
        concludeMethodCache = new HashMap<>();
    }

    /**
     * Constructor
     */
    private TestEngineReflectionUtils() {
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
                ReflectionUtils
                        .findAllClassesInClasspathRoot(uri, classFilter -> true, classNameFilter -> true);

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
                ReflectionSupport.findAllClassesInPackage(packageName, classFilter -> true, classNameFilter -> true);

        classes = new ArrayList<>(classes);
        sortClasses(classes);
        validateDistinctOrder(classes);

        return classes;
    }

    /**
     * Method to get a @TestEngine.ArgumentSupplier Method
     *
     * @param clazz class to inspect
     * @return Method the return value
     */
    public static Method getArgumentSupplierMethod(Class<?> clazz) {
        synchronized (argumentSupplierMethodCache) {
            LOGGER.trace("getArgumentSupplierMethod(%s)", clazz.getName());

            if (argumentSupplierMethodCache.containsKey(clazz)) {
                return argumentSupplierMethodCache.get(clazz);
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
                    clazz.getName(),
                    methodList.size());

            if (methodList.size() != 1) {
                throw new TestClassConfigurationException(
                        String.format(
                                "Test class [%s] must define one @TestEngine.ArgumentSupplier method",
                                clazz.getName()));
            }


            Method method = methodList.get(0);
            argumentSupplierMethodCache.put(clazz, method);

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
                                "Test class [%s] must define one @TestEngine.ArgumentSupplier method",
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
     * Method to get a List of @TestEngine.Prepare Methods sorted alphabetically
     *
     * @param clazz class to inspect
     * @return list of Methods
     */
    public static List<Method> getPrepareMethods(Class<?> clazz) {
        synchronized (prepareMethodCache) {
            LOGGER.trace("getPrepareMethods(%s)", clazz.getName());

            if (prepareMethodCache.containsKey(clazz)) {
                return prepareMethodCache.get(clazz);
            }

            List<Method> methods =
                    getMethods(
                            clazz,
                            TestEngine.Prepare.class,
                            Scope.NON_STATIC,
                            Void.class,
                            (Class<?>[]) null);

            LOGGER.trace("class [%s] @TestEngine.Prepare method count [%d]", clazz.getName(), methods.size());

            if (!methods.isEmpty()) {
                sortMethods(methods);
                validateDistinctOrder(clazz, methods);
            }

            methods = Collections.unmodifiableList(methods);
            prepareMethodCache.put(clazz, methods);

            return methods;
        }
    }

    /**
     * Method to get @TestEngine.Argument Field
     *
     * @param clazz class to inspect
     * @return Field the Argument field
     */
    public static Field getArgumentField(Class<?> clazz) {
        synchronized (argumentFieldCaches) {
            LOGGER.trace("getArgumentField(%s)", clazz.getName());

            if (argumentFieldCaches.containsKey(clazz)) {
                return argumentFieldCaches.get(clazz);
            }

            List<Field> argumentFields = getFields(clazz, TestEngine.Argument.class, Argument.class);
            LOGGER.trace(
                    "class [%s] @TestEngine.Argument field count [%d]",
                    clazz.getName(),
                    argumentFields.size());

            if (argumentFields.size() != 1) {
                throw new TestClassConfigurationException(
                        String.format(
                                "Test class [%s] must define one @TestEngine.Argument field",
                                clazz.getName()));
            }

            argumentFieldCaches.put(clazz, argumentFields.get(0));

            return argumentFields.get(0);
        }
    }

    /**
     * Method to get a List of @TestEngine.BeforeAll Methods sorted alphabetically
     *
     * @param clazz class to inspect
     * @return list of Methods
     */
    public static List<Method> getBeforeAllMethods(Class<?> clazz) {
        synchronized (beforeAllMethodCache) {
            LOGGER.trace("getBeforeAllMethods(%s)", clazz.getName());

            if (beforeAllMethodCache.containsKey(clazz)) {
                return beforeAllMethodCache.get(clazz);
            }

            List<Method> methods =
                    getMethods(
                            clazz,
                            TestEngine.BeforeAll.class,
                            Scope.NON_STATIC,
                            Void.class,
                            (Class<?>[]) null);

            LOGGER.trace("class [%s] @TestEngine.BeforeAll method count [%d]", clazz.getName(), methods.size());

            if (!methods.isEmpty()) {
                sortMethods(methods);
                validateDistinctOrder(clazz, methods);
            }

            methods = Collections.unmodifiableList(methods);
            beforeAllMethodCache.put(clazz, methods);

            return methods;
        }
    }

    /**
     * Method to get a List of @TestEngine.BeforeEach Methods sorted alphabetically
     *
     * @param clazz class to inspect
     * @return list of Methods
     */
    public static List<Method> getBeforeEachMethods(Class<?> clazz) {
        synchronized (beforeEachMethodCache) {
            LOGGER.trace("getBeforeEachMethods(%s)", clazz.getName());

            if (beforeEachMethodCache.containsKey(clazz)) {
                return beforeEachMethodCache.get(clazz);
            }

            List<Method> methods =
                    getMethods(
                            clazz,
                            TestEngine.BeforeEach.class,
                            Scope.NON_STATIC,
                            Void.class,
                            (Class<?>[]) null);

            LOGGER.trace("class [%s] @TestEngine.BeforeEach method count [%d]", clazz.getName(), methods.size());

            if (!methods.isEmpty()) {
                sortMethods(methods);
                validateDistinctOrder(clazz, methods);
            }

            methods = Collections.unmodifiableList(methods);
            beforeEachMethodCache.put(clazz, methods);

            return methods;
        }
    }

    /**
     * Method to get a List of @TestEngine.Test Methods sorted alphabetically
     *
     * @param clazz class to inspect
     * @return list of Methods
     */
    public static List<Method> getTestMethods(Class<?> clazz) {
        synchronized (testMethodCache) {
            LOGGER.trace("getTestMethods(%s)", clazz.getName());

            if (testMethodCache.containsKey(clazz)) {
                return testMethodCache.get(clazz);
            }

            List<Method> methods =
                    getMethods(
                            clazz,
                            TestEngine.Test.class,
                            Scope.NON_STATIC,
                            Void.class,
                            (Class<?>[]) null)
                            .stream()
                            .filter(method -> !method.isAnnotationPresent(TestEngine.Disabled.class))
                            .collect(Collectors.toList());

            LOGGER.trace("class [%s] @TestEngine.Test method count [%d]", clazz.getName(), methods.size());

            if (!methods.isEmpty()) {
                sortMethods(methods);
                validateDistinctOrder(clazz, methods);
                methods = Collections.unmodifiableList(methods);
            }

            testMethodCache.put(clazz, methods);

            return methods;
        }
    }

    /**
     * Method to get a List of @TestEngine.AfterEach Methods sorted alphabetically
     *
     * @param clazz class to inspect
     * @return list of Methods
     */
    public static List<Method> getAfterEachMethods(Class<?> clazz) {
        synchronized (afterEachMethodCache) {
            LOGGER.trace("getAfterEachMethods(%s)", clazz.getName());

            if (afterEachMethodCache.containsKey(clazz)) {
                return afterEachMethodCache.get(clazz);
            }

            List<Method> methods =
                    getMethods(
                            clazz,
                            TestEngine.AfterEach.class,
                            Scope.NON_STATIC,
                            Void.class,
                            (Class<?>[]) null);

            LOGGER.trace("class [%s] @TestEngine.AfterEach method count [%d]", clazz.getName(), methods.size());

            if (!methods.isEmpty()) {
                sortMethods(methods);
                validateDistinctOrder(clazz, methods);
            }

            methods = Collections.unmodifiableList(methods);
            afterEachMethodCache.put(clazz, methods);

            return methods;
        }
    }

    /**
     * Method to get a List of @TestEngine.AfterAll Methods sorted alphabetically
     *
     * @param clazz class to inspect
     * @return list of Methods
     */
    public static List<Method> getAfterAllMethods(Class<?> clazz) {
        synchronized (afterAllMethodCache) {
            LOGGER.trace("getAfterAllMethods(%s)", clazz.getName());

            if (afterAllMethodCache.containsKey(clazz)) {
                return afterAllMethodCache.get(clazz);
            }

            List<Method> methods =
                    getMethods(
                            clazz,
                            TestEngine.AfterAll.class,
                            Scope.NON_STATIC,
                            Void.class,
                            (Class<?>[]) null);

            LOGGER.trace("class [%s] @TestEngine.AfterAll method count [%d]", clazz.getName(), methods.size());

            if (!methods.isEmpty()) {
                sortMethods(methods);
                validateDistinctOrder(clazz, methods);
            }

            methods = Collections.unmodifiableList(methods);
            afterAllMethodCache.put(clazz, methods);

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
        synchronized (concludeMethodCache) {
            LOGGER.trace("getConcludeMethods(%s)", clazz.getName());

            if (concludeMethodCache.containsKey(clazz)) {
                return concludeMethodCache.get(clazz);
            }

            List<Method> methods =
                    getMethods(
                            clazz,
                            TestEngine.Conclude.class,
                            Scope.NON_STATIC,
                            Void.class,
                            (Class<?>[]) null);

            LOGGER.trace("class [%s] @TestEngine.Conclude method count [%d]", clazz.getName(), methods.size());

            if (!methods.isEmpty()) {
                sortMethods(methods);
                validateDistinctOrder(clazz, methods);
            }

            methods = Collections.unmodifiableList(methods);
            concludeMethodCache.put(clazz, methods);

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

        if (method.isAnnotationPresent(TestEngine.DisplayName.class)) {
            String value = method.getAnnotation(TestEngine.DisplayName.class).value();
            if (value != null && !value.trim().isEmpty()) {
                displayName = value.trim();
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

        if (clazz.isAnnotationPresent(TestEngine.DisplayName.class)) {
            String value = clazz.getAnnotation(TestEngine.DisplayName.class).value();
            if (value != null && !value.trim().isEmpty()) {
                displayName = value.trim();
            }
        }

        LOGGER.trace("class [%s] display name [%s]", clazz.getName(), displayName);

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
    private static List<Field> getFields(
            Class<?> clazz,
            Class<? extends Annotation> annotation,
            Class<?> fieldType) {
        LOGGER.trace("getFields(%s, %s, %s)", clazz.getName(), annotation.getName(), fieldType.getName());

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
        LOGGER.trace("resolveFields(%s, %s, %s)", clazz.getName(), annotation.getName(), fieldType.getName());

        Stream.of(clazz.getDeclaredFields())
                .filter(field -> {
                    int modifiers = field.getModifiers();
                    return !Modifier.isFinal(modifiers)
                            && !Modifier.isStatic(modifiers)
                            && field.isAnnotationPresent(annotation)
                            && fieldType.isAssignableFrom(field.getType());
                })
                .forEach(field -> {
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
            Class<?> ... parameterTypes) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder
                .append(
                        String.format(
                                "getMethods(%s, %s, %s, %s",
                                clazz.getName(),
                                annotation.getName(),
                                scope,
                                returnType.getName()));

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

            stringBuilder
                    .append(
                            String.format(
                                    "resolveMethods(%s, %s, %s, %s",
                                    clazz.getName(),
                                    annotation.getName(),
                                    scope,
                                    returnType.getName()));

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
                    .filter(method -> {
                        int modifiers = method.getModifiers();
                        return Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers);
                    })
                    .filter(method -> {
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
                    .filter(method -> {
                        if (parameterTypes == null) {
                            return method.getParameterTypes().length == 0;
                        }
                        if (parameterTypes.length != method.getParameterCount()) {
                            return false;
                        }
                        Class<?>[] methodParameterTypes = method.getParameterTypes();
                        for (int i = 0; i < parameterTypes.length; i++) {
                            if (!parameterTypes[i].isAssignableFrom(methodParameterTypes[i])) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .filter(method -> {
                        if (returnType == Void.class) {
                            return method.getReturnType().getName().equals("void");
                        } else {
                            return returnType.isAssignableFrom(method.getReturnType());
                        }
                    })
                    .forEach(method -> {
                        if (methodMap.putIfAbsent(method.getName(), method) == null) {
                            method.setAccessible(true);
                        }
                    });
        } catch (Throwable t) {
            if (t instanceof NoClassDefFoundError) {
                // DO NOTHING
            } else {
                Throwables.throwIfUnchecked(t);
            }
        }

        Class<?> declaringClass = clazz.getSuperclass();
        if (declaringClass != null && !declaringClass.equals(Object.class)) {
            resolveMethods(declaringClass, annotation, scope, returnType, parameterTypes, methodMap);
        }
    }

    /**
     * Method to sort a List of classes first by @TestEngine.Order annotation, then by display name / class name
     *
     * @param classes list of Classes to sort
     */
    private static void sortClasses(List<Class<?>> classes) {
        classes.sort((o1, o2) -> {
            boolean o1AnnotationPresent = o1.isAnnotationPresent(TestEngine.Order.class);
            boolean o2AnnotationPresent = o2.isAnnotationPresent(TestEngine.Order.class);
            if (o1AnnotationPresent) {
                if (o2AnnotationPresent) {
                    // Sort based on @TestEngine.Order value
                    int o1Order = o1.getAnnotation(TestEngine.Order.class).value();
                    int o2Order = o2.getAnnotation(TestEngine.Order.class).value();
                    return Integer.compare(o1Order, o2Order);
                } else {
                    return -1;
                }
            } else if (o2AnnotationPresent) {
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
     * Method to validate @TestEngine.Order is unique on classes
     *
     * @param classes classes
     */
    private static void validateDistinctOrder(List<Class<?>> classes) {
        Set<Integer> integers = new HashSet<>();

        classes.forEach(clazz -> {
            if (clazz.isAnnotationPresent(TestEngine.Order.class)) {
                int value = clazz.getAnnotation(TestEngine.Order.class).value();
                if (integers.contains(value)) {
                    throw new TestClassConfigurationException(
                            String.format(
                                    "Test class [%s] (or superclass) contains a duplicate @TestEngine.Order(%d) class annotation",
                                    clazz.getName(),
                                    value));
                } else {
                    integers.add(value);
                }
            }
        });
    }

    /**
     * Method to sort a List of methods first by @TestEngine.Order annotation, then alphabetically
     *
     * @param methods list of Methods to sort
     */
    private static void sortMethods(List<Method> methods) {
        methods.sort((o1, o2) -> {
            boolean o1AnnotationPresent = o1.isAnnotationPresent(TestEngine.Order.class);
            boolean o2AnnotationPresent = o2.isAnnotationPresent(TestEngine.Order.class);
            if (o1AnnotationPresent) {
                if (o2AnnotationPresent) {
                    // Sort based on @TestEngine.Order value
                    int o1Order = o1.getAnnotation(TestEngine.Order.class).value();
                    int o2Order = o2.getAnnotation(TestEngine.Order.class).value();
                    return Integer.compare(o1Order, o2Order);
                } else {
                    return -1;
                }
            } else if (o2AnnotationPresent) {
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

        methods.forEach(method -> {
            if (method.isAnnotationPresent(TestEngine.Order.class)) {
                int value = method.getAnnotation(TestEngine.Order.class).value();
                if (integers.contains(value)) {
                    throw new TestClassConfigurationException(
                            String.format(
                                    "Test class [%s] (or superclass) contains a duplicate @TestEngine.Order(%d) method annotation",
                                    clazz.getName(),
                                    value));
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
                annotation.getDeclaringClass().getSimpleName(),
                annotation.getSimpleName());
    }
}