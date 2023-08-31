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

package org.antublue.test.engine;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.util.ReflectionUtils;

public class Filter {

    private static final ReflectionUtils reflectionUtils = ReflectionUtils.singleton();

    public static final Predicate<Field> ARGUMENT_FIELD =
            field -> {
                if (!field.isAnnotationPresent(TestEngine.Argument.class)) {
                    return false;
                }

                return Argument.class.isAssignableFrom(field.getType());
            };

    public static final Predicate<Method> ARGUMENT_SUPPLIER_METHOD =
            method -> {
                if (!method.isAnnotationPresent(TestEngine.ArgumentSupplier.class)) {
                    return false;
                }

                if (!reflectionUtils.isStatic(method)) {
                    return false;
                }

                if (!(reflectionUtils.isPublic(method) || reflectionUtils.isProtected(method))) {
                    return false;
                }

                if (!reflectionUtils.hasParameterCount(method, 0)) {
                    return false;
                }

                if (!(reflectionUtils.hasReturnType(method, Iterable.class)
                        || reflectionUtils.hasReturnType(method, Stream.class))) {
                    return false;
                }

                return true;
            };

    public static final Predicate<Method> EXTENSION_SUPPLIER_METHOD =
            method -> {
                if (!method.isAnnotationPresent(TestEngine.ExtensionSupplier.class)) {
                    return false;
                }

                if (!reflectionUtils.isStatic(method)) {
                    return false;
                }

                if (!(reflectionUtils.isPublic(method) || reflectionUtils.isProtected(method))) {
                    return false;
                }

                if (!reflectionUtils.hasParameterCount(method, 0)) {
                    return false;
                }

                if (!(reflectionUtils.hasReturnType(method, Iterable.class)
                        || reflectionUtils.hasReturnType(method, Stream.class))) {
                    return false;
                }

                return true;
            };

    public static final Predicate<Field> ANNOTATED_FIELD =
            field ->
                    field.isAnnotationPresent(TestEngine.Argument.class)
                            || field.isAnnotationPresent(TestEngine.AutoClose.class)
                            || field.isAnnotationPresent(TestEngine.RandomBoolean.class)
                            || field.isAnnotationPresent(TestEngine.RandomInteger.class)
                            || field.isAnnotationPresent(TestEngine.RandomLong.class)
                            || field.isAnnotationPresent(TestEngine.RandomFloat.class)
                            || field.isAnnotationPresent(TestEngine.RandomDouble.class)
                            || field.isAnnotationPresent(TestEngine.RandomBigInteger.class)
                            || field.isAnnotationPresent(TestEngine.RandomBigDecimal.class)
                            || field.isAnnotationPresent(TestEngine.UUID.class);

    public static final Predicate<Class<?>> TEST_CLASS =
            clazz -> {
                boolean isTestClass =
                        !clazz.isAnnotationPresent(TestEngine.BaseClass.class)
                                && !clazz.isAnnotationPresent(TestEngine.Disabled.class)
                                && !Modifier.isAbstract(clazz.getModifiers());

                if (isTestClass) {
                    List<Method> methods =
                            ReflectionUtils.singleton()
                                    .findMethods(clazz)
                                    .collect(Collectors.toList());

                    boolean hasTest = false;
                    boolean hasArgumentSupplier = false;

                    for (Method method : methods) {
                        if (method.isAnnotationPresent(TestEngine.Test.class)
                                && !reflectionUtils.isStatic(method)
                                && (reflectionUtils.isProtected(method)
                                        || reflectionUtils.isPublic(method))
                                && !reflectionUtils.isAbstract(method)) {
                            hasTest = true;
                        } else if (method.isAnnotationPresent(TestEngine.ArgumentSupplier.class)
                                && reflectionUtils.isStatic(method)
                                && (reflectionUtils.isProtected(method)
                                        || reflectionUtils.isPublic(method))
                                && !reflectionUtils.isAbstract(method)) {
                            hasArgumentSupplier = true;
                        }
                    }

                    isTestClass = hasTest && hasArgumentSupplier;
                }

                return isTestClass;
            };

    public static final Predicate<Method> PREPARE_METHOD =
            method -> {
                if (!method.isAnnotationPresent(TestEngine.Prepare.class)) {
                    return false;
                }
                if (method.isAnnotationPresent(TestEngine.Disabled.class)) {
                    return false;
                }

                if (reflectionUtils.isStatic(method)) {
                    return false;
                }
                if (!(reflectionUtils.isPublic(method) || reflectionUtils.isProtected(method))) {
                    return false;
                }
                if (!(reflectionUtils.hasParameterCount(method, 0)
                        || reflectionUtils.acceptsParameters(method, Argument.class))) {
                    return false;
                }
                if (!reflectionUtils.hasReturnType(method, Void.class)) {
                    return false;
                }
                method.setAccessible(true);
                return true;
            };

    public static final Predicate<Method> BEFORE_ALL_METHOD =
            method -> {
                if (!method.isAnnotationPresent(TestEngine.BeforeAll.class)) {
                    return false;
                }
                if (method.isAnnotationPresent(TestEngine.Disabled.class)) {
                    return false;
                }

                if (reflectionUtils.isStatic(method)) {
                    return false;
                }
                if (!(reflectionUtils.isPublic(method) || reflectionUtils.isProtected(method))) {
                    return false;
                }
                if (!(reflectionUtils.hasParameterCount(method, 0)
                        || reflectionUtils.acceptsParameters(method, Argument.class))) {
                    return false;
                }
                if (!reflectionUtils.hasReturnType(method, Void.class)) {
                    return false;
                }
                method.setAccessible(true);
                return true;
            };

    public static final Predicate<Method> BEFORE_EACH_METHOD =
            method -> {
                if (!method.isAnnotationPresent(TestEngine.BeforeEach.class)) {
                    return false;
                }

                if (method.isAnnotationPresent(TestEngine.Disabled.class)) {
                    return false;
                }

                if (reflectionUtils.isStatic(method)) {
                    return false;
                }

                if (!(reflectionUtils.isPublic(method) || reflectionUtils.isProtected(method))) {
                    return false;
                }

                if (!(reflectionUtils.hasParameterCount(method, 0)
                        || reflectionUtils.acceptsParameters(method, Argument.class))) {
                    return false;
                }

                if (!reflectionUtils.hasReturnType(method, Void.class)) {
                    return false;
                }

                method.setAccessible(true);

                return true;
            };

    public static final Predicate<Method> TEST_METHODS =
            method -> {
                if (!method.isAnnotationPresent(TestEngine.Test.class)) {
                    return false;
                }
                if (method.isAnnotationPresent(TestEngine.Disabled.class)) {
                    return false;
                }
                if (reflectionUtils.isStatic(method)) {
                    return false;
                }
                if (!(reflectionUtils.isPublic(method) || reflectionUtils.isProtected(method))) {
                    return false;
                }
                if (!(reflectionUtils.hasParameterCount(method, 0)
                        || reflectionUtils.acceptsParameters(method, Argument.class))) {
                    return false;
                }
                if (!reflectionUtils.hasReturnType(method, Void.class)) {
                    return false;
                }

                method.setAccessible(true);

                return true;
            };

    public static final Predicate<Method> AFTER_EACH_METHOD =
            method -> {
                if (!method.isAnnotationPresent(TestEngine.AfterEach.class)) {
                    return false;
                }
                if (method.isAnnotationPresent(TestEngine.Disabled.class)) {
                    return false;
                }

                if (reflectionUtils.isStatic(method)) {
                    return false;
                }
                if (!(reflectionUtils.isPublic(method) || reflectionUtils.isProtected(method))) {
                    return false;
                }
                if (!(reflectionUtils.hasParameterCount(method, 0)
                        || reflectionUtils.acceptsParameters(method, Argument.class))) {
                    return false;
                }
                if (!reflectionUtils.hasReturnType(method, Void.class)) {
                    return false;
                }

                method.setAccessible(true);

                return true;
            };

    public static final Predicate<Method> AFTER_ALL_METHOD =
            method -> {
                if (!method.isAnnotationPresent(TestEngine.AfterAll.class)) {
                    return false;
                }
                if (method.isAnnotationPresent(TestEngine.Disabled.class)) {
                    return false;
                }

                if (reflectionUtils.isStatic(method)) {
                    return false;
                }
                if (!(reflectionUtils.isPublic(method) || reflectionUtils.isProtected(method))) {
                    return false;
                }
                if (!(reflectionUtils.hasParameterCount(method, 0)
                        || reflectionUtils.acceptsParameters(method, Argument.class))) {
                    return false;
                }
                if (!reflectionUtils.hasReturnType(method, Void.class)) {
                    return false;
                }

                method.setAccessible(true);

                return true;
            };

    public static final Predicate<Method> CONCLUDE_METHOD =
            method -> {
                if (!method.isAnnotationPresent(TestEngine.Conclude.class)) {
                    return false;
                }
                if (method.isAnnotationPresent(TestEngine.Disabled.class)) {
                    return false;
                }

                if (reflectionUtils.isStatic(method)) {
                    return false;
                }
                if (!(reflectionUtils.isPublic(method) || reflectionUtils.isProtected(method))) {
                    return false;
                }
                if (!(reflectionUtils.hasParameterCount(method, 0)
                        || reflectionUtils.acceptsParameters(method, Argument.class))) {
                    return false;
                }
                if (!reflectionUtils.hasReturnType(method, Void.class)) {
                    return false;
                }

                method.setAccessible(true);

                return true;
            };

    private Filter() {
        // DO NOTHING
    }
}
