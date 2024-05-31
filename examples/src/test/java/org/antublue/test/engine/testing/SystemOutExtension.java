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

import static java.lang.String.format;

import java.lang.reflect.Method;
import org.antublue.test.engine.api.Extension;
import org.antublue.test.engine.api.Named;
import org.antublue.test.engine.internal.util.StandardStreams;

public class SystemOutExtension implements Extension {

    @Override
    public void postInstantiateCallback(Object testInstance) {
        System.out.println(
                format("postInstantiateCallback(class [%s])", testInstance.getClass().getName()));
    }

    @Override
    public void prePrepareMethodsCallback(Object testInstance) {
        System.out.println(
                format("prePrepareMethodsCallback(class [%s])", testInstance.getClass().getName()));
    }

    @Override
    public void postPrepareMethodsCallback(Object testInstance) {
        System.out.println(
                format("postPrepareCallback(class [%s])", testInstance.getClass().getName()));
    }

    @Override
    public void preBeforeAllMethodsCallback(Object testInstance, Named testArgument) {
        System.out.println(
                format(
                        "preBeforeAllMethodsCallback(class [%s])",
                        testInstance.getClass().getName()));
    }

    @Override
    public void postBeforeAllMethodsCallback(Object testInstance, Named testArgument) {
        System.out.println(
                format("postBeforeAllCallback(class [%s])", testInstance.getClass().getName()));
    }

    @Override
    public void preBeforeEachMethodsCallback(Object testInstance, Named testArgument) {
        System.out.println(
                format(
                        "preBeforeEachMethodsCallback(class [%s])",
                        testInstance.getClass().getName()));
    }

    @Override
    public void postBeforeEachMethodsCallback(Object testInstance, Named testArgument) {
        System.out.println(
                format(
                        "postBeforeEachMethodsCallback(class [%s])",
                        testInstance.getClass().getName()));
    }

    @Override
    public void preTestMethodsCallback(Method method, Object testInstance, Named testArgument) {
        System.out.println(
                format("preTestCallback(class [%s])", testInstance.getClass().getName()));
    }

    @Override
    public void postTestMethodsCallback(Method method, Object testInstance, Named testArgument) {
        System.out.println(
                format("postTestMethodsCallback(class [%s])", testInstance.getClass().getName()));
    }

    @Override
    public void preAfterEachMethodsCallback(Object testInstance, Named testArgument) {
        System.out.println(
                format(
                        "preAfterEachMethodsCallback(class [%s])",
                        testInstance.getClass().getName()));
    }

    @Override
    public void postAfterEachMethodsCallback(Object testInstance, Named testArgument) {
        System.out.println(
                format("postAfterEachCallback(class [%s])", testInstance.getClass().getName()));
    }

    @Override
    public void preAfterAllMethodsCallback(Object testInstance, Named testArgument) {
        System.out.println(
                format(
                        "preAfterAllMethodsCallback(class [%s])",
                        testInstance.getClass().getName()));
    }

    @Override
    public void postAfterAllMethodsCallback(Object testInstance, Named testArgument) {
        System.out.println(
                format("postAfterAllCallback(class [%s])", testInstance.getClass().getName()));
    }

    @Override
    public void preConcludeMethodsCallback(Object testInstance) {
        System.out.println(
                format(
                        "preConcludeMethodsCallback(class [%s])",
                        testInstance.getClass().getName()));
    }

    @Override
    public void postConcludeMethodsCallback(Object testInstance) {
        System.out.println(
                format(
                        "postConcludeMethodsCallback(class [%s])",
                        testInstance.getClass().getName()));
    }

    @Override
    public void preDestroyCallback(Class<?> clazz, Object testInstance) {
        StandardStreams.println(
                "preDestroyCallback() class [%s] instance is present [%s]",
                clazz.getName(), testInstance != null);
    }
}
