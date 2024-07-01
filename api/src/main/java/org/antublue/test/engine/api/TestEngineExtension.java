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

package org.antublue.test.engine.api;

/** Interface to implement TestEngineExtension */
public interface TestEngineExtension {

    /**
     * Method to prepare the environment
     *
     * @throws Throwable Throwable
     */
    default void initializeCallback() throws Throwable {
        // DO NOTHING
    }

    /**
     * Method to conclude the environment
     *
     * @throws Throwable Throwable
     */
    default void destroyCallback() throws Throwable {
        // DO NOTHING
    }
}
