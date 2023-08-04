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
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.internal.AutoCloseAnnotationUtils;
import org.antublue.test.engine.internal.ExecutorContext;
import org.antublue.test.engine.internal.LockAnnotationUtils;
import org.antublue.test.engine.internal.ReflectionUtils;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.util.StateMachine;
import org.antublue.test.engine.internal.util.ThrowableCollector;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;

/** Class to implement an argument test descriptor */
@SuppressWarnings("PMD.EmptyCatchBlock")
public final class ArgumentTestDescriptor extends ExtendedAbstractTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArgumentTestDescriptor.class);

    private static final Object[] NO_ARGS = null;

    private static final ReflectionUtils REFLECTION_UTILS = ReflectionUtils.singleton();

    private enum State {
        BEGIN,
        SET_FIELD_SUCCESS,
        SET_FIELD_FAIL,
        BEFORE_ALL_SUCCESS,
        BEFORE_ALL_FAIL,
        EXECUTE_SUCCESS,
        SKIP_SUCCESS,
        CLEAR_FIELD_SUCCESS,
        CLEAR_FIELD_FAILED,
        AFTER_ALL_FAIL,
        AFTER_ALL_SUCCESS
    }

    private final Class<?> testClass;
    private final Argument testArgument;

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
    }

    /**
     * Method to get the TestSource
     *
     * @return the return value
     */
    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(
                MethodSource.from(REFLECTION_UTILS.getArgumentSupplierMethod(testClass)));
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

    /** Method to execute the test descriptor */
    @Override
    public void execute(ExecutorContext executorContext) {
        LOGGER.trace(
                "execute uniqueId [%s] testClass [%s] testArgument [%s]",
                getUniqueId(), testClass.getName(), testArgument.name());

        EngineExecutionListener engineExecutionListener =
                executorContext.getExecutionRequest().getEngineExecutionListener();

        engineExecutionListener.executionStarted(this);

        Object testInstance = executorContext.getTestInstance();

        ThrowableCollector throwableCollector = new ThrowableCollector();

        LockAnnotationUtils lockAnnotationUtils = LockAnnotationUtils.singleton();

        StateMachine<State> stateMachine = new StateMachine<>(this.toString(), State.BEGIN);

        stateMachine.mapTransition(
                State.BEGIN,
                sm -> {
                    try {
                        Optional<Field> optional = REFLECTION_UTILS.getArgumentField(testClass);
                        if (optional.isPresent()) {
                            Field field = optional.get();
                            field.set(testInstance, testArgument);
                        }
                        sm.next(State.SET_FIELD_SUCCESS);
                    } catch (Throwable t) {
                        throwableCollector.accept(t);
                        sm.next(State.SET_FIELD_FAIL);
                    }
                });

        stateMachine.mapTransition(
                State.SET_FIELD_SUCCESS,
                sm -> {
                    try {
                        List<Method> methods = REFLECTION_UTILS.getBeforeAllMethods(testClass);
                        for (Method method : methods) {
                            try {
                                lockAnnotationUtils.processLockAnnotations(method);
                                if (REFLECTION_UTILS.acceptsArgument(method, testArgument)) {
                                    method.invoke(testInstance, testArgument);
                                } else {
                                    method.invoke(testInstance, NO_ARGS);
                                }
                            } finally {
                                lockAnnotationUtils.processUnlockAnnotations(method);
                            }
                        }
                        sm.next(State.BEFORE_ALL_SUCCESS);
                    } catch (Throwable t) {
                        throwableCollector.accept(t);
                        sm.next(State.BEFORE_ALL_FAIL);
                    }
                });

        stateMachine.mapTransition(
                State.BEFORE_ALL_SUCCESS,
                sm -> {
                    getChildren(MethodTestDescriptor.class)
                            .forEach(
                                    methodTestDescriptor ->
                                            methodTestDescriptor.execute(executorContext));

                    sm.next(State.EXECUTE_SUCCESS);
                });

        stateMachine.mapTransition(
                State.BEFORE_ALL_FAIL,
                sm -> {
                    getChildren(MethodTestDescriptor.class)
                            .forEach(
                                    methodTestDescriptor ->
                                            methodTestDescriptor.skip(executorContext));

                    sm.next(State.SKIP_SUCCESS);
                });

        StateMachine.Transition<State> transition =
                sm -> {
                    List<Method> methods = REFLECTION_UTILS.getAfterAllMethods(testClass);
                    for (Method method : methods) {
                        try {
                            lockAnnotationUtils.processLockAnnotations(method);
                            if (REFLECTION_UTILS.acceptsArgument(method, testArgument)) {
                                method.invoke(testInstance, testArgument);
                            } else {
                                method.invoke(testInstance, NO_ARGS);
                            }
                        } catch (Throwable t) {
                            throwableCollector.accept(t);
                        } finally {
                            lockAnnotationUtils.processUnlockAnnotations(method);
                        }
                    }

                    if (throwableCollector.isEmpty()) {
                        sm.next(State.AFTER_ALL_SUCCESS);
                    } else {
                        sm.next(State.AFTER_ALL_FAIL);
                    }
                };

        stateMachine.mapTransition(
                new State[] {State.EXECUTE_SUCCESS, State.SKIP_SUCCESS}, transition);

        stateMachine.mapTransition(
                new State[] {State.AFTER_ALL_FAIL, State.AFTER_ALL_SUCCESS},
                sm -> {
                    try {
                        Optional<Field> optional = REFLECTION_UTILS.getArgumentField(testClass);
                        if (optional.isPresent()) {
                            Field field = optional.get();
                            field.set(testInstance, null);
                        }
                        sm.next(State.CLEAR_FIELD_SUCCESS);
                    } catch (Throwable t) {
                        throwableCollector.accept(t);
                        sm.next(State.CLEAR_FIELD_FAILED);
                    }
                });

        stateMachine.mapTransition(
                new State[] {State.CLEAR_FIELD_SUCCESS, State.CLEAR_FIELD_FAILED},
                StateMachine::finish);

        stateMachine.run();
        AutoCloseAnnotationUtils.singleton()
                .processAutoCloseAnnotatedFields(
                        testInstance, "@TestEngine.AfterAll", throwableCollector);

        if (throwableCollector.isEmpty()) {
            engineExecutionListener.executionFinished(this, TestExecutionResult.successful());
        } else {
            engineExecutionListener.executionFinished(
                    this, TestExecutionResult.failed(throwableCollector.getFirst().orElse(null)));
        }
    }
}
