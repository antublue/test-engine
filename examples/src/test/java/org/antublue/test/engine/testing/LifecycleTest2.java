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
import org.antublue.test.engine.api.support.named.NamedString;

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

    public static Stream<NamedString> arguments() {
        return StringArgumentSupplier.arguments();
    }

    @TestEngine.Prepare
    public static void prepare() {
        System.out.println("prepare()");
        ACTUAL_ORDER.add("prepare()");
    }

    @TestEngine.Prepare
    @TestEngine.Order(order = 0)
    public static void prepare2() {
        System.out.println("prepare2()");
        ACTUAL_ORDER.add("prepare2()");
    }

    @TestEngine.BeforeAll
    public void beforeAll(NamedString argument) {
        System.out.println("beforeAll()");
        ACTUAL_ORDER.add(argument + ".beforeAll()");
    }

    @TestEngine.BeforeEach
    public void beforeEach(NamedString argument) {
        System.out.println("beforeEach()");
        ACTUAL_ORDER.add(argument + ".beforeEach()");
    }

    @TestEngine.Test
    @TestEngine.Order(order = 1)
    public void test1(NamedString argument) {
        System.out.println("test1(" + argument + ")");
        ACTUAL_ORDER.add(argument + ".test1()");
    }

    @TestEngine.Test
    @TestEngine.Order(order = 0)
    public void test2(NamedString argument) {
        System.out.println("test2(" + argument + ")");
        ACTUAL_ORDER.add(argument + ".test2()");
    }

    @TestEngine.AfterEach
    public void afterEach(NamedString argument) {
        System.out.println("afterEach()");
        ACTUAL_ORDER.add(argument + ".afterEach()");
    }

    @TestEngine.AfterAll
    public void afterAll(NamedString argument) {
        System.out.println("afterAll()");
        ACTUAL_ORDER.add(argument + ".afterAll()");
    }

    @TestEngine.AfterAll
    @TestEngine.Order(order = 0)
    public void afterAll2(NamedString argument) {
        System.out.println("afterAll2()");
        ACTUAL_ORDER.add(argument + ".afterAll2()");
    }

    @TestEngine.Conclude
    public void conclude(NamedString argument) {
        System.out.println("conclude()");
        assertThat(argument).isNull();
        ACTUAL_ORDER.add("conclude()");
        assertThat(ACTUAL_ORDER).isEqualTo(EXPECTED_ORDER);
    }

    private static class StringArgumentSupplier {

        public static Stream<NamedString> arguments() {
            Collection<NamedString> collection = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                collection.add(NamedString.of(String.valueOf(i)));
            }
            return collection.stream();
        }
    }
}
