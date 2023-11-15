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

package org.antublue.test.engine.internal.test.descriptor.standard;

import java.lang.reflect.Method;
import java.util.function.Predicate;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.test.descriptor.filter.AnnotationMethodFilter;
import org.antublue.test.engine.internal.test.util.ReflectionUtils;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;

public class StandardTestPredicates {

    public static final Predicate<Class<?>> TEST_CLASS = new TestClassFilter();

    public static final Predicate<Method> TEST_METHOD = new TestMethodFilter();

    private static class TestClassFilter implements Predicate<Class<?>> {

        @Override
        public boolean test(Class<?> clazz) {
            return !ReflectionUtils.isAbstract(clazz)
                    && !clazz.isAnnotationPresent(TestEngine.Disabled.class)
                    && !ReflectionSupport.findMethods(
                                    clazz,
                                    AnnotationMethodFilter.of(TestEngine.Test.class),
                                    HierarchyTraversalMode.TOP_DOWN)
                            .isEmpty()
                    && ReflectionSupport.findMethods(
                                    clazz,
                                    AnnotationMethodFilter.of(TestEngine.Supplier.Argument.class),
                                    HierarchyTraversalMode.TOP_DOWN)
                            .isEmpty();
        }
    }

    private static class TestMethodFilter implements Predicate<Method> {

        @Override
        public boolean test(Method method) {
            return !ReflectionUtils.isAbstract(method)
                    && !method.isAnnotationPresent(TestEngine.Disabled.class)
                    && method.isAnnotationPresent(TestEngine.Test.class)
                    && method.getParameterTypes().length == 0;
        }
    }
}
