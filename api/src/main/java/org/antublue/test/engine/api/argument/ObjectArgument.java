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

/** Class to implement a ObjectArgument */
public class ObjectArgument<T> extends AbstractArgument {

    private final String name;
    private final T value;

    /**
     * Constructor
     *
     * @param name name
     * @param value value
     */
    public ObjectArgument(String name, T value) {
        this.name = validateName(name);
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

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjectArgument<?> that = (ObjectArgument<?>) o;
        return Objects.equals(name, that.name) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    /**
     * Method to create an ObjectArgument
     *
     * @param name name
     * @param value value
     * @return the return vaalue
     * @param <T> the type
     */
    public static <T> ObjectArgument<T> of(String name, T value) {
        return new ObjectArgument<>(name, value);
    }
}
