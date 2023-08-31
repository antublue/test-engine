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

import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.Extension;

/** Example Extension */
public class ExampleExtension implements Extension {

    @Override
    public void beforePrepare(Object testInstance) throws Throwable {
        System.out.println(
                String.format(
                        "%s beforePrepare(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
    }

    @Override
    public void prepareCallback(Object testInstance) throws Throwable {
        System.out.println(
                String.format(
                        "%s afterPrepare(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
    }

    @Override
    public void beforeBeforeAll(Object testInstance, Argument testArgument) throws Throwable {
        System.out.println(
                String.format(
                        "%s beforeBeforeAll(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
    }

    @Override
    public void beforeAllCallback(Object testInstance, Argument testArgument) throws Throwable {
        System.out.println(
                String.format(
                        "%s afterBeforeAll(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
    }

    @Override
    public void beforeBeforeEach(Object testInstance, Argument testArgument) throws Throwable {
        System.out.println(
                String.format(
                        "%s beforeBeforeEach(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
    }

    @Override
    public void beforeEachCallback(Object testInstance, Argument testArgument) throws Throwable {
        System.out.println(
                String.format(
                        "%s afterBeforeEach(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
    }

    @Override
    public void beforeTest(Object testInstance, Argument testArgument) throws Throwable {
        System.out.println(
                String.format(
                        "%s beforeTest(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
    }

    @Override
    public void testCallback(Object testInstance, Argument testArgument) throws Throwable {
        System.out.println(
                String.format(
                        "%s afterTest(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
    }

    @Override
    public void beforeAfterEach(Object testInstance, Argument testArgument) throws Throwable {
        System.out.println(
                String.format(
                        "%s beforeAfterEach(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
    }

    @Override
    public void afterEachCallback(Object testInstance, Argument testArgument) throws Throwable {
        System.out.println(
                String.format(
                        "%s afterAfterEach(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
    }

    @Override
    public void beforeAfterAll(Object testInstance, Argument testArgument) throws Throwable {
        System.out.println(
                String.format(
                        "%s beforeAfterAll(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
    }

    @Override
    public void afterAllCallback(Object testInstance, Argument testArgument) throws Throwable {
        System.out.println(
                String.format(
                        "%s afterAfterAll(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
    }

    @Override
    public void beforeConclude(Object testInstance) throws Throwable {
        System.out.println(
                String.format(
                        "%s beforeConclude(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
    }

    @Override
    public void afterConcludeCallback(Object testInstance) throws Throwable {
        System.out.println(
                String.format(
                        "%s afterConclude(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
    }
}
