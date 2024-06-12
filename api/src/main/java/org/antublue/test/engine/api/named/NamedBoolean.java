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

package org.antublue.test.engine.api.named;

import java.util.Objects;

/** Class to implement a BooleanArgument */
public class NamedBoolean extends AbstractNamed<Boolean> {

    private final String name;
    private final boolean value;

    /**
     * Constructor
     *
     * @param name name
     * @param value value
     */
    public NamedBoolean(String name, boolean value) {
        this.name = validateName(name);
        this.value = value;
    }

    /**
     * Method to get the BooleanArgument name
     *
     * @return the return value
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Method to get the BooleanArgument value
     *
     * @return the return value
     */
    public Boolean getPayload() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NamedBoolean that = (NamedBoolean) o;
        return value == that.value && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    /**
     * Method to create a BooleanArgument
     *
     * @param value value
     * @return the return value
     */
    public static NamedBoolean of(boolean value) {
        return new NamedBoolean(String.valueOf(value), value);
    }
}
