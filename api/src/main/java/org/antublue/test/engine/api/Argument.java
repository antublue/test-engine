/*
 * Copyright (C) 2015-2024 The AntuBLUE test-engine project authors
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

package org.antublue.test.engine.api;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.platform.commons.util.Preconditions;

/**
 * {@code Named} is a container that associates a name with a given payload.
 *
 * @param <T> the type of the payload
 * @since 5.8
 */
public interface Argument<T> {

    static Argument<Boolean> ofBoolean(boolean value) {
        return of(String.valueOf(value), value);
    }

    static Argument<Byte> ofByte(byte value) {
        return of(String.valueOf(value), value);
    }

    static Argument<Character> ofChar(char value) {
        return of(String.valueOf(value), value);
    }

    static Argument<Short> ofShort(short value) {
        return of(String.valueOf(value), value);
    }

    static Argument<Integer> ofInt(int value) {
        return of(String.valueOf(value), value);
    }

    static Argument<Long> ofLong(long value) {
        return of(String.valueOf(value), value);
    }

    static Argument<Float> ofFloat(float value) {
        return of(String.valueOf(value), value);
    }

    static Argument<Double> ofDouble(double value) {
        return of(String.valueOf(value), value);
    }

    static Argument<String> ofString(String value) {
        if (value == null) {
            return of("String=/null/", value);
        } else if (value.isEmpty()) {
            return of("String=/empty/", value);
        } else {
            return of(value, value);
        }
    }

    static Argument<BigInteger> ofBigInteger(BigInteger value) {
        if (value == null) {
            return of("BigInteger=/null/", value);
        } else {
            return of(value.toString(), value);
        }
    }

    static Argument<BigDecimal> ofBigDecimal(BigDecimal value) {
        if (value == null) {
            return of("BigDecimal=/null/", value);
        } else {
            return of(value.toString(), value);
        }
    }

    static <T> Argument<T> of(String name, T payload) {
        Preconditions.notBlank(name, "name must not be null or blank");

        return new Argument<T>() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public T getPayload() {
                return payload;
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }

    /**
     * Get the name of the payload.
     *
     * @return the name of the payload; never {@code null} or blank
     */
    String getName();

    /**
     * Get the payload.
     *
     * @return the payload; may be {@code null} depending on the use case
     */
    T getPayload();
}
