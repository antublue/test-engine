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
import org.antublue.test.engine.api.Argument;

/** Example test */
public class DuplicateArgumentNamesLifecycleTest {

    private static final List<String> EXPECTED = new ArrayList<>();
    private static final List<String> ACTUAL = new ArrayList<>();

    static {
        EXPECTED.add("prepare2()");
        EXPECTED.add("prepare()");
        arguments()
                .forEach(
                        stringArgument -> {
                            EXPECTED.add("beforeAll(" + stringArgument + ")");
                            EXPECTED.add("beforeEach(" + stringArgument + ")");
                            EXPECTED.add("test2(" + stringArgument + ")");
                            EXPECTED.add("afterEach(" + stringArgument + ")");
                            EXPECTED.add("beforeEach(" + stringArgument + ")");
                            EXPECTED.add("test1(" + stringArgument + ")");
                            EXPECTED.add("afterEach(" + stringArgument + ")");
                            EXPECTED.add("afterAll2(" + stringArgument + ")");
                            EXPECTED.add("afterAll(" + stringArgument + ")");
                        });
        EXPECTED.add("conclude()");
    }

    @TestEngine.Argument public Argument<String> argument;

    @TestEngine.ArgumentSupplier
    public static Stream<Argument<String>> arguments() {
        Collection<Argument<String>> collection = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            collection.add(Argument.ofString("StringArgument " + i));
        }
        return collection.stream();
    }

    @TestEngine.Prepare
    public void prepare() {
        System.out.println("prepare()");
        ACTUAL.add("prepare()");
    }

    @TestEngine.Prepare
    @TestEngine.Order(order = 0)
    public void prepare2() {
        System.out.println("prepare2()");
        ACTUAL.add("prepare2()");
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
        ACTUAL.add("beforeAll(" + argument + ")");
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach()");
        ACTUAL.add("beforeEach(" + argument + ")");
    }

    @TestEngine.Test
    @TestEngine.Order(order = 1)
    public void test1() {
        System.out.println("test1(" + argument + ")");
        ACTUAL.add("test1(" + argument + ")");
    }

    @TestEngine.Test
    @TestEngine.Order(order = 0)
    public void test2() {
        System.out.println("test2(" + argument + ")");
        ACTUAL.add("test2(" + argument + ")");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
        ACTUAL.add("afterEach(" + argument + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
        ACTUAL.add("afterAll(" + argument + ")");
    }

    @TestEngine.AfterAll
    @TestEngine.Order(order = 0)
    public void afterAll2() {
        System.out.println("afterAll2()");
        ACTUAL.add("afterAll2(" + argument + ")");
        assertThat(argument).isNotNull();
    }

    @TestEngine.Conclude
    public void conclude() {
        System.out.println("conclude()");
        ACTUAL.add("conclude()");
        assertThat(ACTUAL).isEqualTo(EXPECTED);
    }
}
