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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.antublue.test.engine.configuration.Configuration;
import org.antublue.test.engine.exception.TestClassDefinitionException;
import org.antublue.test.engine.exception.TestEngineException;
import org.antublue.test.engine.logger.Logger;
import org.antublue.test.engine.logger.LoggerFactory;
import org.antublue.test.engine.test.extension.ExtensionManager;
import org.antublue.test.engine.test.parameterized.ParameterizedTestFactory;
import org.antublue.test.engine.test.parameterized.ParameterizedTestUtils;
import org.antublue.test.engine.test.standard.StandardTestFactory;
import org.antublue.test.engine.test.util.AutoCloseProcessor;
import org.antublue.test.engine.test.util.LockProcessor;
import org.antublue.test.engine.test.util.TestUtils;
import org.antublue.test.engine.util.ReflectionUtils;
import org.antublue.test.engine.util.Singleton;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

/** Class to implement the AntuBLUE Test Engine */
public class TestEngine implements org.junit.platform.engine.TestEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEngine.class);

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
            Singleton.register(Configuration.class, clazz -> new Configuration());
            Singleton.register(
                    ConfigurationParameters.class, clazz -> new ConfigurationParameters());
            Singleton.register(ReflectionUtils.class, clazz -> new ReflectionUtils());
            Singleton.register(TestUtils.class, clazz -> new TestUtils());
            Singleton.register(ParameterizedTestUtils.class, clazz -> new ParameterizedTestUtils());
            Singleton.register(LockProcessor.class, clazz -> new LockProcessor());
            Singleton.register(AutoCloseProcessor.class, clazz -> new AutoCloseProcessor());
            Singleton.register(ExtensionManager.class, clazz -> new ExtensionManager());

            // Create an engine descriptor to build the list of test descriptors
            EngineDescriptor engineDescriptor = new EngineDescriptor(uniqueId, getId());

            // Use the test factories to find tests build the test descriptor tree
            new StandardTestFactory().discover(engineDiscoveryRequest, engineDescriptor);
            new ParameterizedTestFactory().discover(engineDiscoveryRequest, engineDescriptor);

            // Shuffle or sort then engine descriptor's children
            shuffleOrSortTestDescriptors(engineDescriptor);

            return engineDescriptor;
        } catch (TestClassDefinitionException | TestEngineException t) {
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
     * Method to shuffle or sort an engine descriptor's children
     *
     * <p>Workaround for the fact that the engine descriptor returns an unmodifiable Set which can't
     * be sorted
     *
     * @param engineDescriptor engineDescriptor
     */
    private void shuffleOrSortTestDescriptors(EngineDescriptor engineDescriptor) {
        Configuration configuration = Singleton.get(Configuration.class);

        // Get the test descriptors and remove them from the engine descriptor
        List<TestDescriptor> testDescriptors = new ArrayList<>(engineDescriptor.getChildren());
        testDescriptors.forEach(testDescriptor -> engineDescriptor.removeChild(testDescriptor));

        // Shuffle or sort the test descriptor list based on configuration
        Optional<String> optionalShuffle = configuration.get(Constants.TEST_CLASS_SHUFFLE);

        if (optionalShuffle.isPresent() && Constants.TRUE.equals(optionalShuffle.get())) {
            Collections.shuffle(testDescriptors);
        } else {
            testDescriptors.sort(Comparator.comparing(TestDescriptor::getDisplayName));
        }

        // Add the shuffled or sorted test descriptors to the engine descriptor
        testDescriptors.forEach(testDescriptor -> engineDescriptor.addChild(testDescriptor));
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
            Singleton.get(ExtensionManager.class).initialize();
        } catch (Throwable t) {
            throw new TestEngineException("Exception loading extensions", t);
        }

        // printTestDescriptorTree(executionRequest.getRootTestDescriptor(), 0);

        EngineExecutionListener engineExecutionListener =
                executionRequest.getEngineExecutionListener();

        try {
            engineExecutionListener.executionStarted(executionRequest.getRootTestDescriptor());

            ConfigurationParameters configurationParameters =
                    Singleton.get(ConfigurationParameters.class);

            new Executor()
                    .execute(
                            ExecutionRequest.create(
                                    executionRequest.getRootTestDescriptor(),
                                    executionRequest.getEngineExecutionListener(),
                                    configurationParameters));
        } finally {
            engineExecutionListener.executionFinished(
                    executionRequest.getRootTestDescriptor(), TestExecutionResult.successful());
        }
    }
}
