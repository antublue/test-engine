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
import org.antublue.test.engine.api.Named;
import org.antublue.test.engine.api.TestEngine;

/** Example test */
public class DebugDuplicateOrderTest {

    public static Stream<Named<Integer>> arguments() {
        Collection<Named<Integer>> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add(Named.ofInt(i));
        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    @TestEngine.Order(order = 0)
    public void beforeAll(Named<Integer> argument) {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    @TestEngine.Order(order = 0)
    public void test1(Named<Integer> argument) {
        System.out.println("test1(" + argument.getPayload() + ")");
    }

    @TestEngine.Test
    @TestEngine.Order(order = 1)
    // Switch to @TestEngine.Order(0) to test duplicate @TestEngine.Order detection
    public void test2(Named<Integer> argument) {
        System.out.println("test2(" + argument.getPayload() + ")");
    }

    @TestEngine.AfterAll
    public void afterAll(Named<Integer> argument) {
        System.out.println("afterAll()");
    }
}
