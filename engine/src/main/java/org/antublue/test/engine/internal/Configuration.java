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

    private static Configuration SINGLETON;

    private static final String USER_HOME = System.getProperty("user.home");

    private Properties properties;

    /** Constructor */
    private Configuration() {
        trace("Entering constructor");

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

        trace("properties filename [%s]", propertiesFilename);

        if (propertiesFilename != null) {
            trace("loading properties filename [%s]", propertiesFilename);

            try (Reader reader = new FileReader(propertiesFilename)) {
                properties.load(reader);
                trace("properties loaded filename [%s]", propertiesFilename);
            } catch (IOException e) {
                // TODO ?
            }
        }

        trace("Leaving constructor");
    }

    /**
     * Method to get the singleton
     *
     * @return the singleton
     */
    public static synchronized Configuration singleton() {
        if (SINGLETON == null) {
            SINGLETON = new Configuration();
        }
        return SINGLETON;
    }

    /**
     * Method to get a configuration property
     *
     * @param name the property name
     * @return the property value
     */
    public Optional<String> get(String name) {
        String value = properties.getProperty(name);
        trace("get() name [%s] value [%s]", name, value);
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
            String string = value.get();
            trace("getBoolean() name [%s] value [%s]", name, string);
            return Optional.of(Boolean.parseBoolean(string));
        } else {
            trace("getBoolean() name [%s] value [%s]", name, null);
            return Optional.empty();
        }
    }

    private void trace(String format, Object... objects) {
        if (TRACE) {
            trace(String.format(format, objects));
        }
    }

    private void trace(String message) {
        if (TRACE) {
            System.out.println("[TRACE] " + getClass().getName() + " " + message);
        }
    }
}
