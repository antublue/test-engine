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

import java.util.List;
import java.util.Optional;
import org.antublue.test.engine.exception.TestClassDefinitionException;
import org.antublue.test.engine.exception.TestEngineException;
import org.antublue.test.engine.internal.Executor;
import org.antublue.test.engine.internal.configuration.ConfigurationParameters;
import org.antublue.test.engine.internal.configuration.Constants;
import org.antublue.test.engine.internal.discovery.EngineDiscoveryRequestResolver;
import org.antublue.test.engine.internal.extension.TestEngineExtensionManager;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;

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
    public static final String VERSION = Information.getInstance().getVersion();

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
     * @return the root TestDescriptor
     */
    @Override
    public TestDescriptor discover(
            EngineDiscoveryRequest engineDiscoveryRequest, UniqueId uniqueId) {
        LOGGER.trace("discover(" + uniqueId + ")");

        try {
            EngineDescriptor engineDescriptor = new EngineDescriptor(uniqueId, getId());

            new EngineDiscoveryRequestResolver()
                    .resolveSelector(engineDiscoveryRequest, engineDescriptor);

            return engineDescriptor;
        } catch (TestClassDefinitionException | TestEngineException t) {
            if (Constants.TRUE.equals(System.getProperty(Constants.MAVEN_PLUGIN))) {
                throw t;
            }

            t.printStackTrace(System.err);
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
        LOGGER.trace(
                "execute() rootTestDescriptor children [%d]",
                executionRequest.getRootTestDescriptor().getChildren().size());

        if (executionRequest.getRootTestDescriptor().getChildren().isEmpty()) {
            return;
        }

        executionRequest
                .getEngineExecutionListener()
                .executionStarted(executionRequest.getRootTestDescriptor());

        TestEngineExtensionManager testEngineExtensionManager = new TestEngineExtensionManager();
        testEngineExtensionManager.load();

        ThrowableCollector throwableCollector = new ThrowableCollector(throwable -> true);
        if (throwableCollector.isEmpty()) {
            throwableCollector.execute(() -> testEngineExtensionManager.initialize());
        }

        if (throwableCollector.isEmpty()) {
            throwableCollector.execute(
                    () -> {
                        Executor executor = new Executor();

                        executor.execute(
                                ExecutionRequest.create(
                                        executionRequest.getRootTestDescriptor(),
                                        executionRequest.getEngineExecutionListener(),
                                        ConfigurationParameters.getInstance()));

                        executor.await();
                    });
        }

        List<Throwable> throwables = testEngineExtensionManager.cleanup();

        if (throwableCollector.isEmpty() && throwables.isEmpty()) {
            executionRequest
                    .getEngineExecutionListener()
                    .executionFinished(
                            executionRequest.getRootTestDescriptor(),
                            TestExecutionResult.successful());
        } else {
            Throwable throwable =
                    throwableCollector.isNotEmpty()
                            ? throwableCollector.getThrowable()
                            : throwables.get(0);

            executionRequest
                    .getEngineExecutionListener()
                    .executionFinished(
                            executionRequest.getRootTestDescriptor(),
                            TestExecutionResult.failed(throwable));
        }
    }
}
