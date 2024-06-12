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

package example.argument.supplier;

import java.util.ArrayList;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.named.NamedInteger;

/** Example test */
public class IterableTest {

    @TestEngine.Argument protected NamedInteger argument;

    @TestEngine.ArgumentSupplier
    public static Iterable<NamedInteger> arguments() {
        ArrayList<NamedInteger> arguments = new ArrayList<>();
        arguments.add(NamedInteger.of(1));
        arguments.add(NamedInteger.of(2));
        arguments.add(NamedInteger.of(3));
        return arguments;
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
        System.out.println("test1(" + argument + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + argument + ")");
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
