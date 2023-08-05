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
public class DebugLifecycleTest {

    private static String exceptionIn = null;

    private static final List<String> EXPECTED_STATES = new ArrayList<>();

    private static final List<String> ACTUAL_STATES = new ArrayList<>();

    static {
        EXPECTED_STATES.add("prepare");
        EXPECTED_STATES.add("beforeAll");
        EXPECTED_STATES.add("beforeEach");
        if (!"test1".equals(exceptionIn)) {
            EXPECTED_STATES.add("test1");
        }
        EXPECTED_STATES.add("afterEach");
        EXPECTED_STATES.add("beforeEach");
        if (!"test2".equals(exceptionIn)) {
            EXPECTED_STATES.add("test2");
        }
        EXPECTED_STATES.add("afterEach");
        EXPECTED_STATES.add("afterAll");
        EXPECTED_STATES.add("conclude");
    }

    @TestEngine.Argument protected StringArgument stringArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<StringArgument> arguments() {
        Collection<StringArgument> collection = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            collection.add(StringArgument.of("StringArgument " + i));
        }
        return collection.stream();
    }

    @TestEngine.Prepare
    public void prepare() {
        System.out.println("prepare()");
        assertThat(stringArgument).isNull();
        ACTUAL_STATES.add("prepare");
        assertThat(ACTUAL_STATES.size()).isEqualTo(1);
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll(" + stringArgument + ")");
        assertThat(stringArgument).isNotNull();
        ACTUAL_STATES.add("beforeAll");
        assertThat(ACTUAL_STATES.size()).isEqualTo(2);
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach(" + stringArgument + ")");
        assertThat(stringArgument).isNotNull();
        ACTUAL_STATES.add("beforeEach");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + stringArgument + ")");
        if ("test1".equals(exceptionIn)) {
            throw new RuntimeException("Forced exception");
        }
        assertThat(stringArgument).isNotNull();
        ACTUAL_STATES.add("test1");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + stringArgument + ")");
        assertThat(stringArgument).isNotNull();
        ACTUAL_STATES.add("test2");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach(" + stringArgument + ")");
        assertThat(stringArgument).isNotNull();
        ACTUAL_STATES.add("afterEach");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll(" + stringArgument + ")");
        assertThat(stringArgument).isNotNull();
        ACTUAL_STATES.add("afterAll");
    }

    @TestEngine.Conclude
    public void conclude() {
        System.out.println("conclude()");
        assertThat(stringArgument).isNull();
        ACTUAL_STATES.add("conclude");
        assertThat(ACTUAL_STATES).isEqualTo(EXPECTED_STATES);
    }
}
