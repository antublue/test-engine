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

public interface Context {

    /**
     * Method to get the global Store
     *
     * @return the global Store
     */
    Store getStore();

    /**
     * Method to get a namespaced Store. The value Store.GLOBAL will a reference the global Store.
     *
     * @param namespace namespace
     * @return the namespaced Store
     */
    Store getStore(Object namespace);

    LockManager getLockManager();

    LockManager getLockManager(Object namespace);

    /**
     * Method to get the test engine configuration
     *
     * @return the test engine Configuration
     */
    Configuration getConfiguration();
}
