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

package example.extension;

import static java.lang.String.format;

import java.lang.reflect.Method;
import java.util.Optional;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.Extension;

/** Example Extension */
public class ExampleExtension implements Extension {

    @Override
    public void preInstantiateCallback(Class<?> testClass) {
        System.out.println(
                format(
                        "%s preInstantiateCallback(class [%s])",
                        this.getClass().getSimpleName(), testClass.getName()));
    }

    @Override
    public void postPrepareMethodsCallback(Object testInstance) {
        System.out.println(
                format(
                        "%s postPrepareCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
    }

    @Override
    public void postBeforeAllMethodsCallback(Object testInstance, Argument testArgument) {
        System.out.println(
                format(
                        "%s postBeforeAllCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
    }

    @Override
    public void postBeforeEachMethodsCallback(Object testInstance, Argument testArgument) {
        System.out.println(
                format(
                        "%s postBeforeEachMethodsCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
    }

    @Override
    public void preTestMethodsCallback(Method method, Object testInstance, Argument testArgument) {
        System.out.println(
                format(
                        "%s preTestCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
    }

    @Override
    public void postTestMethodsCallback(Method method, Object testInstance, Argument testArgument) {
        System.out.println(
                format(
                        "%s postTestMethodsCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
    }

    @Override
    public void postAfterEachMethodsCallback(Object testInstance, Argument testArgument) {
        System.out.println(
                format(
                        "%s postAfterEachCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
    }

    @Override
    public void postAfterAllMethodsCallback(Object testInstance, Argument testArgument) {
        System.out.println(
                format(
                        "%s postAfterAllCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
    }

    @Override
    public void postConcludeMethodsCallback(Object testInstance) {
        System.out.println(
                format(
                        "%s postConcludeMethodsCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
    }

    @Override
    public void preDestroyCallback(Class<?> testClass, Optional<Object> testInstance) {
        Object value = testInstance.isPresent() ? testInstance.get() : null;
        System.out.println(
                format("test class [%s] test instance [%s]", testClass.getName(), value));
    }
}
