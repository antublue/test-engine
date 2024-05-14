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

package org.antublue.test.engine;

import static java.lang.String.format;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

/** Class to implement Configuration */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class Configuration {

    /** Configuration constant */
    public static final String ANTUBLUE_TEST_ENGINE_CONFIGURATION_TRACE =
            "ANTUBLUE_TEST_ENGINE_CONFIGURATION_TRACE";

    private static final String ANTUBLUE_TEST_ENGINE_PROPERTIES_ENVIRONMENT_VARIABLE =
            "ANTUBLUE_TEST_ENGINE_PROPERTIES";

    private static final String ANTUBLUE_TEST_ENGINE_PROPERTIES_SYSTEM_PROPERTY =
            "antublue.test.engine.properties";

    private static final String ANTUBLUE_TEST_ENGINE_PROPERTIES_FILENAME_1 =
            "antublue-test-engine.properties";

    private static final String ANTUBLUE_TEST_ENGINE_PROPERTIES_FILENAME_2 =
            ".antublue-test-engine.properties";

    private static final boolean TRACE =
            "true".equals(System.getenv(ANTUBLUE_TEST_ENGINE_CONFIGURATION_TRACE));

    private String propertiesFilename;

    private final Properties properties;

    private Set<String> keySet;

    /** Constructor */
    public Configuration() {
        trace("Configuration()");

        properties = new Properties();

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

        File propertiesFile = find(Paths.get("."), ANTUBLUE_TEST_ENGINE_PROPERTIES_FILENAME_1);
        if (propertiesFile == null) {
            propertiesFile = find(Paths.get("."), ANTUBLUE_TEST_ENGINE_PROPERTIES_FILENAME_2);
        }

        if (propertiesFile != null) {
            propertiesFilename = propertiesFile.getAbsolutePath();
            trace("loading properties [%s]", propertiesFilename);
            try (Reader reader = new FileReader(propertiesFile)) {
                properties.load(reader);

                Map<String, Boolean> map = new TreeMap<>();
                for (Object key : properties.keySet()) {
                    map.put((String) key, true);
                }
                keySet = map.keySet();
                trace("properties loaded [%s]", propertiesFilename);
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
        return SingletonHolder.INSTANCE;
    }

    /**
     * Method to the properties filename
     *
     * @return the properties filename
     */
    public String getPropertiesFilename() {
        return propertiesFilename;
    }

    /**
     * Method to get a configuration property
     *
     * @param key the key
     * @return the value
     */
    public Optional<String> get(String key) {
        String value = properties.getProperty(key);
        trace("get key [%s] value [%s]", key, value);
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
            trace("getBoolean key [%s] value [%s]", key, string);
            return Optional.of(Boolean.parseBoolean(string));
        } else {
            trace("getBoolean key [%s] value [%s]", key, null);
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

    private void trace(String format, Object... objects) {
        if (TRACE) {
            System.out.println("[TRACE] " + Configuration.class + " " + format(format, objects));
            System.out.flush();
        }
    }

    private File find(Path path, String filename) {
        Path currentPath = path.toAbsolutePath().normalize();

        while (true) {
            trace("searching path [%s]", currentPath);
            File propertiesFile =
                    new File(currentPath.toAbsolutePath() + File.separator + filename);
            if (propertiesFile.exists() && propertiesFile.isFile() && propertiesFile.canRead()) {
                return propertiesFile;
            }

            currentPath = currentPath.getParent();
            if (currentPath == null) {
                break;
            }

            currentPath = currentPath.toAbsolutePath().normalize();
        }

        return null;
    }

    /** Class to hold the singleton instance */
    private static final class SingletonHolder {

        /** The singleton instance */
        private static final Configuration INSTANCE = new Configuration();
    }
}
