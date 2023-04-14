/*
 * Copyright 2023 Douglas Hoard
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

/**
 * Class to implement a Java property configuration value
 */
@SuppressWarnings("PMD.ImmutableField")
public class Java {

    private String binary;
    private SystemProperty[] systemProperties;

    /**
     * Method to get the Java binary path
     * @return
     */
    public String getBinary() {
        return binary;
    }

    /**
     * Method to get the System properties
     *
     * @return
     */
    public SystemProperty[] getSystemProperties() {
        return systemProperties;
    }
}
