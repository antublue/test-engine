/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

/*
 * Copied from JUnit5 project for test engine API import consistency
 */

package org.antublue.test.engine.api;

import org.junit.platform.commons.util.Preconditions;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * {@code Named} is a container that associates a name with a given payload.
 *
 * @param <T> the type of the payload
 * @since 5.8
 */
public interface Named<T> {

    static Named<Boolean> ofBoolean(boolean value) {
        return of(String.valueOf(value), value);
    }

    static Named<Byte> ofByte(byte value) {
        return of(String.valueOf(value), value);
    }

    static Named<Character> ofChar(char value) {
        return of(String.valueOf(value), value);
    }

    static Named<Short> ofShort(short value) {
        return of(String.valueOf(value), value);
    }

    static Named<Integer> ofInt(int value) {
        return of(String.valueOf(value), value);
    }

    static Named<Long> ofLong(long value) {
        return of(String.valueOf(value), value);
    }

    static Named<Float> ofFloat(float value) {
        return of(String.valueOf(value), value);
    }

    static Named<Double> ofDouble(double value) {
        return of(String.valueOf(value), value);
    }

    static Named<String> ofString(String value) {
        if (value == null) {
            return of("String=/null/", value);
        } else if (value.isEmpty()) {
            return of("String=/empty/", value);
        } else {
            return of(value, value);
        }
    }

    static Named<BigInteger> ofBigInteger(BigInteger value) {
        if (value == null) {
            return of("BigInteger=/null/", value);
        } else {
            return of(value.toString(), value);
        }
    }

    static Named<BigDecimal> ofBigDecimal(BigDecimal value) {
        if (value == null) {
            return of("BigDecimal=/null/", value);
        } else {
            return of(value.toString(), value);
        }
    }

    static <T> Named<T> of(String name, T payload) {
        Preconditions.notBlank(name, "name must not be null or blank");

        return new Named<T>() {
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
