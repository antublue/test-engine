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

package org.antublue.test.engine;

import org.antublue.test.engine.internal.TestEngineConfiguration;
import org.antublue.test.engine.internal.TestEngineEngineDiscoveryRequest;
import org.antublue.test.engine.internal.TestEngineExecutor;
import org.antublue.test.engine.internal.TestEngineInformation;
import org.antublue.test.engine.internal.TestEngineTestDescriptorStore;
import org.antublue.test.engine.internal.descriptor.ExtendedEngineDescriptor;
import org.antublue.test.engine.internal.discovery.TestEngineDiscoveryRequestResolver;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;

import java.util.Optional;

/**
 * Class to implement the AntuBLUE Test Engine
 */
public class TestEngine implements org.junit.platform.engine.TestEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEngine.class);

    public static final String ENGINE_ID = "antublue-test-engine";
    public static final String GROUP_ID = "org.antublue";
    public static final String ARTIFACT_ID = "test-engine";
    public static final String VERSION = TestEngineInformation.getVersion();

    /**
     * Method to get the test engine id
     *
     * @return the return value
     */
    @Override
    public String getId() {
        return ENGINE_ID;
    }

    /**
     * Method to get the test engine group id
     *
     * @return the return value
     */
    @Override
    public Optional<String> getGroupId() {
        return Optional.of(GROUP_ID);
    }

    /**
     * Method to get the test engine artifact id
     *
     * @return the return value
     */
    @Override
    public Optional<String> getArtifactId() {
        return Optional.of(ARTIFACT_ID);
    }

    /**
     * Method to get the test engine version
     *
     * @return the return value
     */
    @Override
    public Optional<String> getVersion() {
        return Optional.of(VERSION);
    }

    /**
     * Method to discover test classes
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @param uniqueId uniqueId
     * @return the return value
     */
    @Override
    public TestDescriptor discover(EngineDiscoveryRequest engineDiscoveryRequest, UniqueId uniqueId) {
        LOGGER.trace("discover(EngineDiscoveryRequest, UniqueId)");

        // Wrap the discovery request
        TestEngineEngineDiscoveryRequest testEngineDiscoveryRequest =
                new TestEngineEngineDiscoveryRequest(
                        engineDiscoveryRequest,
                        TestEngineConfiguration.getInstance());

        // Create an EngineDescriptor as the target
        ExtendedEngineDescriptor extendedEngineDescriptor =
                new ExtendedEngineDescriptor(UniqueId.forEngine(getId()), getId());

        // Create a TestEngineDiscoverySelectorResolver and
        // resolve selectors, adding them to the engine descriptor
        new TestEngineDiscoveryRequestResolver().resolve(testEngineDiscoveryRequest, extendedEngineDescriptor);

        // Store the test descriptors
        TestEngineTestDescriptorStore.getInstance().store(extendedEngineDescriptor);

        // Return the engine descriptor with all child test descriptors
        return extendedEngineDescriptor;
    }

    /**
     * Method to execute an ExecutionRequest
     *
     * @param executionRequest executionRequest
     */
    @Override
    public void execute(ExecutionRequest executionRequest) {
        LOGGER.trace("execute(ExecutionRequest)");

        new TestEngineExecutor().execute(executionRequest);
    }
}
