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

package org.antublue.test.engine.internal.test.descriptor.parameterized;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.exception.TestArgumentFailedException;
import org.antublue.test.engine.exception.TestEngineException;
import org.antublue.test.engine.internal.test.descriptor.ExecutableTestDescriptor;
import org.antublue.test.engine.internal.test.descriptor.MetadataConstants;
import org.antublue.test.engine.internal.test.descriptor.filter.AnnotationFieldFilter;
import org.antublue.test.engine.internal.test.descriptor.filter.AnnotationMethodFilter;
import org.antublue.test.engine.internal.test.descriptor.standard.StandardMethodTestDescriptor;
import org.antublue.test.engine.internal.test.extension.ExtensionManager;
import org.antublue.test.engine.internal.test.util.AutoCloseProcessor;
import org.antublue.test.engine.internal.test.util.LockProcessor;
import org.antublue.test.engine.internal.test.util.StateMachine;
import org.antublue.test.engine.internal.test.util.TestUtils;
import org.antublue.test.engine.internal.util.StandardStreams;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;

/** Class to implement a ParameterMethodTestDescriptor */
public class ParameterizedMethodTestDescriptor extends ExecutableTestDescriptor {

    protected static final ExtensionManager EXTENSION_MANAGER = ExtensionManager.getSingleton();

    private final Class<?> testClass;
    private final Argument testArgument;
    private final List<Field> autoCloseFields;
    private final List<Method> beforeEachMethods;
    private final Method testMethod;
    private final List<Method> afterEachMethods;

    private enum State {
        BEGIN,
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
    private ParameterizedMethodTestDescriptor(Builder builder) {
        super(builder.uniqueId, builder.displayName);
        this.testClass = builder.testClass;
        this.testArgument = builder.testArgument;
        this.autoCloseFields = builder.autoCloseFields;
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
        return TestUtils.getTag(testMethod);
    }

    @Override
    public void execute(ExecutionRequest executionRequest) {
        getStopWatch().start();

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
                            getStopWatch().elapsedNanoTime());
            getMetadata().put(MetadataConstants.TEST_DESCRIPTOR_STATUS, MetadataConstants.SKIP);
            executionRequest.getEngineExecutionListener().executionSkipped(this, "");
            return;
        }

        setExecutionRequest(executionRequest);
        executionRequest.getEngineExecutionListener().executionStarted(this);

        StateMachine<State> stateMachine =
                new StateMachine<State>(getUniqueId().toString())
                        .define(State.BEGIN, this::begin, State.PRE_BEFORE_EACH)
                        .define(
                                State.PRE_BEFORE_EACH,
                                this::preBeforeEach,
                                State.BEFORE_EACH,
                                State.POST_BEFORE_EACH)
                        .define(State.BEFORE_EACH, this::beforeEach, State.POST_BEFORE_EACH)
                        .define(State.POST_BEFORE_EACH, this::postBeforeEach, State.PRE_TEST)
                        .define(State.PRE_TEST, this::preTest, State.TEST, State.POST_TEST)
                        .define(State.TEST, this::test, State.POST_TEST)
                        .define(State.POST_TEST, this::postTest, State.PRE_AFTER_EACH)
                        .define(
                                State.PRE_AFTER_EACH,
                                this::preAfterEach,
                                State.AFTER_EACH,
                                State.POST_AFTER_EACH)
                        .define(State.AFTER_EACH, this::afterEach, State.POST_AFTER_EACH)
                        .define(
                                State.POST_AFTER_EACH,
                                this::postAfterEach,
                                State.CLOSE_AUTO_CLOSE_FIELDS)
                        .define(
                                State.CLOSE_AUTO_CLOSE_FIELDS,
                                this::closeAutoCloseFields,
                                State.END)
                        .afterEach(
                                () -> {
                                    StandardStreams.flush();
                                    throttle();
                                    return null;
                                })
                        .end(State.END, this::end);

        try {
            stateMachine.run(State.BEGIN);
        } catch (Throwable t) {
            getThrowableContext().add(testClass, t);
        }

        setExecutionRequest(null);
        setTestInstance(null);

        getStopWatch().stop();
        getMetadata()
                .put(
                        MetadataConstants.TEST_DESCRIPTOR_ELAPSED_TIME,
                        getStopWatch().elapsedNanoTime());

        if (getThrowableContext().isEmpty()) {
            getMetadata().put(MetadataConstants.TEST_DESCRIPTOR_STATUS, MetadataConstants.PASS);
            executionRequest
                    .getEngineExecutionListener()
                    .executionFinished(this, TestExecutionResult.successful());
        } else {
            getParent(ParameterizedArgumentTestDescriptor.class)
                    .getThrowableContext()
                    .add(
                            testClass,
                            new TestArgumentFailedException(
                                    String.format(
                                            "Exception testing test argument name [%s]",
                                            testArgument.name())));
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
                LockProcessor.processLocks(method);
                TestUtils.invoke(method, getTestInstance(), testArgument, getThrowableContext());
                LockProcessor.processUnlocks(method);
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

        LockProcessor.processLocks(testMethod);
        TestUtils.invoke(testMethod, getTestInstance(), testArgument, getThrowableContext());
        LockProcessor.processUnlocks(testMethod);

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
            LockProcessor.processLocks(method);
            TestUtils.invoke(method, getTestInstance(), testArgument, getThrowableContext());
            LockProcessor.processUnlocks(method);
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

        AutoCloseProcessor autoCloseProcessor = AutoCloseProcessor.getSingleton();

        for (Field field : autoCloseFields) {
            autoCloseProcessor.close(getTestInstance(), field, getThrowableContext());
        }

        return State.END;
    }

    private State end() {
        return null;
    }

    public static class Builder {

        private TestDescriptor parentTestDescriptor;
        private Class<?> testClass;
        private Argument testArgument;
        private Method testMethod;

        private UniqueId uniqueId;
        private String displayName;
        private List<Field> autoCloseFields;
        private List<Method> beforeEachMethods;
        private List<Method> afterEachMethods;

        public Builder setParentTestDescriptor(TestDescriptor parentTestDescriptor) {
            this.parentTestDescriptor = parentTestDescriptor;
            return this;
        }

        public Builder setTestClass(Class<?> testClass) {
            this.testClass = testClass;
            return this;
        }

        public Builder setTestArgument(Argument testArgument) {
            this.testArgument = testArgument;
            return this;
        }

        public Builder setTestMethod(Method testMethod) {
            this.testMethod = testMethod;
            return this;
        }

        public TestDescriptor build() {
            try {
                uniqueId =
                        parentTestDescriptor
                                .getUniqueId()
                                .append(
                                        StandardMethodTestDescriptor.class.getName(),
                                        UUID.randomUUID() + "/" + testMethod.getName());

                displayName = TestUtils.getDisplayName(testMethod);

                autoCloseFields =
                        ReflectionSupport.findFields(
                                testClass,
                                AnnotationFieldFilter.of(TestEngine.AutoClose.AfterEach.class),
                                HierarchyTraversalMode.TOP_DOWN);

                beforeEachMethods =
                        ReflectionSupport.findMethods(
                                testClass,
                                AnnotationMethodFilter.of(TestEngine.BeforeEach.class),
                                HierarchyTraversalMode.TOP_DOWN);

                beforeEachMethods =
                        TestUtils.orderTestMethods(
                                beforeEachMethods, HierarchyTraversalMode.TOP_DOWN);

                afterEachMethods =
                        ReflectionSupport.findMethods(
                                testClass,
                                AnnotationMethodFilter.of(TestEngine.AfterEach.class),
                                HierarchyTraversalMode.BOTTOM_UP);

                afterEachMethods =
                        TestUtils.orderTestMethods(
                                afterEachMethods, HierarchyTraversalMode.BOTTOM_UP);

                validate();

                TestDescriptor testDescriptor = new ParameterizedMethodTestDescriptor(this);

                parentTestDescriptor.addChild(testDescriptor);

                return testDescriptor;
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable t) {
                throw new TestEngineException(t);
            }
        }

        private void validate() {
            // TODO validate
        }
    }
}
