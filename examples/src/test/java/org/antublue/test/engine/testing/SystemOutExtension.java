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

import java.lang.reflect.Method;
import java.util.Optional;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.Extension;
import org.antublue.test.engine.internal.util.StandardStreams;

public class SystemOutExtension implements Extension {

    @Override
    public void postInstantiateCallback(Object testInstance) {
        System.out.println(
                String.format(
                        "postInstantiateCallback(class [%s])", testInstance.getClass().getName()));
    }

    @Override
    public void prePrepareMethodsCallback(Object testInstance) {
        System.out.println(
                String.format(
                        "prePrepareMethodsCallback(class [%s])",
                        testInstance.getClass().getName()));
    }

    @Override
    public void postPrepareMethodsCallback(Object testInstance) {
        System.out.println(
                String.format(
                        "postPrepareCallback(class [%s])", testInstance.getClass().getName()));
    }

    @Override
    public void preBeforeAllMethodsCallback(Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "preBeforeAllMethodsCallback(class [%s])",
                        testInstance.getClass().getName()));
    }

    @Override
    public void postBeforeAllMethodsCallback(Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "postBeforeAllCallback(class [%s])", testInstance.getClass().getName()));
    }

    @Override
    public void preBeforeEachMethodsCallback(Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "preBeforeEachMethodsCallback(class [%s])",
                        testInstance.getClass().getName()));
    }

    @Override
    public void postBeforeEachMethodsCallback(Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "postBeforeEachMethodsCallback(class [%s])",
                        testInstance.getClass().getName()));
    }

    @Override
    public void preTestMethodsCallback(Method method, Object testInstance, Argument testArgument) {
        System.out.println(
                String.format("preTestCallback(class [%s])", testInstance.getClass().getName()));
    }

    @Override
    public void postTestMethodsCallback(Method method, Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "postTestMethodsCallback(class [%s])", testInstance.getClass().getName()));
    }

    @Override
    public void preAfterEachMethodsCallback(Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "preAfterEachMethodsCallback(class [%s])",
                        testInstance.getClass().getName()));
    }

    @Override
    public void postAfterEachMethodsCallback(Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "postAfterEachCallback(class [%s])", testInstance.getClass().getName()));
    }

    @Override
    public void preAfterAllMethodsCallback(Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "preAfterAllMethodsCallback(class [%s])",
                        testInstance.getClass().getName()));
    }

    @Override
    public void postAfterAllMethodsCallback(Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "postAfterAllCallback(class [%s])", testInstance.getClass().getName()));
    }

    @Override
    public void preConcludeMethodsCallback(Object testInstance) {
        System.out.println(
                String.format(
                        "preConcludeMethodsCallback(class [%s])",
                        testInstance.getClass().getName()));
    }

    @Override
    public void postConcludeMethodsCallback(Object testInstance) {
        System.out.println(
                String.format(
                        "postConcludeMethodsCallback(class [%s])",
                        testInstance.getClass().getName()));
    }

    @Override
    public void preDestroyCallback(Class<?> clazz, Optional<Object> optionalInstance) {
        StandardStreams.println(
                "preDestroyCallback() class [%s] instance is present [%s]",
                clazz.getName(), optionalInstance.isPresent());
    }
}
