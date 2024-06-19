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

import static org.junit.jupiter.api.Assertions.fail;

import java.util.stream.Stream;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;

/** Test used for debugging IntelliJ */
@TestEngine.Disabled
public class DebugTestA {

    @TestEngine.Argument public Argument<Integer> argument;

    @TestEngine.ArgumentSupplier
    public static Stream<Argument<Integer>> arguments() {
        return Stream.of(Argument.ofInt(1), Argument.ofInt(2), Argument.ofInt(3));
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + argument.getPayload() + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + argument.getPayload() + ")");
        if (argument.getPayload() == 1) {
            fail("FORCED");
        }
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
