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

/** Class to implement a Key */
public final class Key {

    /** Constructor */
    private Key() {
        // DO NOTHING
    }

    /**
     * Method to create a key
     *
     * @param objects objects
     * @return a key
     */
    public static String of(Object... objects) {
        checkNotNull(objects, "objects is null");
        checkTrue(objects.length > 0, "objects is an empty array");

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < objects.length; i++) {
            stringBuilder.append("/").append(objects[i]);
        }
        return stringBuilder.toString().trim();
    }

    /**
     * Method to validate a condition is true
     *
     * @param b b
     * @param message message
     */
    private static void checkTrue(boolean b, String message) {
        if (!b) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Method to validate a value is not null
     *
     * @param object object
     * @param message message
     */
    private static void checkNotNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
