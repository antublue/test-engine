/*
 * Copyright (C) 2024 The AntuBLUE test-engine project authors
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
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.support.RandomAnnotationSupport;

public class RandomSupportTest {

    @TestEngine.Argument public Argument<String> argument;

    @TestEngine.Random.Integer public Integer randomInteger;

    @TestEngine.ArgumentSupplier
    public static Stream<Argument<String>> arguments() {
        Collection<Argument<String>> collection = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            collection.add(Argument.ofString("StringArgument " + i));
        }
        return collection.stream();
    }

    @TestEngine.Prepare
    public void prepare() {
        System.out.println("prepare()");
    }

    @TestEngine.BeforeAll
    public void beforeAll() throws Throwable {
        System.out.println("beforeAll(" + argument + ")");
        System.out.println("randomInteger = [" + randomInteger + "]");
        assertThat(argument).isNotNull();
        assertThat(randomInteger).isNotNull();

        RandomAnnotationSupport.setRandomFields(this);

        System.out.println("randomInteger = [" + randomInteger + "]");
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach(" + argument + ")");
        assertThat(argument).isNotNull();
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + argument + ")");
        assertThat(argument).isNotNull();
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + argument + ")");
        assertThat(argument).isNotNull();
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach(" + argument + ")");
        assertThat(argument).isNotNull();
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll(" + argument + ")");
        System.out.println("randomInteger = [" + randomInteger + "]");
        assertThat(argument).isNotNull();
        assertThat(randomInteger).isNotNull();
    }

    @TestEngine.Conclude
    public void conclude() {
        System.out.println("conclude()");
    }
}
