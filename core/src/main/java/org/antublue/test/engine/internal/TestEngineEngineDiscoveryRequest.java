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

package org.antublue.test.engine.internal;

import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.DiscoveryFilter;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.EngineDiscoveryListener;
import org.junit.platform.engine.EngineDiscoveryRequest;

import java.util.List;
import java.util.Objects;

public class TestEngineEngineDiscoveryRequest implements EngineDiscoveryRequest {

    private final EngineDiscoveryRequest engineDiscoveryRequest;
    private final ConfigurationParameters configurationParameters;

    public TestEngineEngineDiscoveryRequest(EngineDiscoveryRequest engineDiscoveryRequest, ConfigurationParameters configurationParameters) {
        Objects.requireNonNull(engineDiscoveryRequest);

        this.engineDiscoveryRequest = engineDiscoveryRequest;
        this.configurationParameters = configurationParameters;
    }

    @Override
    public <T extends DiscoverySelector> List<T> getSelectorsByType(Class<T> clazz) {
        return engineDiscoveryRequest.getSelectorsByType(clazz);
    }

    @Override
    public <T extends DiscoveryFilter<?>> List<T> getFiltersByType(Class<T> clazz) {
        return engineDiscoveryRequest.getFiltersByType(clazz);
    }

    @Override
    public ConfigurationParameters getConfigurationParameters() {
        return configurationParameters;
    }

    @Override
    public EngineDiscoveryListener getDiscoveryListener() {
        return engineDiscoveryRequest.getDiscoveryListener();
    }
}
