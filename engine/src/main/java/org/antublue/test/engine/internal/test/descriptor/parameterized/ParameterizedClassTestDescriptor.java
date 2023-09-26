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
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.exception.TestEngineException;
import org.antublue.test.engine.internal.test.descriptor.ExecutableTestDescriptor;
import org.antublue.test.engine.internal.test.descriptor.MetadataConstants;
import org.antublue.test.engine.internal.test.descriptor.filter.AnnotationFieldFilter;
import org.antublue.test.engine.internal.test.descriptor.filter.AnnotationMethodFilter;
import org.antublue.test.engine.internal.test.extension.ExtensionManager;
import org.antublue.test.engine.internal.test.util.AutoCloseProcessor;
import org.antublue.test.engine.internal.test.util.LockProcessor;
import org.antublue.test.engine.internal.test.util.ReflectionUtils;
import org.antublue.test.engine.internal.test.util.StateMachine;
import org.antublue.test.engine.internal.test.util.TestUtils;
import org.antublue.test.engine.internal.test.util.ThrowableContext;
import org.antublue.test.engine.internal.util.StandardStreams;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;

/** Class to implement a ParameterClassTestDescriptor */
@SuppressWarnings("unchecked")
public class ParameterizedClassTestDescriptor extends ExecutableTestDescriptor {

    protected static final ExtensionManager EXTENSION_MANAGER = ExtensionManager.getSingleton();

    private final Class<?> testClass;
    private final List<Field> autoCloseFields;
    private final List<Method> prepareMethods;
    private final List<Method> concludeMethods;

    private enum State {
        BEGIN,
        PRE_INSTANTIATE,
        INSTANTIATE,
        POST_INSTANTIATE,
        PRE_PREPARE,
        PREPARE,
        POST_PREPARE,
        EXECUTE_OR_SKIP,
        PRE_CONCLUDE,
        CONCLUDE,
        POST_CONCLUDE,
        CLOSE_AUTO_CLOSE_FIELDS,
        END
    }

    /**
     * Constructor
     *
     * @param builder builder
     */
    private ParameterizedClassTestDescriptor(Builder builder) {
        super(builder.uniqueId, builder.displayName);
        this.testClass = builder.testClass;
        this.autoCloseFields = builder.autoCloseFields;
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
        return TestUtils.getTag(testClass);
    }

    @Override
    public void execute(ExecutionRequest executionRequest) {
        getStopWatch().start();

        getMetadata().put(MetadataConstants.TEST_CLASS, testClass);

        setExecutionRequest(executionRequest);
        executionRequest.getEngineExecutionListener().executionStarted(this);

        try {
            EXTENSION_MANAGER.initialize(testClass);
        } catch (Throwable t) {
            throw new TestEngineException(
                    String.format("Exception loading extensions for test class [%s]", testClass));
        }

        StateMachine<State> stateMachine =
                new StateMachine<State>(getUniqueId().toString())
                        .define(State.BEGIN, this::begin, State.PRE_INSTANTIATE)
                        .define(
                                State.PRE_INSTANTIATE,
                                this::preInstantiate,
                                State.INSTANTIATE,
                                State.POST_INSTANTIATE)
                        .define(State.INSTANTIATE, this::instantiate, State.POST_INSTANTIATE)
                        .define(
                                State.POST_INSTANTIATE,
                                this::postInstantiate,
                                State.PRE_PREPARE,
                                State.EXECUTE_OR_SKIP)
                        .define(
                                State.PRE_PREPARE,
                                this::prePrepare,
                                State.PREPARE,
                                State.POST_PREPARE)
                        .define(State.PREPARE, this::prepare, State.POST_PREPARE)
                        .define(State.POST_PREPARE, this::postPrepare, State.EXECUTE_OR_SKIP)
                        .define(State.EXECUTE_OR_SKIP, this::executeOrSkip, State.PRE_CONCLUDE)
                        .define(
                                State.PRE_CONCLUDE,
                                this::preConclude,
                                State.CONCLUDE,
                                State.POST_CONCLUDE)
                        .define(State.CONCLUDE, this::conclude, State.POST_CONCLUDE)
                        .define(
                                State.POST_CONCLUDE,
                                this::postConclude,
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
        return State.PRE_INSTANTIATE;
    }

    private State preInstantiate() {
        EXTENSION_MANAGER.preInstantiateCallback(testClass, getThrowableContext());

        if (getThrowableContext().isEmpty()) {
            return State.INSTANTIATE;
        } else {
            return State.END;
        }
    }

    private State instantiate() {
        Preconditions.condition(getThrowableContext().isEmpty(), "Programming error");

        try {
            setTestInstance(ReflectionUtils.newInstance(testClass));
        } catch (Throwable t) {
            getThrowableContext().add(testClass, t);
        }

        return State.POST_INSTANTIATE;
    }

    private State postInstantiate() {
        EXTENSION_MANAGER.postInstantiateCallback(getTestInstance(), getThrowableContext());

        if (getThrowableContext().isEmpty()) {
            return State.PRE_PREPARE;
        } else {
            return State.END;
        }
    }

    private State prePrepare() {
        Preconditions.notNull(getTestInstance(), "testInstance is null");

        EXTENSION_MANAGER.prePrepareMethodsCallback(getTestInstance(), getThrowableContext());

        if (getThrowableContext().isEmpty()) {
            return State.PREPARE;
        } else {
            return State.POST_PREPARE;
        }
    }

    private State prepare() {
        Preconditions.notNull(getTestInstance(), "testInstance is null");

        try {
            for (Method method : prepareMethods) {
                LockProcessor.processLocks(method);
                TestUtils.invoke(method, getTestInstance(), null, getThrowableContext());
                LockProcessor.processUnlocks(method);
                if (!getThrowableContext().isEmpty()) {
                    break;
                }
            }
        } catch (Throwable t) {
            getThrowableContext().add(testClass, t);
        }

        return State.POST_PREPARE;
    }

    private State postPrepare() {
        Preconditions.notNull(getTestInstance(), "testInstance is null");

        EXTENSION_MANAGER.postPrepareMethodsCallback(getTestInstance(), getThrowableContext());

        return State.EXECUTE_OR_SKIP;
    }

    private State executeOrSkip() {
        Preconditions.notNull(getTestInstance(), "testInstance is null");

        getChildren()
                .forEach(
                        testDescriptor -> {
                            if (testDescriptor instanceof ExecutableTestDescriptor) {
                                ((ExecutableTestDescriptor) testDescriptor)
                                        .execute(getExecutionRequest());
                            }
                        });

        return State.PRE_CONCLUDE;
    }

    private State preConclude() {
        EXTENSION_MANAGER.preConcludeMethodsCallback(getTestInstance(), getThrowableContext());

        if (getThrowableContext().isEmpty()) {
            return State.CONCLUDE;
        } else {
            return State.POST_CONCLUDE;
        }
    }

    private State conclude() {
        Preconditions.notNull(getTestInstance(), "testInstance is null");

        for (Method method : concludeMethods) {
            LockProcessor.processLocks(method);
            TestUtils.invoke(method, getTestInstance(), null, getThrowableContext());
            LockProcessor.processUnlocks(method);
        }

        return State.POST_CONCLUDE;
    }

    private State postConclude() {
        Preconditions.notNull(getTestInstance(), "testInstance is null");

        EXTENSION_MANAGER.postConcludeMethodsCallback(getTestInstance(), getThrowableContext());

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
        EXTENSION_MANAGER.preDestroyCallback(
                testClass, Optional.ofNullable(getTestInstance()), new ThrowableContext());

        return null;
    }

    /** Class to implement a Builder */
    public static class Builder {

        private Class<?> testClass;
        private List<Argument> testArguments;
        private List<Method> testMethods;

        private UniqueId uniqueId;
        private String displayName;
        private List<Field> autoCloseFields;
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
        public Builder setTestArguments(List<Argument> testArguments) {
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
                EXTENSION_MANAGER.initialize(testClass);

                uniqueId =
                        parentTestDescriptor
                                .getUniqueId()
                                .append(
                                        ParameterizedClassTestDescriptor.class.getName(),
                                        testClass.getName());

                displayName = TestUtils.getDisplayName(testClass);

                prepareMethods =
                        ReflectionSupport.findMethods(
                                testClass,
                                AnnotationMethodFilter.of(TestEngine.Prepare.class),
                                HierarchyTraversalMode.TOP_DOWN);

                prepareMethods =
                        TestUtils.orderTestMethods(prepareMethods, HierarchyTraversalMode.TOP_DOWN);

                concludeMethods =
                        ReflectionSupport.findMethods(
                                testClass,
                                AnnotationMethodFilter.of(TestEngine.Conclude.class),
                                HierarchyTraversalMode.BOTTOM_UP);

                concludeMethods =
                        TestUtils.orderTestMethods(
                                concludeMethods, HierarchyTraversalMode.BOTTOM_UP);

                autoCloseFields =
                        ReflectionSupport.findFields(
                                testClass,
                                AnnotationFieldFilter.of(TestEngine.AutoClose.Conclude.class),
                                HierarchyTraversalMode.BOTTOM_UP);

                TestDescriptor testDescriptor = new ParameterizedClassTestDescriptor(this);

                parentTestDescriptor.addChild(testDescriptor);

                int testArgumentIndex = 0;
                for (Argument testArgument : testArguments) {
                    new ParameterizedArgumentTestDescriptor.Builder()
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
