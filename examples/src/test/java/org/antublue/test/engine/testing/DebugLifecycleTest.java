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
import java.util.stream.Stream;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.support.NamedString;

/** Example test */
public class DebugLifecycleTest {

    // Set exceptionIn to match the method name to simulate an exception
    private static final String exceptionIn = "";

    @TestEngine.Argument protected NamedString argument;

    @TestEngine.ArgumentSupplier
    public static Stream<NamedString> arguments() {
        Collection<NamedString> collection = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            collection.add(NamedString.of("StringArgument " + i));
        }
        return collection.stream();
    }

    @TestEngine.Prepare
    public void prepare() {
        System.out.println("prepare()");
        assertThat(argument).isNull();
        if ("prepare()".equals(exceptionIn)) {
            throw new RuntimeException("Exception in prepare()");
        }
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll(" + argument + ")");
        assertThat(argument).isNotNull();
        if ("beforeAll()".equals(exceptionIn)) {
            throw new RuntimeException("Exception in beforeAll()");
        }
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach(" + argument + ")");
        assertThat(argument).isNotNull();
        if ("beforeEach()".equals(exceptionIn)) {
            throw new RuntimeException("Exception in beforeEach()");
        }
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + argument + ")");
        assertThat(argument).isNotNull();
        if ("test1()".equals(exceptionIn)) {
            throw new RuntimeException("Exception in test1()");
        }
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + argument + ")");
        assertThat(argument).isNotNull();
        if ("test2()".equals(exceptionIn)) {
            throw new RuntimeException("Exception in test2()");
        }
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach(" + argument + ")");
        assertThat(argument).isNotNull();
        if ("afterEach()".equals(exceptionIn)) {
            throw new RuntimeException("Exception in afterEach()");
        }
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll(" + argument + ")");
        assertThat(argument).isNotNull();
        if ("afterAll()".equals(exceptionIn)) {
            throw new RuntimeException("Exception in afterAll()");
        }
    }

    @TestEngine.Conclude
    public void conclude() {
        System.out.println("conclude()");
        assertThat(argument).isNull();
        if ("conclude()".equals(exceptionIn)) {
            throw new RuntimeException("Exception in conclude()");
        }
    }
}
