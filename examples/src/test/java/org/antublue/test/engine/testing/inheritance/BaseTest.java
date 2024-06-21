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
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;

public abstract class BaseTest {

    public final List<String> EXPECTED = new ArrayList<>();
    public final List<String> ACTUAL = new ArrayList<>();

    public static Stream<Argument<Integer>> arguments() {
        Collection<Argument<Integer>> collection = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            collection.add(Argument.ofInt(i));
        }
        return collection.stream();
    }

    @TestEngine.Prepare
    public void prepare() {
        System.out.println("b/prepare()");
        ACTUAL.add("b/prepare()");
    }

    @TestEngine.Prepare
    public void prepare3() {
        System.out.println("b/prepare3()");
        ACTUAL.add("b/prepare3()");
    }

    @TestEngine.BeforeAll
    public void beforeAll(Argument<Integer> argument) {
        System.out.println("b/beforeAll(" + argument + ")");
        assertThat(argument).isNotNull();
        ACTUAL.add("b/beforeAll(" + argument + ")");
    }

    @TestEngine.BeforeEach
    public void beforeEach(Argument<Integer> argument) {
        System.out.println("b/beforeEach(" + argument + ")");
        assertThat(argument).isNotNull();
        ACTUAL.add("b/beforeEach(" + argument + ")");
    }

    @TestEngine.Test
    public void testA(Argument<Integer> argument) {
        System.out.println("b/testA(" + argument + ")");
        assertThat(argument).isNotNull();
        ACTUAL.add("b/testA(" + argument + ")");
    }

    @TestEngine.AfterEach
    public void afterEach(Argument<Integer> argument) {
        System.out.println("b/afterEach(" + argument + ")");
        assertThat(argument).isNotNull();
        ACTUAL.add("b/afterEach(" + argument + ")");
    }

    @TestEngine.AfterAll
    public void afterAll(Argument<Integer> argument) {
        System.out.println("b/afterAll(" + argument + ")");
        assertThat(argument).isNotNull();
        ACTUAL.add("b/afterAll(" + argument + ")");
    }

    @TestEngine.Conclude
    public void conclude() {
        System.out.println("b/conclude()");
        ACTUAL.add("b/conclude()");
    }

    @TestEngine.Conclude
    public void conclude3() {
        System.out.println("b/conclude3()");
        ACTUAL.add("b/conclude3()");

        validate();
    }

    protected void validate() {
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
