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

package org.antublue.test.engine.maven.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Class to implement Information */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class Information {

    private static final String RESOURCE_PATH = "/test-engine-maven-plugin.properties";
    private static final String VERSION = "version";
    private static final String UNKNOWN = "Unknown";

    /** Constructor */
    private Information() {
        // DO NOTHING
    }

    /**
     * Method to get the singleton instance
     *
     * @return the singleton instance
     */
    public static Information getInstance() {
        return SingletonHolder.SINGLETON;
    }

    /**
     * Method to get the TestEngine version
     *
     * @return the return value
     */
    public String getVersion() {
        return getProperty(VERSION, UNKNOWN);
    }

    /**
     * Method to get value for a key from the test engine properties
     *
     * @param key the property key
     * @param defaultValue the default value
     * @return the value
     */
    private String getProperty(String key, String defaultValue) {
        String value = defaultValue;

        try (InputStream inputStream = Information.class.getResourceAsStream(RESOURCE_PATH)) {
            if (inputStream != null) {
                Properties properties = new Properties();
                properties.load(inputStream);
                value = properties.getProperty(key, defaultValue).trim();
            }
        } catch (IOException e) {
            // DO NOTHING
        }

        return value;
    }

    /** Class to hold the singleton instance */
    private static final class SingletonHolder {

        /** The singleton instance */
        private static final Information SINGLETON = new Information();
    }
}
