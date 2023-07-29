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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Class to get TestEngine information */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class Information {

    private static final String RESOURCE_PATH = "/test-engine.properties";
    private static final String VERSION = "version";
    private static final String UNKNOWN = "Unknown";

    /**
     * Method to get the TestEngine version
     *
     * @return the return value
     */
    public static String getVersion() {
        return getProperty(VERSION, UNKNOWN);
    }

    /**
     * Method to get value for a key from the test engine properties
     *
     * @param key the property key
     * @param defaultValue the default value
     * @return the value
     */
    private static String getProperty(String key, String defaultValue) {
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
}
