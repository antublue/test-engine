/*
 * Copyright (C) 2024 The AntuBLUE test-engine project authors
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Predicate;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.util.ReflectionUtils;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;

public class Predicates {

    public static final Predicate<Field> CONTEXT_FIELD =
            field -> {
                int modifiers = field.getModifiers();
                return Modifier.isPublic(modifiers)
                        && !Modifier.isAbstract(modifiers)
                        && Modifier.isStatic(modifiers)
                        && !Modifier.isFinal(modifiers)
                        && field.isAnnotationPresent(TestEngine.Context.class);
            };

    public static final Predicate<Field> ARGUMENT_FIELD =
            field -> {
                int modifiers = field.getModifiers();
                return Modifier.isPublic(modifiers)
                        && !Modifier.isAbstract(modifiers)
                        && !Modifier.isStatic(modifiers)
                        && !Modifier.isFinal(modifiers)
                        && field.isAnnotationPresent(TestEngine.Argument.class);
            };

    public static final Predicate<Method> ARGUMENT_SUPPLIER_METHOD =
            method -> {
                int modifiers = method.getModifiers();

                return Modifier.isPublic(modifiers)
                        && Modifier.isStatic(modifiers)
                        // TODO check return type
                        && (method.getParameterCount() == 0)
                        && method.isAnnotationPresent(TestEngine.ArgumentSupplier.class);
            };

    public static final Predicate<Class<?>> TEST_CLASS =
            clazz -> {
                int modifiers = clazz.getModifiers();

                return !Modifier.isAbstract(modifiers)
                        && !clazz.isAnnotationPresent(TestEngine.Disabled.class)
                        && !ReflectionSupport.findMethods(
                                        clazz,
                                        Predicates.TEST_METHOD,
                                        HierarchyTraversalMode.TOP_DOWN)
                                .isEmpty()
                        && !ReflectionSupport.findMethods(
                                        clazz,
                                        Predicates.ARGUMENT_SUPPLIER_METHOD,
                                        HierarchyTraversalMode.TOP_DOWN)
                                .isEmpty();
            };

    public static final Predicate<Method> PREPARE_METHOD =
            method -> {
                int modifiers = method.getModifiers();

                return !ReflectionUtils.isAbstract(method)
                        && Modifier.isPublic(modifiers)
                        && !Modifier.isStatic(modifiers)
                        && (method.getParameterCount() == 0)
                        && !method.isAnnotationPresent(TestEngine.Disabled.class)
                        && method.isAnnotationPresent(TestEngine.Prepare.class);
            };

    public static final Predicate<Method> BEFORE_ALL =
            method -> {
                int modifiers = method.getModifiers();

                return !ReflectionUtils.isAbstract(method)
                        && Modifier.isPublic(modifiers)
                        && !Modifier.isStatic(modifiers)
                        && (method.getParameterCount() == 0)
                        && !method.isAnnotationPresent(TestEngine.Disabled.class)
                        && method.isAnnotationPresent(TestEngine.BeforeAll.class);
            };

    public static final Predicate<Method> BEFORE_EACH_METHOD =
            method -> {
                int modifiers = method.getModifiers();

                return !ReflectionUtils.isAbstract(method)
                        && Modifier.isPublic(modifiers)
                        && !Modifier.isStatic(modifiers)
                        && (method.getParameterCount() == 0)
                        && !method.isAnnotationPresent(TestEngine.Disabled.class)
                        && method.isAnnotationPresent(TestEngine.BeforeEach.class);
            };

    public static final Predicate<Method> TEST_METHOD =
            method -> {
                int modifiers = method.getModifiers();

                return !ReflectionUtils.isAbstract(method)
                        && Modifier.isPublic(modifiers)
                        && !Modifier.isStatic(modifiers)
                        && (method.getParameterCount() == 0)
                        && !method.isAnnotationPresent(TestEngine.Disabled.class)
                        && method.isAnnotationPresent(TestEngine.Test.class);
            };

    public static final Predicate<Method> AFTER_EACH_METHOD =
            method -> {
                int modifiers = method.getModifiers();

                return !ReflectionUtils.isAbstract(method)
                        && Modifier.isPublic(modifiers)
                        && !Modifier.isStatic(modifiers)
                        && (method.getParameterCount() == 0)
                        && !method.isAnnotationPresent(TestEngine.Disabled.class)
                        && method.isAnnotationPresent(TestEngine.AfterEach.class);
            };

    public static final Predicate<Method> AFTER_ALL =
            method -> {
                int modifiers = method.getModifiers();

                return !ReflectionUtils.isAbstract(method)
                        && Modifier.isPublic(modifiers)
                        && !Modifier.isStatic(modifiers)
                        && (method.getParameterCount() == 0)
                        && !method.isAnnotationPresent(TestEngine.Disabled.class)
                        && method.isAnnotationPresent(TestEngine.AfterAll.class);
            };

    public static final Predicate<Method> CONCLUDE_METHOD =
            method -> {
                int modifiers = method.getModifiers();

                return !ReflectionUtils.isAbstract(method)
                        && Modifier.isPublic(modifiers)
                        && !Modifier.isStatic(modifiers)
                        && (method.getParameterCount() == 0)
                        && !method.isAnnotationPresent(TestEngine.Disabled.class)
                        && method.isAnnotationPresent(TestEngine.Conclude.class);
            };
}
