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
import org.antublue.test.engine.api.statemachine.StateMachine;
import org.antublue.test.engine.internal.AutoCloseAnnotationUtils;
import org.antublue.test.engine.internal.ExecutorContext;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
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
    private final ThrowableCollector throwableCollector;
    private final StateMachine<State> stateMachine;
    private ExecutorContext executorContext;
    private Object testInstance;

    public enum State {
        BEGIN,
        PRE_BEFORE_ALL,
        BEFORE_ALL,
        POST_BEFORE_ALL,
        EXECUTE,
        SKIP,
        SKIP2,
        PRE_AFTER_ALL,
        AFTER_ALL,
        POST_AFTER_ALL,
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
        this.throwableCollector = new ThrowableCollector();
        this.stateMachine =
                new StateMachine<State>(getClass().getName())
                        .addTransition(State.BEGIN, this::begin)
                        .addTransition(State.PRE_BEFORE_ALL, this::preBeforeAll)
                        .addTransition(State.BEFORE_ALL, this::beforeAll)
                        .addTransition(State.POST_BEFORE_ALL, this::postBeforeAll)
                        .addTransition(State.EXECUTE, this::execute)
                        .addTransition(State.SKIP, this::skip)
                        .addTransition(State.SKIP2, this::skip2)
                        .addTransition(State.PRE_AFTER_ALL, this::preAfterAll)
                        .addTransition(State.AFTER_ALL, this::afterAll)
                        .addTransition(State.POST_AFTER_ALL, this::postAfterAll)
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

        try {
            stateMachine.run(State.BEGIN);
        } catch (Throwable t) {
            t.printStackTrace();
            System.out.flush();
        }
    }

    private void begin(StateMachine<State> stateStateMachine) {
        try {
            executorContext
                    .getExecutionRequest()
                    .getEngineExecutionListener()
                    .executionStarted(this);

            testInstance = executorContext.getTestInstance();

            Optional<Field> optionalField = REFLECTION_UTILS.getArgumentField(testClass);
            if (optionalField.isPresent()) {
                LOGGER.trace("injecting test argument");
                optionalField.get().set(testInstance, testArgument);
            }

            stateStateMachine.signal(State.PRE_BEFORE_ALL);
        } catch (Throwable t) {
            throwableCollector.accept(THROWABLE_UTILS.pruneStackTrace(testClass, t));
            stateStateMachine.signal(State.SKIP);
        } finally {
            System.out.flush();
        }
    }

    private void preBeforeAll(StateMachine<State> stateStateMachine) {
        stateStateMachine.signal(State.BEFORE_ALL);
    }

    private void beforeAll(StateMachine<State> stateStateMachine) {
        try {
            List<Method> methods = REFLECTION_UTILS.getBeforeAllMethods(testClass);
            for (Method method : methods) {
                try {
                    LOCK_ANNOTATION_UTILS.processLockAnnotations(method);
                    if (REFLECTION_UTILS.acceptsArgument(method, testArgument)) {
                        method.invoke(testInstance, testArgument);
                    } else {
                        method.invoke(testInstance, NO_OBJECT_ARGS);
                    }
                } finally {
                    LOCK_ANNOTATION_UTILS.processUnlockAnnotations(method);
                    System.out.flush();
                }
            }
        } catch (Throwable t) {
            throwableCollector.accept(THROWABLE_UTILS.pruneStackTrace(testClass, t));
        } finally {
            stateStateMachine.signal(State.POST_BEFORE_ALL);
            System.out.flush();
        }
    }

    private void postBeforeAll(StateMachine<State> stateStateMachine) {
        if (throwableCollector.isEmpty()) {
            stateStateMachine.signal(State.EXECUTE);
        } else {
            stateStateMachine.signal(State.SKIP);
        }
    }

    private void execute(StateMachine<State> stateStateMachine) {
        try {
            List<MethodTestDescriptor> methodTestDescriptors =
                    getChildren(MethodTestDescriptor.class);
            for (MethodTestDescriptor methodTestDescriptor : methodTestDescriptors) {
                methodTestDescriptor.execute(executorContext);
            }
        } catch (Throwable t) {
            throwableCollector.accept(THROWABLE_UTILS.pruneStackTrace(testClass, t));
        } finally {
            stateStateMachine.signal(State.PRE_AFTER_ALL);
            System.out.flush();
        }
    }

    private void skip(StateMachine<State> stateStateMachine) {
        try {
            List<MethodTestDescriptor> methodTestDescriptors =
                    getChildren(MethodTestDescriptor.class);
            for (MethodTestDescriptor methodTestDescriptor : methodTestDescriptors) {
                methodTestDescriptor.skip(executorContext);
            }
        } catch (Throwable t) {
            throwableCollector.accept(THROWABLE_UTILS.pruneStackTrace(testClass, t));
        } finally {
            stateStateMachine.signal(State.PRE_AFTER_ALL);
            System.out.flush();
        }
    }

    private void skip2(StateMachine<State> stateStateMachine) {
        try {
            List<MethodTestDescriptor> methodTestDescriptors =
                    getChildren(MethodTestDescriptor.class);
            for (MethodTestDescriptor methodTestDescriptor : methodTestDescriptors) {
                methodTestDescriptor.skip(executorContext);
            }
        } catch (Throwable t) {
            throwableCollector.accept(t);
        } finally {
            stateStateMachine.signal(State.END);
            System.out.flush();
        }
    }

    private void preAfterAll(StateMachine<State> stateStateMachine) {
        stateStateMachine.signal(State.AFTER_ALL);
    }

    private void afterAll(StateMachine<State> stateStateMachine) {
        try {
            List<Method> methods = REFLECTION_UTILS.getAfterAllMethods(testClass);
            for (Method method : methods) {
                try {
                    LOCK_ANNOTATION_UTILS.processLockAnnotations(method);
                    if (REFLECTION_UTILS.acceptsArgument(method, testArgument)) {
                        method.invoke(testInstance, testArgument);
                    } else {
                        method.invoke(testInstance, NO_OBJECT_ARGS);
                    }
                } catch (Throwable t) {
                    throwableCollector.accept(THROWABLE_UTILS.pruneStackTrace(testClass, t));
                } finally {
                    LOCK_ANNOTATION_UTILS.processUnlockAnnotations(method);
                    System.out.flush();
                }
            }
        } catch (Throwable t) {
            throwableCollector.accept(t);
        } finally {
            stateStateMachine.signal(State.POST_AFTER_ALL);
            System.out.flush();
        }
    }

    private void postAfterAll(StateMachine<State> stateStateMachine) {
        stateStateMachine.signal(State.END);
    }

    private void end(StateMachine<State> stateStateMachine) {
        try {
            Optional<Field> optionalField = REFLECTION_UTILS.getArgumentField(testClass);
            if (optionalField.isPresent()) {
                LOGGER.trace("injecting test argument");
                optionalField.get().set(testInstance, null);
            }
        } catch (Throwable t) {
            // DO NOTHING
        } finally {
            AutoCloseAnnotationUtils.singleton()
                    .processAutoCloseAnnotatedFields(
                            testInstance, "@TestEngine.AfterAll", throwableCollector);

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
                                this,
                                TestExecutionResult.failed(
                                        throwableCollector.getFirst().orElse(null)));
            }

            stateStateMachine.stop();
            System.out.flush();
        }
    }
}
