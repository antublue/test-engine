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

package org.antublue.test.engine;

import java.util.Optional;
import org.antublue.test.engine.descriptor.ExtendedEngineDescriptor;
import org.antublue.test.engine.logger.Logger;
import org.antublue.test.engine.logger.LoggerFactory;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

/** Class to implement the AntuBLUE Test Engine */
public class TestEngine implements org.junit.platform.engine.TestEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEngine.class);

    private static final ConfigurationParameters CONFIGURATION_PARAMETERS =
            ConfigurationParameters.singleton();

    /** Configuration constant */
    public static final String ENGINE_ID = "antublue-test-engine";

    /** Configuration constant */
    public static final String GROUP_ID = "org.antublue";

    /** Configuration constant */
    public static final String ARTIFACT_ID = "test-engine";

    /** Configuration constant */
    public static final String VERSION = Information.getVersion();

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
    public TestDescriptor discover(
            EngineDiscoveryRequest engineDiscoveryRequest, UniqueId uniqueId) {
        LOGGER.trace("discover()");

        try {
            EngineDescriptor engineDescriptor = new ExtendedEngineDescriptor(uniqueId, getId());

            new TestDescriptorBuilder()
                    .build(engineDiscoveryRequest, CONFIGURATION_PARAMETERS, engineDescriptor);

            return engineDescriptor;
        } catch (TestClassConfigurationException | TestEngineException t) {
            if (Constants.TRUE.equals(System.getProperty(Constants.MAVEN_PLUGIN))) {
                throw t;
            }

            System.err.println(t.getMessage());
            System.exit(1);
        } catch (Throwable t) {
            throw new TestEngineException("General exception", t);
        }

        return null;
    }

    /**
     * Method to execute an ExecutionRequest
     *
     * @param executionRequest executionRequest
     */
    @Override
    public void execute(ExecutionRequest executionRequest) {
        LOGGER.trace("execute()");

        try {
            ExtensionManager.singleton().initialize();
        } catch (Throwable t) {
            throw new TestEngineException("Exception loading extensions", t);
        }

        new Executor()
                .execute(
                        ExecutionRequest.create(
                                executionRequest.getRootTestDescriptor(),
                                executionRequest.getEngineExecutionListener(),
                                CONFIGURATION_PARAMETERS));
    }
}
