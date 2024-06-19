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

package example;

import java.util.stream.Stream;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;

/** Example test */
public class MethodOrderAndDisplayNameTest {

    public static Stream<Argument<Integer>> arguments() {
        return Stream.of(Argument.ofInt(1), Argument.ofInt(2), Argument.ofInt(3));
    }

    @TestEngine.BeforeAll
    public void beforeAll(Argument<Integer> argument) {
        System.out.println("beforeAll()");
    }

    @TestEngine.BeforeEach
    public void beforeEach(Argument<Integer> argument) {
        System.out.println("beforeEach()");
    }

    @TestEngine.Test
    @TestEngine.Order(order = 2)
    @TestEngine.DisplayName(name = "Test A")
    public void testA(Argument<Integer> argument) {
        System.out.println("testA(" + argument + ")");
    }

    @TestEngine.Test
    @TestEngine.Order(order = 1)
    @TestEngine.DisplayName(name = "Test B")
    public void testB(Argument<Integer> argument) {
        System.out.println("testB(" + argument + ")");
    }

    @TestEngine.AfterEach
    public void afterEach(Argument<Integer> argument) {
        System.out.println("afterEach()");
    }

    @TestEngine.AfterAll
    public void afterAll(Argument<Integer> argument) {
        System.out.println("afterAll()");
    }
}
