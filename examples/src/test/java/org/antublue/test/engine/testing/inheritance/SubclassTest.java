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
import java.util.stream.Stream;
import org.antublue.test.engine.api.Extension;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.testing.Validation;
import org.antublue.test.engine.testing.ValidationExtension;

/** Example test */
public class SubclassTest extends BaseTest implements Validation {

    @TestEngine.ExtensionSupplier
    public static Stream<Extension> extensions() {
        Collection<Extension> collection = new ArrayList<>();
        collection.add(new ValidationExtension());
        return collection.stream();
    }

    @TestEngine.Prepare
    public void prepare2() {
        System.out.println("prepare2()");
        assertThat(integerArgument).isNull();
        actual.add("prepare2()");
    }

    @TestEngine.BeforeAll
    public void beforeAll2() {
        System.out.println("beforeAll2(" + integerArgument + ")");
        assertThat(integerArgument).isNotNull();
        actual.add("beforeAll2(" + integerArgument + ")");
    }

    @TestEngine.BeforeEach
    public void beforeEach2() {
        System.out.println("beforeEach2(" + integerArgument + ")");
        assertThat(integerArgument).isNotNull();
        actual.add("beforeEach2(" + integerArgument + ")");
    }

    @TestEngine.Test
    public void testB() {
        System.out.println("testB(" + integerArgument + ")");
        assertThat(integerArgument).isNotNull();
        actual.add("testB(" + integerArgument + ")");
    }

    @TestEngine.AfterEach
    public void afterEach2() {
        System.out.println("afterEach2(" + integerArgument + ")");
        assertThat(integerArgument).isNotNull();
        actual.add("afterEach2(" + integerArgument + ")");
    }

    @TestEngine.AfterAll
    public void afterAll2() {
        System.out.println("afterAll2(" + integerArgument + ")");
        assertThat(integerArgument).isNotNull();
        actual.add("afterAll2(" + integerArgument + ")");
    }

    @TestEngine.Conclude
    public void conclude2() {
        System.out.println("conclude2()");
        assertThat(integerArgument).isNull();
        actual.add("conclude2()");
    }

    @Override
    public void validate() {
        assertThat(actual.size()).isEqualTo(EXPECTED.size());

        for (int i = 0; i < actual.size(); i++) {
            if (!actual.get(i).equals(EXPECTED.get(i))) {
                System.out.println(
                        "equal ["
                                + actual.get(i).equals(EXPECTED.get(i))
                                + "] index "
                                + i
                                + " actual ["
                                + actual.get(i)
                                + "] expected ["
                                + EXPECTED.get(i)
                                + "]");
            }
        }

        assertThat(actual).isEqualTo(EXPECTED);
    }
}
