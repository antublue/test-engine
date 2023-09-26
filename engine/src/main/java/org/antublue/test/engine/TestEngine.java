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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.antublue.test.engine.exception.TestClassDefinitionException;
import org.antublue.test.engine.exception.TestEngineException;
import org.antublue.test.engine.internal.Executor;
import org.antublue.test.engine.internal.configuration.Configuration;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.test.descriptor.parameterized.ParameterizedClassTestDescriptor;
import org.antublue.test.engine.internal.test.descriptor.parameterized.ParameterizedMethodTestDescriptor;
import org.antublue.test.engine.internal.test.descriptor.parameterized.ParameterizedTestFactory;
import org.antublue.test.engine.internal.test.descriptor.standard.StandardClassTestDescriptor;
import org.antublue.test.engine.internal.test.descriptor.standard.StandardMethodTestDescriptor;
import org.antublue.test.engine.internal.test.descriptor.standard.StandardTestFactory;
import org.antublue.test.engine.internal.test.extension.ExtensionManager;
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
            // Create an engine descriptor to build the list of test descriptors
            EngineDescriptor engineDescriptor = new EngineDescriptor(uniqueId, getId());

            // Use the test factories to find tests build the test descriptor tree
            new StandardTestFactory().discover(engineDiscoveryRequest, engineDescriptor);
            new ParameterizedTestFactory().discover(engineDiscoveryRequest, engineDescriptor);

            // Filter the engine descriptor
            filterTestClassesByClassName(engineDescriptor);
            filterTestClassesByTag(engineDescriptor);
            filterTestMethodsByMethodName(engineDescriptor);
            filterTestMethodsByTag(engineDescriptor);

            // Prune the engine descriptor
            prune(engineDescriptor);

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
     * Method to filter test classes
     *
     * @param engineDescriptor engineDescriptor
     */
    private void filterTestClassesByClassName(EngineDescriptor engineDescriptor) {
        LOGGER.trace("filterTestClassesByClassName()");

        Configuration configuration = Configuration.getSingleton();

        Optional<String> optional = configuration.get(Constants.TEST_CLASS_INCLUDE_REGEX);
        if (optional.isPresent()) {
            Pattern pattern = Pattern.compile(optional.get());
            Matcher matcher = pattern.matcher("");

            Set<? extends TestDescriptor> children =
                    new LinkedHashSet<>(engineDescriptor.getChildren());
            for (TestDescriptor testDescriptor : children) {
                if (testDescriptor instanceof ParameterizedClassTestDescriptor) {
                    ParameterizedClassTestDescriptor parameterizedClassTestDescriptor =
                            (ParameterizedClassTestDescriptor) testDescriptor;
                    matcher.reset(parameterizedClassTestDescriptor.getTestClass().getName());
                    if (!matcher.find()) {
                        parameterizedClassTestDescriptor.removeFromHierarchy();
                    }
                    continue;
                }
                if (testDescriptor instanceof StandardClassTestDescriptor) {
                    StandardClassTestDescriptor standardClassTestDescriptor =
                            (StandardClassTestDescriptor) testDescriptor;
                    matcher.reset(standardClassTestDescriptor.getTestClass().getName());
                    if (!matcher.find()) {
                        standardClassTestDescriptor.removeFromHierarchy();
                    }
                }
            }
        }

        optional = configuration.get(Constants.TEST_CLASS_EXCLUDE_REGEX);
        if (optional.isPresent()) {
            Pattern pattern = Pattern.compile(optional.get());
            Matcher matcher = pattern.matcher("");

            Set<? extends TestDescriptor> children =
                    new LinkedHashSet<>(engineDescriptor.getDescendants());
            for (TestDescriptor testDescriptor : children) {
                if (testDescriptor instanceof ParameterizedClassTestDescriptor) {
                    ParameterizedClassTestDescriptor parameterizedClassTestDescriptor =
                            (ParameterizedClassTestDescriptor) testDescriptor;
                    matcher.reset(parameterizedClassTestDescriptor.getTestClass().getName());
                    if (matcher.find()) {
                        parameterizedClassTestDescriptor.removeFromHierarchy();
                    }
                    continue;
                }
                if (testDescriptor instanceof StandardClassTestDescriptor) {
                    StandardClassTestDescriptor standardClassTestDescriptor =
                            (StandardClassTestDescriptor) testDescriptor;
                    matcher.reset(standardClassTestDescriptor.getTestClass().getName());
                    if (matcher.find()) {
                        standardClassTestDescriptor.removeFromHierarchy();
                    }
                }
            }
        }
    }

    /**
     * Method to filter test classes
     *
     * @param engineDescriptor engineDescriptor
     */
    private void filterTestClassesByTag(EngineDescriptor engineDescriptor) {
        LOGGER.trace("filterTestClassesByTag()");

        Configuration configuration = Configuration.getSingleton();

        Optional<String> optional = configuration.get(Constants.TEST_CLASS_TAG_INCLUDE_REGEX);
        if (optional.isPresent()) {
            Pattern pattern = Pattern.compile(optional.get());
            Matcher matcher = pattern.matcher("");

            Set<? extends TestDescriptor> children =
                    new LinkedHashSet<>(engineDescriptor.getChildren());
            for (TestDescriptor testDescriptor : children) {
                if (testDescriptor instanceof ParameterizedClassTestDescriptor) {
                    ParameterizedClassTestDescriptor parameterizedClassTestDescriptor =
                            (ParameterizedClassTestDescriptor) testDescriptor;
                    String tag = parameterizedClassTestDescriptor.getTag();
                    if (tag != null) {
                        matcher.reset(tag);
                        if (!matcher.find()) {
                            parameterizedClassTestDescriptor.removeFromHierarchy();
                        }
                    } else {
                        parameterizedClassTestDescriptor.removeFromHierarchy();
                    }
                    continue;
                }
                if (testDescriptor instanceof StandardClassTestDescriptor) {
                    StandardClassTestDescriptor standardClassTestDescriptor =
                            (StandardClassTestDescriptor) testDescriptor;
                    String tag = standardClassTestDescriptor.getTag();
                    if (tag != null) {
                        matcher.reset(tag);
                        if (!matcher.find()) {
                            standardClassTestDescriptor.removeFromHierarchy();
                        }
                    } else {
                        standardClassTestDescriptor.removeFromHierarchy();
                    }
                }
            }
        }

        optional = configuration.get(Constants.TEST_CLASS_TAG_EXCLUDE_REGEX);
        if (optional.isPresent()) {
            Pattern pattern = Pattern.compile(optional.get());
            Matcher matcher = pattern.matcher("");

            Set<? extends TestDescriptor> children =
                    new LinkedHashSet<>(engineDescriptor.getDescendants());
            for (TestDescriptor testDescriptor : children) {
                if (testDescriptor instanceof ParameterizedClassTestDescriptor) {
                    ParameterizedClassTestDescriptor parameterizedClassTestDescriptor =
                            (ParameterizedClassTestDescriptor) testDescriptor;
                    String tag = parameterizedClassTestDescriptor.getTag();
                    if (tag != null) {
                        matcher.reset(tag);
                        if (matcher.find()) {
                            parameterizedClassTestDescriptor.removeFromHierarchy();
                        }
                    }
                    continue;
                }
                if (testDescriptor instanceof StandardClassTestDescriptor) {
                    StandardClassTestDescriptor standardClassTestDescriptor =
                            (StandardClassTestDescriptor) testDescriptor;
                    String tag = standardClassTestDescriptor.getTag();
                    if (tag != null) {
                        matcher.reset(tag);
                        if (matcher.find()) {
                            standardClassTestDescriptor.removeFromHierarchy();
                        }
                    }
                }
            }
        }
    }

    /**
     * Method to filter test methods by test method name
     *
     * @param engineDescriptor engineDescriptor
     */
    private void filterTestMethodsByMethodName(EngineDescriptor engineDescriptor) {
        LOGGER.trace("filterTestMethodsByMethodName()");

        Configuration configuration = Configuration.getSingleton();

        Optional<String> optional = configuration.get(Constants.TEST_METHOD_INCLUDE_REGEX);
        if (optional.isPresent()) {
            LOGGER.trace("[%s] = [%s]", Constants.TEST_METHOD_INCLUDE_REGEX, optional.get());
            Pattern pattern = Pattern.compile(optional.get());
            Matcher matcher = pattern.matcher("");

            Set<? extends TestDescriptor> children =
                    new LinkedHashSet<>(engineDescriptor.getDescendants());
            for (TestDescriptor testDescriptor : children) {
                if (testDescriptor instanceof ParameterizedMethodTestDescriptor) {
                    ParameterizedMethodTestDescriptor parameterizedMethodTestDescriptor =
                            (ParameterizedMethodTestDescriptor) testDescriptor;
                    matcher.reset(parameterizedMethodTestDescriptor.getTestMethod().getName());
                    if (!matcher.find()) {
                        parameterizedMethodTestDescriptor.removeFromHierarchy();
                    }
                    continue;
                }
                if (testDescriptor instanceof StandardMethodTestDescriptor) {
                    StandardMethodTestDescriptor standardMethodTestDescriptor =
                            (StandardMethodTestDescriptor) testDescriptor;
                    matcher.reset(standardMethodTestDescriptor.getTestMethod().getName());
                    if (!matcher.find()) {
                        standardMethodTestDescriptor.removeFromHierarchy();
                    }
                }
            }
        }

        optional = configuration.get(Constants.TEST_METHOD_EXCLUDE_REGEX);
        if (optional.isPresent()) {
            LOGGER.trace("[%s] = [%s]", Constants.TEST_METHOD_EXCLUDE_REGEX, optional.get());
            Pattern pattern = Pattern.compile(optional.get());
            Matcher matcher = pattern.matcher("");

            Set<? extends TestDescriptor> children =
                    new LinkedHashSet<>(engineDescriptor.getChildren());
            for (TestDescriptor testDescriptor : children) {
                if (testDescriptor instanceof ParameterizedMethodTestDescriptor) {
                    ParameterizedMethodTestDescriptor parameterizedMethodTestDescriptor =
                            (ParameterizedMethodTestDescriptor) testDescriptor;
                    matcher.reset(parameterizedMethodTestDescriptor.getTestMethod().getName());
                    if (matcher.find()) {
                        parameterizedMethodTestDescriptor.removeFromHierarchy();
                    }
                    continue;
                }
                if (testDescriptor instanceof StandardMethodTestDescriptor) {
                    StandardMethodTestDescriptor standardMethodTestDescriptor =
                            (StandardMethodTestDescriptor) testDescriptor;
                    matcher.reset(standardMethodTestDescriptor.getTestMethod().getName());
                    if (matcher.find()) {
                        standardMethodTestDescriptor.removeFromHierarchy();
                    }
                }
            }
        }
    }

    /**
     * Method to filter test methods by tag
     *
     * @param engineDescriptor engineDescriptor
     */
    private void filterTestMethodsByTag(EngineDescriptor engineDescriptor) {
        LOGGER.trace("filterTestMethodsByTag()");

        Configuration configuration = Configuration.getSingleton();

        Optional<String> optional = configuration.get(Constants.TEST_METHOD_TAG_INCLUDE_REGEX);
        if (optional.isPresent()) {
            LOGGER.trace("[%s] = [%s]", Constants.TEST_METHOD_TAG_INCLUDE_REGEX, optional.get());
            Pattern pattern = Pattern.compile(optional.get());
            Matcher matcher = pattern.matcher("");

            Set<? extends TestDescriptor> children =
                    new LinkedHashSet<>(engineDescriptor.getDescendants());
            for (TestDescriptor testDescriptor : children) {
                if (testDescriptor instanceof ParameterizedMethodTestDescriptor) {
                    ParameterizedMethodTestDescriptor parameterizedMethodTestDescriptor =
                            (ParameterizedMethodTestDescriptor) testDescriptor;
                    String tag = parameterizedMethodTestDescriptor.getTag();
                    if (tag != null) {
                        matcher.reset(tag);
                        if (!matcher.find()) {
                            parameterizedMethodTestDescriptor.removeFromHierarchy();
                        }
                    } else {
                        parameterizedMethodTestDescriptor.removeFromHierarchy();
                    }
                    continue;
                }
                if (testDescriptor instanceof StandardMethodTestDescriptor) {
                    StandardMethodTestDescriptor standardMethodTestDescriptor =
                            (StandardMethodTestDescriptor) testDescriptor;
                    String tag = standardMethodTestDescriptor.getTag();
                    if (tag != null) {
                        matcher.reset(tag);
                        if (!matcher.find()) {
                            standardMethodTestDescriptor.removeFromHierarchy();
                        }
                    } else {
                        standardMethodTestDescriptor.removeFromHierarchy();
                    }
                }
            }
        }

        optional = configuration.get(Constants.TEST_METHOD_EXCLUDE_REGEX);
        if (optional.isPresent()) {
            LOGGER.trace("[%s] = [%s]", Constants.TEST_METHOD_EXCLUDE_REGEX, optional.get());
            Pattern pattern = Pattern.compile(optional.get());
            Matcher matcher = pattern.matcher("");

            Set<? extends TestDescriptor> children =
                    new LinkedHashSet<>(engineDescriptor.getChildren());
            for (TestDescriptor testDescriptor : children) {
                if (testDescriptor instanceof ParameterizedMethodTestDescriptor) {
                    ParameterizedMethodTestDescriptor parameterizedMethodTestDescriptor =
                            (ParameterizedMethodTestDescriptor) testDescriptor;
                    String tag = parameterizedMethodTestDescriptor.getTag();
                    if (tag != null) {
                        matcher.reset(tag);
                        if (matcher.find()) {
                            parameterizedMethodTestDescriptor.removeFromHierarchy();
                        }
                    }
                    continue;
                }
                if (testDescriptor instanceof StandardMethodTestDescriptor) {
                    StandardMethodTestDescriptor standardMethodTestDescriptor =
                            (StandardMethodTestDescriptor) testDescriptor;
                    String tag = standardMethodTestDescriptor.getTag();
                    if (tag != null) {
                        matcher.reset(tag);
                        if (matcher.find()) {
                            standardMethodTestDescriptor.removeFromHierarchy();
                        }
                    }
                }
            }
        }
    }

    /**
     * Method to prune a test descriptor depth first
     *
     * @param testDescriptor testDescriptor
     */
    private void prune(TestDescriptor testDescriptor) {
        // Prune child test descriptors
        Set<? extends TestDescriptor> children = new LinkedHashSet<>(testDescriptor.getChildren());
        for (TestDescriptor child : children) {
            prune(child);
        }

        // If we are the root, ignore pruning
        if (testDescriptor.isRoot()) {
            return;
        }

        // If test descriptor doesn't have children, remove it
        if (testDescriptor.isContainer() && testDescriptor.getChildren().isEmpty()) {
            testDescriptor.removeFromHierarchy();
        }
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
        Configuration configuration = Configuration.getSingleton();

        // Get the test descriptors and remove them from the engine descriptor
        List<TestDescriptor> testDescriptors = new ArrayList<>(engineDescriptor.getChildren());
        testDescriptors.forEach(engineDescriptor::removeChild);

        // Shuffle or sort the test descriptor list based on configuration
        Optional<String> optionalShuffle = configuration.get(Constants.TEST_CLASS_SHUFFLE);
        if (optionalShuffle.isPresent() && Constants.TRUE.equals(optionalShuffle.get())) {
            Collections.shuffle(testDescriptors);
        } else {
            testDescriptors.sort(Comparator.comparing(TestDescriptor::getDisplayName));
        }

        // Add the shuffled or sorted test descriptors to the engine descriptor
        testDescriptors.forEach(engineDescriptor::addChild);
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

        EngineExecutionListener engineExecutionListener =
                executionRequest.getEngineExecutionListener();

        try {
            engineExecutionListener.executionStarted(executionRequest.getRootTestDescriptor());

            ConfigurationParameters configurationParameters =
                    ConfigurationParameters.getSingleton();

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
