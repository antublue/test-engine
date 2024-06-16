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

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.antublue.test.engine.exception.TestClassDefinitionException;
import org.antublue.test.engine.exception.TestEngineException;
import org.antublue.test.engine.internal.Executor;
import org.antublue.test.engine.internal.configuration.ConfigurationParameters;
import org.antublue.test.engine.internal.configuration.Constants;
import org.antublue.test.engine.internal.discovery.EngineDiscoveryRequestResolver;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.util.OrdererUtils;
import org.antublue.test.engine.internal.util.Predicates;
import org.antublue.test.engine.internal.util.StandardStreams;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
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
            // System.err.println(t.getMessage());
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
        
        Set<Class<?>> lifeCycleClasses = new LinkedHashSet<>();
        for (URI uri : getClasspathURIs()) {
            lifeCycleClasses.addAll(
                    ReflectionSupport.findAllClassesInClasspathRoot(
                            uri, Predicates.LIFE_CYCLE_CLASS, s -> true));
        }

        Set<Object> lifeCycleInstances = new LinkedHashSet<>();

        try {
            for (Class<?> lifecyleClass : lifeCycleClasses) {
                Object lifeCycleInstance = lifecyleClass.getConstructor().newInstance();
                lifeCycleInstances.add(lifeCycleInstance);

                List<Method> prepareMethods =
                        ReflectionSupport.findMethods(
                                lifecyleClass,
                                Predicates.PREPARE_METHOD,
                                HierarchyTraversalMode.TOP_DOWN);

                prepareMethods =
                        OrdererUtils.orderTestMethods(
                                prepareMethods, HierarchyTraversalMode.TOP_DOWN);

                for (Method prepareMethod : prepareMethods) {
                    prepareMethod.invoke(lifeCycleInstance);
                }
            }
        } catch (Throwable t) {
            // TODO handle
            t.printStackTrace();
        }

        EngineExecutionListener engineExecutionListener =
                executionRequest.getEngineExecutionListener();

        try {
            engineExecutionListener.executionStarted(executionRequest.getRootTestDescriptor());

            Executor executor = new Executor();

            executor.execute(
                    ExecutionRequest.create(
                            executionRequest.getRootTestDescriptor(),
                            engineExecutionListener,
                            ConfigurationParameters.getInstance()));

            executor.await();
        } finally {
            for (Object lifeCycleInstance : lifeCycleInstances) {
                List<Method> concludeMethods =
                        ReflectionSupport.findMethods(
                                lifeCycleInstance.getClass(),
                                Predicates.CONCLUDE_METHOD,
                                HierarchyTraversalMode.BOTTOM_UP);

                concludeMethods =
                        OrdererUtils.orderTestMethods(
                                concludeMethods, HierarchyTraversalMode.TOP_DOWN);

                for (Method concludeMethod : concludeMethods) {
                    try {
                        concludeMethod.invoke(lifeCycleInstance);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }

            StandardStreams.flush();

            // TODO handle exception if LifeCycle failure

            engineExecutionListener.executionFinished(
                    executionRequest.getRootTestDescriptor(), TestExecutionResult.successful());
        }
    }

    /**
     * Method to get a Set of classpath URIs
     *
     * @return a Set of classpath URIs
     */
    private static Set<URI> getClasspathURIs() {
        LOGGER.trace("getClasspathURIs()");

        String classpath = System.getProperty("java.class.path");
        Set<URI> uris = new LinkedHashSet<>();

        String[] paths = classpath.split(File.pathSeparator);
        for (String path : paths) {
            try {
                URI uri = new File(path).toURI();
                uris.add(uri);
            } catch (Exception e) {
                System.err.println("Error converting path to URI [" + path + "]");
                e.printStackTrace();
            }
        }

        return uris;
    }
}
