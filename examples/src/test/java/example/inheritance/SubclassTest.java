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

import org.antublue.test.engine.api.TestEngine;

public class SubclassTest extends BaseTest {

    @TestEngine.Prepare
    public void prepare2() {
        System.out.println("  prepare2()");
        assertThat(argument).isNull();
    }

    @TestEngine.BeforeAll
    public void beforeAll2() {
        System.out.println("  beforeAll2()");
        assertThat(argument).isNotNull();
    }

    @TestEngine.BeforeEach
    public void beforeEach2() {
        System.out.println("  beforeEach2()");
        assertThat(argument).isNotNull();
    }

    @TestEngine.Test
    public void testB() {
        System.out.println("  testB()");
        assertThat(argument).isNotNull();
    }

    @TestEngine.AfterEach
    public void afterEach2() {
        System.out.println("  afterEach2()");
        assertThat(argument).isNotNull();
    }

    @TestEngine.AfterAll
    public void afterAll2() {
        System.out.println("  afterAll2()");
        assertThat(argument).isNotNull();
    }

    @TestEngine.Conclude
    public void conclude2() {
        System.out.println("  conclude2()");
        assertThat(argument).isNull();
    }
}
