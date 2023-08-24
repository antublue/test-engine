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
import static org.assertj.core.api.Fail.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.IntegerArgument;

@TestEngine.BaseClass
public class BaseTest {

    private static final List<String> EXPECTED = new ArrayList<>();
    protected final List<String> actual = new ArrayList<>();

    static {
        EXPECTED.add("prepare()");
        EXPECTED.add("prepare2()");

        arguments()
                .forEach(
                        stringArgument -> {
                            EXPECTED.add("beforeAll(" + stringArgument + ")");
                            EXPECTED.add("beforeAll2(" + stringArgument + ")");
                            EXPECTED.add("beforeEach(" + stringArgument + ")");
                            EXPECTED.add("beforeEach2(" + stringArgument + ")");
                            EXPECTED.add("testA(" + stringArgument + ")");
                            EXPECTED.add("afterEach2(" + stringArgument + ")");
                            EXPECTED.add("afterEach(" + stringArgument + ")");
                            EXPECTED.add("beforeEach(" + stringArgument + ")");
                            EXPECTED.add("beforeEach2(" + stringArgument + ")");
                            EXPECTED.add("testB(" + stringArgument + ")");
                            EXPECTED.add("afterEach2(" + stringArgument + ")");
                            EXPECTED.add("afterEach(" + stringArgument + ")");
                            EXPECTED.add("afterAll2(" + stringArgument + ")");
                            EXPECTED.add("afterAll(" + stringArgument + ")");
                        });

        EXPECTED.add("conclude2()");
        EXPECTED.add("conclude()");
    }

    @TestEngine.Argument protected IntegerArgument integerArgument;

    @TestEngine.ArgumentSupplier
    protected static Stream<IntegerArgument> arguments() {
        Collection<IntegerArgument> collection = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            collection.add(IntegerArgument.of(i));
        }
        return collection.stream();
    }

    @TestEngine.Prepare
    public void prepare() {
        System.out.println("prepare()");
        assertThat(integerArgument).isNull();
        actual.add("prepare()");
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll(" + integerArgument + ")");
        assertThat(integerArgument).isNotNull();
        actual.add("beforeAll(" + integerArgument + ")");
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach(" + integerArgument + ")");
        assertThat(integerArgument).isNotNull();
        actual.add("beforeEach(" + integerArgument + ")");
    }

    @TestEngine.Test
    public void testA() {
        System.out.println("testA(" + integerArgument + ")");
        assertThat(integerArgument).isNotNull();
        actual.add("testA(" + integerArgument + ")");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach(" + integerArgument + ")");
        assertThat(integerArgument).isNotNull();
        actual.add("afterEach(" + integerArgument + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll(" + integerArgument + ")");
        assertThat(integerArgument).isNotNull();
        actual.add("afterAll(" + integerArgument + ")");
    }

    @TestEngine.Conclude
    public void conclude() {
        System.out.println("conclude()");
        assertThat(integerArgument).isNull();
        actual.add("conclude()");
        assertThat(actual.size()).isEqualTo(EXPECTED.size());
        for (int i = 0; i < actual.size(); i++) {
            if (!actual.get(i).equals(EXPECTED.get(i))) {
                fail(
                        String.format(
                                "index [%d] actual [%s] expected [%s]",
                                i, actual.get(i), EXPECTED.get(i)));
            }
        }
    }
}
