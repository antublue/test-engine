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

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.support.RandomSupport;

public abstract class RandomGeneratorTest {

    public static final int ITERATIONS = 1000000;

    @TestEngine.ArgumentSupplier
    public static Stream<Argument<String>> arguments() {
        return Stream.of(Argument.ofString("----"));
    }

    protected void testInteger(int minimum, int maximum) {
        System.out.format("testInteger() minimum %s maximum %s", minimum, maximum).println();
        int random = RandomSupport.randomInt(minimum, maximum);
        System.out.println(
                format(
                        "testInteger() minimum %s maximum %s -> random %s",
                        minimum, maximum, random));
        assertThat(random).isBetween(minimum, maximum);
    }

    protected void testIntegerUntil(int minimum, int maximum, int until, int interations) {
        System.out.println(
                format(
                        "testIntegerUntil() minimum %s maximum %s until %s iterations %s",
                        minimum, maximum, until, interations));
        for (int i = 0; i < interations; i++) {
            int random;
            do {
                random = RandomSupport.randomInt(minimum, maximum);
            } while (random != until);
            assertThat(random).isEqualTo(until);
        }
    }

    protected void testLong(long minimum, long maximum) {
        System.out.format("testLong() minimum %s maximum %s", minimum, maximum).println();
        long random = RandomSupport.randomLong(minimum, maximum);
        System.out.println(
                format("testLong() minimum %s maximum %s -> random %s", minimum, maximum, random));
        assertThat(random).isBetween(minimum, maximum);
    }

    protected void testLongUntil(long minimum, long maximum, long until, int iterations) {
        System.out.println(
                format(
                        "testLongUntil() minimum %s maximum %s until %s iterations %s",
                        minimum, maximum, until, iterations));

        for (int i = 0; i < iterations; i++) {
            long random;
            do {
                random = RandomSupport.randomLong(minimum, maximum);
            } while (random != until);
            assertThat(random).isEqualTo(until);
        }
    }

    protected void testFloat(float minimum, float maximum) {
        System.out.format("testFloat() minimum %s maximum %s", minimum, maximum).println();
        float random = RandomSupport.randomFloat(minimum, maximum);
        System.out.println(
                format("testFloat() minimum %s maximum %s -> random %s", minimum, maximum, random));
        assertThat(random).isBetween(minimum, maximum);
    }

    protected void testFloatUntil(float minimum, float maximum, float until, int iterations) {
        System.out.println(
                format(
                        "testFloatUntil() minimum %s maximum %s until %s iterations %s",
                        minimum, maximum, until, iterations));
        for (int i = 0; i < iterations; i++) {
            float random;
            do {
                random = RandomSupport.randomFloat(minimum, maximum);
            } while (random != until);
            assertThat(random).isEqualTo(until);
        }
    }

    protected void testDouble(double minimum, double maximum) {
        System.out.format("testDouble() minimum %s maximum %s", minimum, maximum).println();
        double random = RandomSupport.randomDouble(minimum, maximum);
        System.out.println(
                format(
                        "testDouble() minimum %s maximum %s -> random %s",
                        minimum, maximum, random));
        assertThat(random).isBetween(minimum, maximum);
    }

    protected void testDoubleUntil(double minimum, double maximum, double until, int iterations) {
        System.out.println(
                format(
                        "testDoubleUntil() minimum %s maximum %s until %s iterations %s",
                        minimum, maximum, until, iterations));
        for (int i = 0; i < iterations; i++) {
            double random;
            do {
                random = RandomSupport.randomDouble(minimum, maximum);
            } while (random != until);
            assertThat(random).isEqualTo(until);
        }
    }

    protected void testBigInteger(String minimum, String maximum) {
        System.out.format("testBigInteger() minimum %s maximum %s", minimum, maximum).println();
        BigInteger random = RandomSupport.randomBigInteger(minimum, maximum);
        System.out.println(
                format(
                        "testBigInteger() minimum %s maximum %s -> random %s",
                        minimum, maximum, random));
        assertThat(random).isBetween(new BigInteger(minimum), new BigInteger(maximum));
    }

    protected void testBigIntegerUntil(
            String minimum, String maximum, String until, int iterations) {
        System.out.println(
                format(
                        "testBigIntegerUntil() minimum %s maximum %s until %s iterations %s",
                        minimum, maximum, until, iterations));
        BigInteger untilBigInteger = new BigInteger(until);
        for (int i = 0; i < iterations; i++) {
            BigInteger random;
            do {
                random = RandomSupport.randomBigInteger(minimum, maximum);
            } while (untilBigInteger.compareTo(random) != 0);
            assertThat(random.toString()).isEqualTo(untilBigInteger.toString());
        }
    }

    protected void testBigDecimal(String minimum, String maximum) {
        System.out.format("testBigDecimal() minimum %s maximum %s", minimum, maximum).println();
        BigDecimal random = RandomSupport.randomBigDecimal(minimum, maximum);
        System.out.println(
                format(
                        "testBigDecimal() minimum %s maximum %s -> random %s",
                        minimum, maximum, random));
        assertThat(random).isBetween(new BigDecimal(minimum), new BigDecimal(maximum));
    }

    protected void testBigDecimalUntil(
            String minimum, String maximum, String until, int iterations) {
        System.out.println(
                format(
                        "testBigDecimalUntil() minimum %s maximum %s until %s iterations %s",
                        minimum, maximum, until, iterations));
        BigDecimal untilBigDecimal = new BigDecimal(until);
        for (int i = 0; i < iterations; i++) {
            BigDecimal random;
            do {
                random = RandomSupport.randomBigDecimal(minimum, maximum);
            } while (untilBigDecimal.compareTo(random) != 0);
            assertThat(random.toPlainString()).isEqualTo(untilBigDecimal.toPlainString());
        }
    }
}
