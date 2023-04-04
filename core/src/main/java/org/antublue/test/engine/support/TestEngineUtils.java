/*
 * Copyright 2022-2023 Douglas Hoard
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

package org.antublue.test.engine.support;

import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.support.logger.Logger;
import org.antublue.test.engine.support.logger.LoggerFactory;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.launcher.TestPlan;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Class to implement methods to get test class fields / methods, caching the information
 */
@SuppressWarnings("PMD.GodClass")
public final class TestEngineUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEngineUtils.class);

    private enum Scope { STATIC, NON_STATIC }

    private static final Map<Class<?>, Collection<Method>> argumentsMethodCache;
    private static final Map<Class<?>, Collection<Method>> argumentMethodCache;
    private static final Map<Class<?>, Collection<Method>> beforeClassMethodCache;
    private static final Map<Class<?>, Collection<Method>> beforeAllMethodCache;
    private static final Map<Class<?>, Collection<Method>> beforeEachMethodCache;
    private static final Map<Class<?>, Collection<Method>> testMethodCache;
    private static final Map<Class<?>, Collection<Method>> afterEachMethodCache;
    private static final Map<Class<?>, Collection<Method>> afterAllMethodCache;
    private static final Map<Class<?>, Collection<Method>> afterClassMethodCache;
    private static final Map<Class<?>, String> classDisplayNameCache;
    private static final Map<Method, String> methodDisplayNameCache;

    static {
        argumentsMethodCache = new HashMap<>();
        argumentMethodCache = new HashMap<>();
        beforeClassMethodCache = new HashMap<>();
        beforeAllMethodCache = new HashMap<>();
        beforeEachMethodCache = new HashMap<>();
        testMethodCache = new HashMap<>();
        afterEachMethodCache = new HashMap<>();
        afterAllMethodCache = new HashMap<>();
        afterClassMethodCache = new HashMap<>();
        classDisplayNameCache = new HashMap<>();
        methodDisplayNameCache = new HashMap<>();
    }

    /**
     * Constructor
     */
    private TestEngineUtils() {
        // DO NOTHING
    }

    /**
     * Method to get a Collection of all methods from a Class and super Classes
     *
     * @param clazz
     * @return
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
                            "getAllMethods(%s, %s, %s, %s",
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
     * @param clazz
     * @param annotation
     * @param scope
     * @param returnType
     * @param parameterTypes
     * @param methodMap
     */
    private static void resolveMethods(
            Class<?> clazz,
            Class<? extends Annotation> annotation,
            Scope scope,
            Class<?> returnType,
            Class<?>[] parameterTypes,
            Map<String, Method> methodMap) {
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

        Stream.of(clazz.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(annotation))
                .filter(method -> {
                    int modifiers = method.getModifiers();
                    return Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers);
                })
                .filter(method -> {
                    int modifiers = method.getModifiers();
                    if (scope == Scope.STATIC) {
                        return Modifier.isStatic(modifiers);
                    }
                    else {
                        return !Modifier.isStatic(modifiers);
                    }
                })
                .filter(method -> {
                    if (parameterTypes == null) {
                        return (method.getParameterTypes().length == 0);
                    }

                    if (parameterTypes.length != method.getParameterCount()) {
                        return false;
                    }

                    Class<?>[] methodParameterTypes = method.getParameterTypes();

                    for (int i = 0; i < parameterTypes.length; i++) {
                        if (!methodParameterTypes[i].isAssignableFrom(parameterTypes[i])) {
                            return false;
                        }
                    }

                    return true;
                })
                .filter(method -> {
                    // TODO understand why void is special
                    if (returnType == Void.class) {
                        return method.getReturnType().getName().equals("void");
                    } else {
                        return method.getReturnType().equals(returnType);
                    }
                })
                .forEach(method -> {
                    if (methodMap.putIfAbsent(method.getName(), method) == null) {
                        method.setAccessible(true);
                    }
                });

        Class<?> declaringClass = clazz.getSuperclass();
        if ((declaringClass != null) && !declaringClass.equals(Object.class)) {
            resolveMethods(declaringClass, annotation, scope, returnType, parameterTypes, methodMap);
        }
    }

    /**
     * Method to get a Collection of @TestEngine.BeforeClass Methods sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static Collection<Method> getBeforeClassMethods(Class<?> clazz) {
        synchronized (beforeClassMethodCache) {
            LOGGER.trace(String.format("getBeforeClassMethods(%s)", clazz.getName()));

            if (beforeClassMethodCache.containsKey(clazz)) {
                return beforeClassMethodCache.get(clazz);
            }

            List<Method> methodList =
                    getMethods(
                            clazz,
                            TestEngine.BeforeClass.class,
                            Scope.STATIC,
                            Void.class,
                            (Class<?>[]) null);

            sortByOrderAnnotation(methodList);

            Collection<Method> methods = Collections.unmodifiableCollection(methodList);

            beforeClassMethodCache.put(clazz, methods);

            return methods;
        }
    }

    /**
     * Method to get a Collection of @TestEngine.Arguments Methods sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static Collection<Method> getArgumentsMethods(Class<?> clazz) {
        synchronized (argumentsMethodCache) {
            LOGGER.trace(String.format("getArgumentsMethods(%s)", clazz.getName()));

            if (argumentsMethodCache.containsKey(clazz)) {
                return argumentsMethodCache.get(clazz);
            }

            List<Method> methodList =
                    getMethods(
                            clazz,
                            TestEngine.Arguments.class,
                            Scope.STATIC,
                            Stream.class,
                            (Class<?>[]) null);

            sortByOrderAnnotation(methodList);

            Collection<Method> methods = Collections.unmodifiableCollection(methodList);

            argumentsMethodCache.put(clazz, methods);

            return methods;
        }
    }

    public static Collection<Method> getArgumentMethods(Class<?> clazz) {
        synchronized (argumentMethodCache) {
            LOGGER.trace(String.format("getArgumentMethods(%s)", clazz.getName()));

            if (argumentMethodCache.containsKey(clazz)) {
                return argumentMethodCache.get(clazz);
            }
            List<Method> methodList =
                    getMethods(
                            clazz,
                            TestEngine.Argument.class,
                            Scope.NON_STATIC,
                            Void.class,
                            Argument.class);

            sortByOrderAnnotation(methodList);

            Collection<Method> methods = Collections.unmodifiableCollection(methodList);

            argumentMethodCache.put(clazz, methods);

            return methods;
        }
    }

    /**
     * Method to get a Collection of @TestEngine.BeforeAll Methods sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static Collection<Method> getBeforeAllMethods(Class<?> clazz) {
        synchronized (beforeAllMethodCache) {
            if (beforeAllMethodCache.containsKey(clazz)) {
                return beforeAllMethodCache.get(clazz);
            }

            List<Method> methodList =
                    getMethods(
                            clazz,
                            TestEngine.BeforeAll.class,
                            Scope.NON_STATIC,
                            Void.class,
                            (Class<?>[]) null);

            sortByOrderAnnotation(methodList);

            Collection<Method> methods = Collections.unmodifiableCollection(methodList);

            beforeAllMethodCache.put(clazz, methods);

            return methods;
        }
    }

    /**
     * Method to get a Collection of @TestEngine.BeforeEach Methods sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static Collection<Method> getBeforeEachMethods(Class<?> clazz) {
        synchronized (beforeEachMethodCache) {
            if (beforeEachMethodCache.containsKey(clazz)) {
                return beforeEachMethodCache.get(clazz);
            }

            List<Method> methodList =
                    getMethods(
                            clazz,
                            TestEngine.BeforeEach.class,
                            Scope.NON_STATIC,
                            Void.class,
                            (Class<?>[]) null);

            sortByOrderAnnotation(methodList);

            Collection<Method> methods = Collections.unmodifiableCollection(methodList);

            beforeEachMethodCache.put(clazz, methods);

            return methods;
        }
    }

    /**
     * Method to get a Collection of @TestEngine.Test Methods sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static Collection<Method> getTestMethods(Class<?> clazz) {
        synchronized (testMethodCache) {
            LOGGER.trace(String.format("getTestMethods(%s)", clazz.getName()));

            if (testMethodCache.containsKey(clazz)) {
                return testMethodCache.get(clazz);
            }

            List<Method> methodList =
                    getMethods(
                            clazz,
                            TestEngine.Test.class,
                            Scope.NON_STATIC,
                            Void.class,
                            (Class<?>[]) null);

            sortByOrderAnnotation(methodList);

            Collection<Method> methods = Collections.unmodifiableCollection(methodList);

            testMethodCache.put(clazz, methods);

            return methods;
        }
    }

    /**
     * Method to get a Collection of @TestEngine.AfterEach Methods sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static Collection<Method> getAfterEachMethods(Class<?> clazz) {
        synchronized (afterEachMethodCache) {
            if (afterEachMethodCache.containsKey(clazz)) {
                return afterEachMethodCache.get(clazz);
            }

            List<Method> methodList =
                    getMethods(
                            clazz,
                            TestEngine.AfterEach.class,
                            Scope.NON_STATIC,
                            Void.class,
                            (Class<?>[]) null);

            sortByOrderAnnotation(methodList);

            Collection<Method> methods = Collections.unmodifiableCollection(methodList);

            afterEachMethodCache.put(clazz, methods);

            return methods;
        }
    }

    /**
     * Method to get a Collection of @TestEngine.AfterAll Methods sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static Collection<Method> getAfterAllMethods(Class<?> clazz) {
        synchronized (afterAllMethodCache) {
            if (afterAllMethodCache.containsKey(clazz)) {
                return afterAllMethodCache.get(clazz);
            }

            List<Method> methodList =
                    getMethods(
                            clazz,
                            TestEngine.AfterAll.class,
                            Scope.NON_STATIC,
                            Void.class,
                            (Class<?>[]) null);

            sortByOrderAnnotation(methodList);

            Collection<Method> methods = Collections.unmodifiableCollection(methodList);

            afterAllMethodCache.put(clazz, methods);

            return methods;
        }
    }

    /**
     * Method to get a Collection of @TestEngine.AfterClass Methods sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static Collection<Method> getAfterClassMethods(Class<?> clazz) {
        synchronized (afterClassMethodCache) {
            if (afterClassMethodCache.containsKey(clazz)) {
                return afterClassMethodCache.get(clazz);
            }

            List<Method> methodList =
                    getMethods(
                            clazz,
                            TestEngine.AfterClass.class,
                            Scope.STATIC,
                            Void.class,
                            (Class<?>[]) null);

            sortByOrderAnnotation(methodList);

            Collection<Method> methods = Collections.unmodifiableCollection(methodList);

            afterClassMethodCache.put(clazz, methods);

            return methods;
        }
    }

    /**
     * Method to sort a List of methods first by @TestEngine.Order annotation, then alphabetically
     *
     * @param methods
     */
    private static void sortByOrderAnnotation(List<Method> methods) {
        Collections.sort(methods, (o1, o2) -> {
            boolean o1AnnotationPresent = o1.isAnnotationPresent(TestEngine.Order.class);
            boolean o2AnnotationPresent = o2.isAnnotationPresent(TestEngine.Order.class);
            if (o1AnnotationPresent) {
                if (o2AnnotationPresent) {
                    // Sort based on @TestEngine.Test.Order value
                    int o1Order = o1.getAnnotation(TestEngine.Order.class).value();
                    int o2Order = o2.getAnnotation(TestEngine.Order.class).value();
                    return (o1Order < o2Order) ? -1 : ((o1Order == o2Order) ? 0 : 1);
                } else {
                    return -1;
                }
            } else if (o2AnnotationPresent) {
                return 1;
            } else {
                return o1.getName().compareTo(o2.getName());
            }
        });
    }

    /**
     * Method to get whether a test class is a base class
     *
     * @param clazz
     * @return
     */
    public static boolean isBaseClass(Class<?> clazz) {
        return clazz.isAnnotationPresent(TestEngine.BaseClass.class);
    }

    /**
     * Method to get whether a test class is disabled
     *
     * @param clazz
     * @return
     */
    public static boolean isDisabled(Class<?> clazz) {
        return clazz.isAnnotationPresent(TestEngine.Disabled.class);
    }

    /**
     * Method to get whether a test Method is disabled
     *
     * @param method
     * @return
     */
    public static boolean isDisabled(Method method) {
        return method.isAnnotationPresent(TestEngine.Disabled.class);
    }

    /**
     * Method to create a TestPlan from a TestDescriptor
     *
     * @param testDescriptor
     * @param configurationParameters
     * @return
     */
    public static TestPlan createTestPlan(TestDescriptor testDescriptor, ConfigurationParameters configurationParameters) {
        return TestPlan.from(Collections.singleton(testDescriptor), configurationParameters);
    }
}
