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

package org.antublue.test.engine.internal.util;

import java.util.function.Supplier;

/**
 * Class to cast an Object to another Object
 */
@SuppressWarnings("unchecked")
public final class Cast {

    /**
     * Constructor
     */
    private Cast() {
        // DO NOTHING
    }

    /**
     * Method to cast an Object to another Object
     *
     * @param object object
     * @return the return value
     * @param <T> the return type
     */
    public static <T> T cast(Object object) {
        return (T) object;
    }

    /**
     * Method to cast an Object to another Object, throwing an Exception
     * provided by the Supplier if there is a ClassCastException
     *
     * @param object object
     * @param runtimeExceptionSupplier runtimeExceptionSupplier
     * @return the return value
     * @param <T> the return type
     */
    public static <T> T cast(Object object, Supplier<RuntimeException> runtimeExceptionSupplier) {
        try {
            return (T) object;
        } catch (ClassCastException e) {
            throw runtimeExceptionSupplier.get();
        }
    }
}
