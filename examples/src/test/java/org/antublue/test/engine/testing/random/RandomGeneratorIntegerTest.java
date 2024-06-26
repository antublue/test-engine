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

package org.antublue.test.engine.testing.random;

import org.antublue.test.engine.api.TestEngine;

public class RandomGeneratorIntegerTest extends RandomGeneratorTest {

    @TestEngine.Test
    public void testIntegers() {
        testInteger(0, 0);
        testInteger(Integer.MIN_VALUE, Integer.MIN_VALUE);
        testInteger(Integer.MAX_VALUE, Integer.MAX_VALUE);
        testInteger(Integer.MIN_VALUE, Integer.MAX_VALUE);
        testInteger(Integer.MIN_VALUE, 0);
        testInteger(0, Integer.MAX_VALUE);
        testInteger(-123, 456);
        testIntegerUntil(
                Integer.MAX_VALUE - 1, Integer.MAX_VALUE, Integer.MAX_VALUE - 1, ITERATIONS);
        testIntegerUntil(Integer.MAX_VALUE - 1, Integer.MAX_VALUE, Integer.MAX_VALUE, ITERATIONS);
        testIntegerUntil(
                Integer.MAX_VALUE, Integer.MAX_VALUE - 1, Integer.MAX_VALUE - 1, ITERATIONS);
        testIntegerUntil(Integer.MAX_VALUE, Integer.MAX_VALUE - 1, Integer.MAX_VALUE, ITERATIONS);
    }
}
