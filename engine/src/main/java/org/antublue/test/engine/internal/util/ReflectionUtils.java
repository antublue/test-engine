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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.junit.platform.commons.support.ReflectionSupport;

/** Class to implement ReflectionUtils */
@SuppressWarnings({"PMD.AvoidAccessibilityAlteration", "PMD.EmptyCatchBlock"})
public final class ReflectionUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionUtils.class);

    private static final ReflectionUtils SINGLETON = new ReflectionUtils();

    public enum Order {
        SUPER_CLASS_FIRST,
        SUB_CLASS_FIRST
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
     * @param order order
     * @return a List of Classes
     */
    public List<Class<?>> getClassHierarchy(Class<?> clazz, Order order) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("getClassHierarchy class [%s] order [%s]", clazz.getName(), order);
        }

        Set<Class<?>> classSet = new LinkedHashSet<>();
        resolveClasses(clazz, order, classSet);

        return new ArrayList<>(classSet);
    }

    private void resolveClasses(Class<?> clazz, Order order, Set<Class<?>> classSet) {
        if (clazz == Object.class) {
            return;
        }

        switch (order) {
            case SUPER_CLASS_FIRST:
                {
                    Class<?> superClass = clazz.getSuperclass();
                    resolveClasses(superClass, order, classSet);
                    classSet.add(clazz);
                }
            case SUB_CLASS_FIRST:
                {
                    classSet.add(clazz);
                    Class<?> superClass = clazz.getSuperclass();
                    resolveClasses(superClass, order, classSet);
                }
        }
    }

    /**
     * Method to get a List of all fields from a Class and super Classes
     *
     * @param clazz class to inspect
     * @return list of Fields
     */
    public List<Field> getFields(Class<?> clazz, Order order) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("getFields class [%s] ", clazz.getName());
        }

        Set<Field> fieldSet = new LinkedHashSet<>();
        resolveFields(clazz, order, fieldSet);
        List<Field> fields = new ArrayList<>(fieldSet);

        LOGGER.trace(" class [%s] field count [%d]", clazz.getName(), fields.size());

        return fields;
    }

    /**
     * Method to recursively resolve Fields for a Class and super Classes
     *
     * @param clazz class to inspect
     * @param fieldSet set of Fields
     */
    private void resolveFields(Class<?> clazz, Order order, Set<Field> fieldSet) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("resolveFields class [%s]", clazz.getName());
        }

        if (clazz == Object.class) {
            return;
        }

        switch (order) {
            case SUPER_CLASS_FIRST:
                {
                    Class<?> superClass = clazz.getSuperclass();
                    resolveFields(superClass, order, fieldSet);
                    Field[] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        if (fieldSet.add(field)) {
                            field.setAccessible(true);
                        }
                    }
                    break;
                }
            case SUB_CLASS_FIRST:
                {
                    Field[] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        if (fieldSet.add(field)) {
                            field.setAccessible(true);
                        }
                    }
                    Class<?> superClass = clazz.getSuperclass();
                    resolveFields(superClass, order, fieldSet);
                }
        }
    }

    /**
     * Method to get a List of all methods
     *
     * @param clazz class to inspect
     * @return list of Methods
     */
    public List<Method> getMethods(Class<?> clazz, Order order) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("getMethods class [%s] method order [%s]", clazz.getName(), order);
        }

        Map<String, Method> methodMap = new LinkedHashMap<>();
        resolveMethods(clazz, order, methodMap);

        if (LOGGER.isTraceEnabled()) {
            synchronized (LOGGER) {
                LOGGER.trace(" class [%s] method count [%d]", clazz.getName(), methodMap.size());

                for (Method method : methodMap.values()) {
                    LOGGER.trace(
                            " class [%s] method [%s %s]",
                            clazz.getName(),
                            method.getDeclaringClass().getName(),
                            method.getName());
                }
            }
        }

        return new ArrayList<>(methodMap.values());
    }

    private void resolveMethods(Class<?> clazz, Order order, Map<String, Method> methodMap) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("resolveMethods class [%s] method order [%s]", clazz.getName(), order);
        }

        if (clazz == Object.class) {
            return;
        }

        try {
            switch (order) {
                case SUPER_CLASS_FIRST:
                    {
                        Class<?> superClass = clazz.getSuperclass();
                        resolveMethods(superClass, order, methodMap);
                        for (Method method : clazz.getDeclaredMethods()) {
                            methodMap.putIfAbsent(method.getName(), method);
                        }
                        break;
                    }
                case SUB_CLASS_FIRST:
                    {
                        for (Method method : clazz.getDeclaredMethods()) {
                            methodMap.putIfAbsent(method.getName(), method);
                        }
                        Class<?> superClass = clazz.getSuperclass();
                        resolveMethods(superClass, order, methodMap);
                        break;
                    }
            }
        } catch (NoClassDefFoundError e) {
            // DO NOTHING
        }
    }

    public boolean isStatic(Method method) {
        return Modifier.isStatic(method.getModifiers());
    }

    public boolean isNotStatic(Method method) {
        return !isStatic(method);
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
        if (parameterTypes.length != methodParameterTypes.length) {
            return false;
        }

        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i].isAssignableFrom(methodParameterTypes[i])) {
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
