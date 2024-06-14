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
import org.antublue.test.engine.api.Named;
import org.antublue.test.engine.api.TestEngine;

/** Example test */
public class StoreExampleTest6 {

    private static final String TEST_OBJECT_KEY = "testObject";

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
        System.out.println(format("key [%s]", TEST_OBJECT_KEY));

        TestObject testObject1 = new TestObject();
        context.getStore().put(TEST_OBJECT_KEY, testObject1);

        TestObject testObject2 = new TestObject();
        context.getStore(StoreExampleTest6.class).put(TEST_OBJECT_KEY, testObject2);

        assertThat(context.getStore().get(TEST_OBJECT_KEY) == testObject1).isTrue();
        assertThat(context.getStore(StoreExampleTest6.class).get(TEST_OBJECT_KEY) == testObject2)
                .isTrue();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll(" + argument + ")");
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
    public void afterAll() {
        System.out.println("afterAll(" + argument + ")");
    }

    @TestEngine.Conclude
    public void conclude() {
        System.out.println("conclude()");

        TestObject testObject = context.getStore().remove(TEST_OBJECT_KEY);
        testObject.close();

        assertThat(context.getStore().get(TEST_OBJECT_KEY, Object.class)).isNull();
        assertThat(context.getStore(StoreExampleTest6.class).get(TEST_OBJECT_KEY, Object.class))
                .isNotNull();

        testObject = context.getStore(StoreExampleTest6.class).remove(TEST_OBJECT_KEY);
        testObject.close();

        assertThat(context.getStore(StoreExampleTest6.class).get(TEST_OBJECT_KEY, Object.class))
                .isNull();
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
