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

import static java.lang.String.format;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.antublue.test.engine.api.Named;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.exception.TestClassFailedException;
import org.antublue.test.engine.exception.TestEngineException;
import org.antublue.test.engine.internal.ExtensionManager;
import org.antublue.test.engine.internal.MetadataConstants;
import org.antublue.test.engine.internal.predicate.AnnotationMethodPredicate;
import org.antublue.test.engine.internal.processor.ArgumentAnnotationProcessor;
import org.antublue.test.engine.internal.processor.AutoCloseAnnotationProcessor;
import org.antublue.test.engine.internal.processor.LockAnnotationProcessor;
import org.antublue.test.engine.internal.processor.RandomAnnotationProcessor;
import org.antublue.test.engine.internal.util.StandardStreams;
import org.antublue.test.engine.internal.util.TestUtils;
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

    private static final TestUtils TEST_UTILS = TestUtils.getInstance();

    private static final ArgumentAnnotationProcessor ARGUMENT_ANNOTATION_PROCESSOR =
            ArgumentAnnotationProcessor.getInstance();

    private static final RandomAnnotationProcessor RANDOM_ANNOTATION_PROCESSOR =
            RandomAnnotationProcessor.getInstance();

    private static final LockAnnotationProcessor LOCK_ANNOTATION_PROCESSOR =
            LockAnnotationProcessor.getInstance();

    private static final AutoCloseAnnotationProcessor AUTO_CLOSE_ANNOTATION_PROCESSOR =
            AutoCloseAnnotationProcessor.getInstance();

    private static final ExtensionManager EXTENSION_MANAGER = ExtensionManager.getInstance();

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
        getStopWatch().reset();

        setExecutionRequest(executionRequest);

        Object testInstance = getParent(ExecutableTestDescriptor.class).getTestInstance();
        Preconditions.notNull(testInstance, "testInstance is null");
        setTestInstance(testInstance);

        getMetadata().put(MetadataConstants.TEST_CLASS, testClass);
        getMetadata().put(MetadataConstants.TEST_ARGUMENT, testArgument);

        executionRequest.getEngineExecutionListener().executionStarted(this);

        setArgumentFields();
        postSetArgumentFields();
        if (getThrowableContext().isEmpty()) {
            setRandomFields();
            if (getThrowableContext().isEmpty()) {
                beforeAllMethods();
                if (getThrowableContext().isEmpty()) {
                    execute();
                }
                afterAllMethods();
                clearRandomFields();
            }
            clearArgumentFields();
        }

        // postBeforeAllMethods();

        getStopWatch().stop();

        getMetadata()
                .put(
                        MetadataConstants.TEST_DESCRIPTOR_ELAPSED_TIME,
                        getStopWatch().elapsedNanoseconds());

        if (getThrowableContext().isEmpty()) {
            getMetadata().put(MetadataConstants.TEST_DESCRIPTOR_STATUS, MetadataConstants.PASS);
            executionRequest
                    .getEngineExecutionListener()
                    .executionFinished(this, TestExecutionResult.successful());
        } else {
            getParent(ClassTestDescriptor.class)
                    .getThrowableContext()
                    .add(
                            getTestInstance().getClass(),
                            new TestClassFailedException(
                                    format(
                                            "Exception testing test class [%s]",
                                            TEST_UTILS.getDisplayName(testClass))));
            getMetadata().put(MetadataConstants.TEST_DESCRIPTOR_STATUS, MetadataConstants.FAIL);
            executionRequest
                    .getEngineExecutionListener()
                    .executionFinished(
                            this,
                            TestExecutionResult.failed(
                                    getThrowableContext().getThrowables().get(0)));
        }

        StandardStreams.flush();
    }

    private void setArgumentFields() {
        ARGUMENT_ANNOTATION_PROCESSOR.setArgumentFields(
                getTestInstance(), testArgument, getThrowableContext());
    }

    private void postSetArgumentFields() {
        // TODO
    }

    private void setRandomFields() {
        RANDOM_ANNOTATION_PROCESSOR.setRandomFields(getTestInstance(), getThrowableContext());
    }

    private void postSetRandomFields() {
        // TODO
    }

    private void beforeAllMethods() {
        try {
            for (Method method : beforeAllMethods) {
                LOCK_ANNOTATION_PROCESSOR.processLocks(method);
                TEST_UTILS.invoke(method, getTestInstance(), testArgument, getThrowableContext());
                LOCK_ANNOTATION_PROCESSOR.processUnlocks(method);
                if (!getThrowableContext().isEmpty()) {
                    break;
                }
            }
        } catch (Throwable t) {
            getThrowableContext().add(testClass, t);
        }
    }

    private void postBeforeAllMethods() {
        // TODO
    }

    private void execute() {
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

    private void afterAllMethods() {
        for (Method method : afterAllMethods) {
            LOCK_ANNOTATION_PROCESSOR.processLocks(method);
            TEST_UTILS.invoke(method, getTestInstance(), testArgument, getThrowableContext());
            LOCK_ANNOTATION_PROCESSOR.processUnlocks(method);
        }
    }

    private void postAfterAllMethods() {
        // DO NOTHING
    }

    private void clearRandomFields() {
        RANDOM_ANNOTATION_PROCESSOR.clearRandomFields(getTestInstance(), getThrowableContext());
    }

    private void postClearRandomFields() {
        // TODO
    }

    private void clearArgumentFields() {
        ARGUMENT_ANNOTATION_PROCESSOR.clearArgumentFields(
                getTestInstance(), null, getThrowableContext());
    }

    private void postClearArgumentFields() {
        // TODO
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
                        TEST_UTILS.orderTestMethods(
                                beforeAllMethods, HierarchyTraversalMode.TOP_DOWN);

                afterAllMethods =
                        ReflectionSupport.findMethods(
                                testClass,
                                AnnotationMethodPredicate.of(TestEngine.AfterAll.class),
                                HierarchyTraversalMode.BOTTOM_UP);

                afterAllMethods =
                        TEST_UTILS.orderTestMethods(
                                afterAllMethods, HierarchyTraversalMode.BOTTOM_UP);

                TestDescriptor testDescriptor = new ArgumentTestDescriptor(this);

                parentTestDescriptor.addChild(testDescriptor);

                for (Method testMethod : testMethods) {
                    new MethodTestDescriptor.Builder()
                            .setTestClass(testClass)
                            .setTestArgument(testArgumentIndex, testArgument)
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
