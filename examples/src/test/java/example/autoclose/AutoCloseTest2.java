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
import org.antublue.test.engine.api.argument.StringArgument;

/** Example test */
public class AutoCloseTest2 {

    @TestEngine.Argument protected StringArgument stringArgument;

    @TestEngine.Supplier.Argument
    public static Stream<StringArgument> arguments() {
        Collection<StringArgument> collection = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            collection.add(StringArgument.of("StringArgument " + i));
        }
        return collection.stream();
    }

    @TestEngine.AutoClose.AfterEach(method = "destroy")
    private TestObject afterEachTestObject;

    @TestEngine.AutoClose.AfterAll(method = "destroy")
    private TestObject afterAllTestObject;

    @TestEngine.AutoClose.Conclude(method = "destroy")
    private TestObject afterConcludeTestObject;

    @TestEngine.Prepare
    public void prepare() {
        System.out.println("prepare()");
        afterConcludeTestObject = new TestObject("afterConcludeTestObject");
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll(" + stringArgument + ")");
        afterAllTestObject = new TestObject("afterAllTestObject");
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach(" + stringArgument + ")");
        afterEachTestObject = new TestObject("afterEachTestObject");
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
        assertThat(afterEachTestObject.isDestroyed()).isTrue();
    }

    @TestEngine.Conclude
    public void conclude() {
        System.out.println("conclude()");
        assertThat(afterAllTestObject.isDestroyed()).isTrue();
        assertThat(afterConcludeTestObject.isDestroyed()).isFalse();
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
}
