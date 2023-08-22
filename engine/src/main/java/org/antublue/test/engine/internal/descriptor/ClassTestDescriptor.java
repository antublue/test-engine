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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.antublue.test.engine.internal.AutoCloseAnnotationUtils;
import org.antublue.test.engine.internal.ExecutorContext;
import org.antublue.test.engine.internal.LockAnnotationUtils;
import org.antublue.test.engine.internal.TestEngineReflectionUtils;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.statemachine.StateMachine;
import org.antublue.test.engine.internal.util.InvocationUtils;
import org.antublue.test.engine.internal.util.OptionalUtils;
import org.antublue.test.engine.internal.util.ThrowableUtils;
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
    private final List<Throwable> throwables;
    private final StateMachine<State> stateMachine;
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
        this.throwables = new ArrayList<>();
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
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void begin(StateMachine<State> stateMachine) {
        LOGGER.trace("begin uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());

        Optional<Throwable> optionalThrowable =
                InvocationUtils.run(
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
                        });

        OptionalUtils.ifPresentOrElse(
                optionalThrowable,
                throwable -> {
                    Throwable prunedThrowable = ThrowableUtils.prune(throwable, testClass);
                    throwables.add(prunedThrowable);
                    printStackTrace(System.out, prunedThrowable);
                    stateMachine.signal(State.SKIP_END);
                },
                () -> stateMachine.signal(State.BEFORE_PREPARE));

        System.out.flush();
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void beforePrepare(StateMachine<State> stateMachine) {
        LOGGER.trace(
                "beforePrepare uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());

        stateMachine.signal(State.PREPARE);
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void prepare(StateMachine<State> stateMachine) {
        LOGGER.trace("prepare uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());

        Optional<Throwable> optionalThrowable =
                InvocationUtils.run(
                        () -> {
                            List<Method> methods =
                                    TestEngineReflectionUtils.singleton()
                                            .getPrepareMethods(testClass);
                            for (Method method : methods) {
                                try {
                                    LockAnnotationUtils.singleton().processLockAnnotations(method);
                                    method.invoke(testInstance, NO_OBJECT_ARGS);
                                } finally {
                                    LockAnnotationUtils.singleton()
                                            .processUnlockAnnotations(method);
                                    System.out.flush();
                                }
                            }
                        });

        optionalThrowable.ifPresent(
                throwable -> {
                    Throwable prunedThrowable = ThrowableUtils.prune(throwable, testClass);
                    throwables.add(prunedThrowable);
                    printStackTrace(System.out, prunedThrowable);
                });

        stateMachine.signal(State.AFTER_PREPARE);
        System.out.flush();
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void afterPrepare(StateMachine<State> stateMachine) {
        LOGGER.trace(
                "afterPrepare uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());

        try {
            if (throwables.isEmpty()) {
                stateMachine.signal(State.EXECUTE);
            } else {
                stateMachine.signal(State.SKIP);
            }
        } finally {
            System.out.flush();
        }
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void execute(StateMachine<State> stateMachine) {
        LOGGER.trace("execute uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());

        Optional<Throwable> optionalThrowable =
                InvocationUtils.run(
                        () -> {
                            List<ArgumentTestDescriptor> argumentTestDescriptors =
                                    getChildren(ArgumentTestDescriptor.class);
                            for (ArgumentTestDescriptor argumentTestDescriptor :
                                    argumentTestDescriptors) {
                                argumentTestDescriptor.execute(executorContext);
                            }
                        });

        optionalThrowable.ifPresent(
                throwable -> {
                    Throwable prunedThrowable = ThrowableUtils.prune(throwable, testClass);
                    throwables.add(prunedThrowable);
                    printStackTrace(System.out, prunedThrowable);
                });

        stateMachine.signal(State.BEFORE_CONCLUDE);
        System.out.flush();
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void skip(StateMachine<State> stateMachine) {
        LOGGER.trace("skip uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());

        Optional<Throwable> optionalThrowable =
                InvocationUtils.run(
                        () -> {
                            List<ArgumentTestDescriptor> argumentTestDescriptors =
                                    getChildren(ArgumentTestDescriptor.class);
                            for (ArgumentTestDescriptor argumentTestDescriptor :
                                    argumentTestDescriptors) {
                                argumentTestDescriptor.skip(executorContext);
                            }
                        });

        optionalThrowable.ifPresent(
                throwable -> {
                    Throwable prunedThrowable = ThrowableUtils.prune(throwable, testClass);
                    throwables.add(prunedThrowable);
                    printStackTrace(System.out, prunedThrowable);
                });

        stateMachine.signal(State.BEFORE_CONCLUDE);
        System.out.flush();
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void skipEnd(StateMachine<State> stateMachine) {
        LOGGER.trace("skipEnd uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());

        Optional<Throwable> optionalThrowable =
                InvocationUtils.run(
                        () -> {
                            List<ArgumentTestDescriptor> argumentTestDescriptors =
                                    getChildren(ArgumentTestDescriptor.class);
                            for (ArgumentTestDescriptor argumentTestDescriptor :
                                    argumentTestDescriptors) {
                                argumentTestDescriptor.skip(executorContext);
                            }
                        });

        optionalThrowable.ifPresent(
                throwable -> {
                    Throwable prunedThrowable = ThrowableUtils.prune(throwable, testClass);
                    throwables.add(prunedThrowable);
                    printStackTrace(System.out, prunedThrowable);
                });

        stateMachine.signal(State.END);
        System.out.flush();
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void beforeConclude(StateMachine<State> stateMachine) {
        LOGGER.trace(
                "beforeConclude uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());

        stateMachine.signal(State.CONCLUDE);
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void conclude(StateMachine<State> stateMachine) {
        LOGGER.trace("conclude uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());

        Optional<Throwable> optionalThrowable =
                InvocationUtils.run(
                        () -> {
                            List<Method> methods =
                                    TestEngineReflectionUtils.singleton()
                                            .getConcludeMethods(testClass);
                            for (Method method : methods) {
                                try {
                                    LockAnnotationUtils.singleton().processLockAnnotations(method);
                                    method.invoke(testInstance, NO_OBJECT_ARGS);
                                } catch (Throwable t) {
                                    Throwable prunedThrowable = ThrowableUtils.prune(t, testClass);
                                    throwables.add(prunedThrowable);
                                    printStackTrace(System.out, prunedThrowable);
                                } finally {
                                    LockAnnotationUtils.singleton()
                                            .processUnlockAnnotations(method);
                                    System.out.flush();
                                }
                            }
                        });

        try {
            optionalThrowable.ifPresent(
                    throwable -> {
                        Throwable prunedThrowable = ThrowableUtils.prune(throwable, testClass);
                        throwables.add(prunedThrowable);
                        printStackTrace(System.out, prunedThrowable);
                    });
        } catch (Throwable t) {
            t.printStackTrace();
        }

        stateMachine.signal(State.AFTER_CONCLUDE);
        System.out.flush();
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void afterConclude(StateMachine<State> stateMachine) {
        LOGGER.trace(
                "afterConclude uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());

        stateMachine.signal(State.END);
    }

    /**
     * State machine transition
     *
     * @param stateMachine stateMachine
     */
    private void end(StateMachine<State> stateMachine) {
        LOGGER.trace("end uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());

        AutoCloseAnnotationUtils.singleton()
                .processAutoCloseAnnotatedFields(testInstance, "@TestEngine.Conclude", throwables);

        getStopWatch().stop();

        if (throwables.isEmpty()) {
            executorContext
                    .getExecutionRequest()
                    .getEngineExecutionListener()
                    .executionFinished(this, TestExecutionResult.successful());
        } else {
            executorContext
                    .getExecutionRequest()
                    .getEngineExecutionListener()
                    .executionFinished(this, TestExecutionResult.failed(throwables.get(0)));
        }

        stateMachine.stop();
        executorContext.complete();
        System.out.flush();
    }
}
