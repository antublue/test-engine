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

package org.antublue.test.engine.test.standard;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Predicate;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.util.ReflectionUtils;

public class StandardTestFilters {

    private static final ReflectionUtils REFLECTION_UTILS = ReflectionUtils.getSingleton();

    public static final Predicate<Field> RANDOM_FIELD =
            field -> {
                if (field.isAnnotationPresent(TestEngine.Random.Boolean.class)
                        || field.isAnnotationPresent(TestEngine.Random.Integer.class)
                        || field.isAnnotationPresent(TestEngine.Random.Long.class)
                        || field.isAnnotationPresent(TestEngine.Random.Float.class)
                        || field.isAnnotationPresent(TestEngine.Random.Double.class)
                        || field.isAnnotationPresent(TestEngine.Random.BigInteger.class)
                        || field.isAnnotationPresent(TestEngine.Random.BigDecimal.class)
                        || field.isAnnotationPresent(TestEngine.Random.UUID.class)) {
                    field.setAccessible(true);
                    return true;
                }
                return false;
            };

    public static final Predicate<Field> AUTO_CLOSE_FIELDS =
            field ->
                    field.isAnnotationPresent(TestEngine.AutoClose.AfterEach.class)
                            || field.isAnnotationPresent(TestEngine.AutoClose.AfterAll.class)
                            || field.isAnnotationPresent(TestEngine.AutoClose.Conclude.class);

    public static final Predicate<Class<?>> TEST_CLASS =
            clazz -> {
                boolean isSimpleTestClass =
                        !clazz.isAnnotationPresent(TestEngine.BaseClass.class)
                                && !clazz.isAnnotationPresent(TestEngine.Disabled.class)
                                && !Modifier.isAbstract(clazz.getModifiers());

                if (isSimpleTestClass) {
                    List<Method> methods = REFLECTION_UTILS.findMethods(clazz);

                    int testCount = 0;
                    int argumentSupplierCount = 0;

                    for (Method method : methods) {
                        if (method.isAnnotationPresent(TestEngine.Test.class)
                                && !REFLECTION_UTILS.isStatic(method)
                                && (REFLECTION_UTILS.isProtected(method)
                                        || REFLECTION_UTILS.isPublic(method))
                                && !REFLECTION_UTILS.isAbstract(method)) {
                            testCount++;
                        } else if (method.isAnnotationPresent(TestEngine.ArgumentSupplier.class)
                                && REFLECTION_UTILS.isStatic(method)
                                && (REFLECTION_UTILS.isProtected(method)
                                        || REFLECTION_UTILS.isPublic(method))
                                && !REFLECTION_UTILS.isAbstract(method)) {
                            argumentSupplierCount++;
                        }
                    }

                    isSimpleTestClass = testCount > 0 && argumentSupplierCount == 0;
                }

                return isSimpleTestClass;
            };

    public static final Predicate<Method> PREPARE_METHOD =
            method -> {
                if (!method.isAnnotationPresent(TestEngine.Prepare.class)) {
                    return false;
                }
                if (method.isAnnotationPresent(TestEngine.Disabled.class)) {
                    return false;
                }

                if (REFLECTION_UTILS.isStatic(method)) {
                    return false;
                }
                if (!(REFLECTION_UTILS.isPublic(method) || REFLECTION_UTILS.isProtected(method))) {
                    return false;
                }
                if (!REFLECTION_UTILS.hasParameterCount(method, 0)) {
                    return false;
                }
                if (!REFLECTION_UTILS.hasReturnType(method, Void.class)) {
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

                if (REFLECTION_UTILS.isStatic(method)) {
                    return false;
                }

                if (!(REFLECTION_UTILS.isPublic(method) || REFLECTION_UTILS.isProtected(method))) {
                    return false;
                }

                if (!REFLECTION_UTILS.hasParameterCount(method, 0)) {
                    return false;
                }

                if (!REFLECTION_UTILS.hasReturnType(method, Void.class)) {
                    return false;
                }

                method.setAccessible(true);

                return true;
            };

    public static final Predicate<Method> TEST_METHOD =
            method -> {
                if (!method.isAnnotationPresent(TestEngine.Test.class)) {
                    return false;
                }
                if (method.isAnnotationPresent(TestEngine.Disabled.class)) {
                    return false;
                }
                if (REFLECTION_UTILS.isStatic(method)) {
                    return false;
                }
                if (!(REFLECTION_UTILS.isPublic(method) || REFLECTION_UTILS.isProtected(method))) {
                    return false;
                }
                if (!REFLECTION_UTILS.hasParameterCount(method, 0)) {
                    return false;
                }
                if (!REFLECTION_UTILS.hasReturnType(method, Void.class)) {
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

                if (REFLECTION_UTILS.isStatic(method)) {
                    return false;
                }
                if (!(REFLECTION_UTILS.isPublic(method) || REFLECTION_UTILS.isProtected(method))) {
                    return false;
                }
                if (!REFLECTION_UTILS.hasParameterCount(method, 0)) {
                    return false;
                }
                if (!REFLECTION_UTILS.hasReturnType(method, Void.class)) {
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

                if (REFLECTION_UTILS.isStatic(method)) {
                    return false;
                }
                if (!(REFLECTION_UTILS.isPublic(method) || REFLECTION_UTILS.isProtected(method))) {
                    return false;
                }
                if (!REFLECTION_UTILS.hasParameterCount(method, 0)) {
                    return false;
                }
                if (!REFLECTION_UTILS.hasReturnType(method, Void.class)) {
                    return false;
                }

                method.setAccessible(true);

                return true;
            };

    private StandardTestFilters() {
        // DO NOTHING
    }
}
