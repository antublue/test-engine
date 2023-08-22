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

package example.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.StringArgument;

@TestEngine.BaseClass
@TestEngine.Order(order = 3)
class BaseOrderTest {

    protected final List<String> EXPECTED =
            listOf(
                    "BaseOrderTest.prepare()",
                    "ConcreteOrderTest.prepare2()",
                    "BaseOrderTest.beforeAll()",
                    "ConcreteOrderTest.beforeAll2()",
                    "ConcreteOrderTest.afterAll2()",
                    "BaseOrderTest.afterAll()",
                    "ConcreteOrderTest.conclude2()",
                    "BaseOrderTest.conclude()");

    protected final List<String> actual = new ArrayList<>();

    @TestEngine.Argument protected StringArgument stringArgument;

    @TestEngine.Prepare
    @TestEngine.Order(order = 1)
    public final void prepare() {
        System.out.println("BaseOrderTest.prepare()");
        actual.add("BaseOrderTest.prepare()");
    }

    @TestEngine.BeforeAll
    @TestEngine.Order(order = 1)
    public void beforeAll() {
        System.out.println("BaseOrderTest.beforeAll()");
        actual.add("BaseOrderTest.beforeAll()");
    }

    @TestEngine.Test
    @TestEngine.Order(order = 1)
    public void test2() {
        System.out.println("BaseOrderTest.test2(" + stringArgument + ")");
    }

    @TestEngine.AfterAll
    @TestEngine.Order(order = 2)
    public void afterAll() {
        System.out.println("BaseOrderTest.afterAll()");
        actual.add("BaseOrderTest.afterAll()");
    }

    @TestEngine.Conclude
    @TestEngine.Order(order = 2)
    public final void conclude() {
        System.out.println("BaseOrderTest.conclude()");
        assertThat(stringArgument).isNull();
        actual.add("BaseOrderTest.conclude()");
        assertThat(actual).isEqualTo(EXPECTED);
    }

    private static List<String> listOf(String... strings) {
        List<String> list = new ArrayList<>(strings.length);
        for (String string : strings) {
            list.add(string);
        }
        return list;
    }
}
