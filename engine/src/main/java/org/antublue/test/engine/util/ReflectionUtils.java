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

package org.antublue.test.engine.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.platform.commons.support.ReflectionSupport;

/** Class to implement ReflectionUtils */
@SuppressWarnings({"PMD.AvoidAccessibilityAlteration", "PMD.EmptyCatchBlock"})
public final class ReflectionUtils {

    private static final ReflectionUtils SINGLETON = new ReflectionUtils();

    private static final Predicate<Class<?>> ALL_CLASSES_FILTER = clazz -> clazz != Object.class;

    private static final Predicate<Field> ALL_FIELDS_FILTER =
            field -> field.getDeclaringClass() != Object.class;

    private static final Predicate<Method> ALL_METHODS_FILTER =
            method -> method.getDeclaringClass() != Object.class;

    public static final Class<?>[] NO_CLASS_ARGS = null;

    public static final Object[] NO_OBJECT_ARGS = null;

    private ReflectionUtils() {
        // DO NOTHING
    }

    public static ReflectionUtils getSingleton() {
        return SINGLETON;
    }

    /**
     * Method to find all classes for a URI
     *
     * @param uri uri
     * @return a List of Classes
     */
    public List<Class<?>> findAllClasses(URI uri) {
        return findAllClasses(uri, ALL_CLASSES_FILTER);
    }

    /**
     * Method to find all class for a URI
     *
     * @param uri uri
     * @param classFilter classFilter
     * @return a List of Classes
     */
    public List<Class<?>> findAllClasses(URI uri, Predicate<Class<?>> classFilter) {
        return ReflectionSupport.findAllClassesInClasspathRoot(
                uri, classFilter, classNameFilter -> true);
    }

    /**
     * Method to find all classes with a package name
     *
     * @param packageName packageName
     * @return a List of Classes
     */
    public List<Class<?>> findAllClasses(String packageName) {
        return findAllClasses(packageName, ALL_CLASSES_FILTER);
    }

    /**
     * Method to find all classes with a package name
     *
     * @param packageName packageName
     * @param classFilter classFilter
     * @return a List of Classes
     */
    public List<Class<?>> findAllClasses(String packageName, Predicate<Class<?>> classFilter) {
        return ReflectionSupport.findAllClassesInPackage(
                packageName, classFilter, classNameFilter -> true);
    }

    /**
     * Method to find all fields of a Class and superclasses
     *
     * @param clazz class to inspect
     * @return a List of Fields
     */
    public List<Field> findFields(Class<?> clazz) {
        return findFields(clazz, ALL_FIELDS_FILTER);
    }

    /**
     * Method to find all fields of a Class and superclasses
     *
     * @param clazz class to inspect
     * @param fieldFilter fieldFilter
     * @return a List of Fields
     */
    public List<Field> findFields(Class<?> clazz, Predicate<Field> fieldFilter) {
        List<Field> fields = new ArrayList<>();

        List<Class<?>> classes = buildClassHierarchy(clazz);
        for (Class<?> c : classes) {
            fields.addAll(
                    Arrays.stream(c.getDeclaredFields())
                            .filter(fieldFilter)
                            .peek(field -> field.setAccessible(true))
                            .collect(Collectors.toList()));
        }

        return fields;
    }

    /**
     * Method find all methods of a Class and superclasses
     *
     * @param clazz class to inspect
     * @return a List of Methods
     */
    public List<Method> findMethods(Class<?> clazz) {
        return findMethods(clazz, ALL_METHODS_FILTER);
    }

    /**
     * Method find all methods of a Class
     *
     * @param clazz class to inspect
     * @param methodFilter methodFilter
     * @return a List of Methods
     */
    public List<Method> findMethods(Class<?> clazz, Predicate<Method> methodFilter) {
        try {
            return buildClassHierarchy(clazz).stream()
                    .flatMap(
                            (Function<Class<?>, Stream<Method>>)
                                    c -> Stream.of(c.getDeclaredMethods()))
                    .filter(methodFilter)
                    .filter(
                            distinctByKey(
                                    (Function<Method, String>)
                                            method -> {
                                                StringBuilder stringBuilder =
                                                        new StringBuilder()
                                                                .append(
                                                                        isStatic(method)
                                                                                ? "static "
                                                                                : "")
                                                                .append(
                                                                        method.getReturnType()
                                                                                .getName())
                                                                .append(" ")
                                                                .append(method.getName());
                                                Class<?>[] parameterTypes =
                                                        method.getParameterTypes();
                                                for (Class<?> parameterType : parameterTypes) {
                                                    stringBuilder
                                                            .append(" ")
                                                            .append(parameterType.getName());
                                                }
                                                return stringBuilder.toString();
                                            }))
                    .peek(method -> method.setAccessible(true))
                    .collect(Collectors.toList());
        } catch (NoClassDefFoundError e) {
            return new ArrayList<>();
        }
    }

    /**
     * Method to create a new instance of a Class
     *
     * @param className className
     * @return object object
     * @throws Throwable Throwable
     */
    public Object newInstance(String className) throws Throwable {
        Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
        Constructor<?> constructor = clazz.getDeclaredConstructor(NO_CLASS_ARGS);
        return constructor.newInstance(NO_OBJECT_ARGS);
    }

    /**
     * Method to determine if a Method is abstract
     *
     * @param method method
     * @return true if the Method is abstract, otherwise false
     */
    public boolean isAbstract(Method method) {
        return Modifier.isAbstract(method.getModifiers());
    }

    /**
     * Method to determine if a Method is static
     *
     * @param method method
     * @return true if the Method is static, otherwise false
     */
    public boolean isStatic(Method method) {
        return Modifier.isStatic(method.getModifiers());
    }

    /**
     * Method to determine if a Method is public
     *
     * @param method method
     * @return true if the Method is public, otherwise false
     */
    public boolean isPublic(Method method) {
        return Modifier.isPublic(method.getModifiers());
    }

    /**
     * Method to determine if a Method is protected
     *
     * @param method method
     * @return true if the Method is protected, otherwise false
     */
    public boolean isProtected(Method method) {
        return Modifier.isProtected(method.getModifiers());
    }

    /**
     * Method to determine if a Method has a specified parameter count
     *
     * @param method method
     * @param parameterCount parameterCount
     * @return true if the Method has the specified parameter count, otherwise false
     */
    public boolean hasParameterCount(Method method, int parameterCount) {
        return method.getParameterCount() == parameterCount;
    }

    /**
     * Method to determine if a Method accepts a specified set of parameters
     *
     * @param method method
     * @param parameterTypes parameterTypes
     * @return true if the Method accepts the specified parameters, otherwise false
     */
    public boolean acceptsArguments(Method method, Class<?>... parameterTypes) {
        Class<?>[] methodParameterTypes = method.getParameterTypes();
        if (methodParameterTypes.length != parameterTypes.length) {
            return false;
        }

        for (int i = 0; i < parameterTypes.length; i++) {
            if (!parameterTypes[i].isAssignableFrom(methodParameterTypes[i])) {
                return false;
            }
        }

        return true;
    }

    /**
     * Method to determine if a Method returns a specific type
     *
     * @param method method
     * @param clazz clazz
     * @return true if the Method returns a specific type, otherwise false
     */
    public boolean hasReturnType(Method method, Class<?> clazz) {
        if (clazz == Void.class) {
            return method.getReturnType().getName().equals("void");
        }

        return clazz.isAssignableFrom(method.getReturnType());
    }

    /**
     * Method to get a class hierarchy bottom up
     *
     * @param clazz clazz to inspect
     * @return a list representing the class hierarchy based on order
     */
    private static List<Class<?>> buildClassHierarchy(Class<?> clazz) {
        Set<Class<?>> classSet = new LinkedHashSet<>();
        resolveClasses(clazz, classSet);
        return new ArrayList<>(classSet);
    }

    /**
     * Method to recursively resolve a Class hierarchy top down
     *
     * @param clazz clazz
     * @param classSet classSet
     */
    private static void resolveClasses(Class<?> clazz, Set<Class<?>> classSet) {
        if (clazz == Object.class) {
            return;
        }
        classSet.add(clazz);
        Class<?> superClass = clazz.getSuperclass();
        resolveClasses(superClass, classSet);
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}
