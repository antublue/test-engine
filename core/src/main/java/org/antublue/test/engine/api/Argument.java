/*
 * Copyright 2022-2023 Douglas Hoard
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

import org.antublue.test.engine.support.api.ArgumentImpl;

import java.util.Objects;

/**
 * Interface to implement an Argument
 */
public interface Argument {

    /**
     * Method to get the argument name
     *
     * @return
     */
    String name();

    /**
     * Method to get the argument value cast as the return type
     *
     * @return
     * @param <T>
     */
    <T> T value();

    /**
     * Method to get the argument value cast to a specific type
     *
     * @param clazz
     * @return
     * @param <T>
     */
    <T> T value(Class<T> clazz);

    /**
     * Method to create an Argument object (useful for a static import)
     *
     * @param name
     * @param value
     * @return
     */
    static Argument argument(String name, Object value) {
        return of(name, value);
    }

    /**
     * Method to create an Argument object
     *
     * @param name
     * @param value
     * @return
     */
    static Argument of(String name, Object value) {
        Objects.requireNonNull(name);

        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("name is empty");
        }

        return new ArgumentImpl(name.trim(), value);
    }

    /**
     * Method to create an Argument containing a boolean
     *
     * @param b
     * @return
     */
    static Argument of(boolean b) {
        return of(String.valueOf(b), b);
    }

    /**
     * Method to create a Argument containing as byte
     *
     * @param b
     * @return
     */
    static Argument of(byte b) {
        return of(String.valueOf(b), b);
    }

    /**
     * Method to create a Argument containing as char
     *
     * @param c
     * @return
     */
    static Argument of(char c) {
        return of(String.valueOf(c), c);
    }

    /**
     * Method to create a Argument containing a short
     *
     * @param s
     * @return
     */
    static Argument of(short s) {
        return of(String.valueOf(s), s);
    }

    /**
     * Method to create a Argument containing an int
     *
     * @param i
     * @return
     */
    static Argument of(int i) {
        return of(String.valueOf(i), i);
    }

    /**
     * Method to create a Argument containing a long
     *
     * @param l
     * @return
     */
    static Argument of(long l) {
        return of(String.valueOf(l), l);
    }

    /**
     * Method to create a Argument containing a float
     *
     * @param f
     * @return
     */
    static Argument of(float f) {
        return of(String.valueOf(f), f);
    }

    /**
     * Method to create a Argument containing a double
     *
     * @param d
     * @return
     */
    static Argument of(double d) {
        return of(String.valueOf(d), d);
    }

    /**
     * Method to create a Argument containing a String
     *
     * @param value not null
     * @return
     */
    static Argument of(String value) {
        if (value == null) {
            return new ArgumentImpl("((null))", null);
        } else if (value.trim().isEmpty()) {
            return new ArgumentImpl("((empty))", value);
        } else {
            return new ArgumentImpl(value, value);
        }
    }
}
