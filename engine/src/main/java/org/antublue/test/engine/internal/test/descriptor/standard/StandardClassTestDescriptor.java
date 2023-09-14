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

package org.antublue.test.engine.internal.test.descriptor.standard;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.antublue.test.engine.exception.TestEngineException;
import org.antublue.test.engine.internal.test.descriptor.ExecutableTestDescriptor;
import org.antublue.test.engine.internal.test.descriptor.MetadataConstants;
import org.antublue.test.engine.internal.test.descriptor.parameterized.ParameterizedTestFilters;
import org.antublue.test.engine.internal.test.util.AutoCloseProcessor;
import org.antublue.test.engine.internal.test.util.RandomFieldInjector;
import org.antublue.test.engine.internal.test.util.StateMachine;
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
public class StandardClassTestDescriptor extends ExecutableTestDescriptor {

    private final Class<?> testClass;

    private enum State {
        BEGIN,
        PRE_INSTANTIATE,
        INSTANTIATE,
        POST_INSTANTIATE,
        SET_RANDOM_FIELDS,
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
    private StandardClassTestDescriptor(Builder builder) {
        setUniqueId(builder.uniqueId);
        setDisplayName(builder.displayName);
        this.testClass = builder.testClass;
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
        return TEST_UTILS.getTag(testClass);
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
                                State.SET_RANDOM_FIELDS,
                                State.EXECUTE_OR_SKIP)
                        .define(State.SET_RANDOM_FIELDS, this::setRandomFields, State.PRE_PREPARE)
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
        if (getThrowableContext().isEmpty()) {
            return State.PRE_INSTANTIATE;
        } else {
            return State.END;
        }
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
        try {
            setTestInstance(REFLECTION_UTILS.newInstance(testClass.getName()));
        } catch (Throwable t) {
            getThrowableContext().add(testClass, t);
        }

        return State.POST_INSTANTIATE;
    }

    private State postInstantiate() {
        EXTENSION_MANAGER.postInstantiateCallback(getTestInstance(), getThrowableContext());

        if (getThrowableContext().isEmpty()) {
            return State.SET_RANDOM_FIELDS;
        } else {
            return State.END;
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
            return State.PRE_PREPARE;
        } catch (Throwable t) {
            getThrowableContext().add(testClass, t);
            return State.EXECUTE_OR_SKIP;
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

        List<Method> prepareMethods =
                ReflectionSupport.findMethods(
                        testClass,
                        StandardTestFilters.PREPARE_METHOD,
                        HierarchyTraversalMode.TOP_DOWN);

        prepareMethods = TEST_UTILS.orderTestMethods(prepareMethods);

        for (Method method : prepareMethods) {
            LOCK_PROCESSOR.processLocks(method);
            TEST_UTILS.invoke(method, getTestInstance(), null, getThrowableContext());
            LOCK_PROCESSOR.processUnlocks(method);
            // TODO add optional configuration code to test all methods if there is a test failure
            if (!getThrowableContext().isEmpty()) {
                break;
            }
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
                        (Consumer<TestDescriptor>)
                                testDescriptor -> {
                                    if (testDescriptor instanceof ExecutableTestDescriptor) {
                                        ((ExecutableTestDescriptor) testDescriptor)
                                                .execute(getExecutionRequest());
                                    }
                                });

        return State.PRE_CONCLUDE;
    }

    private State preConclude() {
        Preconditions.notNull(getTestInstance(), "testInstance is null");

        EXTENSION_MANAGER.preConcludeMethodsCallback(getTestInstance(), getThrowableContext());

        if (getThrowableContext().isEmpty()) {
            return State.CONCLUDE;
        } else {
            return State.POST_CONCLUDE;
        }
    }

    private State conclude() {
        Preconditions.notNull(getTestInstance(), "testInstance is null");

        List<Method> concludeMethods =
                ReflectionSupport.findMethods(
                        testClass,
                        ParameterizedTestFilters.CONCLUDE_METHOD,
                        HierarchyTraversalMode.BOTTOM_UP);

        concludeMethods = TEST_UTILS.orderTestMethods(concludeMethods);

        for (Method method : concludeMethods) {
            LOCK_PROCESSOR.processLocks(method);
            TEST_UTILS.invoke(method, getTestInstance(), null, getThrowableContext());
            LOCK_PROCESSOR.processUnlocks(method);
        }

        return State.POST_CONCLUDE;
    }

    private State postConclude() {
        EXTENSION_MANAGER.postConcludeMethodsCallback(getTestInstance(), getThrowableContext());

        return State.CLOSE_AUTO_CLOSE_FIELDS;
    }

    private State closeAutoCloseFields() {
        Preconditions.notNull(getTestInstance(), "testInstance is null");

        AutoCloseProcessor autoCloseProcessor = AutoCloseProcessor.getSingleton();

        List<Field> testFields =
                REFLECTION_UTILS.findFields(
                        testClass, StandardTestFilters.AFTER_CONCLUDE_AUTO_CLOSE_FIELD);

        for (Field testField : testFields) {
            autoCloseProcessor.close(getTestInstance(), testField, getThrowableContext());
        }

        return State.END;
    }

    private State end() {
        EXTENSION_MANAGER.preDestroyCallback(
                testClass, Optional.ofNullable(getTestInstance()), new ThrowableContext());

        return null;
    }

    public static class Builder {

        private TestDescriptor parentTestDescriptor;
        private Class<?> testClass;
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
                                        StandardClassTestDescriptor.class.getName(),
                                        UUID.randomUUID() + "/" + testClass.getName());

                displayName = TEST_UTILS.getDisplayName(testClass);

                TestDescriptor testDescriptor = new StandardClassTestDescriptor(this);

                parentTestDescriptor.addChild(testDescriptor);

                List<Method> testMethods =
                        ReflectionSupport.findMethods(
                                testClass,
                                StandardTestFilters.TEST_METHOD,
                                HierarchyTraversalMode.TOP_DOWN);

                testMethods = TEST_UTILS.orderTestMethods(testMethods);

                ThrowableContext throwableContext = new ThrowableContext();
                EXTENSION_MANAGER.postTestMethodDiscovery(testClass, testMethods, throwableContext);
                throwableContext.throwFirst();

                for (Method testMethod : testMethods) {
                    if (testMethodFilter == null || testMethodFilter.test(testMethod)) {
                        new StandardMethodTestDescriptor.Builder()
                                .setParentTestDescriptor(testDescriptor)
                                .setTestClass(testClass)
                                .setTestMethod(testMethod)
                                .build();
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
