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

public class RandomGeneratorLongTest extends RandomGeneratorTest {

    @TestEngine.Test
    public void testLongs() {
        testLong(0, 0);
        testLong(Long.MIN_VALUE, Long.MIN_VALUE);
        testLong(Long.MAX_VALUE, Long.MAX_VALUE);
        testLong(Long.MIN_VALUE, Long.MAX_VALUE);
        testLong(Long.MIN_VALUE, 0);
        testLong(0, Long.MAX_VALUE);
        testLong(-123, 456);
        testLongUntil(Long.MAX_VALUE - 1, Long.MAX_VALUE, Long.MAX_VALUE - 1, ITERATIONS);
        testLongUntil(Long.MAX_VALUE - 1, Long.MAX_VALUE, Long.MAX_VALUE, ITERATIONS);
    }
}
