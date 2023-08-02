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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import org.antublue.test.engine.Constants;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;

/** Class to implement TestEngineConfiguration */
@SuppressWarnings({"unchecked", "PMD.EmptyCatchBlock"})
public class ConfigurationParameters implements org.junit.platform.engine.ConfigurationParameters {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationParameters.class);
    private static final String USER_HOME = System.getProperty("user.home");

    private final Properties properties;
    private final Map<String, Optional> cache;

    /** Constructor */
    public ConfigurationParameters() {
        cache = Collections.synchronizedMap(new HashMap<>());
        properties = new Properties();

        LOGGER.trace("[%s] = [%s]", "user.home", USER_HOME);

        // Resolve the properties file if defined (environment variable, system property, use.home
        // properties file)

        String propertiesFilename = null;

        String value =
                System.getenv(
                        Constants.TEST_ENGINE_PROPERTIES
                                .toUpperCase(Locale.ENGLISH)
                                .replace('.', '_'));

        if (value != null && !value.trim().isEmpty()) {
            propertiesFilename = value.trim();
        }

        if (propertiesFilename == null) {
            value = System.getProperty(Constants.TEST_ENGINE_PROPERTIES);
            if (value != null && !value.trim().isEmpty()) {
                propertiesFilename = value.trim();
            }
        }

        if (propertiesFilename == null && USER_HOME != null) {
            propertiesFilename = USER_HOME + "/.antublue-test-engine.properties";
        }

        LOGGER.trace("[%s] = [%s]", Constants.TEST_ENGINE_PROPERTIES, propertiesFilename);

        File file = new File(propertiesFilename);
        if (file.exists() && file.isFile() && file.canRead()) {
            try (BufferedReader bufferedReader =
                    Files.newBufferedReader(file.toPath().toAbsolutePath())) {
                properties.load(bufferedReader);
            } catch (IOException e) {
                LOGGER.warn(
                        "Exception loading test engine properties [%s]",
                        file.toPath().toAbsolutePath());
            }
        }
    }

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
     * Method to resolve a configuration value (environment variable, system property, properties
     * file)
     *
     * @param key key
     * @return the return value
     */
    private String resolve(String key) {
        LOGGER.trace("resolve [%s]", key);

        String location = null;

        // Check the cache
        Optional<String> cachedResult = cache.get(key);
        if (cachedResult != null) {
            if (cachedResult.isPresent()) {
                return cachedResult.get();
            } else {
                return null;
            }
        }

        String result = null;

        // Convert the system property to an environment variable and get the value
        String value = System.getenv(key.toUpperCase(Locale.ENGLISH).replace('.', '_'));
        if (value != null && !value.trim().isEmpty()) {
            result = value.trim();
            location = "environment variable";
        }

        if (result == null) {
            // Get the system property value
            value = System.getProperty(key);
            if (value != null && !value.trim().isEmpty()) {
                result = value.trim();
                location = "system property";
            }
        }

        if (result == null) {
            // Get the file property values
            value = properties.getProperty(key);
            if (value != null && !value.trim().isEmpty()) {
                result = value.trim();
                location = "properties file";
            }
        }

        if (result == null) {
            location = "not found";
        }

        LOGGER.trace("[%s] = [%s] (location [%s])", key, result, location);

        cache.put(key, Optional.ofNullable(result));

        return result;
    }
}
