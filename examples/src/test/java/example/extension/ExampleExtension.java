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

import java.lang.reflect.Method;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.extension.Extension;

/** Example Extension */
public class ExampleExtension implements Extension {

    @Override
    public void postPrepare(Object testInstance) {
        System.out.println(
                String.format(
                        "%s afterPrepare(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
    }

    @Override
    public void postBeforeAll(Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "%s afterBeforeAll(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
    }

    @Override
    public void postBeforeEach(Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "%s afterBeforeEach(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
    }

    @Override
    public void preTest(Object testInstance, Argument testArgument, Method testMethod) {
        System.out.println(
                String.format(
                        "%s preTest(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
    }

    @Override
    public void postTest(Object testInstance, Argument testArgument, Method testMethod) {
        System.out.println(
                String.format(
                        "%s postTest(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
    }

    @Override
    public void postAfterEach(Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "%s postAfterEach(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
    }

    @Override
    public void postAfterAll(Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "%s postAfterAll(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
    }

    @Override
    public void postConclude(Object testInstance) {
        System.out.println(
                String.format(
                        "%s postConclude(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
    }
}
