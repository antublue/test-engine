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
@TestEngine.Order(order = 1)
public class ClassOrderTest2 {

    public static Stream<Named<String>> arguments() {
        return StringArgumentSupplier.arguments();
    }

    @TestEngine.BeforeAll
    public void beforeAll(Named<String> argument) {
        System.out.println("beforeAll()");
    }

    @TestEngine.BeforeEach
    public void beforeEach(Named<String> argument) {
        System.out.println("beforeEach()");
    }

    @TestEngine.Test
    public void test1(Named<String> argument) {
        System.out.println("test1(" + argument + ")");
    }

    @TestEngine.Test
    public void test2(Named<String> argument) {
        System.out.println("test2(" + argument + ")");
    }

    @TestEngine.AfterEach
    public void afterEach(Named<String> argument) {
        System.out.println("afterEach()");
    }

    @TestEngine.AfterAll
    public void afterAll(Named<String> argument) {
        System.out.println("afterAll()");
    }

    private static class StringArgumentSupplier {

        public static Stream<Named<String>> arguments() {
            Collection<Named<String>> collection = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                collection.add(Named.ofString(String.valueOf(i)));
            }
            return collection.stream();
        }
    }
}
