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

package org.antublue.test.engine.internal.support;

/** Class to implement ObjectSupport */
@SuppressWarnings("unchecked")
public class ObjectSupport {

    /** Constructor */
    private ObjectSupport() {
        // DO NOTHING
    }

    /**
     * Method to create an instance of a Class
     *
     * @param clazz clazz
     * @param <T> T
     * @return an Object of type T
     * @throws Throwable Throwable
     */
    public static <T> T createObject(Class<?> clazz) throws Throwable {
        return (T) clazz.getConstructor().newInstance();
    }
}
