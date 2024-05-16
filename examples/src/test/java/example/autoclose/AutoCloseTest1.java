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

package example.autoclose;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.support.NamedString;

/** Example test */
public class AutoCloseTest1 {

    @TestEngine.Argument protected NamedString argument;

    @TestEngine.ArgumentSupplier
    public static Stream<NamedString> arguments() {
        Collection<NamedString> collection = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            collection.add(NamedString.of("StringArgument " + i));
        }
        return collection.stream();
    }

    @TestEngine.AutoClose.AfterEach private TestAutoCloseable afterEachAutoClosable;

    @TestEngine.AutoClose.AfterAll private TestAutoCloseable afterAllAutoClosable;

    @TestEngine.AutoClose.Conclude private TestAutoCloseable afterConcludeAutoCloseable;

    @TestEngine.Prepare
    public void prepare() {
        System.out.println("prepare()");
        afterConcludeAutoCloseable = new TestAutoCloseable("afterConcludeAutoCloseable");
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll(" + argument + ")");
        afterAllAutoClosable = new TestAutoCloseable("afterAllAutoCloseable");
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach(" + argument + ")");
        afterEachAutoClosable = new TestAutoCloseable("afterEachAutoCloseable");
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
        assertThat(afterEachAutoClosable.isClosed()).isTrue();
        assertThat(afterAllAutoClosable.isClosed()).isFalse();
        assertThat(afterConcludeAutoCloseable.isClosed()).isFalse();
    }

    @TestEngine.Conclude
    public void conclude() {
        System.out.println("conclude()");
        assertThat(afterAllAutoClosable.isClosed()).isTrue();
        assertThat(afterAllAutoClosable.isClosed()).isTrue();
        assertThat(afterConcludeAutoCloseable.isClosed()).isFalse();
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
}
