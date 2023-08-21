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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
    public void getClassHierarchy() {
        List<Class<?>> classHierarchy =
                ReflectionUtils.singleton()
                        .getClassHierarchy(SubClass4.class, ReflectionUtils.Order.SUB_CLASS_FIRST);

        for (Class<?> clazz : classHierarchy) {
            System.out.format("%s", clazz.getName()).println();
        }
    }

    @TestEngine.Test
    public void getClassHierarchySuperClassFirst() {
        List<Class<?>> classHierarchy =
                ReflectionUtils.singleton()
                        .getClassHierarchy(
                                SubClass4.class, ReflectionUtils.Order.SUPER_CLASS_FIRST);

        for (Class<?> clazz : classHierarchy) {
            System.out.format("%s", clazz.getName()).println();
        }
    }

    @TestEngine.Test
    public void getMethodsSuperClassFirst() {
        List<Method> methods =
                ReflectionUtils.singleton()
                        .getMethods(SubClass4.class, ReflectionUtils.Order.SUPER_CLASS_FIRST);

        for (Method method : methods) {
            System.out.format("%s", method.getName()).println();
        }
    }

    @TestEngine.Test
    public void getMethodsSubClassFirst() {
        List<Method> methods =
                ReflectionUtils.singleton()
                        .getMethods(SubClass4.class, ReflectionUtils.Order.SUB_CLASS_FIRST);

        for (Method method : methods) {
            System.out.format("%s", method.getName()).println();
        }
    }

    @TestEngine.Test
    public void getFieldsSuperClassFirst() {
        List<Field> fields =
                ReflectionUtils.singleton()
                        .getFields(SubClass4.class, ReflectionUtils.Order.SUPER_CLASS_FIRST);

        for (Field field : fields) {
            System.out.format("%s", field.getName()).println();
        }
    }

    @TestEngine.Test
    public void getFieldsSubClassFirst() {
        List<Field> fields =
                ReflectionUtils.singleton()
                        .getFields(SubClass4.class, ReflectionUtils.Order.SUB_CLASS_FIRST);

        for (Field field : fields) {
            System.out.format("%s", field.getName()).println();
        }
    }
}
