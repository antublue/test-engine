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
import java.util.function.Consumer;
import org.antublue.test.engine.api.Named;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.exception.TestEngineException;
import org.antublue.test.engine.internal.MetadataConstants;
import org.antublue.test.engine.internal.annotation.ArgumentAnnotationUtils;
import org.antublue.test.engine.internal.annotation.RandomAnnotationUtils;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.predicate.AnnotationMethodPredicate;
import org.antublue.test.engine.internal.util.StandardStreams;
import org.antublue.test.engine.internal.util.ThrowableCollector;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;

/** Class to implement a ArgumentTestDescriptor */
@SuppressWarnings({"PMD.UnusedPrivateMethod", "PMD.AvoidAccessibilityAlteration"})
public class ArgumentTestDescriptor extends ExecutableTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArgumentTestDescriptor.class);

    private final Class<?> testClass;
    private final Named<?> testArgument;
    private final List<Method> beforeAllMethods;
    private final List<Method> afterAllMethods;

    /**
     * Constructor
     *
     * @param builder builder
     */
    private ArgumentTestDescriptor(Builder builder) {
        super(builder.uniqueId, builder.displayName);
        this.testClass = builder.testClass;
        this.testArgument = builder.testArgument;
        this.beforeAllMethods = builder.beforeAllMethods;
        this.afterAllMethods = builder.afterAllMethods;
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(ClassSource.from(testClass));
    }

    @Override
    public Type getType() {
        return Type.CONTAINER_AND_TEST;
    }

    public Named<?> getTestArgument() {
        return testArgument;
    }

    @Override
    public void execute(ExecutionRequest executionRequest) {
        LOGGER.trace("execute(ExecutionRequest executionRequest)");

        getStopWatch().reset();

        setExecutionRequest(executionRequest);

        Object testInstance = getParent(ExecutableTestDescriptor.class).getTestInstance();
        Preconditions.notNull(testInstance, "testInstance is null");
        setTestInstance(testInstance);

        getMetadata().put(MetadataConstants.TEST_CLASS, testClass);
        getMetadata().put(MetadataConstants.TEST_ARGUMENT, testArgument);

        executionRequest.getEngineExecutionListener().executionStarted(this);

        ThrowableCollector throwableCollector = getThrowableCollector();

        throwableCollector.execute(this::setArgumentFields);
        if (throwableCollector.isEmpty()) {
            throwableCollector.execute(this::setRandomFields);
            if (throwableCollector.isEmpty()) {
                throwableCollector.execute(this::beforeAllMethods);
                if (throwableCollector.isEmpty()) {
                    execute();
                }
                throwableCollector.execute(this::afterAllMethods);
            }
            throwableCollector.execute(this::clearRandomFields);
        }
        throwableCollector.execute(this::clearArgumentFields);

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
            /*
            getParent(ClassTestDescriptor.class)
                    .getThrowableCollector()
                    .add(
                            new TestClassFailedException(
                                    format(
                                            "Exception testing test class [%s]",
                                            getDisplayName(testClass))));
             */
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

    private void setArgumentFields() throws Throwable {
        LOGGER.trace(
                "setArgumentFields() testClass [%s] testInstance [%s] testArgument [%s]",
                getTestInstance().getClass().getName(), getTestInstance(), testArgument);

        ArgumentAnnotationUtils.injectArgumentFields(getTestInstance(), testArgument);
    }

    private void setRandomFields() throws Throwable {
        LOGGER.trace(
                "setRandomFields() testClass [%s] testInstance [%s] testArgument [%s]",
                getTestInstance().getClass().getName(), getTestInstance(), testArgument);

        RandomAnnotationUtils.injectRandomFields(getTestInstance());
    }

    private void beforeAllMethods() throws Throwable {
        LOGGER.trace(
                "beforeAllMethods() testClass [%s] testInstance [%s]",
                getTestInstance().getClass().getName(), getTestInstance());

        for (Method method : beforeAllMethods) {
            LOGGER.trace(
                    "beforeAllMethods() testClass [%s] testInstance [%s] method [%s]",
                    getTestInstance().getClass().getName(), getTestInstance(), method);

            method.setAccessible(true);
            method.invoke(getTestInstance());
        }
    }

    private void execute() {
        LOGGER.trace(
                "execute() testClass [%s] testInstance [%s]",
                getTestInstance().getClass().getName(), getTestInstance());

        getChildren()
                .forEach(
                        (Consumer<TestDescriptor>)
                                testDescriptor -> {
                                    if (testDescriptor instanceof ExecutableTestDescriptor) {
                                        ((ExecutableTestDescriptor) testDescriptor)
                                                .execute(getExecutionRequest());
                                    }
                                });
    }

    private void afterAllMethods() throws Throwable {
        LOGGER.trace(
                "afterAllMethods() testClass [%s] testInstance [%s]",
                getTestInstance().getClass().getName(), getTestInstance());

        for (Method method : afterAllMethods) {
            LOGGER.trace(
                    "afterAllMethods() testClass [%s] testInstance [%s] method [%s]",
                    getTestInstance().getClass().getName(), getTestInstance(), method);

            method.setAccessible(true);
            method.invoke(getTestInstance());
        }
    }

    private void clearRandomFields() throws Throwable {
        LOGGER.trace(
                "clearRandomFields() testClass [%s] testInstance [%s]",
                getTestInstance().getClass().getName(), getTestInstance());

        RandomAnnotationUtils.clearRandomFields(getTestInstance());
    }

    private void clearArgumentFields() throws Throwable {
        LOGGER.trace(
                "clearArgumentFields() testClass [%s] testInstance [%s]",
                getTestInstance().getClass().getName(), getTestInstance());

        ArgumentAnnotationUtils.injectArgumentFields(getTestInstance(), null);
    }

    /** Class to implement a Builder */
    public static class Builder {

        private Class<?> testClass;
        private int testArgumentIndex;
        private Named<?> testArgument;
        private List<Method> testMethods;

        private UniqueId uniqueId;
        private String displayName;
        private List<Method> beforeAllMethods;
        private List<Method> afterAllMethods;

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
         * Method to set the test argument
         *
         * @param testArgumentIndex testArgumentIndex
         * @param testArgument testArgument
         * @return this
         */
        public Builder setTestArgument(int testArgumentIndex, Named<?> testArgument) {
            this.testArgumentIndex = testArgumentIndex;
            this.testArgument = testArgument;
            return this;
        }

        /**
         * Method to set the list of test methods
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
                                .append(
                                        ArgumentTestDescriptor.class.getName(),
                                        testArgumentIndex + "/" + testArgument.getName());

                displayName = testArgument.getName();

                beforeAllMethods =
                        ReflectionSupport.findMethods(
                                testClass,
                                AnnotationMethodPredicate.of(TestEngine.BeforeAll.class),
                                HierarchyTraversalMode.TOP_DOWN);

                beforeAllMethods =
                        orderTestMethods(beforeAllMethods, HierarchyTraversalMode.TOP_DOWN);

                afterAllMethods =
                        ReflectionSupport.findMethods(
                                testClass,
                                AnnotationMethodPredicate.of(TestEngine.AfterAll.class),
                                HierarchyTraversalMode.BOTTOM_UP);

                afterAllMethods =
                        orderTestMethods(afterAllMethods, HierarchyTraversalMode.BOTTOM_UP);

                TestDescriptor testDescriptor = new ArgumentTestDescriptor(this);

                parentTestDescriptor.addChild(testDescriptor);

                for (Method testMethod : testMethods) {
                    new MethodTestDescriptor.Builder()
                            .setTestClass(testClass)
                            .setTestArgument(testArgument)
                            .setTestMethod(testMethod)
                            .build(testDescriptor);
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable t) {
                throw new TestEngineException(t);
            }
        }
    }
}
