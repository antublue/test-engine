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

import org.antublue.test.engine.internal.TestEngineConfigurationParameters;
import org.antublue.test.engine.internal.TestEngineDiscoveryRequestResolver;
import org.antublue.test.engine.internal.TestEngineEngineDiscoveryRequest;
import org.antublue.test.engine.internal.TestEngineExecutor;
import org.antublue.test.engine.internal.TestEngineInformation;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

import java.util.Optional;

/**
 * Class to implement a TestEngine
 */
public class TestEngine implements org.junit.platform.engine.TestEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEngine.class);

    public static final String ENGINE_ID = "antublue-test-engine";
    public static final String GROUP_ID = "org.antublue";
    public static final String ARTIFACT_ID = "test-engine";
    public static final String VERSION = TestEngineInformation.getVersion();

    static {
        /*
        try {
            FileOutputStream fileOutputStream = new FileOutputStream("antublue-test-engine.log");
            DelegatingOutputStream delegatingOutputStream = new DelegatingOutputStream(System.out, fileOutputStream);
            System.setOut(new PrintStream(delegatingOutputStream));
            delegatingOutputStream = new DelegatingOutputStream(System.err, fileOutputStream);
            System.setErr(new PrintStream(delegatingOutputStream));
        } catch (IOException ioe) {
            // DO NOTHING
        }
        */
    }

    @Override
    public String getId() {
        return ENGINE_ID;
    }

    @Override
    public Optional<String> getGroupId() {
        return Optional.of(GROUP_ID);
    }

    @Override
    public Optional<String> getArtifactId() {
        return Optional.of(ARTIFACT_ID);
    }

    @Override
    public Optional<String> getVersion() {
        return Optional.of(VERSION);
    }

    /**
     * Method to discover test classes
     *
     * @param engineDiscoveryRequest
     * @param uniqueId
     * @return
     */
    @Override
    public TestDescriptor discover(EngineDiscoveryRequest engineDiscoveryRequest, UniqueId uniqueId) {
        LOGGER.trace("discover(EngineDiscoveryRequest, UniqueId)");

        // Wrap the discovery request
        TestEngineEngineDiscoveryRequest testEngineDiscoveryRequest =
                new TestEngineEngineDiscoveryRequest(
                        engineDiscoveryRequest,
                        TestEngineConfigurationParameters.getInstance());

        // Create a EngineDescriptor as the target
        EngineDescriptor engineDescriptor = new EngineDescriptor(UniqueId.forEngine(getId()), getId());

        // Create a TestEngineDiscoverySelectorResolver and
        // resolve selectors, adding them to the engine descriptor
        new TestEngineDiscoveryRequestResolver().resolve(testEngineDiscoveryRequest, engineDescriptor);

        // Return the engine descriptor with all child test descriptors
        return engineDescriptor;
    }

    /**
     * Method to execute th ExecutionRequest
     *
     * @param executionRequest
     */
    @Override
    public void execute(ExecutionRequest executionRequest) {
        LOGGER.trace("execute(ExecutionRequest)");

        new TestEngineExecutor().execute(executionRequest);
    }
}
