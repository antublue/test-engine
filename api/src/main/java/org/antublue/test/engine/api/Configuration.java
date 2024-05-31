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

import java.util.Optional;
import java.util.function.Function;

/** Interface to implement Configuration */
public interface Configuration {

    /**
     * Method to get a configuration value
     *
     * @param key key
     * @return an Optional containing the value if it exists, else an empty Optional
     */
    Optional<String> getParameter(String key);

    /**
     * Method to get a configuration value. If the value exists, execute the transformer Function
     *
     * @param key key
     * @param transformer transformer
     * @return an Optional containing the value if it exists, else an empty Optional
     * @param <T> the return type
     */
    <T> Optional<T> getParameter(String key, Function<String, T> transformer);
}
