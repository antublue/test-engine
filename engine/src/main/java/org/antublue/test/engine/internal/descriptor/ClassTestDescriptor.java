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

package org.antublue.test.engine.internal.descriptor;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import org.antublue.test.engine.api.Named;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.exception.TestEngineException;
import org.antublue.test.engine.internal.MetadataConstants;
import org.antublue.test.engine.internal.annotation.ContextAnnotationUtils;
import org.antublue.test.engine.internal.annotation.RandomAnnotationUtils;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.predicate.AnnotationMethodPredicate;
import org.antublue.test.engine.internal.util.StandardStreams;
import org.antublue.test.engine.internal.util.ThrowableCollector;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;

/** Class to implement a ClassTestDescriptor */
@SuppressWarnings("PMD.UnusedPrivateMethod")
public class ClassTestDescriptor extends ExecutableTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassTestDescriptor.class);

    private final Class<?> testClass;
    private final List<Method> prepareMethods;
    private final List<Method> concludeMethods;

    /**
     * Constructor
     *
     * @param builder builder
     */
    private ClassTestDescriptor(Builder builder) {
        super(builder.uniqueId, builder.displayName);
        this.testClass = builder.testClass;
        this.prepareMethods = builder.prepareMethods;
        this.concludeMethods = builder.concludeMethods;
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(ClassSource.from(testClass));
    }

    @Override
    public Type getType() {
        return Type.CONTAINER_AND_TEST;
    }

    public Class<?> getTestClass() {
        return testClass;
    }

    public String getTag() {
        return getTag(testClass);
    }

    @Override
    public void execute(ExecutionRequest executionRequest) {
        LOGGER.trace("execute(ExecutionRequest executionRequest)");

        getStopWatch().reset();

        getMetadata().put(MetadataConstants.TEST_CLASS, testClass);

        setExecutionRequest(executionRequest);
        executionRequest.getEngineExecutionListener().executionStarted(this);

        ThrowableCollector throwableCollector = getThrowableCollector();

        throwableCollector.execute(this::setContextFields);
        if (throwableCollector.isEmpty()) {
            throwableCollector.execute(this::setRandomFields);
            if (throwableCollector.isEmpty()) {
                throwableCollector.execute(this::createTestInstance);
                if (throwableCollector.isEmpty()) {
                    throwableCollector.execute(this::prepare);
                    if (throwableCollector.isEmpty()) {
                        execute();
                    } else {
                        skip(executionRequest);
                    }
                    throwableCollector.execute(this::conclude);
                }
                throwableCollector.execute(this::destroyTestInstance);
            }
            throwableCollector.execute(this::clearRandomFields);
        }
        throwableCollector.execute(this::clearContextFields);

        getStopWatch().stop();

        getMetadata()
                .put(
                        MetadataConstants.TEST_DESCRIPTOR_ELAPSED_TIME,
                        getStopWatch().elapsedNanoseconds());

        if (getThrowableCollector().isEmpty()) {
            getMetadata().put(MetadataConstants.TEST_DESCRIPTOR_STATUS, MetadataConstants.PASS);
            executionRequest
                    .getEngineExecutionListener()
                    .executionFinished(this, TestExecutionResult.successful());
        } else {
            getMetadata().put(MetadataConstants.TEST_DESCRIPTOR_STATUS, MetadataConstants.FAIL);
            executionRequest
                    .getEngineExecutionListener()
                    .executionFinished(
                            this,
                            TestExecutionResult.failed(
                                    getThrowableCollector().getThrowables().get(0)));
        }

        StandardStreams.flush();
    }

    private void setContextFields() throws Throwable {
        LOGGER.trace("setContextFields() testClass [%s]", getTestClass().getName());

        ContextAnnotationUtils.injectContextFields(getTestClass());
    }

    private void setRandomFields() throws Throwable {
        LOGGER.trace("setRandomFields() testClass [%s]", getTestClass().getName());

        RandomAnnotationUtils.injectRandomFields(getTestClass());
    }

    private void prepare() throws Throwable {
        LOGGER.trace(
                "prepare() testClass [%s] testInstance [%s]",
                getTestClass().getName(), getTestInstance());

        for (Method method : prepareMethods) {
            LOGGER.trace(
                    "prepare() testClass [%s] testInstance [%s] method [%s]",
                    getTestClass().getName(), getTestInstance(), method);

            method.setAccessible(true);
            method.invoke(getTestInstance());
        }
    }

    private void createTestInstance() throws Throwable {
        LOGGER.trace("createTestInstance() testClass [%s]", getTestClass().getName());

        setTestInstance(
                getTestClass()
                        .getDeclaredConstructor((Class<?>[]) null)
                        .newInstance((Object[]) null));

        LOGGER.trace(
                "createTestInstance() testClass [%s] testInstance [%s]",
                getTestClass().getName(), getTestInstance());
    }

    private void execute() {
        LOGGER.trace("execute() testClass [%s]", getTestClass().getName());

        getChildren()
                .forEach(
                        testDescriptor -> {
                            if (testDescriptor instanceof ExecutableTestDescriptor) {
                                ((ExecutableTestDescriptor) testDescriptor)
                                        .execute(getExecutionRequest());
                            }
                        });
    }

    private void skip() {
        LOGGER.trace("skip() testClass [%s]", getTestClass().getName());

        getChildren()
                .forEach(
                        testDescriptor -> {
                            if (testDescriptor instanceof ExecutableTestDescriptor) {
                                ((ExecutableTestDescriptor) testDescriptor)
                                        .skip(getExecutionRequest());
                            }
                        });
    }

    private void clearRandomFields() throws Throwable {
        LOGGER.trace("clearRandomFields() testClass [%s]", getTestClass().getName());

        RandomAnnotationUtils.clearRandomFields(getTestClass());
    }

    private void conclude() throws Throwable {
        LOGGER.trace(
                "conclude() testClass [%s] testInstance [%s]",
                getTestClass().getName(), getTestInstance());

        for (Method method : concludeMethods) {
            LOGGER.trace(
                    "conclude() testClass [%s] testInstance [%s] method [%s]",
                    getTestClass().getName(), getTestInstance(), method);

            method.setAccessible(true);
            method.invoke(getTestInstance());
        }
    }

    private void destroyTestInstance() {
        LOGGER.trace(
                "destroyTestInstance() testClass [%s]",
                getTestClass().getName(), getTestInstance());

        setTestInstance(null);
    }

    private void clearContextFields() throws Throwable {
        ContextAnnotationUtils.clearContextFields(getTestClass());
    }

    /** Class to implement a Builder */
    public static class Builder {

        private Class<?> testClass;
        private List<Named<?>> testArguments;
        private List<Method> testMethods;

        private UniqueId uniqueId;
        private String displayName;
        private List<Method> prepareMethods;
        private List<Method> concludeMethods;

        /**
         * Method to set the test class
         *
         * @param testClass testClass
         * @return this
         */
        public Builder setTestClass(Class<?> testClass) {
            this.testClass = testClass;
            return this;
        }

        /**
         * Method to set a list of test arguments
         *
         * @param testArguments testArguments
         * @return this
         */
        public Builder setTestArguments(List<Named<?>> testArguments) {
            this.testArguments = testArguments;
            return this;
        }

        /**
         * Method to set a list of test methods
         *
         * @param testMethods testMethods
         * @return this
         */
        public Builder setTestMethods(List<Method> testMethods) {
            this.testMethods = testMethods;
            return this;
        }

        /**
         * Method to build the test descriptor and any children
         *
         * @param parentTestDescriptor parentTestDescriptor
         */
        public void build(TestDescriptor parentTestDescriptor) {
            try {
                uniqueId =
                        parentTestDescriptor
                                .getUniqueId()
                                .append(ClassTestDescriptor.class.getName(), testClass.getName());

                displayName = getDisplayName(testClass);

                prepareMethods =
                        ReflectionSupport.findMethods(
                                testClass,
                                AnnotationMethodPredicate.of(TestEngine.Prepare.class),
                                HierarchyTraversalMode.TOP_DOWN);

                prepareMethods = orderTestMethods(prepareMethods, HierarchyTraversalMode.TOP_DOWN);

                concludeMethods =
                        ReflectionSupport.findMethods(
                                testClass,
                                AnnotationMethodPredicate.of(TestEngine.Conclude.class),
                                HierarchyTraversalMode.BOTTOM_UP);

                concludeMethods =
                        orderTestMethods(concludeMethods, HierarchyTraversalMode.BOTTOM_UP);

                TestDescriptor testDescriptor = new ClassTestDescriptor(this);

                parentTestDescriptor.addChild(testDescriptor);

                int testArgumentIndex = 0;
                for (Named<?> testArgument : testArguments) {
                    new ArgumentTestDescriptor.Builder()
                            .setTestClass(testClass)
                            .setTestArgument(testArgumentIndex, testArgument)
                            .setTestMethods(testMethods)
                            .build(testDescriptor);
                    testArgumentIndex++;
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable t) {
                throw new TestEngineException(t);
            }
        }
    }
}
