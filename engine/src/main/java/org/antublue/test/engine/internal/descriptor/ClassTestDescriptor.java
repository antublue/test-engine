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
import org.antublue.test.engine.internal.util.ThrowableCollector;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;

/** Class to implement a class test descriptor */
public final class ClassTestDescriptor extends ExtendedAbstractTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassTestDescriptor.class);

    private enum State {
        BEGIN,
        BEFORE_PREPARE,
        PREPARE,
        AFTER_PREPARE,
        EXECUTE,
        SKIP,
        SKIP_END,
        BEFORE_CONCLUDE,
        CONCLUDE,
        AFTER_CONCLUDE,
        END
    }

    private final Class<?> testClass;
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
     */
    ClassTestDescriptor(UniqueId uniqueId, String displayName, Class<?> testClass) {
        super(uniqueId, displayName);
        this.testClass = testClass;

        this.stateMachine =
                new StateMachine<State>(getClass().getName())
                        .addTransition(State.BEGIN, this::begin)
                        .addTransition(State.BEFORE_PREPARE, this::beforePrepare)
                        .addTransition(State.PREPARE, this::prepare)
                        .addTransition(State.AFTER_PREPARE, this::afterPrepare)
                        .addTransition(State.EXECUTE, this::execute)
                        .addTransition(State.SKIP, this::skip)
                        .addTransition(State.SKIP_END, this::skipEnd)
                        .addTransition(State.BEFORE_CONCLUDE, this::beforeConclude)
                        .addTransition(State.CONCLUDE, this::conclude)
                        .addTransition(State.AFTER_CONCLUDE, this::afterConclude)
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
        return Optional.of(ClassSource.from(testClass));
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
     * Method to execute the test descriptor
     *
     * @param executorContext executorContext
     */
    @Override
    public void execute(ExecutorContext executorContext) {
        LOGGER.trace("execute uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());

        this.executorContext = executorContext;

        stateMachine
                .run(State.BEGIN)
                .ifPresent(throwable -> printStackTrace(System.out, throwable));

        flush();
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void begin(StateMachine<State> stateMachine) {
        LOGGER.trace("begin uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());

        throwableCollector.add(
                Invoker.invoke(
                        () -> {
                            getStopWatch().start();

                            executorContext
                                    .getExecutionRequest()
                                    .getEngineExecutionListener()
                                    .executionStarted(this);

                            testInstance =
                                    testClass
                                            .getDeclaredConstructor(NO_CLASS_ARGS)
                                            .newInstance(NO_OBJECT_ARGS);

                            executorContext.setTestInstance(testInstance);

                            TestEngineUtils.singleton()
                                    .getExtensions(testClass, TestEngineUtils.Sort.NORMAL);
                        }));

        if (throwableCollector.isEmpty()) {
            stateMachine.signal(State.BEFORE_PREPARE);
        } else {
            stateMachine.signal(State.SKIP_END);
        }

        flush();
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void beforePrepare(StateMachine<State> stateMachine) {
        LOGGER.trace(
                "beforePrepare uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());

        throwableCollector.add(
                Invoker.invoke(
                        () -> {
                            List<Extension> extensions =
                                    TestEngineUtils.singleton()
                                            .getExtensions(testClass, TestEngineUtils.Sort.NORMAL);

                            for (Extension extension : extensions) {
                                extension.beforePrepare(testInstance);
                            }
                        }));

        if (throwableCollector.isEmpty()) {
            stateMachine.signal(State.PREPARE);
        } else {
            stateMachine.signal(State.BEFORE_CONCLUDE);
        }

        flush();
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void prepare(StateMachine<State> stateMachine) {
        LOGGER.trace("prepare uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());

        LockProcessor lockProcessor = LockProcessor.singleton();

        throwableCollector.add(
                Invoker.invoke(
                        () -> {
                            List<Method> methods =
                                    TestEngineUtils.singleton().getPrepareMethods(testClass);

                            for (Method method : methods) {
                                try {
                                    lockProcessor.processLocks(method);
                                    method.invoke(testInstance, NO_OBJECT_ARGS);
                                } finally {
                                    lockProcessor.processUnlocks(method);
                                }
                            }
                        }));

        stateMachine.signal(State.AFTER_PREPARE);

        flush();
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void afterPrepare(StateMachine<State> stateMachine) {
        LOGGER.trace(
                "afterPrepare uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());

        throwableCollector.add(
                Invoker.invoke(
                        () -> {
                            List<Extension> extensions =
                                    TestEngineUtils.singleton()
                                            .getExtensions(testClass, TestEngineUtils.Sort.REVERSE);

                            for (Extension extension : extensions) {
                                extension.afterPrepare(testInstance);
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
        LOGGER.trace("execute uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());

        throwableCollector.add(
                Invoker.invoke(
                        () -> {
                            List<ArgumentTestDescriptor> argumentTestDescriptors =
                                    getChildren(ArgumentTestDescriptor.class);

                            for (ArgumentTestDescriptor argumentTestDescriptor :
                                    argumentTestDescriptors) {
                                argumentTestDescriptor.execute(executorContext);
                            }
                        }));

        stateMachine.signal(State.BEFORE_CONCLUDE);

        flush();
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void skip(StateMachine<State> stateMachine) {
        LOGGER.trace("skip uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());

        throwableCollector.add(
                Invoker.invoke(
                        () -> {
                            List<ArgumentTestDescriptor> argumentTestDescriptors =
                                    getChildren(ArgumentTestDescriptor.class);

                            for (ArgumentTestDescriptor argumentTestDescriptor :
                                    argumentTestDescriptors) {
                                argumentTestDescriptor.skip(executorContext);
                            }
                        }));

        stateMachine.signal(State.BEFORE_CONCLUDE);

        flush();
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void skipEnd(StateMachine<State> stateMachine) {
        LOGGER.trace("skipEnd uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());

        throwableCollector.add(
                Invoker.invoke(
                        () -> {
                            List<ArgumentTestDescriptor> argumentTestDescriptors =
                                    getChildren(ArgumentTestDescriptor.class);

                            for (ArgumentTestDescriptor argumentTestDescriptor :
                                    argumentTestDescriptors) {
                                argumentTestDescriptor.skip(executorContext);
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
    private void beforeConclude(StateMachine<State> stateMachine) {
        LOGGER.trace(
                "beforeConclude uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());

        throwableCollector.add(
                Invoker.invoke(
                        () -> {
                            List<Extension> extensions =
                                    TestEngineUtils.singleton()
                                            .getExtensions(testClass, TestEngineUtils.Sort.REVERSE);

                            for (Extension extension : extensions) {
                                extension.beforeConclude(testInstance);
                            }
                        }));

        stateMachine.signal(State.CONCLUDE);

        flush();
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void conclude(StateMachine<State> stateMachine) {
        LOGGER.trace("conclude uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());

        LockProcessor lockProcessor = LockProcessor.singleton();

        Invoker.invoke(
                () -> {
                    List<Method> methods =
                            TestEngineUtils.singleton().getConcludeMethods(testClass);

                    for (Method method : methods) {
                        try {
                            lockProcessor.processLocks(method);
                            method.invoke(testInstance, NO_OBJECT_ARGS);
                        } catch (Throwable t) {
                            throwableCollector.add(t);
                        } finally {
                            lockProcessor.processUnlocks(method);
                            flush();
                        }
                    }
                });

        AutoCloseProcessor autoCloseProcessor = AutoCloseProcessor.singleton();

        Invoker.invoke(
                () -> {
                    try {
                        TestEngineUtils testEngineUtils = TestEngineUtils.singleton();
                        List<Field> fields =
                                testEngineUtils.getAnnotatedFields(testClass).stream()
                                        .filter(
                                                field -> {
                                                    TestEngine.AutoClose annotation =
                                                            field.getAnnotation(
                                                                    TestEngine.AutoClose.class);
                                                    return annotation != null
                                                            && "@TestEngine.Conclude"
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

        stateMachine.signal(State.AFTER_CONCLUDE);

        flush();
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void afterConclude(StateMachine<State> stateMachine) {
        LOGGER.trace(
                "afterConclude uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());

        throwableCollector.add(
                Invoker.invoke(
                        () -> {
                            List<Extension> extensions =
                                    TestEngineUtils.singleton()
                                            .getExtensions(testClass, TestEngineUtils.Sort.REVERSE);

                            for (Extension extension : extensions) {
                                extension.afterConclude(testInstance);
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
        LOGGER.trace("end uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());

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
        executorContext.complete();

        flush();
    }
}
