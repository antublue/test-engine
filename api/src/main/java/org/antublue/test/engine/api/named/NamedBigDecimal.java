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

import java.math.BigDecimal;
import java.util.Objects;

/** Class to implement a BigDecimalArgument */
public class NamedBigDecimal extends AbstractNamed<BigDecimal> {

    private final String name;
    private final BigDecimal value;

    /**
     * Constructor
     *
     * @param name name
     * @param value value
     */
    public NamedBigDecimal(String name, BigDecimal value) {
        this.name = validateName(name);
        this.value = value;
    }

    /**
     * Method to get the BigDecimal name
     *
     * @return the return value
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Method to get the BigDecimal value
     *
     * @return the return value
     */
    public BigDecimal getPayload() {
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
        NamedBigDecimal that = (NamedBigDecimal) o;
        return Objects.equals(name, that.name) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    /**
     * Method to create a BigDecimalArgument
     *
     * @param value value
     * @return the return value
     */
    public static NamedBigDecimal of(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("value is null");
        }

        return new NamedBigDecimal(String.valueOf(value), value);
    }
}
