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

package org.antublue.test.engine.testing.inheritance;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.IntegerArgument;

// @TestEngine.BaseClass
public abstract class BaseTest {

    public static final List<String> EXPECTED = new ArrayList<>();
    protected final List<String> actual = new ArrayList<>();

    static {
        EXPECTED.add("b/prepare()");
        EXPECTED.add("b/prepare3()");
        EXPECTED.add("prepare2()");
        arguments()
                .forEach(
                        stringArgument -> {
                            EXPECTED.add("b/beforeAll(" + stringArgument + ")");
                            EXPECTED.add("beforeAll2(" + stringArgument + ")");
                            EXPECTED.add("b/beforeEach(" + stringArgument + ")");
                            EXPECTED.add("beforeEach2(" + stringArgument + ")");
                            EXPECTED.add("b/testA(" + stringArgument + ")");
                            EXPECTED.add("afterEach2(" + stringArgument + ")");
                            EXPECTED.add("b/afterEach(" + stringArgument + ")");
                            EXPECTED.add("b/beforeEach(" + stringArgument + ")");
                            EXPECTED.add("beforeEach2(" + stringArgument + ")");
                            EXPECTED.add("testB(" + stringArgument + ")");
                            EXPECTED.add("afterEach2(" + stringArgument + ")");
                            EXPECTED.add("b/afterEach(" + stringArgument + ")");
                            EXPECTED.add("afterAll2(" + stringArgument + ")");
                            EXPECTED.add("b/afterAll(" + stringArgument + ")");
                        });
        EXPECTED.add("conclude2()");
        EXPECTED.add("b/conclude3()");
        EXPECTED.add("b/conclude()");
    }

    @TestEngine.Argument protected IntegerArgument integerArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<IntegerArgument> arguments() {
        Collection<IntegerArgument> collection = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            collection.add(IntegerArgument.of(i));
        }
        return collection.stream();
    }

    @TestEngine.Prepare
    public void prepare() {
        System.out.println("b/prepare()");
        assertThat(integerArgument).isNull();
        actual.add("b/prepare()");
    }

    @TestEngine.Prepare
    public void prepare3() {
        System.out.println("b/prepare3()");
        assertThat(integerArgument).isNull();
        actual.add("b/prepare3()");
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("b/beforeAll(" + integerArgument + ")");
        assertThat(integerArgument).isNotNull();
        actual.add("b/beforeAll(" + integerArgument + ")");
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("b/beforeEach(" + integerArgument + ")");
        assertThat(integerArgument).isNotNull();
        actual.add("b/beforeEach(" + integerArgument + ")");
    }

    @TestEngine.Test
    public void testA() {
        System.out.println("b/testA(" + integerArgument + ")");
        assertThat(integerArgument).isNotNull();
        actual.add("b/testA(" + integerArgument + ")");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("b/afterEach(" + integerArgument + ")");
        assertThat(integerArgument).isNotNull();
        actual.add("b/afterEach(" + integerArgument + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("b/afterAll(" + integerArgument + ")");
        assertThat(integerArgument).isNotNull();
        actual.add("b/afterAll(" + integerArgument + ")");
    }

    @TestEngine.Conclude
    public void conclude() {
        System.out.println("b/conclude()");
        assertThat(integerArgument).isNull();
        actual.add("b/conclude()");
    }

    @TestEngine.Conclude
    public void conclude3() {
        System.out.println("b/conclude3()");
        assertThat(integerArgument).isNull();
        actual.add("b/conclude3()");
    }
}
