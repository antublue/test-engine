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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.antublue.test.engine.api.Key;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.junit.platform.commons.support.ReflectionSupport;

/** Class to implement ReflectionUtils */
@SuppressWarnings({"PMD.AvoidAccessibilityAlteration", "PMD.EmptyCatchBlock"})
public final class ReflectionUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionUtils.class);

    private static final ReflectionUtils SINGLETON = new ReflectionUtils();

    private final Map<String, List<Field>> fieldCache = new HashMap<>();
    private final Map<String, List<Method>> methodCache = new HashMap<>();

    public enum HierarchyTraversalOrder {
        TOP_DOWN,
        BOTTOM_UP
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
     * Method to get a List of classes from Class
     *
     * @param clazz clazz to inspect
     * @param hierarchyTraversalOrder order
     * @return a List of Classes
     */
    public List<Class<?>> getClassHierarchy(
            Class<?> clazz, HierarchyTraversalOrder hierarchyTraversalOrder) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "getClassHierarchy class [%s] order [%s]",
                    clazz.getName(), hierarchyTraversalOrder);
        }

        Set<Class<?>> classSet = new LinkedHashSet<>();
        resolveClasses(clazz, hierarchyTraversalOrder, classSet);

        return new ArrayList<>(classSet);
    }

    private void resolveClasses(
            Class<?> clazz,
            HierarchyTraversalOrder hierarchyTraversalOrder,
            Set<Class<?>> classSet) {
        if (clazz == Object.class) {
            return;
        }

        switch (hierarchyTraversalOrder) {
            case TOP_DOWN:
                {
                    classSet.add(clazz);
                    Class<?> superClass = clazz.getSuperclass();
                    resolveClasses(superClass, hierarchyTraversalOrder, classSet);
                }
            case BOTTOM_UP:
                {
                    Class<?> superClass = clazz.getSuperclass();
                    resolveClasses(superClass, hierarchyTraversalOrder, classSet);
                    classSet.add(clazz);
                }

            default:
                {
                    // DO NOTHING
                }
        }
    }

    /**
     * Method to get a List of all fields from a Class and super Classes
     *
     * @param clazz class to inspect
     * @return list of Fields
     */
    public List<Field> getFields(Class<?> clazz, HierarchyTraversalOrder hierarchyTraversalOrder) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("getFields class [%s] ", clazz.getName());
        }

        List<Field> fields;

        synchronized (fieldCache) {
            String key = Key.of(clazz, "/", hierarchyTraversalOrder);
            fields = fieldCache.get(key);
            if (fields == null) {
                Set<Field> fieldSet = new LinkedHashSet<>();
                resolveFields(clazz, hierarchyTraversalOrder, fieldSet);
                fields = new ArrayList<>(fieldSet);
                fieldCache.put(key, fields);
            }
        }

        LOGGER.trace(" class [%s] field count [%d]", clazz.getName(), fields.size());

        return fields;
    }

    /**
     * Method to recursively resolve Fields for a Class and super Classes
     *
     * @param clazz class to inspect
     * @param fieldSet set of Fields
     */
    private void resolveFields(
            Class<?> clazz, HierarchyTraversalOrder hierarchyTraversalOrder, Set<Field> fieldSet) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("resolveFields class [%s]", clazz.getName());
        }

        if (clazz == Object.class) {
            return;
        }

        switch (hierarchyTraversalOrder) {
            case TOP_DOWN:
                {
                    Field[] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        if (fieldSet.add(field)) {
                            field.setAccessible(true);
                        }
                    }
                    Class<?> superClass = clazz.getSuperclass();
                    resolveFields(superClass, hierarchyTraversalOrder, fieldSet);
                }
            case BOTTOM_UP:
                {
                    Class<?> superClass = clazz.getSuperclass();
                    resolveFields(superClass, hierarchyTraversalOrder, fieldSet);
                    Field[] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        if (fieldSet.add(field)) {
                            field.setAccessible(true);
                        }
                    }
                    break;
                }
            default:
                {
                    // DO NOTHING
                }
        }
    }

    /**
     * Method to get a List of all methods
     *
     * @param clazz class to inspect
     * @return list of Methods
     */
    public List<Method> getMethods(
            Class<?> clazz, HierarchyTraversalOrder hierarchyTraversalOrder) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "getMethods class [%s] method order [%s]",
                    clazz.getName(), hierarchyTraversalOrder);
        }

        List<Method> methods;

        synchronized (methodCache) {
            String key = Key.of(clazz, "/", hierarchyTraversalOrder);
            methods = methodCache.get(key);
            if (methods == null) {
                Map<String, Method> methodMap = new LinkedHashMap<>();
                resolveMethods(clazz, hierarchyTraversalOrder, methodMap);
                methods = new ArrayList<>(methodMap.values());
                methodCache.put(key, methods);
                if (LOGGER.isTraceEnabled()) {
                    synchronized (LOGGER) {
                        LOGGER.trace(
                                " class [%s] method count [%d]", clazz.getName(), methodMap.size());

                        for (Method method : methodMap.values()) {
                            LOGGER.trace(
                                    " class [%s] method [%s %s]",
                                    clazz.getName(),
                                    method.getDeclaringClass().getName(),
                                    method.getName());
                        }
                    }
                }
            }
        }

        return methods;
    }

    private void resolveMethods(
            Class<?> clazz,
            HierarchyTraversalOrder hierarchyTraversalOrder,
            Map<String, Method> methodMap) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "resolveMethods class [%s] method order [%s]",
                    clazz.getName(), hierarchyTraversalOrder);
        }

        if (clazz == Object.class) {
            return;
        }

        try {
            switch (hierarchyTraversalOrder) {
                case TOP_DOWN:
                    {
                        for (Method method : clazz.getDeclaredMethods()) {
                            methodMap.putIfAbsent(method.getName(), method);
                        }
                        Class<?> superClass = clazz.getSuperclass();
                        resolveMethods(superClass, hierarchyTraversalOrder, methodMap);
                        break;
                    }
                case BOTTOM_UP:
                    {
                        Class<?> superClass = clazz.getSuperclass();
                        resolveMethods(superClass, hierarchyTraversalOrder, methodMap);
                        for (Method method : clazz.getDeclaredMethods()) {
                            methodMap.putIfAbsent(method.getName(), method);
                        }
                        break;
                    }
                default:
                    {
                        // DO NOTHING
                    }
            }
        } catch (NoClassDefFoundError e) {
            // DO NOTHING
        }
    }

    public boolean isStatic(Method method) {
        return Modifier.isStatic(method.getModifiers());
    }

    public boolean isPublic(Method method) {
        return Modifier.isPublic(method.getModifiers());
    }

    public boolean isProtected(Method method) {
        return Modifier.isProtected(method.getModifiers());
    }

    public boolean hasParameterCount(Method method, int parameterCount) {
        return method.getParameterCount() == parameterCount;
    }

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

    public boolean hasReturnType(Method method, Class<?> clazz) {
        if (clazz == Void.class) {
            return method.getReturnType().getName().equals("void");
        }

        return clazz.isAssignableFrom(method.getReturnType());
    }
}
