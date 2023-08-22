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

package org.antublue.test.engine.testing;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.StringArgument;
import org.antublue.test.engine.internal.util.ReflectionUtils;

/** Example test */
public class ReflectionUtilsTest {

    @TestEngine.Argument protected StringArgument stringArgument;

    // The stringArgument provides a node in the hierarchy, but is not actually used in test methods
    @TestEngine.ArgumentSupplier
    protected static Stream<StringArgument> arguments() {
        return Stream.of(StringArgument.of("----"));
    }

    @TestEngine.Test
    public void getClassHierarchyTopDown() {
        List<Class<?>> expectedList = new ArrayList<>();
        expectedList.add(SubClass4.class);
        expectedList.add(SubClass3.class);
        expectedList.add(SubClass2.class);
        expectedList.add(SubClass1.class);

        List<Class<?>> classHierarchy =
                ReflectionUtils.singleton()
                        .getClassHierarchy(
                                SubClass4.class, ReflectionUtils.HierarchyTraversalOrder.TOP_DOWN);

        assertThat(classHierarchy).isEqualTo(expectedList);
    }

    @TestEngine.Test
    public void getClassHierarchyBottomUp() {
        List<Class<?>> expectedList = new ArrayList<>();
        expectedList.add(SubClass1.class);
        expectedList.add(SubClass2.class);
        expectedList.add(SubClass3.class);
        expectedList.add(SubClass4.class);

        List<Class<?>> classHierarchy =
                ReflectionUtils.singleton()
                        .getClassHierarchy(
                                SubClass4.class, ReflectionUtils.HierarchyTraversalOrder.BOTTOM_UP);

        assertThat(classHierarchy).isEqualTo(expectedList);
    }

    @TestEngine.Test
    public void getMethodsTopDown() {
        List<String> expectedList = new ArrayList<>();
        expectedList.add("subClass4Method");
        expectedList.add("subClass3Method");
        expectedList.add("subClass2Method");
        expectedList.add("subClass1Method");

        List<Method> methods =
                ReflectionUtils.singleton()
                        .getMethods(
                                SubClass4.class, ReflectionUtils.HierarchyTraversalOrder.TOP_DOWN);

        assertThat(methods.size()).isEqualTo(expectedList.size());

        for (int i = 0; i < expectedList.size(); i++) {
            assertThat(methods.get(i).getName()).isEqualTo(expectedList.get(i));
        }
    }

    @TestEngine.Test
    public void getMethodsBottomUp() {
        List<String> expectedList = new ArrayList<>();
        expectedList.add("subClass1Method");
        expectedList.add("subClass2Method");
        expectedList.add("subClass3Method");
        expectedList.add("subClass4Method");

        List<Method> methods =
                ReflectionUtils.singleton()
                        .getMethods(
                                SubClass4.class, ReflectionUtils.HierarchyTraversalOrder.BOTTOM_UP);

        assertThat(methods.size()).isEqualTo(expectedList.size());

        for (int i = 0; i < expectedList.size(); i++) {
            assertThat(methods.get(i).getName()).isEqualTo(expectedList.get(i));
        }
    }

    @TestEngine.Test
    public void getFieldsTopDown() {
        List<String> expectedList = new ArrayList<>();
        expectedList.add("subClass4Field");
        expectedList.add("subClass3Field");
        expectedList.add("subClass2Field");
        expectedList.add("subClass1Field");

        List<Field> fields =
                ReflectionUtils.singleton()
                        .getFields(
                                SubClass4.class, ReflectionUtils.HierarchyTraversalOrder.TOP_DOWN);

        assertThat(fields.size()).isEqualTo(expectedList.size());

        for (int i = 0; i < expectedList.size(); i++) {
            assertThat(fields.get(i).getName()).isEqualTo(expectedList.get(i));
        }
    }

    @TestEngine.Test
    public void getFieldsBottomUp() {
        List<String> expectedList = new ArrayList<>();
        expectedList.add("subClass1Field");
        expectedList.add("subClass2Field");
        expectedList.add("subClass3Field");
        expectedList.add("subClass4Field");

        List<Field> fields =
                ReflectionUtils.singleton()
                        .getFields(
                                SubClass4.class, ReflectionUtils.HierarchyTraversalOrder.BOTTOM_UP);

        assertThat(fields.size()).isEqualTo(expectedList.size());

        for (int i = 0; i < expectedList.size(); i++) {
            assertThat(fields.get(i).getName()).isEqualTo(expectedList.get(i));
        }
    }
}
