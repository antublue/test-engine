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
import org.antublue.test.engine.ExtensionManager;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.exception.TestClassFailedException;
import org.antublue.test.engine.exception.TestEngineException;
import org.antublue.test.engine.internal.MetadataConstants;
import org.antublue.test.engine.internal.predicate.AnnotationMethodPredicate;
import org.antublue.test.engine.internal.processor.ArgumentAnnotationProcessor;
import org.antublue.test.engine.internal.processor.AutoCloseAnnotationProcessor;
import org.antublue.test.engine.internal.processor.LockAnnotationProcessor;
import org.antublue.test.engine.internal.processor.RandomAnnotationProcessor;
import org.antublue.test.engine.internal.util.StandardStreams;
import org.antublue.test.engine.internal.util.StateMachine;
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
@SuppressWarnings("PMD.AvoidAccessibilityAlteration")
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
    private final Argument testArgument;
    private final List<Method> beforeAllMethods;
    private final List<Method> afterAllMethods;

    private enum State {
        NULL,
        SET_ARGUMENT_FIELDS,
        SET_RANDOM_FIELDS,
        PRE_BEFORE_ALL,
        BEFORE_ALL,
        POST_BEFORE_ALL,
        EXECUTE_OR_SKIP,
        PRE_AFTER_ALL,
        AFTER_ALL,
        POST_AFTER_ALL,
        CLEAR_ARGUMENTS_FIELDS,
        CLOSE_AUTO_CLOSE_FIELDS,
        END
    }

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

    public Argument getTestArgument() {
        return testArgument;
    }

    @Override
    public void execute(ExecutionRequest executionRequest) {
        getStopWatch().start();

        Object testInstance = getParent(ExecutableTestDescriptor.class).getTestInstance();
        Preconditions.notNull(testInstance, "testInstance is null");
        setTestInstance(testInstance);

        getMetadata().put(MetadataConstants.TEST_CLASS, testClass);
        getMetadata().put(MetadataConstants.TEST_ARGUMENT, testArgument);

        if (!getThrowableContext().isEmpty()) {
            getChildren()
                    .forEach(
                            (Consumer<TestDescriptor>)
                                    testDescriptor -> {
                                        if (testDescriptor instanceof ExecutableTestDescriptor) {
                                            ((ExecutableTestDescriptor) testDescriptor)
                                                    .execute(executionRequest);
                                        }
                                    });

            getStopWatch().stop();
            getMetadata()
                    .put(
                            MetadataConstants.TEST_DESCRIPTOR_ELAPSED_TIME,
                            getStopWatch().elapsedNanoseconds());
            getMetadata().put(MetadataConstants.TEST_DESCRIPTOR_STATUS, MetadataConstants.SKIP);
            executionRequest.getEngineExecutionListener().executionSkipped(this, "");
            return;
        }

        setExecutionRequest(executionRequest);
        executionRequest.getEngineExecutionListener().executionStarted(this);

        StateMachine<State> stateMachine = new StateMachine<State>(getUniqueId().toString());

        try {
            stateMachine
                    .definition(State.NULL, this::begin, State.SET_ARGUMENT_FIELDS)
                    .definition(
                            State.SET_ARGUMENT_FIELDS,
                            this::setArgumentFields,
                            State.SET_RANDOM_FIELDS,
                            State.EXECUTE_OR_SKIP)
                    .definition(
                            State.SET_RANDOM_FIELDS,
                            this::setRandomFields,
                            State.PRE_BEFORE_ALL,
                            State.EXECUTE_OR_SKIP)
                    .definition(
                            State.PRE_BEFORE_ALL,
                            this::preBeforeAll,
                            State.BEFORE_ALL,
                            State.POST_BEFORE_ALL)
                    .definition(State.BEFORE_ALL, this::beforeAll, State.POST_BEFORE_ALL)
                    .definition(State.POST_BEFORE_ALL, this::postBeforeAll, State.EXECUTE_OR_SKIP)
                    .definition(State.EXECUTE_OR_SKIP, this::executeOrSkip, State.PRE_AFTER_ALL)
                    .definition(
                            State.PRE_AFTER_ALL,
                            this::preAfterAll,
                            State.AFTER_ALL,
                            State.POST_AFTER_ALL)
                    .definition(State.AFTER_ALL, this::afterAll, State.POST_AFTER_ALL)
                    .definition(
                            State.POST_AFTER_ALL, this::postAfterAll, State.CLOSE_AUTO_CLOSE_FIELDS)
                    .definition(
                            State.CLOSE_AUTO_CLOSE_FIELDS,
                            this::closeAutoCloseFields,
                            State.CLEAR_ARGUMENTS_FIELDS)
                    .definition(State.CLEAR_ARGUMENTS_FIELDS, this::clearArgumentFields, State.END)
                    .afterEach(
                            () -> {
                                StandardStreams.flush();
                                throttle();
                                return null;
                            })
                    .end(State.END, this::end)
                    .run(State.NULL);
        } catch (Throwable t) {
            getThrowableContext().add(testClass, t);
        }

        setExecutionRequest(null);

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
                                    String.format(
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

    private State begin() {
        return State.SET_ARGUMENT_FIELDS;
    }

    private State setArgumentFields() {
        Preconditions.notNull(getTestInstance(), "testInstance is null");

        ARGUMENT_ANNOTATION_PROCESSOR.prepare(
                getTestInstance(), testArgument, getThrowableContext());

        if (!getThrowableContext().isEmpty()) {
            return State.EXECUTE_OR_SKIP;
        }

        return State.SET_RANDOM_FIELDS;
    }

    private State setRandomFields() {
        Preconditions.notNull(getTestInstance(), "testInstance is null");

        RANDOM_ANNOTATION_PROCESSOR.prepare(getTestInstance(), getThrowableContext());

        if (!getThrowableContext().isEmpty()) {
            return State.EXECUTE_OR_SKIP;
        }

        return State.PRE_BEFORE_ALL;
    }

    private State preBeforeAll() {
        EXTENSION_MANAGER.preBeforeAllMethodsCallback(
                getTestInstance(), testArgument, getThrowableContext());

        if (getThrowableContext().isEmpty()) {
            return State.BEFORE_ALL;
        } else {
            return State.POST_BEFORE_ALL;
        }
    }

    private State beforeAll() {
        Preconditions.notNull(getTestInstance(), "testInstance is null");

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

        return State.POST_BEFORE_ALL;
    }

    private State postBeforeAll() {
        Preconditions.notNull(getTestInstance(), "testInstance is null");

        EXTENSION_MANAGER.postBeforeAllMethodsCallback(
                getTestInstance(), testArgument, getThrowableContext());

        return State.EXECUTE_OR_SKIP;
    }

    private State executeOrSkip() {
        Preconditions.notNull(getTestInstance(), "testInstance is null");

        getChildren()
                .forEach(
                        (Consumer<TestDescriptor>)
                                testDescriptor -> {
                                    if (testDescriptor instanceof ExecutableTestDescriptor) {
                                        ((ExecutableTestDescriptor) testDescriptor)
                                                .execute(getExecutionRequest());
                                    }
                                });

        return State.PRE_AFTER_ALL;
    }

    private State preAfterAll() {
        Preconditions.notNull(getTestInstance(), "testInstance is null");

        EXTENSION_MANAGER.preAfterAllMethodsCallback(
                getTestInstance(), testArgument, getThrowableContext());

        if (getThrowableContext().isEmpty()) {
            return State.AFTER_ALL;
        } else {
            return State.POST_AFTER_ALL;
        }
    }

    private State afterAll() {
        Preconditions.notNull(getTestInstance(), "testInstance is null");

        for (Method method : afterAllMethods) {
            LOCK_ANNOTATION_PROCESSOR.processLocks(method);
            TEST_UTILS.invoke(method, getTestInstance(), testArgument, getThrowableContext());
            LOCK_ANNOTATION_PROCESSOR.processUnlocks(method);
        }

        return State.POST_AFTER_ALL;
    }

    private State postAfterAll() {
        Preconditions.notNull(getTestInstance(), "testInstance is null");

        EXTENSION_MANAGER.postAfterAllMethodsCallback(
                getTestInstance(), testArgument, getThrowableContext());

        return State.CLOSE_AUTO_CLOSE_FIELDS;
    }

    private State closeAutoCloseFields() {
        Preconditions.notNull(getTestInstance(), "testInstance is null");

        AUTO_CLOSE_ANNOTATION_PROCESSOR.conclude(
                getTestInstance(),
                AutoCloseAnnotationProcessor.Type.AFTER_ALL,
                getThrowableContext());

        return State.CLEAR_ARGUMENTS_FIELDS;
    }

    private State clearArgumentFields() {
        Preconditions.notNull(getTestInstance(), "testInstance is null");

        ARGUMENT_ANNOTATION_PROCESSOR.conclude(getTestInstance(), null, getThrowableContext());

        return State.END;
    }

    private State end() {
        return null;
    }

    /** Class to implement a Builder */
    public static class Builder {

        private Class<?> testClass;
        private int testArgumentIndex;
        private Argument testArgument;
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
        public Builder setTestArgument(int testArgumentIndex, Argument testArgument) {
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
                                        testArgumentIndex + "/" + testArgument.name());

                displayName = testArgument.name();

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
