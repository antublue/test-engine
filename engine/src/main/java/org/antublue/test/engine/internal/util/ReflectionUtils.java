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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/** Class to implement ReflectionUtils */
@SuppressWarnings({"PMD.AvoidAccessibilityAlteration", "PMD.EmptyCatchBlock"})
public final class ReflectionUtils {

    public static final Class<?>[] NO_CLASS_ARGS = null;

    public static final Object[] NO_OBJECT_ARGS = null;

    private ReflectionUtils() {
        // DO NOTHING
    }

    /**
     * Method to create a new instance of a Class
     *
     * @param clazz clazz
     * @return object object
     * @throws Throwable Throwable
     */
    public static Object newInstance(Class<?> clazz) throws Throwable {
        Constructor<?> constructor = clazz.getDeclaredConstructor(NO_CLASS_ARGS);
        return constructor.newInstance(NO_OBJECT_ARGS);
    }

    /**
     * Method to determine if a Class is abstract
     *
     * @param clazz clazz
     * @return true if the Class is abstract, otherwise false
     */
    public static boolean isAbstract(Class<?> clazz) {
        return Modifier.isAbstract(clazz.getModifiers());
    }

    /**
     * Method to determine if a Method is abstract
     *
     * @param method method
     * @return true if the Method is abstract, otherwise false
     */
    public static boolean isAbstract(Method method) {
        return Modifier.isAbstract(method.getModifiers());
    }

    /**
     * Method to determine if a Method is static
     *
     * @param method method
     * @return true if the Method is static, otherwise false
     */
    public static boolean isStatic(Method method) {
        return Modifier.isStatic(method.getModifiers());
    }

    /**
     * Method to determine if a Method is public
     *
     * @param method method
     * @return true if the Method is public, otherwise false
     */
    public static boolean isPublic(Method method) {
        return Modifier.isPublic(method.getModifiers());
    }

    /**
     * Method to determine if a Method is protected
     *
     * @param method method
     * @return true if the Method is protected, otherwise false
     */
    public static boolean isProtected(Method method) {
        return Modifier.isProtected(method.getModifiers());
    }

    /**
     * Method to determine if a Method has a specified parameter count
     *
     * @param method method
     * @param parameterCount parameterCount
     * @return true if the Method has the specified parameter count, otherwise false
     */
    public static boolean hasParameterCount(Method method, int parameterCount) {
        return method.getParameterCount() == parameterCount;
    }

    /**
     * Method to determine if a Method accepts a specified set of parameters
     *
     * @param method method
     * @param parameterTypes parameterTypes
     * @return true if the Method accepts the specified parameters, otherwise false
     */
    public static boolean acceptsArguments(Method method, Class<?>... parameterTypes) {
        Class<?>[] methodParameterTypes = method.getParameterTypes();

        if (parameterTypes == null || methodParameterTypes.length != parameterTypes.length) {
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
    public static boolean hasReturnType(Method method, Class<?> clazz) {
        if (clazz == Void.class) {
            return method.getReturnType().getName().equals("void");
        }

        return clazz.isAssignableFrom(method.getReturnType());
    }
}
