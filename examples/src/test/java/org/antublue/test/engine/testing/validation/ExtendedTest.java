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

package org.antublue.test.engine.testing.validation;

import org.antublue.test.engine.api.TestEngine;

public class ExtendedTest extends BaseTest {

    @TestEngine.BeforeEach
    @TestEngine.Order(order = 2)
    public void beforeEach2() {
        System.out.format("    %s beforeEach2()", ExtendedTest.class.getName()).println();
    }

    @TestEngine.Test
    @TestEngine.Order(order = 20)
    public void test2() {
        System.out.format("%s test2()", ExtendedTest.class.getName()).println();
    }

    @TestEngine.Test
    @TestEngine.Order(order = 30)
    public void test3() {
        System.out.format("%s test3()", ExtendedTest.class.getName()).println();
    }

    @TestEngine.AfterEach
    public void afterEach2() {
        System.out.format("    %s afterEach2()", ExtendedTest.class.getName()).println();
    }
}
