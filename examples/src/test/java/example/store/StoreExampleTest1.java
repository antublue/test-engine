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

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Context;
import org.antublue.test.engine.api.Named;
import org.antublue.test.engine.api.Store;
import org.antublue.test.engine.api.TestEngine;

/** Example test */
public class StoreExampleTest1 {

    private static final String CLOSEABLE_KEY = "closeable";

    private static final String AUTO_CLOSEABLE_KEY = "autoClosable";

    private static final String HIDDEN_KEY = ".hidden";

    private Store store;

    @TestEngine.Context public static Context context;

    @TestEngine.Argument public Named<String> argument;

    @TestEngine.ArgumentSupplier
    public static Stream<Named<String>> arguments() {
        Collection<Named<String>> collection = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            collection.add(Named.ofString("StringArgument " + i));
        }
        return collection.stream();
    }

    @TestEngine.Prepare
    public void prepare() {
        System.out.println("prepare()");
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll(" + argument + ")");

        store = context.getStore(StoreExampleTest1.class);
        store.put(CLOSEABLE_KEY, new TestCloseable());
        store.put(AUTO_CLOSEABLE_KEY, new TestAutoCloseable());
        store.put(HIDDEN_KEY, new Object());
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach(" + argument + ")");
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
    public void afterAll() throws Exception {
        System.out.println("afterAll(" + argument + ")");

        for (String key : store.keySet()) {
            System.out.println("key [" + key + "]");
        }

        assertThat(store.keySet()).doesNotContain(HIDDEN_KEY);
        assertThat(store.get(HIDDEN_KEY, Object.class)).isNotNull();

        Closeable closeable = (Closeable) store.remove(CLOSEABLE_KEY);
        closeable.close();

        AutoCloseable autoCloseable = (AutoCloseable) store.remove(AUTO_CLOSEABLE_KEY);
        autoCloseable.close();

        assertThat(store.get(CLOSEABLE_KEY, Object.class)).isNull();
        assertThat(store.get(AUTO_CLOSEABLE_KEY, Object.class)).isNull();
    }

    @TestEngine.Conclude
    public void conclude() {
        System.out.println("conclude()");
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
