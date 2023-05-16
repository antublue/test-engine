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

package org.antublue.test.engine.api;

import java.util.Objects;

/**
 * Class to implement a SimpleParameter
 */
@SuppressWarnings("unchecked")
public final class SimpleParameter<T> implements Parameter {

    private final String name;
    private final T value;

    /**
     * Constructor
     *
     * @param name name
     * @param value value
     */
    public SimpleParameter(String name, T value) {
        Objects.requireNonNull(name, "name is null");

        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("name is empty");
        }

        this.name = name.trim();
        this.value = value;
    }

    /**
     * Method to get the SimpleParameter name
     *
     * @return the return value
     */
    @Override
    public String name() {
        return name;
    }

    public T value() {
        return value;
    }

    /**
     * Method to create a SimpleParameter containing a boolean
     *
     * @param b b
     * @return the return value
     */
    public static SimpleParameter<Boolean> of(boolean b) {
        return new SimpleParameter<>(String.valueOf(b), b);
    }

    /**
     * Method to create a SimpleParameter containing as byte
     *
     * @param b b
     * @return the return value
     */
    public static SimpleParameter<Byte> of(byte b) {
        return new SimpleParameter<>(String.valueOf(b), b);
    }

    /**
     * Method to create a SimpleParameter containing as char
     *
     * @param c c
     * @return the return value
     */
    public static SimpleParameter<Character> of(char c) {
        return new SimpleParameter<>(String.valueOf(c), c);
    }

    /**
     * Method to create a SimpleParameter containing a short
     *
     * @param s s
     * @return the return value
     */
    public static SimpleParameter<Short> of(short s) {
        return new SimpleParameter<>(String.valueOf(s), s);
    }

    /**
     * Method to create a SimpleParameter containing an int
     *
     * @param i i
     * @return the return value
     */
    public static SimpleParameter<Integer> of(int i) {
        return new SimpleParameter<>(String.valueOf(i), i);
    }

    /**
     * Method to create a SimpleParameter containing a long
     *
     * @param l l
     * @return the return value
     */
    public static SimpleParameter of(long l) {
        return new SimpleParameter<>(String.valueOf(l), l);
    }

    /**
     * Method to create a SimpleParameter containing a float
     *
     * @param f f
     * @return the return value
     */
    public static SimpleParameter<Float> of(float f) {
        return new SimpleParameter<>(String.valueOf(f), f);
    }

    /**
     * Method to create a SimpleParameter containing a double
     *
     * @param d d
     * @return the return value
     */
    public static SimpleParameter<Double> of(double d) {
        return new SimpleParameter<>(String.valueOf(d), d);
    }

    /**
     * Method to create a SimpleParameter containing a String
     *
     * @param value not null
     * @return the return value
     */
    public static SimpleParameter<String> of(String value) {
        String name = value;
        if (name == null) {
            name = "((null))";
        } else if (name.isEmpty()) {
            name = "((empty))";
        }
        return new SimpleParameter<>(name, value);
    }
}