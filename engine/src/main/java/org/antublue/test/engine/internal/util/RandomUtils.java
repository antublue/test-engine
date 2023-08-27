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

package org.antublue.test.engine.internal.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Class to implement RandomUtils */
@SuppressWarnings("PMD.NPathComplexity")
public class RandomUtils {

    /** Constructor */
    private RandomUtils() {
        // DO NOTHING
    }

    /**
     * Method to get a random UUID
     *
     * @return a random UUID
     */
    public static UUID nextUUID() {
        return UUID.randomUUID();
    }

    /**
     * Method to get a random boolean
     *
     * @return a random boolean
     */
    public static boolean nextBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    /**
     * Method to get a random integer in a range (minimum and maximum are both inclusive)
     *
     * @param minimum minimum
     * @param maximum maximum
     * @return a random integer
     */
    public static int nextInteger(int minimum, int maximum) {
        if (minimum == maximum) {
            return minimum;
        }

        if (minimum > maximum) {
            int tmp = minimum;
            minimum = maximum;
            maximum = tmp;
        }

        int random = ThreadLocalRandom.current().nextInt(minimum, maximum);

        if (ThreadLocalRandom.current().nextBoolean()) {
            random += 1;
        }

        return random;
    }

    /**
     * Method to get a random long in a range (minimum and maximum are both inclusive)
     *
     * @param minimum minimum
     * @param maximum maximum
     * @return a random long
     */
    public static long nextLong(long minimum, long maximum) {
        if (minimum == maximum) {
            return minimum;
        }

        if (minimum > maximum) {
            long tmp = minimum;
            minimum = maximum;
            maximum = tmp;
        }

        long random = ThreadLocalRandom.current().nextLong(minimum, maximum);

        if (ThreadLocalRandom.current().nextBoolean()) {
            random += 1;
        }

        return random;
    }

    /**
     * Method to get a random float in a range (minimum and maximum are both inclusive)
     *
     * @param minimum minimum
     * @param maximum maximum
     * @return a random float
     */
    public static float nextFloat(float minimum, float maximum) {
        if (minimum == maximum) {
            return minimum;
        }

        return nextBigDecimal(BigDecimal.valueOf(minimum), BigDecimal.valueOf(maximum))
                .floatValue();
    }

    /**
     * Method to get a random double in a range (minimum and maximum are both inclusive)
     *
     * @param minimum minimum
     * @param maximum maximum
     * @return a random double
     */
    public static double nextDouble(double minimum, double maximum) {
        if (minimum == maximum) {
            return minimum;
        }

        return nextBigDecimal(BigDecimal.valueOf(minimum), BigDecimal.valueOf(maximum))
                .doubleValue();
    }

    /**
     * Method to get a random BigInteger in a range (minimum and maximum are both inclusive)
     *
     * @param minimum minimum
     * @param maximum maximum
     * @return a random BigInteger
     */
    public static BigInteger nextBigInteger(String minimum, String maximum) {
        if (minimum.contains(".")) {
            throw new NumberFormatException(
                    String.format("BigInteger minimum [%s] is invalid ", minimum));
        }

        if (maximum.contains(".")) {
            throw new NumberFormatException(
                    String.format("BigInteger maximum [%s] is invalid", maximum));
        }

        BigDecimal minimumBigDecimal;
        BigDecimal maximumBigDecimal;

        try {
            minimumBigDecimal = new BigDecimal(minimum);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(
                    String.format("BigDecimal minimum [%s] is invalid", minimum));
        }

        try {
            maximumBigDecimal = new BigDecimal(maximum);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(
                    String.format("BigDecimal maximum [%s] is invalid", maximum));
        }

        if (minimumBigDecimal.equals(maximumBigDecimal)) {
            return new BigInteger(minimum);
        }

        if (maximumBigDecimal.subtract(minimumBigDecimal).abs().equals(BigDecimal.ONE)) {
            if (ThreadLocalRandom.current().nextBoolean()) {
                return new BigInteger(minimum);
            } else {
                return new BigInteger(maximum);
            }
        }

        if (minimumBigDecimal.compareTo(maximumBigDecimal) > 0) {
            BigDecimal temp = minimumBigDecimal;
            maximumBigDecimal = minimumBigDecimal;
            minimumBigDecimal = temp;
        }

        maximumBigDecimal = maximumBigDecimal.add(BigDecimal.ONE);
        int digitCount = Math.max(minimumBigDecimal.precision(), maximumBigDecimal.precision());
        int bitCount = (int) (digitCount / Math.log10(2.0));

        BigDecimal random = nextBigDecimal(new BigDecimal(minimum), new BigDecimal(maximum));

        BigInteger bigInteger =
                random.round(new MathContext(bitCount, RoundingMode.DOWN)).toBigInteger();
        if (ThreadLocalRandom.current().nextBoolean()) {
            bigInteger = bigInteger.add(BigInteger.ONE);
        }

        return bigInteger;
    }

    /**
     * Method to get a BigDecimal in a range (minimum and maximum are both inclusive)
     *
     * @param minimum minimum
     * @param maximum maximum
     * @return a random BigDecimal
     */
    public static BigDecimal nextBigDecimal(String minimum, String maximum) {
        BigDecimal minimumBigDecimal;
        BigDecimal maximummBigDecimal;

        try {
            minimumBigDecimal = new BigDecimal(minimum);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(
                    String.format("BigDecimal minimum [%s] is invalid", minimum));
        }

        try {
            maximummBigDecimal = new BigDecimal(maximum);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(
                    String.format("BigDecimal maximum [%s] is invalid", maximum));
        }

        return nextBigDecimal(minimumBigDecimal, maximummBigDecimal);
    }

    /**
     * Method to get a random BigDecimal in a range (minimum and maximum are both inclusive)
     *
     * @param minimum minimum
     * @param maximum maximum
     * @return a random BigDecimal
     */
    private static BigDecimal nextBigDecimal(BigDecimal minimum, BigDecimal maximum) {
        if (minimum.equals(maximum)) {
            return minimum;
        }

        if (minimum.compareTo(maximum) < 0) {
            BigDecimal temp = minimum;
            minimum = maximum;
            maximum = temp;
        }

        int digitCount = Math.max(minimum.precision(), maximum.precision()) + 10;
        int bitCount = (int) (digitCount / Math.log10(2.0));

        // convert Random BigInteger to a BigDecimal between 0 and 1
        BigDecimal alpha =
                new BigDecimal(new BigInteger(bitCount, ThreadLocalRandom.current()))
                        .movePointLeft(digitCount);

        return minimum.add(maximum.subtract(minimum).multiply(alpha, new MathContext(digitCount)));
    }
}
