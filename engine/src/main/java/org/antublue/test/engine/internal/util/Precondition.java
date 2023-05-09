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

/**
 * Class to check preconditions
 */
public final class Precondition {

    /**
     * Constructor
     */
    private Precondition() {
        // DO NOTHING
    }

    /**
     * Method to validate an Object it not null
     *
     * @param object object
     */
    public static void notNull(Object object) {
        notNull(object, "object is null");
    }

    /**
     * Method to validate an Object it not null
     *
     * @param object object
     * @param message message
     */
    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new PreconditionException(message);
        }
    }

    /**
     * Method to validate a String is not null or blank
     *
     * @param string string
     */
    public static void notBlank(String string) {
        notBlank(string, "string is null or blank");
    }

    /**
     * Method to validate a String is not null or blank
     *
     * @param string string
     * @param message message
     */
    public static void notBlank(String string, String message) {
        if (string == null || string.trim().isEmpty()) {
            throw new PreconditionException(message);
        }
    }
}
