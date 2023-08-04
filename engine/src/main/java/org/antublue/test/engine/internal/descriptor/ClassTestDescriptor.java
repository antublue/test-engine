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
import java.util.List;
import java.util.Optional;
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
import org.junit.platform.engine.support.descriptor.ClassSource;

/** Class to implement a class test descriptor */
@SuppressWarnings({"PMD.AvoidAccessibilityAlteration", "PMD.EmptyCatchBlock"})
public final class ClassTestDescriptor extends ExtendedAbstractTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassTestDescriptor.class);

    private static final ReflectionUtils REFLECTION_UTILS = ReflectionUtils.singleton();

    private static final Object[] NO_ARGS = null;

    private enum State {
        BEGIN,
        INSTANTIATE_TEST_INSTANCE_SUCCESS,
        INSTANTIATE_TEST_INSTANCE_FAIL,
        PREPARE_SUCCESS,
        PREPARE_FAIL,
        EXECUTE_SUCCESS,
        SKIP_SUCCESS,
        CONCLUDE_SUCCESS,
        CONCLUDE_FAIL
    }

    private final Class<?> testClass;

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

    /** Method to execute the test descriptor */
    @Override
    public void execute(ExecutorContext executorContext) {
        LOGGER.trace("execute uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());

        EngineExecutionListener engineExecutionListener =
                executorContext.getExecutionRequest().getEngineExecutionListener();

        engineExecutionListener.executionStarted(this);

        ThrowableCollector throwableCollector = new ThrowableCollector();

        LockAnnotationUtils lockAnnotationUtils = LockAnnotationUtils.singleton();

        StateMachine<State> stateMachine = new StateMachine<>(this.toString(), State.BEGIN);

        stateMachine.mapTransition(
                State.BEGIN,
                simpleStateMachine -> {
                    try {
                        testInstance =
                                testClass
                                        .getDeclaredConstructor((Class<?>[]) null)
                                        .newInstance((Object[]) null);
                        executorContext.setTestInstance(testInstance);
                        simpleStateMachine.next(State.INSTANTIATE_TEST_INSTANCE_SUCCESS);
                    } catch (Throwable t) {
                        throwableCollector.accept(t);
                        simpleStateMachine.next(State.INSTANTIATE_TEST_INSTANCE_FAIL);
                    }
                });

        stateMachine.mapTransition(
                State.INSTANTIATE_TEST_INSTANCE_SUCCESS,
                simpleStateMachine -> {
                    try {
                        List<Method> methods = REFLECTION_UTILS.getPrepareMethods(testClass);
                        for (Method method : methods) {
                            try {
                                lockAnnotationUtils.processLockAnnotations(method);
                                method.invoke(testInstance, NO_ARGS);
                            } finally {
                                lockAnnotationUtils.processUnlockAnnotations(method);
                            }
                        }
                        simpleStateMachine.next(State.PREPARE_SUCCESS);
                    } catch (Throwable t) {
                        throwableCollector.accept(t);
                        simpleStateMachine.next(State.PREPARE_FAIL);
                    }
                });

        stateMachine.mapTransition(
                State.PREPARE_SUCCESS,
                simpleStateMachine -> {
                    getChildren(ArgumentTestDescriptor.class)
                            .forEach(
                                    argumentTestDescriptor ->
                                            argumentTestDescriptor.execute(executorContext));

                    stateMachine.next(State.EXECUTE_SUCCESS);
                });

        stateMachine.mapTransition(
                State.PREPARE_FAIL,
                sm -> {
                    getChildren(ArgumentTestDescriptor.class)
                            .forEach(
                                    argumentTestDescriptor -> {
                                        LOGGER.trace(
                                                "skip uniqueId [%s] testClass [%s] testArgument"
                                                        + " [%s]",
                                                argumentTestDescriptor.getUniqueId(),
                                                testClass.getName(),
                                                argumentTestDescriptor.getTestArgument().name());

                                        argumentTestDescriptor.skip(executorContext);
                                    });

                    sm.next(State.SKIP_SUCCESS);
                });

        StateMachine.Transition<State> transition =
                sm -> {
                    List<Method> methods = REFLECTION_UTILS.getConcludeMethods(testClass);
                    for (Method method : methods) {
                        try {
                            lockAnnotationUtils.processLockAnnotations(method);
                            method.invoke(testInstance, NO_ARGS);
                        } catch (Throwable t) {
                            throwableCollector.accept(t);
                        } finally {
                            lockAnnotationUtils.processUnlockAnnotations(method);
                        }
                    }

                    if (throwableCollector.isEmpty()) {
                        sm.next(State.CONCLUDE_SUCCESS);
                    } else {
                        sm.next(State.CONCLUDE_FAIL);
                    }
                };

        stateMachine.mapTransition(
                new State[] {State.EXECUTE_SUCCESS, State.SKIP_SUCCESS}, transition);

        stateMachine.mapTransition(
                new State[] {State.CONCLUDE_SUCCESS, State.CONCLUDE_FAIL}, StateMachine::finish);

        stateMachine.run();

        AutoCloseAnnotationUtils.singleton()
                .processAutoCloseAnnotatedFields(
                        testInstance, "@TestEngine.Conclude", throwableCollector);

        if (throwableCollector.isEmpty()) {
            engineExecutionListener.executionFinished(this, TestExecutionResult.successful());
        } else {
            engineExecutionListener.executionFinished(
                    this, TestExecutionResult.failed(throwableCollector.getFirst().orElse(null)));
        }

        testInstance = null;
        executorContext.complete();
    }
}
