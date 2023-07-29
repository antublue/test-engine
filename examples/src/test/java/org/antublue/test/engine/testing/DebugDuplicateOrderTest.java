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

package org.antublue.test.engine.testing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.IntegerArgument;

/** Example test */
public class DebugDuplicateOrderTest {

    @TestEngine.Argument protected IntegerArgument integerArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<IntegerArgument> arguments() {
        Collection<IntegerArgument> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add(IntegerArgument.of(i));
        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    @TestEngine.Order(order = 0)
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    @TestEngine.Order(order = 0)
    public void test1() {
        System.out.println("test1(" + integerArgument.value() + ")");
    }

    @TestEngine.Test
    @TestEngine.Order(order = 1)
    // Switch to @TestEngine.Order(0) to test duplicate @TestEngine.Order detection
    public void test2() {
        System.out.println("test2(" + integerArgument.value() + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
