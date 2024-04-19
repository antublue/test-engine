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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.StringArgument;

/** Example test */
public class LifecycleTest2 {

    private static final List<String> EXPECTED_ORDER = new ArrayList<>();
    private static final List<String> ACTUAL_ORDER = new ArrayList<>();

    static {
        EXPECTED_ORDER.add("prepare2()");
        EXPECTED_ORDER.add("prepare()");

        StringArgumentSupplier.arguments()
                .forEach(
                        stringArgument -> {
                            EXPECTED_ORDER.add(stringArgument + ".beforeAll()");
                            EXPECTED_ORDER.add(stringArgument + ".beforeEach()");
                            EXPECTED_ORDER.add(stringArgument + ".test2()");
                            EXPECTED_ORDER.add(stringArgument + ".afterEach()");
                            EXPECTED_ORDER.add(stringArgument + ".beforeEach()");
                            EXPECTED_ORDER.add(stringArgument + ".test1()");
                            EXPECTED_ORDER.add(stringArgument + ".afterEach()");
                            EXPECTED_ORDER.add(stringArgument + ".afterAll2()");
                            EXPECTED_ORDER.add(stringArgument + ".afterAll()");
                        });

        EXPECTED_ORDER.add("conclude()");
    }

    public static Stream<StringArgument> arguments() {
        return StringArgumentSupplier.arguments();
    }

    @TestEngine.Prepare
    public void prepare() {
        System.out.println("prepare()");
        ACTUAL_ORDER.add("prepare()");
    }

    @TestEngine.Prepare
    @TestEngine.Order(order = 0)
    public void prepare2() {
        System.out.println("prepare2()");
        ACTUAL_ORDER.add("prepare2()");
    }

    @TestEngine.BeforeAll
    public void beforeAll(StringArgument stringArgument) {
        System.out.println("beforeAll()");
        ACTUAL_ORDER.add(stringArgument + ".beforeAll()");
    }

    @TestEngine.BeforeEach
    public void beforeEach(StringArgument stringArgument) {
        System.out.println("beforeEach()");
        ACTUAL_ORDER.add(stringArgument + ".beforeEach()");
    }

    @TestEngine.Test
    @TestEngine.Order(order = 1)
    public void test1(StringArgument stringArgument) {
        System.out.println("test1(" + stringArgument + ")");
        ACTUAL_ORDER.add(stringArgument + ".test1()");
    }

    @TestEngine.Test
    @TestEngine.Order(order = 0)
    public void test2(StringArgument stringArgument) {
        System.out.println("test2(" + stringArgument + ")");
        ACTUAL_ORDER.add(stringArgument + ".test2()");
    }

    @TestEngine.AfterEach
    public void afterEach(StringArgument stringArgument) {
        System.out.println("afterEach()");
        ACTUAL_ORDER.add(stringArgument + ".afterEach()");
    }

    @TestEngine.AfterAll
    public void afterAll(StringArgument stringArgument) {
        System.out.println("afterAll()");
        ACTUAL_ORDER.add(stringArgument + ".afterAll()");
    }

    @TestEngine.AfterAll
    @TestEngine.Order(order = 0)
    public void afterAll2(StringArgument stringArgument) {
        System.out.println("afterAll2()");
        ACTUAL_ORDER.add(stringArgument + ".afterAll2()");
    }

    @TestEngine.Conclude
    public void conclude(StringArgument stringArgument) {
        System.out.println("conclude()");
        assertThat(stringArgument).isNull();
        ACTUAL_ORDER.add("conclude()");
        assertThat(ACTUAL_ORDER).isEqualTo(EXPECTED_ORDER);
    }

    private static class StringArgumentSupplier {

        public static Stream<StringArgument> arguments() {
            Collection<StringArgument> collection = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                collection.add(StringArgument.of(String.valueOf(i)));
            }
            return collection.stream();
        }
    }
}
