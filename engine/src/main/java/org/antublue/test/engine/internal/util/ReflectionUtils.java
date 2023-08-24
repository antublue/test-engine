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

package org.antublue.test.engine.internal.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.junit.platform.commons.support.ReflectionSupport;

/** Class to implement ReflectionUtils */
@SuppressWarnings({"PMD.AvoidAccessibilityAlteration", "PMD.EmptyCatchBlock"})
public final class ReflectionUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionUtils.class);

    private static final ReflectionUtils SINGLETON = new ReflectionUtils();

    private final Map<Class<?>, List<Field>> fieldCache = new HashMap<>();
    private final Map<ClassOrderKey, List<Method>> methodCache = new HashMap<>();

    /** Enum to represent hierarchy order */
    public enum Order {
        /** superclass first */
        SUPERCLASS_FIRST,
        /** superclass last */
        SUPERCLASS_LAST
    }

    /** Constructor */
    private ReflectionUtils() {
        // DO NOTHING
    }

    /**
     * Method to get the singleton instance
     *
     * @return the singleton instance
     */
    public static ReflectionUtils singleton() {
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

        List<Class<?>> classes =
                org.junit.platform.commons.util.ReflectionUtils.findAllClassesInClasspathRoot(
                        uri, classFilter -> true, classNameFilter -> true);

        return new ArrayList<>(classes);
    }

    /**
     * Method to find all classes with a package name
     *
     * @param packageName packageName
     * @return the return value
     */
    public List<Class<?>> findAllClasses(String packageName) {
        LOGGER.trace("findAllClasses package name [%s]", packageName);

        List<Class<?>> classes =
                ReflectionSupport.findAllClassesInPackage(
                        packageName, classFilter -> true, classNameFilter -> true);

        classes = new ArrayList<>(classes);

        return classes;
    }

    /**
     * Method to get a Class hierarchy
     *
     * @param clazz clazz to inspect
     * @param order order
     * @return a List representing the Class hierarchy based on order
     */
    private List<Class<?>> buildClassHierarchy(Class<?> clazz, Order order) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.info("getClassHierarchy class [%s] order [%s]", clazz.getName(), order);
        }

        Set<Class<?>> classSet = new LinkedHashSet<>();
        resolveClasses(clazz, classSet);

        List<Class<?>> classes = new ArrayList<>(classSet);

        if (order == Order.SUPERCLASS_LAST) {
            Collections.reverse(classes);
        }

        return classes;
    }

    /**
     * Method to recursively resolve a Class hierarchy (superclass first)
     *
     * @param clazz clazz
     * @param classSet classSet
     */
    private void resolveClasses(Class<?> clazz, Set<Class<?>> classSet) {
        if (clazz == Object.class) {
            return;
        }

        Class<?> superClass = clazz.getSuperclass();
        resolveClasses(superClass, classSet);
        classSet.add(clazz);
    }

    /**
     * Method to get a List of all fields (superclass first)
     *
     * @param clazz class to inspect
     * @return list of Fields
     */
    public List<Field> getFields(Class<?> clazz) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("getFields class [%s] ", clazz.getName());
        }

        List<Field> fields;
        Set<Field> uniqueFields = new HashSet<>();

        synchronized (fieldCache) {
            fields = fieldCache.get(clazz);
            if (fields == null) {
                fields = new ArrayList<>();
                List<Class<?>> classes = buildClassHierarchy(clazz, Order.SUPERCLASS_LAST);
                for (Class<?> c : classes) {
                    Field[] declaredFields = c.getDeclaredFields();
                    for (Field field : declaredFields) {
                        if (uniqueFields.contains(field)) {
                            continue;
                        }
                        uniqueFields.add(field);
                        fields.add(field);
                    }
                }
                fieldCache.put(clazz, fields);
            }
        }

        return fields;
    }

    /**
     * Method to get a List of all methods
     *
     * @param clazz class to inspect
     * @param order order to resolve
     * @return list of Methods
     */
    public List<Method> getMethods(Class<?> clazz, Order order) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("getMethods class [%s] order [%s]", clazz.getName(), order);
        }

        List<Method> methods;

        ClassOrderKey classOrderKey = ClassOrderKey.of(clazz, order);
        Set<MethodKey> uniqueMethodKeys = new HashSet<>();

        synchronized (methodCache) {
            methods = methodCache.get(classOrderKey);
            if (methods == null) {
                methods = new ArrayList<>();
                List<Class<?>> classes = buildClassHierarchy(clazz, order);
                for (Class<?> c : classes) {
                    try {
                        Method[] declaredMethods = c.getDeclaredMethods();
                        for (Method method : declaredMethods) {
                            MethodKey methodKey = MethodKey.of(method);
                            if (uniqueMethodKeys.contains(methodKey)) {
                                continue;
                            }
                            uniqueMethodKeys.add(methodKey);
                            methods.add(method);
                        }
                    } catch (NoClassDefFoundError e) {
                        // DO NOTHING
                        //
                        // Occurs when discover finds a class in tests that the test engine can't
                        // resolve
                    }
                }
                methodCache.put(classOrderKey, methods);
            }
        }

        return methods;
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
    public boolean acceptsParameters(Method method, Class<?>... parameterTypes) {
        Class<?>[] methodParameterTypes = method.getParameterTypes();
        if (methodParameterTypes.length != parameterTypes.length) {
            return false;
        }

        for (int i = 0; i < parameterTypes.length; i++) {
            if (methodParameterTypes[i].isAssignableFrom(parameterTypes[i])) {
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

    private static class MethodKey {

        private final boolean protectedOrPublic;
        private final Class<?> returnType;
        private final String name;
        private final Class<?>[] parameterTypes;

        private MethodKey(
                int modifiers, Class<?> returnType, String name, Class<?>[] parameterTypes) {
            this.protectedOrPublic =
                    Modifier.isProtected(modifiers) || Modifier.isPublic(modifiers);
            this.returnType = returnType;
            this.name = name;
            this.parameterTypes = parameterTypes;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            MethodKey methodKey = (MethodKey) object;
            return protectedOrPublic == methodKey.protectedOrPublic
                    && Objects.equals(returnType, methodKey.returnType)
                    && Objects.equals(name, methodKey.name)
                    && Arrays.equals(parameterTypes, methodKey.parameterTypes);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(protectedOrPublic, returnType, name);
            result = 31 * result + Arrays.hashCode(parameterTypes);
            return result;
        }

        @Override
        public String toString() {
            return protectedOrPublic + " / " + returnType + " / " + name + " / " + parameterTypes;
        }

        public static MethodKey of(Method method) {
            return new MethodKey(
                    method.getModifiers(),
                    method.getReturnType(),
                    method.getName(),
                    method.getParameterTypes());
        }
    }

    private static class ClassOrderKey {

        private final Class<?> clazz;
        private final Order order;

        private ClassOrderKey(Class<?> clazz, Order order) {
            this.clazz = clazz;
            this.order = order;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            ClassOrderKey that = (ClassOrderKey) object;
            return Objects.equals(clazz, that.clazz) && order == that.order;
        }

        @Override
        public int hashCode() {
            return Objects.hash(clazz, order);
        }

        public static ClassOrderKey of(Class<?> clazz, Order order) {
            return new ClassOrderKey(clazz, order);
        }
    }
}
