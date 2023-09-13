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

public class BaseTest {

    @TestEngine.BeforeEach
    @TestEngine.Order(order = 1)
    public void beforeEach() {
        System.out.format("%s beforeEach()", BaseTest.class.getName()).println();
    }

    @TestEngine.BeforeEach
    @TestEngine.Order(order = 3)
    public void beforeEach3() {
        System.out.format("%s beforeEach3()", BaseTest.class.getName()).println();
    }

    @TestEngine.Test
    @TestEngine.Order(order = 1)
    public void test4() {
        System.out.format("%s test4()", BaseTest.class.getName()).println();
    }

    @TestEngine.Test
    @TestEngine.Order(order = 10000)
    public void test5() {
        System.out.format("%s test4()", BaseTest.class.getName()).println();
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.format("%s afterEach()", BaseTest.class.getName()).println();
    }

    @TestEngine.AfterEach
    public void afterEach3() {
        System.out.format("%s afterEach3()", BaseTest.class.getName()).println();
    }
}
