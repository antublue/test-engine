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

package org.antublue.test.engine.api.argument;

import org.antublue.test.engine.api.Argument;

/**
 * Class to implement a BooleanArgument
 */
public class IntegerArgument implements Argument {

    private final String name;
    private final int value;

    /**
     * Constructor
     *
     * @param name name
     * @param value value
     */
    public IntegerArgument(String name, int value) {
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }

        name = name.trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name is empty");
        }

        this.name = name;
        this.value = value;
    }

    /**
     * Method to get the IntegerArgument name
     *
     * @return the return value
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * Method to get the IntegerArgument value
     *
     * @return the return value
     */
    public int value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Method to create a IntegerArgument
     *
     * @param value not null
     * @return the return value
     */
    public static IntegerArgument of(int value) {
        return new IntegerArgument(String.valueOf(value).toUpperCase(), value);
    }
}