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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Optional;
import java.util.Properties;

/** Class to implement Configuration */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class Configuration {

    private static final String ANTUBLUE_TEST_ENGINE_PROPERTIES_ENVIRONMENT_VARIABLE =
            "ANTUBLUE_TEST_ENGINE_PROPERTIES";

    private static final String ANTUBLUE_TEST_ENGINE_PROPERTIES_SYSTEM_PROPERTY =
            "antublue.test.engine.properties";

    private static final String ANTUBLUE_TEST_ENGINE_PROPERTIES_FILENAME =
            ".antublue-test-engine.properties";

    private static Configuration SINGLETON = new Configuration();

    private static final String USER_HOME = System.getProperty("user.home");

    private Properties properties;

    /** Constructor */
    private Configuration() {
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
            File file = new File(USER_HOME + "/" + ANTUBLUE_TEST_ENGINE_PROPERTIES_FILENAME);
            if (file.exists() && file.canRead() && file.isFile()) {
                propertiesFilename = file.getAbsolutePath();
            }
        }

        if (propertiesFilename != null) {
            try (Reader reader = new FileReader(propertiesFilename)) {
                properties.load(reader);
            } catch (IOException e) {
                // TODO ?
            }
        }
    }

    /**
     * Method to get the singleton
     *
     * @return the singleton
     */
    public static Configuration singleton() {
        return SINGLETON;
    }

    /**
     * Method to get a configuration property
     *
     * @param name the property name
     * @return the property value
     */
    public Optional<String> get(String name) {
        return Optional.ofNullable(properties.getProperty(name));
    }

    /**
     * Method to get a configuration property
     *
     * @param name the property name
     * @return the property value
     */
    public Optional<Boolean> getBoolean(String name) {
        Optional<String> value = get(name);
        if (value.isPresent()) {
            return Optional.of(Boolean.parseBoolean(value.get()));
        } else {
            return Optional.empty();
        }
    }
}
