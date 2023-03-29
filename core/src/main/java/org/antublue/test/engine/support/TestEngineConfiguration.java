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

import java.util.Locale;

/**
 * Class to get configuration values
 */
public final class TestEngineConfiguration {

    /**
     * Constructor
     */
    private TestEngineConfiguration() {
        // DO NOTHING
    }

    /**
     * Method to get a configuration value.
     *
     * The method parameter is a system property, but the code converts the system
     * property value to an environment variable (uppercase / replace "." with "_")
     * and checks for environment variable first.
     *
     * @param systemProperty
     * @return
     */
    public static String getConfigurationValue(String systemProperty) {
        // Convert the system property to an environment variable and get the value
        String environmentVariableValue =
                System.getenv(
                        systemProperty
                                .toUpperCase(Locale.ENGLISH)
                                .replace('.', '_'));

        // Get the system property value
        String systemPropertyValue = System.getProperty(systemProperty);

        // Check the environment value first
        if ((environmentVariableValue != null) && !environmentVariableValue.trim().isEmpty()) {
            return environmentVariableValue;
        } else if ((systemPropertyValue != null) && !systemPropertyValue.trim().isEmpty()) {
            return systemPropertyValue;
        }

        return null;
    }
}
