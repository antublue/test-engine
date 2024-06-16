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

package example.ordering;

import java.util.ArrayList;
import java.util.List;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;

public abstract class BaseOrderTest {

    protected static final List<String> EXPECTED =
            listOf(
                    "BaseOrderTest.prepare()",
                    "ConcreteOrderTest.prepare2()",
                    "BaseOrderTest.beforeAll()",
                    "ConcreteOrderTest.beforeAll2()",
                    "ConcreteOrderTest.afterAll2()",
                    "BaseOrderTest.afterAll()",
                    "ConcreteOrderTest.conclude2()",
                    "BaseOrderTest.conclude()");

    protected static final List<String> ACTUAL = new ArrayList<>();

    @TestEngine.Argument public Argument<String> argument;

    @TestEngine.Prepare
    @TestEngine.Order(order = 1)
    public static final void prepare() {
        System.out.println("BaseOrderTest.prepare()");
        ACTUAL.add("BaseOrderTest.prepare()");
    }

    @TestEngine.BeforeAll
    @TestEngine.Order(order = 1)
    public void beforeAll() {
        System.out.println("BaseOrderTest.beforeAll()");
        ACTUAL.add("BaseOrderTest.beforeAll()");
    }

    @TestEngine.Test
    @TestEngine.Order(order = 1)
    public void test2() {
        System.out.println("BaseOrderTest.test2(" + argument + ")");
    }

    @TestEngine.AfterAll
    @TestEngine.Order(order = 2)
    public void afterAll() {
        System.out.println("BaseOrderTest.afterAll()");
        ACTUAL.add("BaseOrderTest.afterAll()");
    }

    @TestEngine.Conclude
    @TestEngine.Order(order = 2)
    public static final void conclude() {
        System.out.println("BaseOrderTest.conclude()");
        ACTUAL.add("BaseOrderTest.conclude()");
    }

    private static List<String> listOf(String... strings) {
        List<String> list = new ArrayList<>(strings.length);
        for (String string : strings) {
            list.add(string);
        }
        return list;
    }
}
