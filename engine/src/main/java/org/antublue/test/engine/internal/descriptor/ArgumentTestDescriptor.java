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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.Extension;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.ExecutorContext;
import org.antublue.test.engine.internal.descriptor.util.ArgumentFieldInjector;
import org.antublue.test.engine.internal.descriptor.util.LockProcessor;
import org.antublue.test.engine.internal.descriptor.util.RandomFieldInjector;
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

/** Class to implement an argument test descriptor */
@SuppressWarnings("PMD.EmptyCatchBlock")
public final class ArgumentTestDescriptor extends ExtendedAbstractTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArgumentTestDescriptor.class);

    private final Class<?> testClass;
    private final Argument testArgument;
    private final StateMachine<State> stateMachine;
    private final ThrowableCollector throwableCollector;
    private ExecutorContext executorContext;
    private Object testInstance;

    private enum State {
        BEGIN,
        BEFORE_BEFORE_ALL,
        BEFORE_ALL,
        AFTER_BEFORE_ALL,
        EXECUTE,
        SKIP,
        SKIP_END,
        BEFORE_AFTER_ALL,
        AFTER_ALL,
        AFTER_AFTER_ALL,
        END
    }

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     * @param testClass testClass
     * @param testArgument testArgument
     */
    ArgumentTestDescriptor(
            UniqueId uniqueId, String displayName, Class<?> testClass, Argument testArgument) {
        super(uniqueId, displayName);
        this.testClass = testClass;
        this.testArgument = testArgument;

        this.stateMachine =
                new StateMachine<State>(getClass().getName())
                        .addTransition(State.BEGIN, this::begin)
                        .addTransition(State.BEFORE_BEFORE_ALL, this::beforeBeforeAll)
                        .addTransition(State.BEFORE_ALL, this::beforeAll)
                        .addTransition(State.AFTER_BEFORE_ALL, this::afterBeforeAll)
                        .addTransition(State.EXECUTE, this::execute)
                        .addTransition(State.SKIP, this::skip)
                        .addTransition(State.SKIP_END, this::skipEnd)
                        .addTransition(State.BEFORE_AFTER_ALL, this::beforeAfterAll)
                        .addTransition(State.AFTER_ALL, this::afterAll)
                        .addTransition(State.AFTER_AFTER_ALL, this::afterAfterAll)
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
        return Optional.of(
                MethodSource.from(testEngineUtils.getArgumentSupplierMethods(testClass).get(0)));
    }

    /**
     * Method to get the test descriptor Type
     *
     * @return the return value
     */
    @Override
    public Type getType() {
        return Type.CONTAINER;
    }

    /**
     * Method to return whether the test descriptor is a test
     *
     * @return the return value
     */
    @Override
    public boolean isTest() {
        return false;
    }

    /**
     * Method to return whether the test descriptor is a container
     *
     * @return the return value
     */
    @Override
    public boolean isContainer() {
        return true;
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

    @Override
    public void setStatus(Status status) {
        if (this.status != Status.FAIL) {
            this.status = status;
        }

        if (status == Status.FAIL) {
            getParent()
                    .ifPresent(
                            testDescriptor ->
                                    ((ClassTestDescriptor) testDescriptor).setStatus(status));
        }
    }

    /**
     * Method to execute the test descriptor
     *
     * @param executorContext executorContext
     */
    @Override
    public void execute(ExecutorContext executorContext) {
        LOGGER.trace(
                "execute uniqueId [%s] testClass [%s] testArgument [%s]",
                getUniqueId(), testClass.getName(), testArgument.name());

        this.executorContext = executorContext;

        Optional<Throwable> optional = stateMachine.run(State.BEGIN);

        if (optional.isPresent()) {
            setStatus(Status.FAIL);
            printStackTrace(System.out, optional.get());
        }

        flush();
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void begin(StateMachine<State> stateMachine) {
        LOGGER.trace(
                "begin uniqueId [%s] testClass [%s] testArgument [%s]",
                getUniqueId(), testClass.getName(), testArgument.name());

        Invoker.invoke(
                () -> {
                    getStopWatch().start();

                    executorContext
                            .getExecutionRequest()
                            .getEngineExecutionListener()
                            .executionStarted(this);

                    testInstance = executorContext.getTestInstance();

                    List<Field> fields = testEngineUtils.getAnnotatedFields(testClass);

                    ArgumentFieldInjector argumentFieldInjector = ArgumentFieldInjector.singleton();
                    for (Field field : fields) {
                        argumentFieldInjector.inject(testInstance, testArgument, field);
                    }

                    RandomFieldInjector randomFieldInjector = RandomFieldInjector.singleton();
                    for (Field field : fields) {
                        randomFieldInjector.inject(testInstance, field);
                    }
                });

        if (throwableCollector.isEmpty()) {
            stateMachine.signal(State.BEFORE_BEFORE_ALL);
        } else {
            stateMachine.signal(State.SKIP);
        }

        flush();
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void beforeBeforeAll(StateMachine<State> stateMachine) {
        LOGGER.trace(
                "beforeBeforeAll uniqueId [%s] testClass [%s] testArgument [%s]",
                getUniqueId(), testClass.getName(), testArgument.name());

        throwableCollector.add(
                Invoker.invoke(
                        () -> {
                            List<Extension> extensions = testEngineUtils.getExtensions(testClass);

                            for (Extension extension : extensions) {
                                extension.beforeBeforeAll(testInstance, testArgument);
                            }
                        }));

        stateMachine.signal(State.BEFORE_ALL);

        flush();
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void beforeAll(StateMachine<State> stateMachine) {
        LOGGER.trace(
                "beforeAll uniqueId [%s] testClass [%s] testArgument [%s]",
                getUniqueId(), testClass.getName(), testArgument.name());

        ReflectionUtils reflectionUtils = ReflectionUtils.singleton();
        LockProcessor lockProcessor = LockProcessor.singleton();

        throwableCollector.add(
                Invoker.invoke(
                        () -> {
                            List<Method> methods = testEngineUtils.getBeforeAllMethods(testClass);

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

        stateMachine.signal(State.AFTER_BEFORE_ALL);

        flush();
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void afterBeforeAll(StateMachine<State> stateMachine) {
        LOGGER.trace(
                "afterBeforeAll uniqueId [%s] testClass [%s] testArgument [%s]",
                getUniqueId(), testClass.getName(), testArgument.name());

        throwableCollector.add(
                Invoker.invoke(
                        () -> {
                            List<Extension> extensions =
                                    new ArrayList<>(testEngineUtils.getExtensions(testClass));
                            Collections.reverse(extensions);

                            for (Extension extension : extensions) {
                                extension.afterBeforeAll(testInstance, testArgument);
                            }
                        }));

        if (throwableCollector.isEmpty()) {
            stateMachine.signal(State.EXECUTE);
        } else {
            stateMachine.signal(State.SKIP);
        }

        flush();
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void execute(StateMachine<State> stateMachine) {
        LOGGER.trace(
                "execute uniqueId [%s] testClass [%s] testArgument [%s]",
                getUniqueId(), testClass.getName(), testArgument.name());

        throwableCollector.add(
                Invoker.invoke(
                        () -> {
                            List<MethodTestDescriptor> methodTestDescriptors =
                                    getChildren(MethodTestDescriptor.class);

                            for (MethodTestDescriptor methodTestDescriptor :
                                    methodTestDescriptors) {
                                methodTestDescriptor.execute(executorContext);
                            }
                        }));

        stateMachine.signal(State.BEFORE_AFTER_ALL);

        flush();
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void skip(StateMachine<State> stateMachine) {
        LOGGER.trace(
                "skip uniqueId [%s] testClass [%s] testArgument [%s]",
                getUniqueId(), testClass.getName(), testArgument.name());

        throwableCollector.add(
                Invoker.invoke(
                        () -> {
                            List<MethodTestDescriptor> methodTestDescriptors =
                                    getChildren(MethodTestDescriptor.class);

                            for (MethodTestDescriptor methodTestDescriptor :
                                    methodTestDescriptors) {
                                methodTestDescriptor.skip(executorContext);
                            }
                        }));

        stateMachine.signal(State.BEFORE_AFTER_ALL);

        flush();
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void skipEnd(StateMachine<State> stateMachine) {
        LOGGER.trace(
                "skipEnd uniqueId [%s] testClass [%s] testArgument [%s]",
                getUniqueId(), testClass.getName(), testArgument.name());

        throwableCollector.add(
                Invoker.invoke(
                        () -> {
                            List<MethodTestDescriptor> methodTestDescriptors =
                                    getChildren(MethodTestDescriptor.class);

                            for (MethodTestDescriptor methodTestDescriptor :
                                    methodTestDescriptors) {
                                methodTestDescriptor.skip(executorContext);
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
    private void beforeAfterAll(StateMachine<State> stateMachine) {
        LOGGER.trace(
                "beforeAfterAll uniqueId [%s] testClass [%s] testArgument [%s]",
                getUniqueId(), testClass.getName(), testArgument.name());

        throwableCollector.add(
                Invoker.invoke(
                        () -> {
                            List<Extension> extensions = testEngineUtils.getExtensions(testClass);

                            for (Extension extension : extensions) {
                                extension.beforeAfterAll(testInstance, testArgument);
                            }
                        }));

        stateMachine.signal(State.AFTER_ALL);

        flush();
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void afterAll(StateMachine<State> stateMachine) {
        LOGGER.trace(
                "afterAll uniqueId [%s] testClass [%s] testArgument [%s]",
                getUniqueId(), testClass.getName(), testArgument.name());

        Invoker.invoke(
                () -> {
                    List<Method> methods = testEngineUtils.getAfterAllMethods(testClass);

                    for (Method method : methods) {
                        try {
                            lockProcessor.processLocks(method);
                            if (testEngineUtils.acceptsParameterTypes(method, Argument.class)) {
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
                                                            && "@TestEngine.AfterAll"
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

        stateMachine.signal(State.AFTER_AFTER_ALL);

        flush();
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void afterAfterAll(StateMachine<State> stateMachine) {
        LOGGER.trace(
                "afterAfterAll uniqueId [%s] testClass [%s] testArgument [%s]",
                getUniqueId(), testClass.getName(), testArgument.name());

        throwableCollector.add(
                Invoker.invoke(
                        () -> {
                            List<Extension> extensions =
                                    new ArrayList<>(testEngineUtils.getExtensions(testClass));
                            Collections.reverse(extensions);

                            for (Extension extension : extensions) {
                                extension.afterAfterAll(testInstance, testArgument);
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
                "end uniqueId [%s] testClass [%s] testArgument [%s]",
                getUniqueId(), testClass.getName(), testArgument.name());

        Invoker.invoke(
                () -> {
                    List<Field> fields = testEngineUtils.getAnnotatedFields(testClass);

                    ArgumentFieldInjector argumentFieldInjector = ArgumentFieldInjector.singleton();
                    for (Field field : fields) {
                        argumentFieldInjector.inject(testInstance, null, field);
                    }
                });

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
