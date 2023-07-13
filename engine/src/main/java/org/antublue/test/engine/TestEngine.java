/*
 * Copyright (C) 2022-2023 The AntuBLUE test-engine project authors
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

import org.antublue.test.engine.internal.TestClassConfigurationException;
import org.antublue.test.engine.internal.TestEngineConfiguration;
import org.antublue.test.engine.internal.TestEngineEngineDiscoveryRequest;
import org.antublue.test.engine.internal.TestEngineException;
import org.antublue.test.engine.internal.TestEngineExecutor;
import org.antublue.test.engine.internal.TestEngineInformation;
import org.antublue.test.engine.internal.TestEngineTestDescriptorStore;
import org.antublue.test.engine.internal.descriptor.ExtendedEngineDescriptor;
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
    public static final String ANTUBLUE_TEST_ENGINE_MAVEN_PLUGIN = "__ANTUBLUE_TEST_ENGINE_MAVEN_PLUGIN__";
    public static final String ANTUBLUE_TEST_ENGINE_MAVEN_BATCH_MODE = "__ANTUBLUE_TEST_ENGINE_MAVEN_BATCH_MODE__";

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
        LOGGER.trace("discover()");

        // Create an EngineDescriptor as the target
        ExtendedEngineDescriptor extendedEngineDescriptor = null;

        try {
            // Create an ExtendedEngineDescriptor as the target
            extendedEngineDescriptor = new ExtendedEngineDescriptor(UniqueId.forEngine(getId()), getId());

            // Create a TestEngineEngineDiscoveryRequest to resolve test classes
            TestEngineEngineDiscoveryRequest testEngineDiscoveryRequest =
                    new TestEngineEngineDiscoveryRequest(
                            engineDiscoveryRequest,
                            TestEngineConfiguration.getInstance(),
                            extendedEngineDescriptor);

            // Resolve test classes
            testEngineDiscoveryRequest.resolve();

            // Store the test descriptors for use in the test execution listener
            TestEngineTestDescriptorStore.getInstance().store(extendedEngineDescriptor);
        } catch (TestClassConfigurationException | TestEngineException t) {
            if ("true".equals(System.getProperty(ANTUBLUE_TEST_ENGINE_MAVEN_PLUGIN))) {
                throw t;
            }

            System.err.println(t.getMessage());
            System.exit(1);
        }

        // Return the EngineDescriptor with all child TestDescriptors
        return extendedEngineDescriptor;
    }

    /**
     * Method to execute an ExecutionRequest
     *
     * @param executionRequest executionRequest
     */
    @Override
    public void execute(ExecutionRequest executionRequest) {
        LOGGER.trace("execute()");

        // Execute the ExecutionRequest
        new TestEngineExecutor().execute(executionRequest);
    }
}
