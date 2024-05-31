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
import org.antublue.test.engine.api.Named;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.exception.TestArgumentFailedException;
import org.antublue.test.engine.exception.TestEngineException;
import org.antublue.test.engine.internal.ExtensionManager;
import org.antublue.test.engine.internal.MetadataConstants;
import org.antublue.test.engine.internal.predicate.AnnotationMethodPredicate;
import org.antublue.test.engine.internal.processor.AutoCloseAnnotationProcessor;
import org.antublue.test.engine.internal.processor.LockAnnotationProcessor;
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
import org.junit.platform.engine.support.descriptor.MethodSource;

/** Class to implement a MethodTestDescriptor */
public class MethodTestDescriptor extends ExecutableTestDescriptor {

    private static final TestUtils TEST_UTILS = TestUtils.getInstance();

    private static final AutoCloseAnnotationProcessor AUTO_CLOSE_ANNOTATION_PROCESSOR =
            AutoCloseAnnotationProcessor.getInstance();

    private static final LockAnnotationProcessor LOCK_ANNOTATION_PROCESSOR =
            LockAnnotationProcessor.getInstance();

    private static final ExtensionManager EXTENSION_MANAGER = ExtensionManager.getInstance();

    private final Class<?> testClass;
    private final Named<?> testArgument;
    private final List<Method> beforeEachMethods;
    private final Method testMethod;
    private final List<Method> afterEachMethods;

    private enum State {
        NULL,
        PRE_BEFORE_EACH,
        BEFORE_EACH,
        POST_BEFORE_EACH,
        PRE_TEST,
        TEST,
        POST_TEST,
        PRE_AFTER_EACH,
        AFTER_EACH,
        POST_AFTER_EACH,
        CLOSE_AUTO_CLOSE_FIELDS,
        END
    }

    /** Constructor */
    private MethodTestDescriptor(Builder builder) {
        super(builder.uniqueId, builder.displayName);
        this.testClass = builder.testClass;
        this.testArgument = builder.testArgument;
        this.beforeEachMethods = builder.beforeEachMethods;
        this.testMethod = builder.testMethod;
        this.afterEachMethods = builder.afterEachMethods;
    }

    @Override
    public Type getType() {
        return Type.TEST;
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(MethodSource.from(testMethod));
    }

    public Method getTestMethod() {
        return testMethod;
    }

    public String getTag() {
        return TEST_UTILS.getTag(testMethod);
    }

    @Override
    public void execute(ExecutionRequest executionRequest) {
        getStopWatch().reset();

        Object testInstance = getParent(ExecutableTestDescriptor.class).getTestInstance();
        Preconditions.notNull(testInstance, "testInstance is null");
        setTestInstance(testInstance);

        getMetadata().put(MetadataConstants.TEST_CLASS, testClass);
        getMetadata().put(MetadataConstants.TEST_ARGUMENT, testArgument);
        getMetadata().put(MetadataConstants.TEST_METHOD, testMethod);

        if (!getThrowableContext().isEmpty()) {
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

        StateMachine<State> stateMachine = new StateMachine<>(getUniqueId().toString());

        try {
            stateMachine
                    .definition(State.NULL, this::begin, State.PRE_BEFORE_EACH)
                    .definition(
                            State.PRE_BEFORE_EACH,
                            this::preBeforeEach,
                            State.BEFORE_EACH,
                            State.POST_BEFORE_EACH)
                    .definition(State.BEFORE_EACH, this::beforeEach, State.POST_BEFORE_EACH)
                    .definition(State.POST_BEFORE_EACH, this::postBeforeEach, State.PRE_TEST)
                    .definition(State.PRE_TEST, this::preTest, State.TEST, State.POST_TEST)
                    .definition(State.TEST, this::test, State.POST_TEST)
                    .definition(State.POST_TEST, this::postTest, State.PRE_AFTER_EACH)
                    .definition(
                            State.PRE_AFTER_EACH,
                            this::preAfterEach,
                            State.AFTER_EACH,
                            State.POST_AFTER_EACH)
                    .definition(State.AFTER_EACH, this::afterEach, State.POST_AFTER_EACH)
                    .definition(
                            State.POST_AFTER_EACH,
                            this::postAfterEach,
                            State.CLOSE_AUTO_CLOSE_FIELDS)
                    .definition(
                            State.CLOSE_AUTO_CLOSE_FIELDS, this::closeAutoCloseFields, State.END)
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
        setTestInstance(null);

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
            getParent(ArgumentTestDescriptor.class)
                    .getThrowableContext()
                    .add(
                            testClass,
                            new TestArgumentFailedException(
                                    format(
                                            "Exception testing test argument name [%s]",
                                            testArgument.getName())));
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
        return State.PRE_BEFORE_EACH;
    }

    private State preBeforeEach() {
        Preconditions.notNull(getTestInstance(), "testInstance is null");

        EXTENSION_MANAGER.preBeforeEachMethodsCallback(
                getTestInstance(), testArgument, getThrowableContext());

        if (getThrowableContext().isEmpty()) {
            return State.BEFORE_EACH;
        } else {
            return State.POST_BEFORE_EACH;
        }
    }

    private State beforeEach() {
        Preconditions.notNull(getTestInstance(), "testInstance is null");

        try {
            for (Method method : beforeEachMethods) {
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

        return State.POST_BEFORE_EACH;
    }

    private State postBeforeEach() {
        Preconditions.notNull(getTestInstance(), "testInstance is null");

        EXTENSION_MANAGER.postBeforeEachMethodsCallback(
                getTestInstance(), testArgument, getThrowableContext());

        if (getThrowableContext().isEmpty()) {
            return State.PRE_TEST;
        } else {
            return State.PRE_AFTER_EACH;
        }
    }

    private State preTest() {
        Preconditions.notNull(getTestInstance(), "testInstance is null");

        EXTENSION_MANAGER.preTestMethodsCallback(
                testMethod, getTestInstance(), testArgument, getThrowableContext());

        if (getThrowableContext().isEmpty()) {
            return State.TEST;
        } else {
            return State.POST_TEST;
        }
    }

    private State test() {
        Preconditions.notNull(getTestInstance(), "testInstance is null");

        LOCK_ANNOTATION_PROCESSOR.processLocks(testMethod);
        TEST_UTILS.invoke(testMethod, getTestInstance(), testArgument, getThrowableContext());
        LOCK_ANNOTATION_PROCESSOR.processUnlocks(testMethod);

        return State.POST_TEST;
    }

    private State postTest() {
        Preconditions.notNull(getTestInstance(), "testInstance is null");

        EXTENSION_MANAGER.postTestMethodsCallback(
                testMethod, getTestInstance(), testArgument, getThrowableContext());

        return State.PRE_AFTER_EACH;
    }

    private State preAfterEach() {
        Preconditions.notNull(getTestInstance(), "testInstance is null");

        EXTENSION_MANAGER.preAfterEachMethodsCallback(
                getTestInstance(), testArgument, getThrowableContext());

        if (getThrowableContext().isEmpty()) {
            return State.AFTER_EACH;
        } else {
            return State.POST_AFTER_EACH;
        }
    }

    private State afterEach() {
        Preconditions.notNull(getTestInstance(), "testInstance is null");

        for (Method method : afterEachMethods) {
            LOCK_ANNOTATION_PROCESSOR.processLocks(method);
            TEST_UTILS.invoke(method, getTestInstance(), testArgument, getThrowableContext());
            LOCK_ANNOTATION_PROCESSOR.processUnlocks(method);
        }

        return State.POST_AFTER_EACH;
    }

    private State postAfterEach() {
        Preconditions.notNull(getTestInstance(), "testInstance is null");

        EXTENSION_MANAGER.postAfterEachMethodsCallback(
                getTestInstance(), testArgument, getThrowableContext());

        return State.CLOSE_AUTO_CLOSE_FIELDS;
    }

    private State closeAutoCloseFields() {
        Preconditions.notNull(getTestInstance(), "testInstance is null");

        AUTO_CLOSE_ANNOTATION_PROCESSOR.conclude(
                getTestInstance(),
                AutoCloseAnnotationProcessor.Type.AFTER_EACH,
                getThrowableContext());

        return State.END;
    }

    private State end() {
        return null;
    }

    /** Class to implement a Builder */
    public static class Builder {

        private Class<?> testClass;
        private Named<?> testArgument;
        private Method testMethod;

        private UniqueId uniqueId;
        private String displayName;
        private List<Method> beforeEachMethods;
        private List<Method> afterEachMethods;

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
         * Method to set the test argument index and test argument
         *
         * @param testArgumentIndex testArgumentIndex
         * @param testArgument testArgument
         * @return this
         */
        public Builder setTestArgument(int testArgumentIndex, Named<?> testArgument) {
            this.testArgument = testArgument;
            return this;
        }

        /**
         * Method to set the test method
         *
         * @param testMethod testMethod
         * @return this
         */
        public Builder setTestMethod(Method testMethod) {
            this.testMethod = testMethod;
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
                                .append(MethodTestDescriptor.class.getName(), testMethod.getName());

                displayName = TEST_UTILS.getDisplayName(testMethod);

                beforeEachMethods =
                        ReflectionSupport.findMethods(
                                testClass,
                                AnnotationMethodPredicate.of(TestEngine.BeforeEach.class),
                                HierarchyTraversalMode.TOP_DOWN);

                beforeEachMethods =
                        TEST_UTILS.orderTestMethods(
                                beforeEachMethods, HierarchyTraversalMode.TOP_DOWN);

                afterEachMethods =
                        ReflectionSupport.findMethods(
                                testClass,
                                AnnotationMethodPredicate.of(TestEngine.AfterEach.class),
                                HierarchyTraversalMode.BOTTOM_UP);

                afterEachMethods =
                        TEST_UTILS.orderTestMethods(
                                afterEachMethods, HierarchyTraversalMode.BOTTOM_UP);

                TestDescriptor testDescriptor = new MethodTestDescriptor(this);

                parentTestDescriptor.addChild(testDescriptor);
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable t) {
                throw new TestEngineException(t);
            }
        }
    }
}
