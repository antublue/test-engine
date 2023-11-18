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

package example.util;

/** Class to implement a Key */
public final class KeyGenerator {

    /** Constructor */
    private KeyGenerator() {
        // DO NOTHING
    }

    /**
     * Method to create a String key using the toString() value of each object.
     *
     * <p>i.e. objects[0].toString() + "/" + objects[1].toString() + "/" .... objects[n].toString()
     *
     * @param objects objects
     * @return a key
     */
    public static String of(Object... objects) {
        if (objects == null) {
            throw new IllegalArgumentException("objects is null");
        }

        if (objects.length < 1) {
            throw new IllegalArgumentException("objects is an empty array");
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < objects.length; i++) {
            stringBuilder.append("/").append(objects[i]);
        }
        return stringBuilder.toString().trim();
    }
}
