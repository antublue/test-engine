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

package org.antublue.test.engine.internal;

import org.antublue.test.engine.api.Parameter;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.launcher.TestPlan;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class to implement methods to get test class fields / methods, caching the information
 */
@SuppressWarnings({"unchecked", "PMD.GodClass"})
public final class TestEngineReflectionUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEngineReflectionUtils.class);

    private enum Scope { STATIC, NON_STATIC }

    private static final Map<Class<?>, Method> parameterSupplierMethodCache;
    private static final Map<Class<?>, List<Field>> parameterFieldCache;
    private static final Map<Class<?>, List<Method>> parameterMethodCache;
    private static final Map<Class<?>, List<Method>> beforeClassMethodCache;
    private static final Map<Class<?>, List<Method>> beforeAllMethodCache;
    private static final Map<Class<?>, List<Method>> beforeEachMethodCache;
    private static final Map<Class<?>, List<Method>> testMethodCache;
    private static final Map<Class<?>, List<Method>> afterEachMethodCache;
    private static final Map<Class<?>, List<Method>> afterAllMethodCache;
    private static final Map<Class<?>, List<Method>> afterClassMethodCache;

    static {
        parameterSupplierMethodCache = new HashMap<>();
        parameterFieldCache = new HashMap<>();
        parameterMethodCache = new HashMap<>();
        beforeClassMethodCache = new HashMap<>();
        beforeAllMethodCache = new HashMap<>();
        beforeEachMethodCache = new HashMap<>();
        testMethodCache = new HashMap<>();
        afterEachMethodCache = new HashMap<>();
        afterAllMethodCache = new HashMap<>();
        afterClassMethodCache = new HashMap<>();
    }

    /**
     * Constructor
     */
    private TestEngineReflectionUtils() {
        // DO NOTHING
    }

    /**
     * Method to get a List of Parameters for a Class
     *
     * @param clazz
     * @return
     */
    public static List<Parameter> getParameters(Class<?> clazz) {
        try {
            Method method = getParameterSupplierMethod(clazz);

            Stream<Parameter> stream = (Stream<Parameter>) method.invoke(null, (Object[]) null);
            return stream.collect(Collectors.toList());
        } catch (TestClassConfigurationException e) {
            throw e;
        } catch (Throwable t) {
            throw new TestClassConfigurationException("Class [%s]] exception getting Stream<Parameter>", t);
        }
    }

    /**
     * Method to get a List of @TestEngine.Parameter Fields
     *
     * @param clazz
     * @return
     */
    public static List<Field> getParameterFields(Class<?> clazz) {
        synchronized (parameterFieldCache) {
            LOGGER.trace("getParameterFields(%s)", clazz.getName());

            if (parameterFieldCache.containsKey(clazz)) {
                return parameterFieldCache.get(clazz);
            }

            List<Field> parameterFields = getFields(clazz, TestEngine.Parameter.class, Parameter.class);
            parameterFieldCache.put(clazz, parameterFields);

            return parameterFields;
        }
    }

    /**
     * Method to get a List of @TestEngine.BeforeClass Methods sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static List<Method> getBeforeClassMethods(Class<?> clazz) {
        synchronized (beforeClassMethodCache) {
            LOGGER.trace("getBeforeClassMethods(%s)", clazz.getName());

            if (beforeClassMethodCache.containsKey(clazz)) {
                return beforeClassMethodCache.get(clazz);
            }

            List<Method> methods =
                    getMethods(
                            clazz,
                            TestEngine.BeforeClass.class,
                            Scope.STATIC,
                            Void.class,
                            (Class<?>[]) null);

            sortByOrderAnnotation(methods);
            methods = Collections.unmodifiableList(methods);
            beforeClassMethodCache.put(clazz, methods);

            return methods;
        }
    }

    /**
     * Method to get a List of @TestEngine.Parameter Methods sorted alphabetically
     *
     * @param clazz
     * @return
     */
    @SuppressWarnings("deprecation")
    public static List<Method> getParameterMethods(Class<?> clazz) {
        synchronized (parameterMethodCache) {
            LOGGER.trace("getParameterMethods(%s)", clazz.getName());

            if (parameterMethodCache.containsKey(clazz)) {
                return parameterMethodCache.get(clazz);
            }

            List<Method> methods =
                    getMethods(
                            clazz,
                            TestEngine.Parameter.class,
                            Scope.NON_STATIC,
                            Void.class,
                            Parameter.class);

            sortByOrderAnnotation(methods);
            methods = Collections.unmodifiableList(methods);
            parameterMethodCache.put(clazz, methods);

            return methods;
        }
    }

    /**
     * Method to get a List of @TestEngine.BeforeAll Methods sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static List<Method> getBeforeAllMethods(Class<?> clazz) {
        synchronized (beforeAllMethodCache) {
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

            sortByOrderAnnotation(methods);
            methods = Collections.unmodifiableList(methods);
            beforeAllMethodCache.put(clazz, methods);

            return methods;
        }
    }

    /**
     * Method to get a List of @TestEngine.BeforeEach Methods sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static List<Method> getBeforeEachMethods(Class<?> clazz) {
        synchronized (beforeEachMethodCache) {
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

            sortByOrderAnnotation(methods);
            methods = Collections.unmodifiableList(methods);
            beforeEachMethodCache.put(clazz, methods);

            return methods;
        }
    }

    /**
     * Method to get a List of @TestEngine.Test Methods sorted alphabetically
     *
     * @param clazz
     * @return
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

            sortByOrderAnnotation(methods);
            methods = Collections.unmodifiableList(methods);
            testMethodCache.put(clazz, methods);

            return methods;
        }
    }

    /**
     * Method to get a List of @TestEngine.AfterEach Methods sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static List<Method> getAfterEachMethods(Class<?> clazz) {
        synchronized (afterEachMethodCache) {
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

            sortByOrderAnnotation(methods);
            methods = Collections.unmodifiableList(methods);
            afterEachMethodCache.put(clazz, methods);

            return methods;
        }
    }

    /**
     * Method to get a List of @TestEngine.AfterAll Methods sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static List<Method> getAfterAllMethods(Class<?> clazz) {
        synchronized (afterAllMethodCache) {
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

            sortByOrderAnnotation(methods);
            methods = Collections.unmodifiableList(methods);
            afterAllMethodCache.put(clazz, methods);

            return methods;
        }
    }

    /**
     * Method to get a List of @TestEngine.AfterClass Methods sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static List<Method> getAfterClassMethods(Class<?> clazz) {
        synchronized (afterClassMethodCache) {
            if (afterClassMethodCache.containsKey(clazz)) {
                return afterClassMethodCache.get(clazz);
            }

            List<Method> methods =
                    getMethods(
                            clazz,
                            TestEngine.AfterClass.class,
                            Scope.STATIC,
                            Void.class,
                            (Class<?>[]) null);

            sortByOrderAnnotation(methods);
            methods = Collections.unmodifiableList(methods);
            afterClassMethodCache.put(clazz, methods);

            return methods;
        }
    }

    /**
     * Method to get a @TestEngine.ParameterSupplier Method
     *
     * @param clazz
     * @return
     */
    private static Method getParameterSupplierMethod(Class<?> clazz) {
        synchronized (parameterSupplierMethodCache) {
            LOGGER.trace("getParameterSupplierMethod(%s)", clazz.getName());

            if (parameterSupplierMethodCache.containsKey(clazz)) {
                return parameterSupplierMethodCache.get(clazz);
            }

            List<Method> methodList =
                    getMethods(
                            clazz,
                            TestEngine.ParameterSupplier.class,
                            Scope.STATIC,
                            Stream.class,
                            (Class<?>[]) null);

            if (methodList.size() != 1) {
                throw new TestClassConfigurationException("Class [%s] must define one @TestEngine.ParameterSupplier method");
            }

            Method method = methodList.get(0);
            parameterSupplierMethodCache.put(clazz, method);

            return method;
        }
    }

    /**
     * Method to get a List of all fields from a Class and super Classes
     *
     * @param clazz
     * @param annotation
     * @param fieldType
     * @return
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

        return fields;
    }

    /**
     * Method to recursively resolve Fields
     *
     * @param clazz
     * @param annotation
     * @param fieldType
     * @param fieldSet
     */
    private static void resolveFields(
            Class<?> clazz,
            Class<? extends Annotation> annotation,
            Class<?> fieldType,
            Set<Field> fieldSet) {
        LOGGER.trace("resolveFields(%s, %s, %s)", clazz.getName(), annotation.getName(), fieldType);

        Stream.of(clazz.getDeclaredFields())
                .filter(field -> {
                    int modifiers = field.getModifiers();
                    if (!Modifier.isFinal(modifiers)
                            && !Modifier.isStatic(modifiers)
                            && field.isAnnotationPresent(annotation)
                            && field.getType() == fieldType) {
                        return true;
                    }
                    return false;
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
                        return method.getParameterTypes().length == 0;
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
        if (declaringClass != null && !declaringClass.equals(Object.class)) {
            resolveMethods(declaringClass, annotation, scope, returnType, parameterTypes, methodMap);
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
                    // Sort based on @TestEngine.Order value
                    int o1Order = o1.getAnnotation(TestEngine.Order.class).value();
                    int o2Order = o2.getAnnotation(TestEngine.Order.class).value();
                    return o1Order < o2Order ? -1 : o1Order == o2Order ? 0 : 1;
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