/*
 * Copyright 2022-2023 Douglas Hoard
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

import org.junit.platform.engine.ConfigurationParameters;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Class to implement ConfigurationParameters
 */
public final class TestEngineConfiguration implements ConfigurationParameters {

    private static final TestEngineConfiguration INSTANCE = new TestEngineConfiguration();

    private final Map<String, String> map;

    /**
     * Constructor
     */
    private TestEngineConfiguration() {
        map = new LinkedHashMap<>();
    }

    public void put(String key, String value) {
        map.put(key, value);
    }

    /**
     * Method to get the configuration parameters as a Map
     *
     * @return
     */
    public Map<String, String> getConfigurationMap() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<String> get(String key) {
        return Optional.ofNullable(resolve(key));
    }

    @Override
    public Optional<Boolean> getBoolean(String key) {
        return Optional.ofNullable(Boolean.parseBoolean(resolve(key)));
    }

    @Override
    public <T> Optional<T> get(String key, Function<String, T> transformer) {
        String value = resolve(key);
        T t = transformer.apply(value);
        return Optional.ofNullable(t);
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

    public static TestEngineConfiguration getInstance() {
        return INSTANCE;
    }

    private String resolve(String key) {
        String value = map.get(key);
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }

        // Convert the system property to an environment variable and get the value
        value = System.getenv(key.toUpperCase(Locale.ENGLISH).replace('.', '_'));
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }

        // Get the system property value
        value = System.getProperty(key);
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }

        return null;
    }
}
