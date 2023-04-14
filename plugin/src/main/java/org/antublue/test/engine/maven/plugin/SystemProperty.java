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
 * Class to implement a System property configuration value
 */
public class SystemProperty {

    private String key;
    private String value;

    /**
     * Method to set the key
     *
     * @param key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Method to get the key
     *
     * @return
     */
    public String getKey() {
        return key;
    }

    /**
     * Method to set the value
     *
     * @param value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Method to get the value
     *
     * @return
     */
    public String getValue() {
        return value;
    }
}
