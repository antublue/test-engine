/*
 * Copyright (C) 2022-2023 The AntuBLUE test-engine project authors
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

import org.antublue.test.engine.api.internal.ParameterImpl;

import java.util.Objects;

/**
 * Interface to implement a Parameter
 */
public interface Parameter {

    /**
     * Method to get the Parameter name
     *
     * @return the return value
     */
    String name();

    /**
     * Method to get the Parameter value cast as the return type
     *
     * @return the return value
     * @param <T> the return type
     */
    <T> T value();

    /**
     * Method to get the Parameter value cast to a specific type
     *
     * @param clazz clazz
     * @return the return value
     * @param <T> the return type
     */
    <T> T value(Class<T> clazz);

    /**
     * Method to create a Parameter object
     *
     * @param name name
     * @param value value
     * @return the return value
     */
    static Parameter of(String name, Object value) {
        Objects.requireNonNull(name);

        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("name is empty");
        }

        return new ParameterImpl(name.trim(), value);
    }

    /**
     * Method to create a Parameter containing a boolean
     *
     * @param b b
     * @return the return value
     */
    static Parameter of(boolean b) {
        return of(String.valueOf(b), b);
    }

    /**
     * Method to create a Parameter containing as byte
     *
     * @param b b
     * @return the return value
     */
    static Parameter of(byte b) {
        return of(String.valueOf(b), b);
    }

    /**
     * Method to create a Parameter containing as char
     *
     * @param c c
     * @return the return value
     */
    static Parameter of(char c) {
        return of(String.valueOf(c), c);
    }

    /**
     * Method to create a Parameter containing a short
     *
     * @param s s
     * @return the return value
     */
    static Parameter of(short s) {
        return of(String.valueOf(s), s);
    }

    /**
     * Method to create a Parameter containing an int
     *
     * @param i i
     * @return the return value
     */
    static Parameter of(int i) {
        return of(String.valueOf(i), i);
    }

    /**
     * Method to create a Parameter containing a long
     *
     * @param l l
     * @return the return value
     */
    static Parameter of(long l) {
        return of(String.valueOf(l), l);
    }

    /**
     * Method to create a Parameter containing a float
     *
     * @param f f
     * @return the return value
     */
    static Parameter of(float f) {
        return of(String.valueOf(f), f);
    }

    /**
     * Method to create a Parameter containing a double
     *
     * @param d d
     * @return the return value
     */
    static Parameter of(double d) {
        return of(String.valueOf(d), d);
    }

    /**
     * Method to create a Parameter containing a String
     *
     * @param value not null
     * @return the return value
     */
    static Parameter of(String value) {
        if (value == null) {
            return new ParameterImpl("((null))", null);
        } else if (value.trim().isEmpty()) {
            return new ParameterImpl("((empty))", value);
        } else {
            return new ParameterImpl(value, value);
        }
    }
}
