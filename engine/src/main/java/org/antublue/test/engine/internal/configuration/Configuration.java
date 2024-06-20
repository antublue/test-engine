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

package org.antublue.test.engine.internal.configuration;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import org.antublue.test.engine.exception.TestEngineConfigurationException;
import org.junit.platform.commons.util.Preconditions;

/** Class to implement Configuration */
@SuppressWarnings("deprecation")
public class Configuration implements org.junit.platform.engine.ConfigurationParameters {

    private static final String ANTUBLUE_TEST_ENGINE_CONFIGURATION_TRACE =
            "ANTUBLUE_TEST_ENGINE_CONFIGURATION_TRACE";

    private static final String ANTUBLUE_TEST_ENGINE_PROPERTIES_FILENAME =
            "antublue-test-engine.properties";

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd | HH:mm:ss.SSS", Locale.getDefault());

    private boolean IS_TRACE_ENABLED;

    private final Map<String, String> map;

    /** Constructor */
    public Configuration() {
        trace("Constructor()");

        map = Collections.synchronizedMap(new TreeMap<>());

        try {
            Optional<File> optional =
                    find(Paths.get("."), ANTUBLUE_TEST_ENGINE_PROPERTIES_FILENAME);
            if (optional.isPresent()) {
                if (IS_TRACE_ENABLED) {
                    trace(
                            "antublue.test.engine.properties ["
                                    + optional.get().getAbsolutePath()
                                    + "]");
                }

                Properties properties = new Properties();
                try (Reader reader =
                        Files.newBufferedReader(optional.get().toPath(), StandardCharsets.UTF_8)) {
                    properties.load(reader);
                }
                properties.forEach(
                        (key, value) -> set(toEnvironmentVariable((String) key), (String) value));
                properties.put(
                        ANTUBLUE_TEST_ENGINE_PROPERTIES_FILENAME, optional.get().getAbsolutePath());
            }
        } catch (IOException e) {
            throw new TestEngineConfigurationException("Exception loading properties", e);
        }

        if (Constants.TRUE.equals(System.getenv().get(ANTUBLUE_TEST_ENGINE_CONFIGURATION_TRACE))) {
            IS_TRACE_ENABLED = true;
        }

        System.getenv().forEach(this::set);

        if (IS_TRACE_ENABLED) {
            map.forEach((key, value) -> trace(key + " = [" + value + "]"));
        }
    }

    /**
     * Method to set a configuration value
     *
     * @param key key
     * @param value value
     */
    public void set(String key, String value) {
        Preconditions.notNull(key, "key is null");
        Preconditions.notNull(value, "value is null");
        map.put(toEnvironmentVariable(key), value);
    }

    @Override
    public Optional<String> get(String key) {
        Preconditions.notNull(key, "key is null");
        return Optional.ofNullable(map.get(toEnvironmentVariable(key)));
    }

    @Override
    public Optional<Boolean> getBoolean(String key) {
        Preconditions.notNull(key, "key is null");
        Optional<String> optional = get(key);
        return optional.map("true"::equals);
    }

    @Override
    public <T> Optional<T> get(String key, Function<String, T> transformer) {
        Preconditions.notNull(key, "key is null");
        Preconditions.notNull(transformer, "transformer is null");
        String value = map.get(toEnvironmentVariable(key));
        if (value != null) {
            return Optional.ofNullable(transformer.apply(value));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public int size() {
        return keySet().size();
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    /**
     * Method to get the entry set
     *
     * @return the entry set
     */
    public Set<Map.Entry<String, String>> entrySet() {
        return map.entrySet();
    }

    /**
     * Method to get the singleton instance
     *
     * @return the singleton instance
     */
    public static Configuration getInstance() {
        return SingletonHolder.SINGLETON;
    }

    /**
     * Method to convert a Java system property to environment variable
     *
     * @param key key
     * @return the key as an environment variable key
     */
    private static String toEnvironmentVariable(String key) {
        return key.toUpperCase(Locale.ENGLISH).replace('.', '_');
    }

    /**
     * Method to find a properties file, searching the working directory, then parent directories
     * toward the root
     *
     * @param path path
     * @param filename filename
     * @return a optional containing a File
     */
    private static Optional<File> find(Path path, String filename) {
        Path currentPath = path.toAbsolutePath().normalize();

        while (true) {
            File file = new File(currentPath.toAbsolutePath() + File.separator + filename);
            if (file.exists() && file.isFile() && file.canRead()) {
                return Optional.of(file);
            }

            currentPath = currentPath.getParent();
            if (currentPath == null) {
                break;
            }

            currentPath = currentPath.toAbsolutePath().normalize();
        }

        return Optional.empty();
    }

    /**
     * Method to load a TRACE message
     *
     * @param message message
     */
    private void trace(String message) {
        if (IS_TRACE_ENABLED) {
            String dateTime;

            synchronized (SIMPLE_DATE_FORMAT) {
                dateTime = SIMPLE_DATE_FORMAT.format(new Date());
            }

            System.out.println(
                    dateTime
                            + " | "
                            + Thread.currentThread().getName()
                            + " | "
                            + "TRACE"
                            + " | "
                            + Configuration.class.getName()
                            + " | "
                            + message
                            + " ");
        }
    }

    /** Class to hold the singleton instance */
    private static class SingletonHolder {

        /** The singleton instance */
        public static final Configuration SINGLETON = new Configuration();
    }
}
