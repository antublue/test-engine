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

package org.antublue.test.engine.testing.extension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.Extension;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.StringArgument;

/** Example test */
public class ExtensionTest1 {

    @TestEngine.Argument protected StringArgument stringArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<StringArgument> arguments() {
        Collection<StringArgument> collection = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            collection.add(StringArgument.of("StringArgument " + i));
        }
        return collection.stream();
    }

    @TestEngine.ExtensionSupplier
    public static Stream<Extension> extensions() {
        Collection<Extension> collection = new ArrayList<>();
        collection.add(StopWatchExtension.singleton());
        collection.add(new TestExtension1());
        collection.add(new TestExtension2());
        return collection.stream();
    }

    @TestEngine.Prepare
    public void prepare() {
        System.out.println("prepare()");
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll(" + stringArgument + ")");
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach(" + stringArgument + ")");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + stringArgument + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + stringArgument + ")");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach(" + stringArgument + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll(" + stringArgument + ")");
    }

    @TestEngine.Conclude
    public void conclude() {
        System.out.println("conclude()");
    }

    public static class TestExtension1 implements Extension {

        @Override
        public void beforePrepare(Object testInstance) throws Throwable {
            System.out.println(
                    String.format(
                            "%s.beforePrepare(class [%s])",
                            this.getClass().getSimpleName(), testInstance.getClass().getName()));
        }

        @Override
        public void prepareCallback(Object testInstance) throws Throwable {
            System.out.println(
                    String.format(
                            "%s.prepareCallback(class [%s])",
                            this.getClass().getSimpleName(), testInstance.getClass().getName()));
        }

        @Override
        public void beforeBeforeAll(Object testInstance, Argument testArgument) throws Throwable {
            System.out.println(
                    String.format(
                            "%s.beforeBeforeAll(class [%s])",
                            this.getClass().getSimpleName(), testInstance.getClass().getName()));
        }

        @Override
        public void beforeAllCallback(Object testInstance, Argument testArgument) throws Throwable {
            System.out.println(
                    String.format(
                            "%s.beforeAllCallback(class [%s])",
                            this.getClass().getSimpleName(), testInstance.getClass().getName()));
        }

        @Override
        public void beforeBeforeEach(Object testInstance, Argument testArgument) throws Throwable {
            System.out.println(
                    String.format(
                            "%s.beforeBeforeEach(class [%s])",
                            this.getClass().getSimpleName(), testInstance.getClass().getName()));
        }

        @Override
        public void beforeEachCallback(Object testInstance, Argument testArgument) throws Throwable {
            System.out.println(
                    String.format(
                            "%s.beforeEachCallback(class [%s])",
                            this.getClass().getSimpleName(), testInstance.getClass().getName()));
        }

        @Override
        public void beforeTest(Object testInstance, Argument testArgument) throws Throwable {
            System.out.println(
                    String.format(
                            "%s.beforeTest(class [%s])",
                            this.getClass().getSimpleName(), testInstance.getClass().getName()));
        }

        @Override
        public void testCallback(Object testInstance, Argument testArgument) throws Throwable {
            System.out.println(
                    String.format(
                            "%s.testCallback(class [%s])",
                            this.getClass().getSimpleName(), testInstance.getClass().getName()));
        }

        @Override
        public void beforeAfterEach(Object testInstance, Argument testArgument) throws Throwable {
            System.out.println(
                    String.format(
                            "%s.beforeAfterEach(class [%s])",
                            this.getClass().getSimpleName(), testInstance.getClass().getName()));
        }

        @Override
        public void afterEachCallback(Object testInstance, Argument testArgument) throws Throwable {
            System.out.println(
                    String.format(
                            "%s.afterEachCallback(class [%s])",
                            this.getClass().getSimpleName(), testInstance.getClass().getName()));
        }

        @Override
        public void beforeAfterAll(Object testInstance, Argument testArgument) throws Throwable {
            System.out.println(
                    String.format(
                            "%s.beforeAfterAll(class [%s])",
                            this.getClass().getSimpleName(), testInstance.getClass().getName()));
        }

        @Override
        public void afterAllCallback(Object testInstance, Argument testArgument) throws Throwable {
            System.out.println(
                    String.format(
                            "%s.afterAllCallback(class [%s])",
                            this.getClass().getSimpleName(), testInstance.getClass().getName()));
        }

        @Override
        public void beforeConclude(Object testInstance) throws Throwable {
            System.out.println(
                    String.format(
                            "%s.beforeConclude(class [%s])",
                            this.getClass().getSimpleName(), testInstance.getClass().getName()));
        }

        @Override
        public void afterConcludeCallback(Object testInstance) throws Throwable {
            System.out.println(
                    String.format(
                            "%s.afterConcludeCallback(class [%s])",
                            this.getClass().getSimpleName(), testInstance.getClass().getName()));
        }
    }

    public static class TestExtension2 implements Extension {

        @Override
        public void beforePrepare(Object testInstance) throws Throwable {
            System.out.println(
                    String.format(
                            "%s.beforePrepare(class [%s])",
                            this.getClass().getSimpleName(), testInstance.getClass().getName()));
        }

        @Override
        public void prepareCallback(Object testInstance) throws Throwable {
            System.out.println(
                    String.format(
                            "%s.prepareCallback(class [%s])",
                            this.getClass().getSimpleName(), testInstance.getClass().getName()));
        }

        @Override
        public void beforeBeforeAll(Object testInstance, Argument testArgument) throws Throwable {
            System.out.println(
                    String.format(
                            "%s.beforeBeforeAll(class [%s])",
                            this.getClass().getSimpleName(), testInstance.getClass().getName()));
        }

        @Override
        public void beforeAllCallback(Object testInstance, Argument testArgument) throws Throwable {
            System.out.println(
                    String.format(
                            "%s.beforeAllCallback(class [%s])",
                            this.getClass().getSimpleName(), testInstance.getClass().getName()));
        }

        @Override
        public void beforeBeforeEach(Object testInstance, Argument testArgument) throws Throwable {
            System.out.println(
                    String.format(
                            "%s.beforeBeforeEach(class [%s])",
                            this.getClass().getSimpleName(), testInstance.getClass().getName()));
        }

        @Override
        public void beforeEachCallback(Object testInstance, Argument testArgument) throws Throwable {
            System.out.println(
                    String.format(
                            "%s.beforeEachCallback(class [%s])",
                            this.getClass().getSimpleName(), testInstance.getClass().getName()));
        }

        @Override
        public void beforeTest(Object testInstance, Argument testArgument) throws Throwable {
            System.out.println(
                    String.format(
                            "%s.beforeTest(class [%s])",
                            this.getClass().getSimpleName(), testInstance.getClass().getName()));
        }

        @Override
        public void testCallback(Object testInstance, Argument testArgument) throws Throwable {
            System.out.println(
                    String.format(
                            "%s.testCallback(class [%s])",
                            this.getClass().getSimpleName(), testInstance.getClass().getName()));
        }

        @Override
        public void beforeAfterEach(Object testInstance, Argument testArgument) throws Throwable {
            System.out.println(
                    String.format(
                            "%s.beforeAfterEach(class [%s])",
                            this.getClass().getSimpleName(), testInstance.getClass().getName()));
        }

        @Override
        public void afterEachCallback(Object testInstance, Argument testArgument) throws Throwable {
            System.out.println(
                    String.format(
                            "%s.afterEachCallback(class [%s])",
                            this.getClass().getSimpleName(), testInstance.getClass().getName()));
        }

        @Override
        public void beforeAfterAll(Object testInstance, Argument testArgument) throws Throwable {
            System.out.println(
                    String.format(
                            "%s.beforeAfterAll(class [%s])",
                            this.getClass().getSimpleName(), testInstance.getClass().getName()));
        }

        @Override
        public void afterAllCallback(Object testInstance, Argument testArgument) throws Throwable {
            System.out.println(
                    String.format(
                            "%s.afterAllCallback(class [%s])",
                            this.getClass().getSimpleName(), testInstance.getClass().getName()));
        }

        @Override
        public void beforeConclude(Object testInstance) throws Throwable {
            System.out.println(
                    String.format(
                            "%s.beforeConclude(class [%s])",
                            this.getClass().getSimpleName(), testInstance.getClass().getName()));
        }

        @Override
        public void afterConcludeCallback(Object testInstance) throws Throwable {
            System.out.println(
                    String.format(
                            "%s.afterConcludeCallback(class [%s])",
                            this.getClass().getSimpleName(), testInstance.getClass().getName()));
        }
    }
}
