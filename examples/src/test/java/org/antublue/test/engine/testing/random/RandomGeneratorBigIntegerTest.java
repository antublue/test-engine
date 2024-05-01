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

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.antublue.test.engine.api.TestEngine;

public class RandomGeneratorBigIntegerTest extends RandomGeneratorTest {

    @TestEngine.Test
    public void testBigIntegers() {
        testBigInteger("0", "0");
        testBigInteger(toString(Long.MIN_VALUE), toString(Long.MAX_VALUE));
        testBigInteger(toString(Long.MIN_VALUE), toString(Long.MIN_VALUE));
        testBigInteger(toString(Long.MAX_VALUE), toString(Long.MAX_VALUE));
        testBigInteger(toString(Long.MIN_VALUE), "0");
        testBigInteger("0", toString(Long.MAX_VALUE));
        testBigInteger("-123", "456");
        testBigIntegerUntil(
                toString(Long.MAX_VALUE - 1),
                toString(Long.MAX_VALUE),
                toString(Long.MAX_VALUE - 1),
                ITERATIONS);
        testBigIntegerUntil(
                toString(Long.MAX_VALUE - 1),
                toString(Long.MAX_VALUE),
                toString(Long.MAX_VALUE),
                ITERATIONS);
        testBigIntegerUntil(
                toString(Long.MAX_VALUE),
                toString(Long.MAX_VALUE - 1),
                toString(Long.MAX_VALUE - 1),
                ITERATIONS);
        testBigIntegerUntil(
                toString(Long.MAX_VALUE),
                toString(Long.MAX_VALUE - 1),
                toString(Long.MAX_VALUE),
                ITERATIONS);

        assertThatThrownBy(() -> testBigInteger("", "")).isInstanceOf(NumberFormatException.class);
        assertThatThrownBy(() -> testBigInteger("-123.9", "456.1"))
                .isInstanceOf(NumberFormatException.class);
        assertThatThrownBy(() -> testBigInteger("-", "456.1"))
                .isInstanceOf(NumberFormatException.class);
        assertThatThrownBy(() -> testBigInteger("-1", "456.1"))
                .isInstanceOf(NumberFormatException.class);
        assertThatThrownBy(() -> testBigInteger("-1", ""))
                .isInstanceOf(NumberFormatException.class);
        assertThatThrownBy(() -> testBigInteger("-1", ".1"))
                .isInstanceOf(NumberFormatException.class);
    }

    private static String toString(long value) {
        return String.format("%s", value);
    }
}
