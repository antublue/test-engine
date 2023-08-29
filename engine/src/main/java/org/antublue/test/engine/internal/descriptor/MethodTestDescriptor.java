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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.Extension;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.ExecutorContext;
import org.antublue.test.engine.internal.TestEngineUtils;
import org.antublue.test.engine.internal.descriptor.util.AutoCloseProcessor;
import org.antublue.test.engine.internal.descriptor.util.LockProcessor;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.statemachine.StateMachine;
import org.antublue.test.engine.internal.util.Invoker;
import org.antublue.test.engine.internal.util.ReflectionUtils;
import org.antublue.test.engine.internal.util.ThrowableCollector;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;

/** Class to implement a method descriptor */
public final class MethodTestDescriptor extends ExtendedAbstractTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodTestDescriptor.class);

    private enum State {
        BEGIN,
        BEFORE_BEFORE_EACH,
        BEFORE_EACH,
        AFTER_BEFORE_EACH,
        BEFORE_TEST,
        TEST,
        AFTER_TEST,
        BEFORE_AFTER_EACH,
        AFTER_EACH,
        AFTER_AFTER_EACH,
        END
    }

    private final Class<?> testClass;
    private final Argument testArgument;
    private final Method testMethod;
    private final StateMachine<State> stateMachine;
    private final ThrowableCollector throwableCollector;
    private ExecutorContext executorContext;
    private Object testInstance;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     * @param testClass testClass
     * @param testMethod testMethod
     * @param testArgument testArgument
     */
    MethodTestDescriptor(
            UniqueId uniqueId,
            String displayName,
            Class<?> testClass,
            Method testMethod,
            Argument testArgument) {
        super(uniqueId, displayName);
        this.testClass = testClass;
        this.testArgument = testArgument;
        this.testMethod = testMethod;

        this.stateMachine =
                new StateMachine<State>(getClass().getName())
                        .addTransition(State.BEGIN, this::begin)
                        .addTransition(State.BEFORE_BEFORE_EACH, this::beforeBeforeEach)
                        .addTransition(State.BEFORE_EACH, this::beforeEach)
                        .addTransition(State.AFTER_BEFORE_EACH, this::afterBeforeEach)
                        .addTransition(State.BEFORE_TEST, this::beforeTest)
                        .addTransition(State.TEST, this::test)
                        .addTransition(State.AFTER_TEST, this::afterTest)
                        .addTransition(State.BEFORE_AFTER_EACH, this::beforeAfterEach)
                        .addTransition(State.AFTER_EACH, this::afterEach)
                        .addTransition(State.AFTER_AFTER_EACH, this::afterAfterEach)
                        .addTransition(State.END, this::end);

        this.throwableCollector = new ThrowableCollector();
    }

    /**
     * Method to get the TestSource
     *
     * @return the return value
     */
    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(MethodSource.from(testMethod));
    }

    /**
     * Method to get the test descriptor Type
     *
     * @return the return value
     */
    @Override
    public Type getType() {
        return Type.TEST;
    }

    /**
     * Method to return whether the test descriptor is a test
     *
     * @return the return value
     */
    @Override
    public boolean isTest() {
        return true;
    }

    /**
     * Method to return whether the test descriptor is a container
     *
     * @return the return value
     */
    @Override
    public boolean isContainer() {
        return false;
    }

    /**
     * Method to get the test class
     *
     * @return the return value
     */
    public Class<?> getTestClass() {
        return testClass;
    }

    /**
     * Method to get the test argument
     *
     * @return the return value
     */
    public Argument getTestArgument() {
        return testArgument;
    }

    /**
     * Method to get the test method
     *
     * @return the return value
     */
    public Method getTestMethod() {
        return testMethod;
    }

    /**
     * Method to execute the test descriptor
     *
     * @param executorContext executorContext
     */
    @Override
    public void execute(ExecutorContext executorContext) {
        LOGGER.trace(
                "execute uniqueId [%s] testClass [%s] testArgument [%s] testMethod [%s]",
                getUniqueId(), testClass.getName(), testArgument.name(), testMethod.getName());

        this.executorContext = executorContext;

        stateMachine
                .run(State.BEGIN)
                .ifPresent(throwable -> printStackTrace(System.out, throwable));
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void begin(StateMachine<State> stateMachine) {
        LOGGER.trace(
                "begin uniqueId [%s] testClass [%s] testMethod [%s] testArgument [%s]",
                getUniqueId(), testClass.getName(), testMethod.getName(), testArgument.name());

        getStopWatch().start();

        executorContext.getExecutionRequest().getEngineExecutionListener().executionStarted(this);
        testInstance = executorContext.getTestInstance();
        stateMachine.signal(State.BEFORE_BEFORE_EACH);

        flush();
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void beforeBeforeEach(StateMachine<State> stateMachine) {
        LOGGER.trace(
                "beforeBeforeEach uniqueId [%s] testClass [%s] testMethod [%s] testArgument [%s]",
                getUniqueId(), testClass.getName(), testMethod.getName(), testArgument.name());

        throwableCollector.add(
                Invoker.invoke(
                        () -> {
                            List<Extension> extensions =
                                    TestEngineUtils.singleton()
                                            .getExtensions(testClass, TestEngineUtils.Sort.NORMAL);

                            for (Extension extension : extensions) {
                                extension.beforeBeforeEach(testInstance, testArgument);
                            }
                        }));

        stateMachine.signal(State.BEFORE_EACH);

        flush();
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void beforeEach(StateMachine<State> stateMachine) {
        LOGGER.trace(
                "beforeEach uniqueId [%s] testClass [%s] testMethod [%s] testArgument [%s]",
                getUniqueId(), testClass.getName(), testMethod.getName(), testArgument.name());

        ReflectionUtils reflectionUtils = ReflectionUtils.singleton();
        LockProcessor lockProcessor = LockProcessor.singleton();

        throwableCollector.add(
                Invoker.invoke(
                        () -> {
                            List<Method> methods =
                                    TestEngineUtils.singleton().getBeforeEachMethods(testClass);

                            for (Method method : methods) {
                                try {
                                    lockProcessor.processLocks(method);
                                    if (reflectionUtils.acceptsParameters(method, Argument.class)) {
                                        method.invoke(testInstance, testArgument);
                                    } else {
                                        method.invoke(testInstance, NO_OBJECT_ARGS);
                                    }
                                } finally {
                                    lockProcessor.processUnlocks(method);
                                }
                            }
                        }));

        stateMachine.signal(State.AFTER_BEFORE_EACH);

        flush();
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void afterBeforeEach(StateMachine<State> stateMachine) {
        LOGGER.trace(
                "afterBeforeEach uniqueId [%s] testClass [%s] testMethod [%s] testArgument [%s]",
                getUniqueId(), testClass.getName(), testMethod.getName(), testArgument.name());

        throwableCollector.add(
                Invoker.invoke(
                        () -> {
                            List<Extension> extensions =
                                    TestEngineUtils.singleton()
                                            .getExtensions(testClass, TestEngineUtils.Sort.REVERSE);

                            for (Extension extension : extensions) {
                                extension.afterBeforeEach(testInstance, testArgument);
                            }
                        }));

        if (throwableCollector.isEmpty()) {
            stateMachine.signal(State.BEFORE_TEST);
        } else {
            stateMachine.signal(State.BEFORE_AFTER_EACH);
        }

        flush();
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void beforeTest(StateMachine<State> stateMachine) {
        LOGGER.trace(
                "beforeTest uniqueId [%s] testClass [%s] testMethod [%s] testArgument [%s]",
                getUniqueId(), testClass.getName(), testMethod.getName(), testArgument.name());

        throwableCollector.add(
                Invoker.invoke(
                        () -> {
                            List<Extension> extensions =
                                    TestEngineUtils.singleton()
                                            .getExtensions(testClass, TestEngineUtils.Sort.NORMAL);

                            for (Extension extension : extensions) {
                                extension.beforeTest(testInstance, testArgument);
                            }
                        }));

        if (throwableCollector.isEmpty()) {
            stateMachine.signal(State.TEST);
        } else {
            stateMachine.signal(State.AFTER_TEST);
        }

        flush();
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void test(StateMachine<State> stateMachine) {
        LOGGER.trace(
                "test uniqueId [%s] testClass [%s] testMethod [%s] testArgument [%s]",
                getUniqueId(), testClass.getName(), testMethod.getName(), testArgument.name());

        ReflectionUtils reflectionUtils = ReflectionUtils.singleton();
        LockProcessor lockProcessor = LockProcessor.singleton();

        throwableCollector.add(
                Invoker.invoke(
                        () -> {
                            try {
                                lockProcessor.processLocks(testMethod);
                                if (reflectionUtils.acceptsParameters(testMethod, Argument.class)) {
                                    testMethod.invoke(testInstance, testArgument);
                                } else {
                                    testMethod.invoke(testInstance, NO_OBJECT_ARGS);
                                }
                            } finally {
                                lockProcessor.processUnlocks(testMethod);
                            }
                        }));

        stateMachine.signal(State.AFTER_TEST);

        flush();
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void afterTest(StateMachine<State> stateMachine) {
        LOGGER.trace(
                "afterTest uniqueId [%s] testClass [%s] testMethod [%s] testArgument [%s]",
                getUniqueId(), testClass.getName(), testMethod.getName(), testArgument.name());

        throwableCollector.add(
                Invoker.invoke(
                        () -> {
                            List<Extension> extensions =
                                    TestEngineUtils.singleton()
                                            .getExtensions(testClass, TestEngineUtils.Sort.REVERSE);

                            for (Extension extension : extensions) {
                                extension.afterTest(testInstance, testArgument);
                            }
                        }));

        stateMachine.signal(State.BEFORE_AFTER_EACH);

        flush();
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void beforeAfterEach(StateMachine<State> stateMachine) {
        LOGGER.trace(
                "beforeAfterEach uniqueId [%s] testClass [%s] testMethod [%s] testArgument [%s]",
                getUniqueId(), testClass.getName(), testMethod.getName(), testArgument.name());

        throwableCollector.add(
                Invoker.invoke(
                        () -> {
                            List<Extension> extensions =
                                    TestEngineUtils.singleton()
                                            .getExtensions(testClass, TestEngineUtils.Sort.NORMAL);

                            for (Extension extension : extensions) {
                                extension.beforeAfterEach(testInstance, testArgument);
                            }
                        }));

        stateMachine.signal(State.AFTER_EACH);

        flush();
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void afterEach(StateMachine<State> stateMachine) {
        LOGGER.trace(
                "afterEach uniqueId [%s] testClass [%s] testMethod [%s] testArgument [%s]",
                getUniqueId(), testClass.getName(), testMethod.getName(), testArgument.name());

        ReflectionUtils reflectionUtils = ReflectionUtils.singleton();
        TestEngineUtils testEngineUtils = TestEngineUtils.singleton();
        LockProcessor lockProcessor = LockProcessor.singleton();
        AutoCloseProcessor autoCloseProcessor = AutoCloseProcessor.singleton();

        Invoker.invoke(
                () -> {
                    List<Method> methods = testEngineUtils.getAfterEachMethods(testClass);

                    for (Method method : methods) {
                        try {
                            lockProcessor.processLocks(method);
                            if (reflectionUtils.acceptsParameters(method, Argument.class)) {
                                method.invoke(testInstance, testArgument);
                            } else {
                                method.invoke(testInstance, NO_OBJECT_ARGS);
                            }
                        } catch (Throwable t) {
                            throwableCollector.add(t);
                        } finally {
                            lockProcessor.processUnlocks(method);
                        }
                    }
                });

        Invoker.invoke(
                () -> {
                    try {
                        List<Field> fields =
                                testEngineUtils.getAnnotatedFields(testClass).stream()
                                        .filter(
                                                field -> {
                                                    TestEngine.AutoClose annotation =
                                                            field.getAnnotation(
                                                                    TestEngine.AutoClose.class);
                                                    return annotation != null
                                                            && "@TestEngine.AfterEach"
                                                                    .equals(annotation.lifecycle());
                                                })
                                        .collect(Collectors.toList());

                        for (Field field : fields) {
                            try {
                                autoCloseProcessor.close(testInstance, field);
                            } catch (Throwable t) {
                                throwableCollector.add(t);
                            }
                        }
                    } catch (Throwable t) {
                        throwableCollector.add(t);
                    }
                });

        stateMachine.signal(State.AFTER_AFTER_EACH);

        flush();
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void afterAfterEach(StateMachine<State> stateMachine) {
        LOGGER.trace(
                "afterAfterEach uniqueId [%s] testClass [%s] testMethod [%s] testArgument [%s]",
                getUniqueId(), testClass.getName(), testMethod.getName(), testArgument.name());

        throwableCollector.add(
                Invoker.invoke(
                        () -> {
                            List<Extension> extensions =
                                    TestEngineUtils.singleton()
                                            .getExtensions(testClass, TestEngineUtils.Sort.REVERSE);

                            for (Extension extension : extensions) {
                                extension.afterAfterEach(testInstance, testArgument);
                            }
                        }));

        stateMachine.signal(State.END);

        flush();
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void end(StateMachine<State> stateMachine) {
        LOGGER.trace(
                "end uniqueId [%s] testClass [%s] testMethod [%s] testArgument [%s]",
                getUniqueId(), testClass.getName(), testMethod.getName(), testArgument.name());

        getStopWatch().stop();

        if (throwableCollector.isEmpty()) {
            executorContext
                    .getExecutionRequest()
                    .getEngineExecutionListener()
                    .executionFinished(this, TestExecutionResult.successful());
        } else {
            executorContext
                    .getExecutionRequest()
                    .getEngineExecutionListener()
                    .executionFinished(
                            this, TestExecutionResult.failed(throwableCollector.first()));
        }

        stateMachine.stop();

        flush();
    }
}
