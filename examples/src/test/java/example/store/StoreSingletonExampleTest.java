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

package example.store;

import static org.assertj.core.api.Assertions.assertThat;

import example.util.KeyGenerator;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Store;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.StringArgument;

/** Example test */
public class StoreSingletonExampleTest {

    private static final String CLOSEABLE_KEY =
            KeyGenerator.of(StoreExampleTest1.class, "closeable");
    private static final String AUTO_CLOSEABLE_KEY =
            KeyGenerator.of(StoreExampleTest1.class, "autoClosable");

    @TestEngine.Argument protected StringArgument stringArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<StringArgument> arguments() {
        Collection<StringArgument> collection = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            collection.add(StringArgument.of("StringArgument " + i));
        }
        return collection.stream();
    }

    @TestEngine.Prepare
    public void prepare() {
        System.out.println("prepare()");
        Store.singleton().put(CLOSEABLE_KEY, new TestCloseable());
        Store.singleton().put(AUTO_CLOSEABLE_KEY, new TestAutoCloseable());
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
        Store.singleton().removeAndClose(CLOSEABLE_KEY);
        Store.singleton().removeAndClose(AUTO_CLOSEABLE_KEY);
        assertThat(Store.singleton().get(CLOSEABLE_KEY)).isNotPresent();
        assertThat(Store.singleton().get(AUTO_CLOSEABLE_KEY)).isNotPresent();
    }

    private static class TestAutoCloseable implements AutoCloseable {

        public TestAutoCloseable() {
            // DO NOTHING
        }

        public void close() {
            System.out.println(getClass().getName() + ".close()");
        }
    }

    private static class TestCloseable implements Closeable {

        public TestCloseable() {
            // DO NOTHING
        }

        public void close() {
            System.out.println(getClass().getName() + ".close()");
        }
    }
}
