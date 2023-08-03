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

package org.antublue.test.engine.internal;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/** Class to implement TestEngineConfiguration */
@SuppressWarnings({"unchecked", "PMD.EmptyCatchBlock"})
public class ConfigurationParameters implements org.junit.platform.engine.ConfigurationParameters {

    private static final ConfigurationParameters CONFIGURATION_PARAMETERS =
            new ConfigurationParameters();

    private Configuration configuration;

    /** Constructor */
    private ConfigurationParameters() {
        configuration = Configuration.singleton();
    }

    /**
     * Method to get the singleton instance
     *
     * @return the singleton instance
     */
    public static ConfigurationParameters singleton() {
        return CONFIGURATION_PARAMETERS;
    }

    @Override
    public Optional<String> get(String key) {
        return configuration.get(key);
    }

    @Override
    public Optional<Boolean> getBoolean(String key) {
        return configuration.getBoolean(key);
    }

    @Override
    public <T> Optional<T> get(String key, Function<String, T> transformer) {
        Optional<String> value = configuration.get(key);
        if (value.isPresent()) {
            return Optional.ofNullable(transformer.apply(value.get()));
        } else {
            return Optional.empty();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> keySet() {
        throw new UnsupportedOperationException();
    }
}
