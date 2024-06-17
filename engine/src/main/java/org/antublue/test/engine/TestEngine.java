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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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

        executionRequest
                .getEngineExecutionListener()
                .executionStarted(executionRequest.getRootTestDescriptor());

        Set<Object> environmentInstances = new LinkedHashSet<>();
        Map<Object, List<Method>> environmentPrepareMethods = new LinkedHashMap<>();
        Map<Object, List<Method>> environmentConcludeMethods = new LinkedHashMap<>();

        ThrowableCollector throwableCollector = new ThrowableCollector(throwable -> true);
        throwableCollector.execute(
                () -> {
                    Set<Class<?>> environmentClasses = new LinkedHashSet<>();
                    for (URI uri : getClasspathURIs()) {
                        environmentClasses.addAll(
                                ReflectionSupport.findAllClassesInClasspathRoot(
                                        uri, Predicates.ENVIRONMENT_CLASS, s -> true));
                    }

                    for (Class<?> enviromentClass : environmentClasses) {
                        Object environmentInstance = enviromentClass.getConstructor().newInstance();
                        environmentInstances.add(environmentInstance);

                        List<Method> prepareMethods =
                                ReflectionSupport.findMethods(
                                        enviromentClass,
                                        Predicates.PREPARE_METHOD,
                                        HierarchyTraversalMode.TOP_DOWN);

                        prepareMethods =
                                OrdererUtils.orderTestMethods(
                                        prepareMethods, HierarchyTraversalMode.TOP_DOWN);

                        environmentPrepareMethods.put(environmentInstance, prepareMethods);

                        List<Method> concludeMethods =
                                ReflectionSupport.findMethods(
                                        enviromentClass,
                                        Predicates.CONCLUDE_METHOD,
                                        HierarchyTraversalMode.BOTTOM_UP);

                        concludeMethods =
                                OrdererUtils.orderTestMethods(
                                        concludeMethods, HierarchyTraversalMode.BOTTOM_UP);

                        environmentConcludeMethods.put(environmentInstance, concludeMethods);
                    }
                });

        if (throwableCollector.isEmpty()) {
            throwableCollector.execute(
                    () -> {
                        for (Object environmentInstance : environmentInstances) {
                            for (Method environmentPrepareMethod :
                                    environmentPrepareMethods.get(environmentInstance)) {
                                environmentPrepareMethod.invoke(environmentInstance);
                            }
                        }
                    });
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

        List<Throwable> throwables = new ArrayList<>();

        for (Object environmentInstance : environmentInstances) {
            for (Method environmentConcludeMethod :
                    environmentConcludeMethods.get(environmentInstance)) {
                try {
                    environmentConcludeMethod.invoke(environmentInstance);
                } catch (Throwable t) {
                    throwables.add(t);
                }
            }
        }

        StandardStreams.flush();

        if (throwableCollector.isEmpty() && throwables.isEmpty()) {
            executionRequest
                    .getEngineExecutionListener()
                    .executionFinished(
                            executionRequest.getRootTestDescriptor(),
                            TestExecutionResult.successful());
        } else {
            Throwable throwable;
            if (throwableCollector.isNotEmpty()) {
                throwable = throwableCollector.getThrowable();
            } else {
                throwable = throwables.get(0);
            }

            executionRequest
                    .getEngineExecutionListener()
                    .executionFinished(
                            executionRequest.getRootTestDescriptor(),
                            TestExecutionResult.failed(throwable));
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
