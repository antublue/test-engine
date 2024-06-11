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

package example;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Named;
import org.antublue.test.engine.api.TestEngine;

/** Example test */
public class SimpleTest5 {

    @TestEngine.Argument protected String argument;

    @TestEngine.Argument protected Named<String> namedArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<String> arguments() {
        Collection<String> collection = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            collection.add("string " + i);
        }
        return collection.stream();
    }

    @TestEngine.Prepare
    public void prepare() {
        System.out.println("prepare()");
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        assertThat(argument).isNotNull();
        assertThat(namedArgument).isNotNull();
        assertThat(namedArgument.getPayload()).isNotNull();
        assertThat(namedArgument.getPayload().equals(argument)).isNotNull();
        System.out.println("beforeAll(" + argument + ")");
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        assertThat(argument).isNotNull();
        assertThat(namedArgument).isNotNull();
        assertThat(namedArgument.getPayload()).isNotNull();
        assertThat(namedArgument.getPayload().equals(argument)).isNotNull();
        System.out.println("beforeEach(" + argument + ")");
    }

    @TestEngine.Test
    public void test1() {
        assertThat(argument).isNotNull();
        assertThat(namedArgument).isNotNull();
        assertThat(namedArgument.getPayload()).isNotNull();
        assertThat(namedArgument.getPayload().equals(argument)).isNotNull();
        System.out.println("test1(" + argument + ")");
    }

    @TestEngine.Test
    public void test2() {
        assertThat(argument).isNotNull();
        assertThat(namedArgument).isNotNull();
        assertThat(namedArgument.getPayload()).isNotNull();
        assertThat(namedArgument.getPayload().equals(argument)).isNotNull();
        System.out.println("test2(" + argument + ")");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        assertThat(argument).isNotNull();
        assertThat(namedArgument).isNotNull();
        assertThat(namedArgument.getPayload()).isNotNull();
        assertThat(namedArgument.getPayload().equals(argument)).isNotNull();
        System.out.println("afterEach(" + argument + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        assertThat(argument).isNotNull();
        assertThat(namedArgument).isNotNull();
        assertThat(namedArgument.getPayload()).isNotNull();
        assertThat(namedArgument.getPayload().equals(argument)).isNotNull();
        System.out.println("afterAll(" + argument + ")");
    }

    @TestEngine.Conclude
    public void conclude() {
        System.out.println("conclude()");
    }
}
