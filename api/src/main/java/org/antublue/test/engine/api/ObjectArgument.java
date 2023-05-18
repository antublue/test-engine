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
 * Class to implement a ObjectArgument
 */
public class ObjectArgument<T> implements Argument {

    private final String name;
    private final T value;

    /**
     * Constructor
     *
     * @param name name
     * @param value value
     */
    public ObjectArgument(String name, T value) {
        Objects.requireNonNull(name, "name is null");

        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("name is empty");
        }

        this.name = name.trim();
        this.value = value;
    }

    /**
     * Method to get the ObjectArgument name
     *
     * @return the return value
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * Method to get the ObjectArgument value
     *
     * @return the return value
     */
    public T value() {
        return value;
    }

    /**
     * Method to create a ObjectArgument containing a boolean
     *
     * @param b b
     * @return the return value
     */
    public static ObjectArgument<Boolean> of(boolean b) {
        return new ObjectArgument<>(String.valueOf(b), b);
    }

    /**
     * Method to create a ObjectArgument containing as byte
     *
     * @param b b
     * @return the return value
     */
    public static ObjectArgument<Byte> of(byte b) {
        return new ObjectArgument<>(String.valueOf(b), b);
    }

    /**
     * Method to create a ObjectArgument containing as char
     *
     * @param c c
     * @return the return value
     */
    public static ObjectArgument<Character> of(char c) {
        return new ObjectArgument<>(String.valueOf(c), c);
    }

    /**
     * Method to create a ObjectArgument containing a short
     *
     * @param s s
     * @return the return value
     */
    public static ObjectArgument<Short> of(short s) {
        return new ObjectArgument<>(String.valueOf(s), s);
    }

    /**
     * Method to create a ObjectArgument containing an int
     *
     * @param i i
     * @return the return value
     */
    public static ObjectArgument<Integer> of(int i) {
        return new ObjectArgument<>(String.valueOf(i), i);
    }

    /**
     * Method to create a ObjectArgument containing a long
     *
     * @param l l
     * @return the return value
     */
    public static ObjectArgument<Long> of(long l) {
        return new ObjectArgument<>(String.valueOf(l), l);
    }

    /**
     * Method to create a ObjectArgument containing a float
     *
     * @param f f
     * @return the return value
     */
    public static ObjectArgument<Float> of(float f) {
        return new ObjectArgument<>(String.valueOf(f), f);
    }

    /**
     * Method to create a ObjectArgument containing a double
     *
     * @param d d
     * @return the return value
     */
    public static ObjectArgument<Double> of(double d) {
        return new ObjectArgument<>(String.valueOf(d), d);
    }

    /**
     * Method to create a ObjectArgument containing a String
     *
     * @param value not null
     * @return the return value
     */
    public static ObjectArgument<String> of(String value) {
        String name = value;
        if (name == null) {
            name = "((null))";
        } else if (name.isEmpty()) {
            name = "((empty))";
        }
        return new ObjectArgument<>(name, value);
    }
}