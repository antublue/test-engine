/*
 * Copyright (C) 2024 The AntuBLUE test-engine project authors
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

package org.antublue.test.engine.extras;

import java.util.Collection;

/** Class to implement a Key */
public class Key {

    /** Constructor */
    private Key() {
        // DO NOTHING
    }

    /**
     * Method to create a key from a Collection of Objects
     *
     * @param collection collection
     * @return a key
     */
    public static Object of(Collection<Object> collection) {
        if (collection == null) {
            throw new IllegalArgumentException("collection is null");
        }

        if (collection.isEmpty()) {
            throw new IllegalArgumentException("collection is empty");
        }

        return of(collection.toArray());
    }

    /**
     * Method to create a key from an array of Objects
     *
     * @param objects objects
     * @return a key
     */
    public static Object of(Object... objects) {
        if (objects == null) {
            throw new IllegalArgumentException("objects is null");
        }

        if (objects.length == 0) {
            throw new IllegalArgumentException("objects is empty");
        }

        int index = 0;
        StringBuilder stringBuilder = new StringBuilder();
        for (Object object : objects) {
            if (object == null) {
                throw new IllegalArgumentException("object[" + index + "] is null");
            }
            stringBuilder.append(object);
            index++;
        }

        return stringBuilder.toString();
    }
}
