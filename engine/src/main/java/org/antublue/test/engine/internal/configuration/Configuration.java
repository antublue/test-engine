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

package org.antublue.test.engine.internal.configuration;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

/** Class to implement Configuration */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class Configuration {

    private static final Configuration INSTANCE = new Configuration();

    /** Configuration constant */
    public static final String ANTUBLUE_TEST_ENGINE_CONFIGURATION_TRACE =
            "ANTUBLUE_TEST_ENGINE_CONFIGURATION_TRACE";

    private static final String ANTUBLUE_TEST_ENGINE_PROPERTIES_ENVIRONMENT_VARIABLE =
            "ANTUBLUE_TEST_ENGINE_PROPERTIES";

    private static final String ANTUBLUE_TEST_ENGINE_PROPERTIES_SYSTEM_PROPERTY =
            "antublue.test.engine.properties";

    private static final String ANTUBLUE_TEST_ENGINE_PROPERTIES_FILENAME =
            ".antublue-test-engine.properties";

    private static final boolean TRACE =
            "true".equals(System.getenv(ANTUBLUE_TEST_ENGINE_CONFIGURATION_TRACE));

    private static final String USER_HOME = System.getProperty("user.home");

    private final Properties properties;

    private Set<String> keySet;

    /** Constructor */
    public Configuration() {
        properties = new Properties();

        String propertiesFilename = null;

        String value = System.getenv(ANTUBLUE_TEST_ENGINE_PROPERTIES_ENVIRONMENT_VARIABLE);
        if (value != null && !value.trim().isEmpty()) {
            propertiesFilename = value.trim();
        }

        if (propertiesFilename == null) {
            value = System.getProperty(ANTUBLUE_TEST_ENGINE_PROPERTIES_SYSTEM_PROPERTY);
            if (value != null && !value.trim().isEmpty()) {
                propertiesFilename = value.trim();
            }
        }

        if (propertiesFilename == null) {
            File file = new File(ANTUBLUE_TEST_ENGINE_PROPERTIES_FILENAME);
            if (file.exists() && file.canRead() && file.isFile()) {
                propertiesFilename = file.getAbsolutePath();
            }
        }

        if (propertiesFilename == null) {
            AtomicReference<String> atomicReference = new AtomicReference<>();
            recursivelyFindTestEnginePropertiesFile(
                    new File(".").getAbsoluteFile(), atomicReference);
            propertiesFilename = atomicReference.get();
        }

        if (propertiesFilename == null) {
            File file = new File(USER_HOME + "/" + ANTUBLUE_TEST_ENGINE_PROPERTIES_FILENAME);
            if (file.exists() && file.canRead() && file.isFile()) {
                propertiesFilename = file.getAbsolutePath();
            }
        }

        trace("properties filename [%s]", propertiesFilename);

        if (propertiesFilename != null) {
            trace("loading properties filename [%s]", propertiesFilename);

            try (Reader reader = new FileReader(propertiesFilename)) {
                properties.load(reader);

                Map<String, Boolean> map = new TreeMap<>();
                for (Object key : properties.keySet()) {
                    map.put((String) key, true);
                }
                keySet = map.keySet();

                trace("properties loaded filename [%s]", propertiesFilename);
            } catch (IOException e) {
                // TODO ?
            }
        }

        if (keySet == null) {
            keySet = new LinkedHashSet<>();
        }
    }

    /**
     * Method to get the singleton instance
     *
     * @return the singleton instance
     */
    public static Configuration getInstance() {
        return INSTANCE;
    }

    /**
     * Method to get a configuration property
     *
     * @param key the key
     * @return the value
     */
    public Optional<String> get(String key) {
        String value = properties.getProperty(key);
        trace("get name [%s] value [%s]", key, value);
        return Optional.ofNullable(value);
    }

    /**
     * Method to get a configuration property
     *
     * @param key the key
     * @return the value
     */
    public Optional<Boolean> getBoolean(String key) {
        Optional<String> value = get(key);
        if (value.isPresent()) {
            String string = value.get();
            trace("getBoolean name [%s] value [%s]", key, string);
            return Optional.of(Boolean.parseBoolean(string));
        } else {
            trace("getBoolean name [%s] value [%s]", key, null);
            return Optional.empty();
        }
    }

    /**
     * Method to get the number of configuration properties
     *
     * @return the number of configuration properties
     */
    public int size() {
        return properties.size();
    }

    /**
     * Method to get a Set of configuration keys
     *
     * @return a Set of configuration keys
     */
    public Set<String> keySet() {
        return keySet;
    }

    private void recursivelyFindTestEnginePropertiesFile(
            File directory, AtomicReference<String> atomicReference) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.exists()
                        && f.isFile()
                        && f.canRead()
                        && ".antublue-test-engine.properties".equals(f.getName())) {
                    atomicReference.set(f.getAbsolutePath());
                    // System.out.format("found [%s]", f.getAbsolutePath()).println();
                    return;
                }
            }
        }

        if (directory.getParentFile() != null) {
            recursivelyFindTestEnginePropertiesFile(directory.getParentFile(), atomicReference);
        }
    }

    private void trace(String format, Object... objects) {
        if (TRACE) {
            System.out.println(
                    "[TRACE] " + getClass().getName() + " " + String.format(format, objects));
            System.out.flush();
        }
    }
}
