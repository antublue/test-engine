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

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.antublue.test.engine.Configuration;

/** Class to implement ConfigurationParameters */
@SuppressWarnings({"unchecked", "PMD.EmptyCatchBlock"})
public class ConfigurationParameters implements org.junit.platform.engine.ConfigurationParameters {

    private static final Configuration CONFIGURATION = Configuration.getInstance();

    private ConfigurationParameters() {
        // DO NOTHING
    }

    public static ConfigurationParameters getInstance() {
        return SingletonHolder.INSTANCE;
    }

    @Override
    public Optional<String> get(String key) {
        return CONFIGURATION.get(key);
    }

    @Override
    public Optional<Boolean> getBoolean(String key) {
        return CONFIGURATION.getBoolean(key);
    }

    @Override
    public <T> Optional<T> get(String key, Function<String, T> transformer) {
        Optional<String> value = CONFIGURATION.get(key);
        return value.map(transformer);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int size() {
        return CONFIGURATION.size();
    }

    @Override
    public Set<String> keySet() {
        return CONFIGURATION.keySet();
    }

    /** Class to hold the singleton instance */
    private static final class SingletonHolder {

        /** The singleton instance */
        private static final ConfigurationParameters INSTANCE = new ConfigurationParameters();
    }
}