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

import java.lang.reflect.Method;
import java.util.List;
import org.antublue.test.engine.api.MethodProcessor;
import org.antublue.test.engine.api.TestEngine;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

public class ExtendedTest extends BaseTest {

    @TestEngine.MethodProcessorSupplier
    public static MethodProcessor methodProcessor() {
        return new TestMethodProcessor();
    }

    @BeforeEach
    @Order(2)
    @TestEngine.BeforeEach
    @TestEngine.Order(order = 2)
    public void beforeEach2() {
        System.out.format("    %s beforeEach2()", ExtendedTest.class.getName()).println();
    }

    @Test
    @Order(20)
    @TestEngine.Test
    @TestEngine.Order(order = 20)
    public void test2() {
        System.out.format("%s test2()", ExtendedTest.class.getName()).println();
    }

    @Test
    @Order(30)
    @TestEngine.Test
    @TestEngine.Order(order = 30)
    public void test3() {
        System.out.format("%s test3()", ExtendedTest.class.getName()).println();
    }

    @AfterEach
    @TestEngine.AfterEach
    public void afterEach2() {
        System.out.format("    %s afterEach2()", ExtendedTest.class.getName()).println();
    }

    @AfterEach
    @TestEngine.AfterEach
    public void afterEach4() {
        System.out.format("    %s afterEach4()", ExtendedTest.class.getName()).println();
    }

    private static class TestMethodProcessor implements MethodProcessor {

        @Override
        public void process(Class<?> testClass, List<Method> testMethods) {
            for (Method testMethod : testMethods) {
                System.out.println(String.format("testMethod [%s]", testMethod));
            }
        }
    }
}
