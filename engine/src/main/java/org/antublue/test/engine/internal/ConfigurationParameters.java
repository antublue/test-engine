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

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;

/** Class to implement TestEngineConfiguration */
public class ConfigurationParameters implements org.junit.platform.engine.ConfigurationParameters {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationParameters.class);

    @Override
    public Optional<String> get(String key) {
        return Optional.ofNullable(resolve(key));
    }

    @Override
    public Optional<Boolean> getBoolean(String key) {
        return Optional.of(Boolean.parseBoolean(resolve(key)));
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

    /**
     * Method to resolve a configuration key first as a Java property, then as an environment
     * variable
     *
     * @param key key
     * @return the return value
     */
    private String resolve(String key) {
        String result = null;

        // Convert the system property to an environment variable and get the value
        String value = System.getenv(key.toUpperCase(Locale.ENGLISH).replace('.', '_'));
        if (value != null && !value.trim().isEmpty()) {
            result = value.trim();
        }

        // Get the system property value
        value = System.getProperty(key);
        if (value != null && !value.trim().isEmpty()) {
            result = value.trim();
        }

        LOGGER.trace("key [%s] result [%s]", key, result);

        return result;
    }
}
