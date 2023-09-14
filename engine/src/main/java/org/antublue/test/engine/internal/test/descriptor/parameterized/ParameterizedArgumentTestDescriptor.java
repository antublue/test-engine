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
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.exception.TestClassFailedException;
import org.antublue.test.engine.exception.TestEngineException;
import org.antublue.test.engine.internal.test.descriptor.ExecutableTestDescriptor;
import org.antublue.test.engine.internal.test.descriptor.MetadataConstants;
import org.antublue.test.engine.internal.test.util.AutoCloseProcessor;
import org.antublue.test.engine.internal.test.util.RandomFieldInjector;
import org.antublue.test.engine.internal.test.util.StateMachine;
import org.antublue.test.engine.internal.test.util.ThrowableContext;
import org.antublue.test.engine.internal.util.StandardStreams;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;

/** Class to implement a ParameterArgumentTestDescriptor */
public class ParameterizedArgumentTestDescriptor extends ExecutableTestDescriptor {

    private final Class<?> testClass;
    private final Method testArgumentSupplierMethod;
    private final Argument testArgument;

    private enum State {
        BEGIN,
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
    private ParameterizedArgumentTestDescriptor(Builder builder) {
        setUniqueId(builder.uniqueId);
        setDisplayName(builder.displayName);
        this.testClass = builder.testClass;
        this.testArgumentSupplierMethod = builder.testArgumentSupplierMethod;
        this.testArgument = builder.testArgument;
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(MethodSource.from(testArgumentSupplierMethod));
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
                            getStopWatch().elapsedNanoTime());
            getMetadata().put(MetadataConstants.TEST_DESCRIPTOR_STATUS, MetadataConstants.SKIP);
            executionRequest.getEngineExecutionListener().executionSkipped(this, "");
            return;
        }

        setExecutionRequest(executionRequest);
        executionRequest.getEngineExecutionListener().executionStarted(this);

        StateMachine<State> stateMachine =
                new StateMachine<State>(getUniqueId().toString())
                        .define(State.BEGIN, this::begin, State.SET_ARGUMENT_FIELDS)
                        .define(
                                State.SET_ARGUMENT_FIELDS,
                                this::setArgumentFields,
                                State.SET_RANDOM_FIELDS,
                                State.EXECUTE_OR_SKIP)
                        .define(
                                State.SET_RANDOM_FIELDS,
                                this::setRandomFields,
                                State.PRE_BEFORE_ALL,
                                State.EXECUTE_OR_SKIP)
                        .define(
                                State.PRE_BEFORE_ALL,
                                this::preBeforeAll,
                                State.BEFORE_ALL,
                                State.POST_BEFORE_ALL)
                        .define(State.BEFORE_ALL, this::beforeAll, State.POST_BEFORE_ALL)
                        .define(State.POST_BEFORE_ALL, this::postBeforeAll, State.EXECUTE_OR_SKIP)
                        .define(State.EXECUTE_OR_SKIP, this::executeOrSkip, State.PRE_AFTER_ALL)
                        .define(
                                State.PRE_AFTER_ALL,
                                this::preAfterAll,
                                State.AFTER_ALL,
                                State.POST_AFTER_ALL)
                        .define(State.AFTER_ALL, this::afterAll, State.POST_AFTER_ALL)
                        .define(
                                State.POST_AFTER_ALL,
                                this::postAfterAll,
                                State.CLEAR_ARGUMENTS_FIELDS)
                        .define(
                                State.CLEAR_ARGUMENTS_FIELDS,
                                this::clearArgumentFields,
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
            getParent(ParameterizedClassTestDescriptor.class)
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

        try {
            List<Field> fields =
                    REFLECTION_UTILS.findFields(testClass, ParameterizedTestFilters.ARGUMENT_FIELD);
            for (Field field : fields) {
                field.set(getTestInstance(), testArgument);
                EXTENSION_MANAGER.postFieldCallback(
                        field, getTestInstance(), getThrowableContext());
                if (!getThrowableContext().isEmpty()) {
                    return State.EXECUTE_OR_SKIP;
                }
            }

            return State.SET_RANDOM_FIELDS;
        } catch (Throwable t) {
            getThrowableContext().add(testClass, t);
            return State.EXECUTE_OR_SKIP;
        }
    }

    private State setRandomFields() {
        Preconditions.notNull(getTestInstance(), "testInstance is null");

        try {
            List<Field> fields =
                    REFLECTION_UTILS.findFields(testClass, ParameterizedTestFilters.RANDOM_FIELD);
            for (Field field : fields) {
                RandomFieldInjector.inject(getTestInstance(), field);
                EXTENSION_MANAGER.postFieldCallback(
                        field, getTestInstance(), getThrowableContext());
                if (!getThrowableContext().isEmpty()) {
                    return State.EXECUTE_OR_SKIP;
                }
            }

            return State.PRE_BEFORE_ALL;
        } catch (Throwable t) {
            getThrowableContext().add(testClass, t);
            return State.EXECUTE_OR_SKIP;
        }
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
            List<Method> beforeAllMethods =
                    ReflectionSupport.findMethods(
                            testClass,
                            ParameterizedTestFilters.BEFORE_ALL_METHOD,
                            HierarchyTraversalMode.TOP_DOWN);

            beforeAllMethods = TEST_UTILS.orderTestMethods(beforeAllMethods);

            for (Method method : beforeAllMethods) {
                LOCK_PROCESSOR.processLocks(method);
                TEST_UTILS.invoke(method, getTestInstance(), testArgument, getThrowableContext());
                LOCK_PROCESSOR.processUnlocks(method);
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

        List<Method> afterAllMethods =
                ReflectionUtils.findMethods(
                        testClass,
                        ParameterizedTestFilters.AFTER_ALL_METHOD,
                        ReflectionUtils.HierarchyTraversalMode.BOTTOM_UP);

        afterAllMethods = TEST_UTILS.orderTestMethods(afterAllMethods);

        for (Method method : afterAllMethods) {
            LOCK_PROCESSOR.processLocks(method);
            TEST_UTILS.invoke(method, getTestInstance(), testArgument, getThrowableContext());
            LOCK_PROCESSOR.processUnlocks(method);
        }

        return State.POST_AFTER_ALL;
    }

    private State postAfterAll() {
        Preconditions.notNull(getTestInstance(), "testInstance is null");

        EXTENSION_MANAGER.postAfterAllMethodsCallback(
                getTestInstance(), testArgument, getThrowableContext());

        return State.CLEAR_ARGUMENTS_FIELDS;
    }

    private State clearArgumentFields() {
        Preconditions.notNull(getTestInstance(), "testInstance is null");

        List<Field> fields =
                REFLECTION_UTILS.findFields(testClass, ParameterizedTestFilters.ARGUMENT_FIELD);
        for (Field field : fields) {
            try {
                field.set(getTestInstance(), null);
            } catch (Throwable t) {
                getThrowableContext().add(testClass, t);
            }
        }

        return State.CLOSE_AUTO_CLOSE_FIELDS;
    }

    private State closeAutoCloseFields() {
        Preconditions.notNull(getTestInstance(), "testInstance is null");

        AutoCloseProcessor autoCloseProcessor = AutoCloseProcessor.getSingleton();

        List<Field> testFields =
                REFLECTION_UTILS.findFields(
                        testClass, ParameterizedTestFilters.AFTER_ALL_AUTO_CLOSE_FIELD);
        for (Field testField : testFields) {
            autoCloseProcessor.close(getTestInstance(), testField, getThrowableContext());
        }

        return State.END;
    }

    private State end() {
        return null;
    }

    public static class Builder {

        private TestDescriptor parentTestDescriptor;
        private Class<?> testClass;
        private Method testArgumentSupplierMethod;
        private Argument testArgument;
        private Predicate<Method> testMethodFilter;

        private UniqueId uniqueId;
        private String displayName;

        public Builder setParentTestDescriptor(TestDescriptor parentTestDescriptor) {
            this.parentTestDescriptor = parentTestDescriptor;
            return this;
        }

        public Builder setTestClass(Class<?> testClass) {
            this.testClass = testClass;
            return this;
        }

        public Builder setTestArgumentSupplierMethod(Method testArgumentSupplierMethod) {
            this.testArgumentSupplierMethod = testArgumentSupplierMethod;
            return this;
        }

        public Builder setTestArgument(Argument testArgument) {
            this.testArgument = testArgument;
            return this;
        }

        public Builder setTestMethodFilter(Predicate<Method> testMethodFilter) {
            this.testMethodFilter = testMethodFilter;
            return this;
        }

        public TestDescriptor build() {
            try {
                EXTENSION_MANAGER.initialize(testClass);

                uniqueId =
                        parentTestDescriptor
                                .getUniqueId()
                                .append(
                                        ParameterizedArgumentTestDescriptor.class.getName(),
                                        UUID.randomUUID() + "/" + testArgument.name());

                displayName = testArgument.name();

                TestDescriptor testDescriptor = new ParameterizedArgumentTestDescriptor(this);

                parentTestDescriptor.addChild(testDescriptor);

                List<Method> testMethods =
                        ReflectionSupport.findMethods(
                                testClass,
                                ParameterizedTestFilters.TEST_METHOD,
                                HierarchyTraversalMode.TOP_DOWN);

                testMethods = TEST_UTILS.orderTestMethods(testMethods);

                ThrowableContext throwableContext = new ThrowableContext();
                EXTENSION_MANAGER.postTestMethodDiscovery(testClass, testMethods, throwableContext);
                throwableContext.throwFirst();

                for (Method testMethod : testMethods) {
                    if (testMethodFilter == null || testMethodFilter.test(testMethod)) {
                        testDescriptor.addChild(
                                new ParameterizedMethodTestDescriptor.Builder()
                                        .setParentTestDescriptor(testDescriptor)
                                        .setTestClass(testClass)
                                        .setTestArgument(testArgument)
                                        .setTestMethod(testMethod)
                                        .build());
                    }
                }

                return testDescriptor;
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable t) {
                throw new TestEngineException(t);
            }
        }
    }
}
