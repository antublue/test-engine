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

package org.antublue.test.engine.support;

import org.junit.platform.engine.ConfigurationParameters;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * Class to implement ConfigurationParameters
 */
public class TestEngineConfigurationParameters implements ConfigurationParameters {

    private Map<String, String> configurationMap;

    /**
     * Constructor
     */
    public TestEngineConfigurationParameters() {
        this(null);
    }

    /**
     * Constructor
     *
     * @param configurationParameters
     */
    public TestEngineConfigurationParameters(ConfigurationParameters configurationParameters) {
        configurationMap = new TreeMap<>();

        if (configurationParameters != null) {
            Set<String> configurationKeySet = configurationParameters.keySet();
            for (String key : configurationKeySet) {
                configurationParameters.get(key).ifPresent(value -> configurationMap.put(key, value));
            }
        }

        Properties properties = System.getProperties();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            configurationMap.put(key, value);
        }
    }

    /**
     * Method to get the configuration parameters as a Map
     *
     * @return
     */
    public Map<String, String> getConfigurationMap() {
        return configurationMap;
    }

    @Override
    public Optional<String> get(String key) {
        return Optional.ofNullable(configurationMap.get(key));
    }

    @Override
    public Optional<Boolean> getBoolean(String key) {
        String value = configurationMap.get(key);
        return Optional.ofNullable(Boolean.parseBoolean(value));
    }

    @Override
    public <T> Optional<T> get(String key, Function<String, T> transformer) {
        String value = configurationMap.get(key);
        T t = transformer.apply(value);
        return Optional.ofNullable(t);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int size() {
        return configurationMap.size();
    }

    @Override
    public Set<String> keySet() {
        return configurationMap.keySet();
    }
}
