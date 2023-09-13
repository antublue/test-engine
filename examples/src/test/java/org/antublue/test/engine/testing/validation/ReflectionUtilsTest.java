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

package org.antublue.test.engine.testing.validation;

import static org.assertj.core.api.Assertions.assertThat;

import example.inheritance.ConcreteEvenTest;
import example.inheritance.ConcreteOddTest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.utils.ReflectionUtils;
import org.antublue.test.engine.internal.test.descriptor.parameterized.ParameterizedTestFilters;
import org.antublue.test.engine.internal.test.util.TestUtils;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;

public class ReflectionUtilsTest {

    @TestEngine.Test
    public void test1() throws Throwable {
        Class<?> testClass = ExtendedTest.class;
        Class<? extends Annotation> testAnnotationClass = TestEngine.Test.class;
        List<Method> reference =
                ReflectionSupport.findMethods(
                        testClass,
                        method -> method.isAnnotationPresent(testAnnotationClass),
                        HierarchyTraversalMode.TOP_DOWN);
        List<Method> actual =
                ReflectionUtils.getSingleton()
                        .findMethods(
                                testClass,
                                method -> method.isAnnotationPresent(testAnnotationClass));
        TestUtils.getSingleton().orderTestMethods(actual);

        assertThat(actual).isEqualTo(reference);
    }

    @TestEngine.Test
    public void test2() {
        List<Method> methods =
                ReflectionUtils.getSingleton()
                        .findMethods(
                                ConcreteEvenTest.class,
                                ParameterizedTestFilters.ARGUMENT_SUPPLIER_METHOD);

        assertThat(methods).isNotNull();
        assertThat(methods).isNotEmpty();
        assertThat(methods.size()).isEqualTo(1);
    }

    @TestEngine.Test
    public void test3() {
        List<Method> methods =
                ReflectionUtils.getSingleton()
                        .findMethods(
                                ConcreteOddTest.class,
                                ParameterizedTestFilters.ARGUMENT_SUPPLIER_METHOD);

        assertThat(methods).isNotNull();
        assertThat(methods).isNotEmpty();
        assertThat(methods.size()).isEqualTo(1);
    }
}
