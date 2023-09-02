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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.antublue.test.engine.configuration.Configuration;
import org.antublue.test.engine.configuration.Constants;
import org.antublue.test.engine.exception.TestClassDefinitionException;
import org.antublue.test.engine.exception.TestEngineException;
import org.antublue.test.engine.logger.Logger;
import org.antublue.test.engine.logger.LoggerFactory;
import org.antublue.test.engine.test.descriptor.ExecutableContext;
import org.antublue.test.engine.test.descriptor.ExecutableTestDescriptor;
import org.antublue.test.engine.test.descriptor.parameterized.ParameterizedClassTestDescriptor;
import org.antublue.test.engine.test.descriptor.parameterized.ParameterizedFilters;
import org.antublue.test.engine.test.descriptor.standard.StandardClassTestDescriptor;
import org.antublue.test.engine.test.descriptor.standard.StandardFilters;
import org.antublue.test.engine.test.descriptor.standard.StandardMethodTestDescriptor;
import org.antublue.test.engine.test.descriptor.util.ExtensionManager;
import org.antublue.test.engine.util.ReflectionUtils;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

/** Class to implement the AntuBLUE Test Engine */
public class TestEngine implements org.junit.platform.engine.TestEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEngine.class);

    private static final ConfigurationParameters CONFIGURATION_PARAMETERS =
            ConfigurationParameters.getSingleton();

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
            EngineDescriptor engineDescriptor = new EngineDescriptor(uniqueId, getId());

            ReflectionUtils reflectionUtils = ReflectionUtils.getSingleton();

            engineDiscoveryRequest
                    .getSelectorsByType(ClasspathRootSelector.class)
                    .forEach(
                            classpathRootSelector ->
                                    reflectionUtils
                                            .findAllClasses(
                                                    classpathRootSelector.getClasspathRoot(),
                                                    StandardFilters.TEST_CLASS)
                                            .forEach(
                                                    c ->
                                                            engineDescriptor.addChild(
                                                                    new StandardClassTestDescriptor(
                                                                            engineDiscoveryRequest,
                                                                            engineDescriptor
                                                                                    .getUniqueId(),
                                                                            c))));

            engineDiscoveryRequest
                    .getSelectorsByType(PackageSelector.class)
                    .forEach(
                            packageSelector ->
                                    ReflectionUtils.getSingleton()
                                            .findAllClasses(
                                                    packageSelector.getPackageName(),
                                                    StandardFilters.TEST_CLASS)
                                            .forEach(
                                                    c -> {
                                                        if (StandardFilters.TEST_CLASS.test(c)) {
                                                            engineDescriptor.addChild(
                                                                    new StandardClassTestDescriptor(
                                                                            engineDiscoveryRequest,
                                                                            engineDescriptor
                                                                                    .getUniqueId(),
                                                                            c));
                                                        }
                                                    }));

            engineDiscoveryRequest
                    .getSelectorsByType(ClassSelector.class)
                    .forEach(
                            classSelector -> {
                                Class<?> c = classSelector.getJavaClass();
                                if (StandardFilters.TEST_CLASS.test(c)) {
                                    engineDescriptor.addChild(
                                            new StandardClassTestDescriptor(
                                                    engineDiscoveryRequest,
                                                    engineDescriptor.getUniqueId(),
                                                    c));
                                }
                            });

            engineDiscoveryRequest
                    .getSelectorsByType(MethodSelector.class)
                    .forEach(
                            methodSelector -> {
                                Class<?> c = methodSelector.getJavaClass();
                                Method m = methodSelector.getJavaMethod();
                                if (StandardFilters.TEST_CLASS.test(c)
                                        && StandardFilters.TEST_METHOD.test(m)) {
                                    engineDescriptor.addChild(
                                            new StandardMethodTestDescriptor(
                                                    engineDiscoveryRequest,
                                                    engineDescriptor.getUniqueId(),
                                                    m));
                                }
                            });

            engineDiscoveryRequest
                    .getSelectorsByType(ClasspathRootSelector.class)
                    .forEach(
                            classpathRootSelector ->
                                    reflectionUtils
                                            .findAllClasses(
                                                    classpathRootSelector.getClasspathRoot(),
                                                    ParameterizedFilters.TEST_CLASS)
                                            .forEach(
                                                    c ->
                                                            engineDescriptor.addChild(
                                                                    new ParameterizedClassTestDescriptor(
                                                                            engineDiscoveryRequest,
                                                                            engineDescriptor
                                                                                    .getUniqueId(),
                                                                            c))));

            engineDiscoveryRequest
                    .getSelectorsByType(PackageSelector.class)
                    .forEach(
                            packageSelector ->
                                    ReflectionUtils.getSingleton()
                                            .findAllClasses(
                                                    packageSelector.getPackageName(),
                                                    ParameterizedFilters.TEST_CLASS)
                                            .forEach(
                                                    c -> {
                                                        if (ParameterizedFilters.TEST_CLASS.test(
                                                                c)) {
                                                            engineDescriptor.addChild(
                                                                    new ParameterizedClassTestDescriptor(
                                                                            engineDiscoveryRequest,
                                                                            engineDescriptor
                                                                                    .getUniqueId(),
                                                                            c));
                                                        }
                                                    }));

            engineDiscoveryRequest
                    .getSelectorsByType(ClassSelector.class)
                    .forEach(
                            classSelector -> {
                                Class<?> c = classSelector.getJavaClass();
                                if (ParameterizedFilters.TEST_CLASS.test(c)) {
                                    engineDescriptor.addChild(
                                            new ParameterizedClassTestDescriptor(
                                                    engineDiscoveryRequest,
                                                    engineDescriptor.getUniqueId(),
                                                    c));
                                }
                            });

            // TODO add support for ParameterizedMethodTestDescriptor

            // Remove test descriptors
            List<TestDescriptor> testDescriptors = new ArrayList<>(engineDescriptor.getChildren());
            for (TestDescriptor testDescriptor : testDescriptors) {
                engineDescriptor.removeChild(testDescriptor);
            }

            // Shuffle or sort test descriptors
            Optional<String> optionalShuffle =
                    Configuration.getSingleton().get(Constants.TEST_CLASS_SHUFFLE);
            if (optionalShuffle.isPresent() && Constants.TRUE.equals(optionalShuffle.get())) {
                Collections.shuffle(testDescriptors);
            } else {
                testDescriptors.sort(Comparator.comparing(TestDescriptor::getDisplayName));
            }

            // Add test descriptors
            for (TestDescriptor testDescriptor : testDescriptors) {
                engineDescriptor.addChild(testDescriptor);
                ((ExecutableTestDescriptor) testDescriptor)
                        .build(engineDiscoveryRequest, new ExecutableContext());
            }

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
     * Method to execute an ExecutionRequest
     *
     * @param executionRequest executionRequest
     */
    @Override
    public void execute(ExecutionRequest executionRequest) {
        LOGGER.trace("execute()");

        try {
            ExtensionManager.getSingleton().initialize();
        } catch (Throwable t) {
            throw new TestEngineException("Exception loading extensions", t);
        }

        // printTestDescriptorTree(executionRequest.getRootTestDescriptor(), 0);

        EngineExecutionListener engineExecutionListener =
                executionRequest.getEngineExecutionListener();

        try {
            engineExecutionListener.executionStarted(executionRequest.getRootTestDescriptor());

            new Executor()
                    .execute(
                            ExecutionRequest.create(
                                    executionRequest.getRootTestDescriptor(),
                                    executionRequest.getEngineExecutionListener(),
                                    CONFIGURATION_PARAMETERS));
        } finally {
            engineExecutionListener.executionFinished(
                    executionRequest.getRootTestDescriptor(), TestExecutionResult.successful());
        }
    }

    /**
     * Method to log the test descriptor tree hierarchy
     *
     * @param testDescriptor testDescriptor
     * @param indent indent
     */
    private static void printTestDescriptorTree(TestDescriptor testDescriptor, int indent) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            stringBuilder.append(" ");
        }

        System.out.println(stringBuilder.append(testDescriptor.getUniqueId()));

        testDescriptor.getChildren().forEach(t -> printTestDescriptorTree(t, indent + 2));
    }
}
