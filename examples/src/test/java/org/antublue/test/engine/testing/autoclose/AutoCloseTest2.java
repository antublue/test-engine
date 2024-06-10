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

package org.antublue.test.engine.testing.autoclose;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Extension;
import org.antublue.test.engine.api.Named;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.support.NamedString;

/** Example test */
public class AutoCloseTest2 {

    @TestEngine.Argument protected NamedString argument;

    @TestEngine.ArgumentSupplier
    public static Stream<NamedString> arguments() {
        Collection<NamedString> collection = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            collection.add(NamedString.of("StringArgument " + i));
        }
        return collection.stream();
    }

    @TestEngine.ExtensionSupplier
    public static Stream<Extension> extensions() {
        Collection<Extension> collection = new ArrayList<>();
        collection.add(new TestExtension());
        return collection.stream();
    }

    @TestEngine.AutoClose.AfterEach(method = "destroy")
    public TestObject afterEachTestObject;

    @TestEngine.AutoClose.AfterAll(method = "destroy")
    public TestObject afterAllTestObject;

    @TestEngine.AutoClose.Conclude(method = "destroy")
    public static TestObject afterConcludeTestObject;

    @TestEngine.Prepare
    public static void prepare() {
        System.out.println("prepare()");
        afterConcludeTestObject = new TestObject("afterConcludeTestObject");
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll(" + argument + ")");
        afterAllTestObject = new TestObject("afterAllTestObject");
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach(" + argument + ")");
        afterEachTestObject = new TestObject("afterEachTestObject");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + argument + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + argument + ")");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach(" + argument + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll(" + argument + ")");
    }

    @TestEngine.Conclude
    public static void conclude() {
        System.out.println("conclude()");
    }

    private static class TestObject {

        private final String name;
        private boolean isDestroyed;

        public TestObject(String name) {
            this.name = name;
        }

        public void destroy() {
            System.out.println(name + ".destroy()");
            isDestroyed = true;
        }

        public boolean isDestroyed() {
            return isDestroyed;
        }
    }

    public static class TestExtension implements Extension {

        @Override
        public void postAfterEachMethodsCallback(Object testInstance, Named testArgument) {
            AutoCloseTest2 autoCloseExampleTest2 = (AutoCloseTest2) testInstance;
            assertThat(autoCloseExampleTest2.afterEachTestObject.isDestroyed()).isFalse();
        }

        @Override
        public void postAfterAllMethodsCallback(Object testInstance, Named testArgument) {
            AutoCloseTest2 autoCloseExampleTest2 = (AutoCloseTest2) testInstance;
            assertThat(autoCloseExampleTest2.afterAllTestObject.isDestroyed()).isFalse();
        }

        public void postConcludeMethodsCallback(Object testInstance) {
            System.out.println("afterConcludeMethodsCallback()");
            AutoCloseTest2 autoCloseExampleTest2 = (AutoCloseTest2) testInstance;
            assertThat(autoCloseExampleTest2.afterConcludeTestObject.isDestroyed()).isFalse();
        }
    }
}
