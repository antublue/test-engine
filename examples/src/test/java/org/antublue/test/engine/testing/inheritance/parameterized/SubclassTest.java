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

package org.antublue.test.engine.testing.inheritance.parameterized;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.antublue.test.engine.api.TestEngine;

/** Example test */
public class SubclassTest extends BaseTest {

    @TestEngine.Prepare
    public void prepare2() {
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

        System.out.println("prepare2()");
        ACTUAL.add("prepare2()");
    }

    @TestEngine.BeforeAll
    public void beforeAll2() {
        System.out.println("beforeAll2(" + argument + ")");
        assertThat(argument).isNotNull();
        ACTUAL.add("beforeAll2(" + argument + ")");
    }

    @TestEngine.BeforeEach
    public void beforeEach2() {
        System.out.println("beforeEach2(" + argument + ")");
        assertThat(argument).isNotNull();
        ACTUAL.add("beforeEach2(" + argument + ")");
    }

    @TestEngine.Test
    public void testB() {
        System.out.println("testB(" + argument + ")");
        assertThat(argument).isNotNull();
        ACTUAL.add("testB(" + argument + ")");
    }

    @TestEngine.AfterEach
    public void afterEach2() {
        System.out.println("afterEach2(" + argument + ")");
        assertThat(argument).isNotNull();
        ACTUAL.add("afterEach2(" + argument + ")");
    }

    @TestEngine.AfterAll
    public void afterAll2() {
        System.out.println("afterAll2(" + argument + ")");
        assertThat(argument).isNotNull();
        ACTUAL.add("afterAll2(" + argument + ")");
    }

    @TestEngine.Conclude
    public void conclude2() {
        System.out.println("conclude2()");
        ACTUAL.add("conclude2()");
    }
}
