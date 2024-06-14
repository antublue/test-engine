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

import org.antublue.test.engine.api.Named;
import org.antublue.test.engine.api.TestEngine;

/** Example test */
public class SubclassTest extends BaseTest {

    @TestEngine.Prepare
    public static void prepare2() {
        System.out.println("prepare2()");
        ACTUAL.add("prepare2()");
    }

    @TestEngine.BeforeAll
    public void beforeAll2(Named<Integer> argument) {
        System.out.println("beforeAll2(" + argument + ")");
        assertThat(argument).isNotNull();
        ACTUAL.add("beforeAll2(" + argument + ")");
    }

    @TestEngine.BeforeEach
    public void beforeEach2(Named<Integer> argument) {
        System.out.println("beforeEach2(" + argument + ")");
        assertThat(argument).isNotNull();
        ACTUAL.add("beforeEach2(" + argument + ")");
    }

    @TestEngine.Test
    public void testB(Named<Integer> argument) {
        System.out.println("testB(" + argument + ")");
        assertThat(argument).isNotNull();
        ACTUAL.add("testB(" + argument + ")");
    }

    @TestEngine.AfterEach
    public void afterEach2(Named<Integer> argument) {
        System.out.println("afterEach2(" + argument + ")");
        assertThat(argument).isNotNull();
        ACTUAL.add("afterEach2(" + argument + ")");
    }

    @TestEngine.AfterAll
    public void afterAll2(Named<Integer> argument) {
        System.out.println("afterAll2(" + argument + ")");
        assertThat(argument).isNotNull();
        ACTUAL.add("afterAll2(" + argument + ")");
    }

    @TestEngine.Conclude
    public void conclude2() {
        System.out.println("conclude2()");
        ACTUAL.add("conclude2()");
    }

    public void validate() {
        assertThat(ACTUAL.size()).isEqualTo(EXPECTED.size());

        for (int i = 0; i < ACTUAL.size(); i++) {
            if (!ACTUAL.get(i).equals(EXPECTED.get(i))) {
                System.out.println(
                        "equal ["
                                + ACTUAL.get(i).equals(EXPECTED.get(i))
                                + "] index "
                                + i
                                + " actual ["
                                + ACTUAL.get(i)
                                + "] expected ["
                                + EXPECTED.get(i)
                                + "]");
            }
        }

        assertThat(ACTUAL).isEqualTo(EXPECTED);
    }
}
