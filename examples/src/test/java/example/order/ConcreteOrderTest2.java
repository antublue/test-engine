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

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.named.NamedString;

/** Example test */
@TestEngine.Order(order = 4)
public class ConcreteOrderTest2 extends BaseOrderTest {

    @TestEngine.ArgumentSupplier
    public static Stream<NamedString> arguments() {
        Collection<NamedString> collection = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            int value = i * 3;
            collection.add(NamedString.of(String.valueOf(value)));
        }
        return collection.stream();
    }

    @TestEngine.Prepare
    @TestEngine.Order(order = 2)
    public static void prepare2() {
        System.out.println("ConcreteOrderTest.prepare2()");
        actual.add("ConcreteOrderTest.prepare2()");
    }

    @TestEngine.BeforeAll
    @TestEngine.Order(order = 2)
    public void beforeAll2() {
        System.out.println("ConcreteOrderTest.beforeAll2()");
        actual.add("ConcreteOrderTest.beforeAll2()");
    }

    @TestEngine.Test
    public void testA() {
        System.out.println("ConcreteOrderTest.testA(" + argument + ")");
    }

    @TestEngine.Test
    public void testB() {
        System.out.println("ConcreteOrderTest.testB(" + argument + ")");
    }

    @TestEngine.Test
    @TestEngine.Order(order = 2)
    public void test3() {
        System.out.println("ConcreteOrderTest.test3(" + argument + ")");
    }

    @TestEngine.AfterAll
    @TestEngine.Order(order = 1)
    public void afterAll2() {
        System.out.println("ConcreteOrderTest.afterAll2()");
        actual.add("ConcreteOrderTest.afterAll2()");
    }

    @TestEngine.Conclude
    @TestEngine.Order(order = 1)
    public static void conclude2() {
        System.out.println("ConcreteOrderTest.conclude2()");
        actual.add("ConcreteOrderTest.conclude2()");
    }
}
