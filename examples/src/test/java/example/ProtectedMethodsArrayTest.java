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

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.Argument;

/** Example test */
public class ProtectedMethodsArrayTest {

    private String[] values;

    @TestEngine.Argument public Argument<String[]> argument;

    @TestEngine.ArgumentSupplier
    public static Stream<Argument<String[]>> arguments() {
        Collection<Argument<String[]>> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add(
                    Argument.of(
                            "Array [" + i + "]",
                            new String[] {String.valueOf(i), String.valueOf(i * 2)}));
        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
        values = argument.getPayload();
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + values[0] + ", " + values[1] + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + values[0] + ", " + values[1] + ")");
    }

    @TestEngine.Test
    public void test3() {
        System.out.println("test3(" + values[0] + ", " + values[1] + ")");
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
