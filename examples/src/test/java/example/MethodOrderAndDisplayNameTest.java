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
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.support.NamedInteger;

/** Example test */
public class MethodOrderAndDisplayNameTest {

    public static Stream<NamedInteger> arguments() {
        return Stream.of(NamedInteger.of(1), NamedInteger.of(2), NamedInteger.of(3));
    }

    @TestEngine.BeforeAll
    public void beforeAll(NamedInteger argument) {
        System.out.println("beforeAll()");
    }

    @TestEngine.BeforeEach
    public void beforeEach(NamedInteger argument) {
        System.out.println("beforeEach()");
    }

    @TestEngine.Test
    @TestEngine.Order(order = 2)
    @TestEngine.DisplayName(name = "Test A")
    public void testA(NamedInteger argument) {
        System.out.println("testA(" + argument + ")");
    }

    @TestEngine.Test
    @TestEngine.Order(order = 1)
    @TestEngine.DisplayName(name = "Test B")
    public void testB(NamedInteger argument) {
        System.out.println("testB(" + argument + ")");
    }

    @TestEngine.AfterEach
    public void afterEach(NamedInteger argument) {
        System.out.println("afterEach()");
    }

    @TestEngine.AfterAll
    public void afterAll(NamedInteger argument) {
        System.out.println("afterAll()");
    }
}
