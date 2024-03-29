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

import java.util.Objects;

/** Class to implement a StringArgument */
public class StringArgument extends AbstractArgument {

    private final String name;
    private final String value;

    /**
     * Constructor
     *
     * @param name name
     * @param value value
     */
    public StringArgument(String name, String value) {
        this.name = validateName(name);
        this.value = value;
    }

    /**
     * Method to get the StringArgument name
     *
     * @return the return value
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * Method to get the StringArgument value
     *
     * @return the return value
     */
    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringArgument that = (StringArgument) o;
        return Objects.equals(name, that.name) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    /**
     * Method to create a StringArgument
     *
     * @param value value
     * @return the return value
     */
    public static StringArgument of(String value) {
        String name = value;
        if (name == null) {
            name = "((null))";
        } else if (name.isEmpty()) {
            name = "((empty))";
        }

        return new StringArgument(name, value);
    }
}
