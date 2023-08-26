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
import java.util.List;
import java.util.Optional;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.internal.AutoCloseAnnotationUtils;
import org.antublue.test.engine.internal.ExecutorContext;
import org.antublue.test.engine.internal.FieldAnnotationUtils;
import org.antublue.test.engine.internal.LockAnnotationUtils;
import org.antublue.test.engine.internal.TestEngineReflectionUtils;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.statemachine.StateMachine;
import org.antublue.test.engine.internal.util.InvocationUtils;
import org.antublue.test.engine.internal.util.ThrowableUtils;
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
    private final List<Throwable> throwables;
    private final StateMachine<State> stateMachine;
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
        this.throwables = new ArrayList<>();
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
    }

    /**
     * Method to get the TestSource
     *
     * @return the return value
     */
    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(
                MethodSource.from(
                        TestEngineReflectionUtils.singleton()
                                .getArgumentSupplierMethod(testClass)));
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
                "begin uniqueId [%s] testClass [%s] testArgument [%s]",
                getUniqueId(), testClass.getName(), testArgument.name());

        Optional<Throwable> optionalThrowable =
                InvocationUtils.run(
                        () -> {
                            getStopWatch().start();

                            executorContext
                                    .getExecutionRequest()
                                    .getEngineExecutionListener()
                                    .executionStarted(this);

                            testInstance = executorContext.getTestInstance();

                            List<Field> fields =
                                    TestEngineReflectionUtils.singleton()
                                            .getArgumentFields(testClass);

                            for (Field field : fields) {
                                LOGGER.trace("injecting test argument");
                                field.set(testInstance, testArgument);
                            }

                            FieldAnnotationUtils.singleton()
                                    .processAnnotatedFields(testInstance, throwables);
                        });

        optionalThrowable.ifPresent(
                throwable -> {
                    Throwable prunedThrowable = ThrowableUtils.prune(throwable, testClass);
                    throwables.add(prunedThrowable);
                    printStackTrace(System.out, prunedThrowable);
                    stateMachine.signal(State.SKIP);
                });

        stateMachine.signal(State.BEFORE_BEFORE_ALL);
        System.out.flush();
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
        stateMachine.signal(State.BEFORE_ALL);
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

        Optional<Throwable> optionalThrowable =
                InvocationUtils.run(
                        () -> {
                            List<Method> methods =
                                    TestEngineReflectionUtils.singleton()
                                            .getBeforeAllMethods(testClass);
                            for (Method method : methods) {
                                try {
                                    LockAnnotationUtils.singleton().processLockAnnotations(method);
                                    if (TestEngineReflectionUtils.singleton()
                                            .acceptsArgument(method, testArgument)) {
                                        method.invoke(testInstance, testArgument);
                                    } else {
                                        method.invoke(testInstance, NO_OBJECT_ARGS);
                                    }
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

        stateMachine.signal(State.AFTER_BEFORE_ALL);
        System.out.flush();
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

        if (throwables.isEmpty()) {
            stateMachine.signal(State.EXECUTE);
        } else {
            stateMachine.signal(State.SKIP);
        }
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

        Optional<Throwable> optionalThrowable =
                InvocationUtils.run(
                        () -> {
                            List<MethodTestDescriptor> methodTestDescriptors =
                                    getChildren(MethodTestDescriptor.class);
                            for (MethodTestDescriptor methodTestDescriptor :
                                    methodTestDescriptors) {
                                methodTestDescriptor.execute(executorContext);
                            }
                        });

        optionalThrowable.ifPresent(
                throwable -> {
                    Throwable prunedThrowable = ThrowableUtils.prune(throwable, testClass);
                    throwables.add(prunedThrowable);
                    printStackTrace(System.out, prunedThrowable);
                });

        stateMachine.signal(State.BEFORE_AFTER_ALL);
        System.out.flush();
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

        Optional<Throwable> optionalThrowable =
                InvocationUtils.run(
                        () -> {
                            List<MethodTestDescriptor> methodTestDescriptors =
                                    getChildren(MethodTestDescriptor.class);
                            for (MethodTestDescriptor methodTestDescriptor :
                                    methodTestDescriptors) {
                                methodTestDescriptor.skip(executorContext);
                            }
                        });

        optionalThrowable.ifPresent(
                throwable -> {
                    Throwable prunedThrowable = ThrowableUtils.prune(throwable, testClass);
                    throwables.add(prunedThrowable);
                    printStackTrace(System.out, prunedThrowable);
                });

        stateMachine.signal(State.BEFORE_AFTER_ALL);
        System.out.flush();
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

        Optional<Throwable> optionalThrowable =
                InvocationUtils.run(
                        () -> {
                            List<MethodTestDescriptor> methodTestDescriptors =
                                    getChildren(MethodTestDescriptor.class);
                            for (MethodTestDescriptor methodTestDescriptor :
                                    methodTestDescriptors) {
                                methodTestDescriptor.skip(executorContext);
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
    private void beforeAfterAll(StateMachine<State> stateMachine) {
        LOGGER.trace(
                "beforeAfterAll uniqueId [%s] testClass [%s] testArgument [%s]",
                getUniqueId(), testClass.getName(), testArgument.name());
        stateMachine.signal(State.AFTER_ALL);
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

        Optional<Throwable> optionalThrowable =
                InvocationUtils.run(
                        () -> {
                            List<Method> methods =
                                    TestEngineReflectionUtils.singleton()
                                            .getAfterAllMethods(testClass);
                            for (Method method : methods) {
                                try {
                                    LockAnnotationUtils.singleton().processLockAnnotations(method);
                                    if (TestEngineReflectionUtils.singleton()
                                            .acceptsArgument(method, testArgument)) {
                                        method.invoke(testInstance, testArgument);
                                    } else {
                                        method.invoke(testInstance, NO_OBJECT_ARGS);
                                    }
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

        optionalThrowable.ifPresent(
                throwable -> {
                    Throwable prunedThrowable = ThrowableUtils.prune(throwable, testClass);
                    throwables.add(prunedThrowable);
                    printStackTrace(System.out, prunedThrowable);
                });

        stateMachine.signal(State.AFTER_AFTER_ALL);
        System.out.flush();
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

        stateMachine.signal(State.END);
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

        try {
            List<Field> fields = TestEngineReflectionUtils.singleton().getArgumentFields(testClass);
            for (Field field : fields) {
                LOGGER.trace("removing test argument");
                field.set(testInstance, null);
            }
        } catch (Throwable t) {
            // DO NOTHING
        } finally {
            AutoCloseAnnotationUtils.singleton()
                    .processAutoCloseAnnotatedFields(
                            testInstance, "@TestEngine.AfterAll", throwables);

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
            System.out.flush();
        }
    }
}
