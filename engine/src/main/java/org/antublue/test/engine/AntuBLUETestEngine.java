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
import org.antublue.test.engine.internal.configuration.Configuration;
import org.antublue.test.engine.internal.discovery.EngineDiscoveryRequestResolver;
import org.antublue.test.engine.internal.extension.TestEngineExtensionManager;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.util.Executor;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;

/** Class to implement the AntuBLUE Test Engine */
public class AntuBLUETestEngine implements org.junit.platform.engine.TestEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(AntuBLUETestEngine.class);

    /** Configuration constant */
    public static final String ENGINE_ID = "antublue-test-engine";

    /** Configuration constant */
    private static final String GROUP_ID = "org.antublue";

    /** Configuration constant */
    private static final String ARTIFACT_ID = "test-engine";

    /** Configuration constant */
    public static final String VERSION = Information.getInstance().getVersion();

    /** UniqueId constant */
    private static final String UNIQUE_ID = "[engine:" + ENGINE_ID + "]";

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

    /** Constructor */
    public AntuBLUETestEngine() {
        // DO NOTHING
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
        if (!UNIQUE_ID.equals(uniqueId.toString())) {
            return null;
        }

        Configuration.getInstance();

        LOGGER.trace("discover(" + uniqueId + ")");

        EngineDescriptor engineDescriptor = new EngineDescriptor(uniqueId, getId());

        new EngineDiscoveryRequestResolver()
                .resolveSelectors(engineDiscoveryRequest, engineDescriptor);

        return engineDescriptor;
    }

    /**
     * Method to execute an ExecutionRequest
     *
     * @param executionRequest executionRequest
     */
    @Override
    public void execute(ExecutionRequest executionRequest) {
        if (executionRequest.getRootTestDescriptor().getChildren().isEmpty()) {
            return;
        }

        LOGGER.trace(
                "execute() rootTestDescriptor children [%d]",
                executionRequest.getRootTestDescriptor().getChildren().size());

        executionRequest
                .getEngineExecutionListener()
                .executionStarted(executionRequest.getRootTestDescriptor());

        ThrowableCollector throwableCollector = new ThrowableCollector(throwable -> true);
        if (throwableCollector.isEmpty()) {
            throwableCollector.execute(() -> TestEngineExtensionManager.getInstance().prepare());
        }

        if (throwableCollector.isEmpty()) {
            throwableCollector.execute(
                    () -> {
                        Executor executor = new Executor();

                        executor.execute(
                                ExecutionRequest.create(
                                        executionRequest.getRootTestDescriptor(),
                                        executionRequest.getEngineExecutionListener(),
                                        Configuration.getInstance()));

                        executor.await();
                    });
        }

        throwableCollector.execute(
                () -> {
                    TestEngineExtensionManager.getInstance().conclude();
                });

        if (throwableCollector.isEmpty()) {
            executionRequest
                    .getEngineExecutionListener()
                    .executionFinished(
                            executionRequest.getRootTestDescriptor(),
                            TestExecutionResult.successful());
        } else {
            executionRequest
                    .getEngineExecutionListener()
                    .executionFinished(
                            executionRequest.getRootTestDescriptor(),
                            TestExecutionResult.failed(throwableCollector.getThrowable()));
        }
    }
}
