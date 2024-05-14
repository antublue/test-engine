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

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Context;
import org.antublue.test.engine.api.Store;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.StringArgument;

/** Example test */
public class StoreExampleTest6 {

    private static final String TEST_OBJECT_KEY = "testObject";

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
        System.out.println(format("key [%s]", TEST_OBJECT_KEY));

        TestObject testObject1 = new TestObject();
        Context.getInstance().getStore().put(TEST_OBJECT_KEY,  testObject1);

        TestObject testObject2 = new TestObject();
        Context.getInstance().getStore(StoreExampleTest6.class).put(TEST_OBJECT_KEY,  testObject2);

        assertThat(Context.getInstance().getStore().get(TEST_OBJECT_KEY) == testObject1);
        assertThat(Context.getInstance().getStore(StoreExampleTest6.class).get(TEST_OBJECT_KEY) == testObject2);
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

        TestObject testObject = (TestObject) Context.getInstance().getStore().remove(TEST_OBJECT_KEY);
        testObject.close();

        assertThat(Context.getInstance().getStore().get(TEST_OBJECT_KEY)).isNull();
        assertThat(Context.getInstance().getStore(StoreExampleTest6.class).get(TEST_OBJECT_KEY)).isNotNull();

        testObject = (TestObject) Context.getInstance().getStore(StoreExampleTest6.class).remove(TEST_OBJECT_KEY);
        testObject.close();

        assertThat(Context.getInstance().getStore(StoreExampleTest6.class).get(TEST_OBJECT_KEY)).isNull();
    }

    private static class TestObject {

        public TestObject() {
            // DO NOTHING
        }

        public void close() {
            System.out.println(getClass().getName() + ".close()");
        }
    }
}
