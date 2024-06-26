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

package example.inheritance;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;

public abstract class BaseTest {

    protected final List<String> EXPECTED = new ArrayList<>();
    protected final List<String> ACTUAL = new ArrayList<>();

    @TestEngine.Argument public Argument<Integer> privateArgumentInteger;

    @TestEngine.Argument public Argument<Integer> argument;

    @TestEngine.ArgumentSupplier
    public static Stream<Argument<Integer>> arguments() {
        Collection<Argument<Integer>> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add(Argument.ofInt(i));
        }
        return collection.stream();
    }

    @TestEngine.Prepare
    public void prepare() {
        System.out.println("prepare()");
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
        assertThat(privateArgumentInteger).isNotNull();
        assertThat(argument).isNotNull();
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach()");
        assertThat(privateArgumentInteger).isNotNull();
        assertThat(argument).isNotNull();
    }

    @TestEngine.Test
    public void testA() {
        System.out.println("testA()");
        assertThat(privateArgumentInteger).isNotNull();
        assertThat(argument).isNotNull();
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
        assertThat(privateArgumentInteger).isNotNull();
        assertThat(argument).isNotNull();
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
        assertThat(privateArgumentInteger).isNotNull();
        assertThat(argument).isNotNull();
    }

    @TestEngine.Conclude
    public void conclude() {
        System.out.println("conclude()");
    }
}
