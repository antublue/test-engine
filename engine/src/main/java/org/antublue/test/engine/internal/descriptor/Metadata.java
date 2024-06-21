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

package org.antublue.test.engine.internal.descriptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Class to implement MetadataInformation\ */
@SuppressWarnings("unchecked")
public class Metadata {

    private final Map<String, Object> map;

    /** Constructor */
    public Metadata() {
        map = new ConcurrentHashMap<>();
    }

    /**
     * Method to set a metadata key / value
     *
     * @param key key
     * @param value value
     */
    public void put(String key, Object value) {
        map.put(key, value);
    }

    /**
     * Method to get a metadata value
     *
     * @param key key
     * @return a metadata value
     * @param <T> T
     */
    public <T> T get(String key) {
        return (T) map.get(key);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(" ");
            }
            Object value = entry.getValue();
            stringBuilder
                    .append("[")
                    .append(entry.getKey())
                    .append("] = [")
                    .append(value != null ? value.toString() : "null")
                    .append("]");
        }
        return stringBuilder.toString();
    }
}
