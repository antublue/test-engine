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

/** Class to implement a method descriptor */
public final class MethodTestDescriptor extends ExtendedAbstractTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodTestDescriptor.class);

    private enum State {
        BEGIN,
        PRE_BEFORE_EACH,
        BEFORE_EACH,
        POST_BEFORE_EACH,
        PRE_TEST,
        TEST,
        POST_TEST,
        PRE_AFTER_EACH,
        AFTER_EACH,
        POST_AFTER_EACH,
        END
    }

    private final Class<?> testClass;
    private final Argument testArgument;
    private final Method testMethod;

    private final ThrowableCollector throwableCollector;
    private final StateMachine<State> stateMachine;
    private ExecutorContext executorContext;
    private Object testInstance;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     * @param testClass testClass
     * @param testArgument testArgument
     * @param testMethod testMethod
     */
    MethodTestDescriptor(
            UniqueId uniqueId,
            String displayName,
            Class<?> testClass,
            Argument testArgument,
            Method testMethod) {
        super(uniqueId, displayName);
        this.testClass = testClass;
        this.testArgument = testArgument;
        this.testMethod = testMethod;
        this.throwableCollector = new ThrowableCollector();
        this.stateMachine =
                new StateMachine<State>(getClass().getName())
                        .addTransition(State.BEGIN, this::begin)
                        .addTransition(State.PRE_BEFORE_EACH, this::preBeforeEach)
                        .addTransition(State.BEFORE_EACH, this::beforeEach)
                        .addTransition(State.POST_BEFORE_EACH, this::postBeforeEach)
                        .addTransition(State.PRE_TEST, this::preTest)
                        .addTransition(State.TEST, this::test)
                        .addTransition(State.POST_TEST, this::postTest)
                        .addTransition(State.PRE_AFTER_EACH, this::preAfterEach)
                        .addTransition(State.AFTER_EACH, this::afterEach)
                        .addTransition(State.POST_AFTER_EACH, this::postAfterEach)
                        .addTransition(State.END, this::end);
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

        try {
            stateMachine.run(State.BEGIN);
        } catch (Throwable t) {
            t.printStackTrace();
            System.out.flush();
        }
    }

    private void begin(StateMachine<State> stateMachine) {
        try {
            executorContext
                    .getExecutionRequest()
                    .getEngineExecutionListener()
                    .executionStarted(this);
            testInstance = executorContext.getTestInstance();
            stateMachine.signal(State.PRE_BEFORE_EACH);
        } finally {
            System.out.flush();
        }
    }

    private void preBeforeEach(StateMachine<State> stateMachine) {
        stateMachine.signal(State.BEFORE_EACH);
    }

    private void beforeEach(StateMachine<State> stateMachine) {
        try {
            List<Method> methods = REFLECTION_UTILS.getBeforeEachMethods(testClass);
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
            throwableCollector.accept(t);
        } finally {
            stateMachine.signal(State.POST_BEFORE_EACH);
            System.out.flush();
        }
    }

    private void postBeforeEach(StateMachine<State> stateMachine) {
        if (throwableCollector.isEmpty()) {
            stateMachine.signal(State.PRE_TEST);
        } else {
            stateMachine.signal(State.PRE_AFTER_EACH);
        }
    }

    private void preTest(StateMachine<State> stateMachine) {
        stateMachine.signal(State.TEST);
    }

    private void test(StateMachine<State> stateMachine) {
        try {
            Method method = testMethod;
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
        } catch (Throwable t) {
            throwableCollector.accept(t);
        } finally {
            stateMachine.signal(State.POST_TEST);
            System.out.flush();
        }
    }

    private void postTest(StateMachine<State> stateMachine) {
        stateMachine.signal(State.PRE_AFTER_EACH);
    }

    private void preAfterEach(StateMachine<State> stateMachine) {
        stateMachine.signal(State.AFTER_EACH);
    }

    private void afterEach(StateMachine<State> stateMachine) {
        try {
            List<Method> methods = REFLECTION_UTILS.getAfterEachMethods(testClass);
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
            stateMachine.signal(State.POST_AFTER_EACH);
            System.out.flush();
        }
    }

    private void postAfterEach(StateMachine<State> stateMachine) {
        stateMachine.signal(State.END);
    }

    private void end(StateMachine<State> stateMachine) {
        AutoCloseAnnotationUtils.singleton()
                .processAutoCloseAnnotatedFields(
                        testInstance, "@TestEngine.AfterEach", throwableCollector);

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
                            TestExecutionResult.failed(throwableCollector.getFirst().orElse(null)));
        }

        stateMachine.stop();
        System.out.flush();
    }
}
