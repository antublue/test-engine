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

    private static final ReflectionUtils REFLECTION_UTILS = ReflectionUtils.singleton();

    private enum State {
        SET_FIELD,
        SET_FIELD_SUCCESS,
        SET_FIELD_FAIL,
        BEFORE_ALL,
        BEFORE_ALL_SUCCESS,
        BEFORE_ALL_FAIL,
        EXECUTE,
        EXECUTE_SUCCESS,
        SKIP,
        SKIP_SUCCESS,
        AFTER_ALL,
        AFTER_ALL_SUCCESS,
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

        StateMachine<State> stateMachine =
                new StateMachine<State>(this.toString(), State.SET_FIELD);

        REFLECTION_UTILS
                .getArgumentField(testClass)
                .ifPresent(
                        field -> {
                            LOGGER.trace(
                                    "set argument field testClass [%s] field [%s] testArgument"
                                            + " [%s]",
                                    testClass.getName(), field.getName(), testArgument.name());

                            REFLECTION_UTILS.setField(
                                    testInstance, field, testArgument, throwableCollector);

                            stateMachine.ifTrueThenElse(
                                    throwableCollector.isEmpty(),
                                    State.SET_FIELD_SUCCESS,
                                    State.SET_FIELD_FAIL);
                        });

        stateMachine.ifThen(State.SET_FIELD, State.SET_FIELD_SUCCESS);

        if (stateMachine.ifThen(State.SET_FIELD_SUCCESS, State.BEFORE_ALL)) {
            List<Method> methods = REFLECTION_UTILS.getBeforeAllMethods(testClass);
            for (Method method : methods) {
                LOGGER.trace(
                        "invoke uniqueId [%s] testClass [%s] testMethod [%s]",
                        getUniqueId(), testClass.getName(), method.getName());

                lockAnnotationUtils.processLockAnnotations(method);

                boolean acceptsArgument = REFLECTION_UTILS.acceptsArgument(method, testArgument);

                LOGGER.trace(
                        "class [%s] method [%s] acceptsArgument [%b]",
                        testClass.getName(), method.getName(), acceptsArgument);

                if (acceptsArgument) {
                    REFLECTION_UTILS.invoke(
                            testInstance, method, new Object[] {testArgument}, throwableCollector);
                } else {
                    REFLECTION_UTILS.invoke(testInstance, method, throwableCollector);
                }

                lockAnnotationUtils.processUnlockAnnotations(method);

                if (throwableCollector.isNotEmpty()) {
                    break;
                }
            }

            stateMachine.ifTrueThenElse(
                    throwableCollector.isEmpty(), State.BEFORE_ALL_SUCCESS, State.BEFORE_ALL_FAIL);
        }

        List<MethodTestDescriptor> methodTestDescriptors = getChildren(MethodTestDescriptor.class);

        if (stateMachine.ifThen(State.BEFORE_ALL_SUCCESS, State.EXECUTE)) {
            methodTestDescriptors.forEach(
                    methodTestDescriptor -> methodTestDescriptor.execute(executorContext));

            stateMachine.set(State.EXECUTE_SUCCESS);
        }

        if (stateMachine.ifThen(State.BEFORE_ALL_FAIL, State.SKIP)) {
            methodTestDescriptors.forEach(
                    methodTestDescriptor -> {
                        LOGGER.trace(
                                "skip uniqueId [%s] testClass [%s] testMethod [%s]",
                                methodTestDescriptor.getUniqueId(),
                                testClass.getName(),
                                methodTestDescriptor.getTestMethod().getName());

                        methodTestDescriptor.skip(executorContext);
                    });

            stateMachine.set(State.SKIP_SUCCESS);
        }

        if (stateMachine.ifNotThen(State.SET_FIELD_FAIL, State.AFTER_ALL)) {
            List<Method> methods = REFLECTION_UTILS.getAfterAllMethods(testClass);
            for (Method method : methods) {
                LOGGER.trace(
                        "invoke uniqueId [%s] testClass [%s] testMethod [%s]",
                        getUniqueId(), testClass.getName(), method.getName());

                lockAnnotationUtils.processLockAnnotations(method);

                boolean acceptsArgument = REFLECTION_UTILS.acceptsArgument(method, testArgument);

                LOGGER.trace(
                        "class [%s] method [%s] acceptsArgument [%b]",
                        testClass.getName(), method.getName(), acceptsArgument);

                if (acceptsArgument) {
                    REFLECTION_UTILS.invoke(
                            testInstance, method, new Object[] {testArgument}, throwableCollector);
                } else {
                    REFLECTION_UTILS.invoke(testInstance, method, throwableCollector);
                }

                lockAnnotationUtils.processUnlockAnnotations(method);
            }

            stateMachine.set(State.AFTER_ALL_SUCCESS);
        }

        AutoCloseAnnotationUtils.singleton()
                .processAutoCloseAnnotatedFields(
                        testInstance, "@TestEngine.AfterAll", throwableCollector);

        REFLECTION_UTILS
                .getArgumentField(testClass)
                .ifPresent(
                        field -> {
                            LOGGER.trace(
                                    "set argument field testClass [%s] field [%s] testArgument"
                                            + " [%s]",
                                    testClass.getName(), field.getName(), null);

                            REFLECTION_UTILS.setField(
                                    testInstance, field, null, throwableCollector);
                        });

        if (throwableCollector.isEmpty()) {
            engineExecutionListener.executionFinished(this, TestExecutionResult.successful());
        } else {
            engineExecutionListener.executionFinished(
                    this, TestExecutionResult.failed(throwableCollector.getFirst().orElse(null)));
        }
    }
}
