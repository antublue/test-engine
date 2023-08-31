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
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.StringArgument;

/** Example test */
public class AutoCloseTest1 {

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
        collection.add(new TestExtension());
        return collection.stream();
    }

    @TestEngine.AutoClose(lifecycle = "@TestEngine.AfterEach")
    private TestAutoCloseable afterEachAutoClosable;

    @TestEngine.AutoClose(lifecycle = "@TestEngine.AfterAll")
    private TestAutoCloseable afterAllAutoClosable;

    @TestEngine.AutoClose(lifecycle = "@TestEngine.Conclude")
    private TestAutoCloseable afterConcludeAutoCloseable;

    @TestEngine.Prepare
    public void prepare() {
        System.out.println("prepare()");
        afterConcludeAutoCloseable = new TestAutoCloseable("afterConcludeAutoCloseable");
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll(" + stringArgument + ")");
        afterAllAutoClosable = new TestAutoCloseable("afterAllAutoCloseable");
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach(" + stringArgument + ")");
        afterEachAutoClosable = new TestAutoCloseable("afterEachAutoCloseable");
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

    private static class TestAutoCloseable implements AutoCloseable {

        private final String name;
        private boolean isClosed;

        public TestAutoCloseable(String name) {
            this.name = name;
        }

        public void close() {
            System.out.println(name + ".close()");
            isClosed = true;
        }

        public boolean isClosed() {
            return isClosed;
        }
    }

    public static class TestExtension implements Extension {

        public void afterAfterEach(Object testInstance) {
            AutoCloseTest1 autoCloseExampleTest1 = (AutoCloseTest1) testInstance;
            assertThat(autoCloseExampleTest1.afterEachAutoClosable.isClosed()).isTrue();
        }

        public void afterAfterAll(Object testInstance) {
            AutoCloseTest1 autoCloseExampleTest1 = (AutoCloseTest1) testInstance;
            assertThat(autoCloseExampleTest1.afterAllAutoClosable.isClosed()).isTrue();
        }

        public void afterConcludeCallback(Object testInstance) {
            AutoCloseTest1 autoCloseExampleTest1 = (AutoCloseTest1) testInstance;
            assertThat(autoCloseExampleTest1.afterConcludeAutoCloseable.isClosed()).isTrue();
        }
    }
}
