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

public class RandomUtilsBigDecimalTest extends RandomUtilsTest {

    @TestEngine.Test
    public void testBigDecimals() {
        testBigDecimal("0", "0");
        testBigDecimal(toString(-Double.MAX_VALUE), toString(Double.MAX_VALUE));
        testBigDecimal(toString(-Double.MAX_VALUE), toString(-Double.MAX_VALUE));
        testBigDecimal(toString(Double.MAX_VALUE), toString(Double.MAX_VALUE));
        testBigDecimal(toString(-Double.MAX_VALUE), "0");
        testBigDecimal("0", toString(Double.MAX_VALUE));
        testBigDecimal("-123.08", "456.08");
        testBigDecimal("-123", "456");
        testBigDecimalUntil(
                toString(Double.MAX_VALUE - 1),
                toString(Double.MAX_VALUE),
                toString(Double.MAX_VALUE - 1),
                ITERATIONS);
        testBigDecimalUntil(
                toString(Double.MAX_VALUE - 1),
                toString(Double.MAX_VALUE),
                toString(Double.MAX_VALUE),
                ITERATIONS);
        testBigDecimalUntil(
                toString(Double.MAX_VALUE),
                toString(Double.MAX_VALUE - 1),
                toString(Double.MAX_VALUE - 1),
                ITERATIONS);
        testBigDecimalUntil(
                toString(Double.MAX_VALUE),
                toString(Double.MAX_VALUE - 1),
                toString(Double.MAX_VALUE),
                ITERATIONS);

        assertThatThrownBy(() -> testBigInteger("", "")).isInstanceOf(NumberFormatException.class);
        assertThatThrownBy(() -> testBigInteger("-", "456.1"))
                .isInstanceOf(NumberFormatException.class);
        assertThatThrownBy(() -> testBigInteger("1", "-"))
                .isInstanceOf(NumberFormatException.class);
        assertThatThrownBy(() -> testBigInteger("-1", ""))
                .isInstanceOf(NumberFormatException.class);
    }

    private static String toString(double value) {
        return String.format("%s", value);
    }
}
