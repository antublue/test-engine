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

/** Class to implement a method descriptor */
@SuppressWarnings({"PMD.AvoidAccessibilityAlteration", "PMD.EmptyCatchBlock"})
public final class MethodTestDescriptor extends ExtendedAbstractTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodTestDescriptor.class);

    private static final ReflectionUtils REFLECTION_UTILS = ReflectionUtils.singleton();

    private enum State {
        BEFORE_EACH,
        BEFORE_EACH_SUCCESS,
        BEFORE_EACH_FAIL,
        EXECUTE,
        EXECUTE_SUCCESS,
        EXECUTE_FAIL
    }

    private final Class<?> testClass;
    private final Argument testArgument;
    private final Method testMethod;

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
        testMethod.setAccessible(true);
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

    /** Method to execute the test descriptor */
    @Override
    public void execute(ExecutorContext executorContext) {
        LOGGER.trace(
                "execute uniqueId [%s] testClass [%s] testArgument [%s] testMethod [%s]",
                getUniqueId(), testClass.getName(), testArgument.name(), testMethod.getName());

        EngineExecutionListener engineExecutionListener =
                executorContext.getExecutionRequest().getEngineExecutionListener();

        engineExecutionListener.executionStarted(this);

        ThrowableCollector throwableCollector = new ThrowableCollector();

        LockAnnotationUtils lockAnnotationUtils = LockAnnotationUtils.singleton();

        Object testInstance = executorContext.getTestInstance();

        StateMachine<State> stateMachine = new StateMachine<>(this.toString(), State.BEFORE_EACH);

        List<Method> methods = REFLECTION_UTILS.getBeforeEachMethods(testClass);
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
                throwableCollector.isEmpty(), State.BEFORE_EACH_SUCCESS, State.BEFORE_EACH_FAIL);

        if (stateMachine.ifThen(State.BEFORE_EACH_SUCCESS, State.EXECUTE)) {
            LOGGER.trace(
                    "invoke uniqueId [%s] testClass [%s] testMethod [%s]",
                    getUniqueId(), testClass.getName(), testMethod.getName());

            lockAnnotationUtils.processLockAnnotations(testMethod);

            boolean acceptsArgument = REFLECTION_UTILS.acceptsArgument(testMethod, testArgument);

            LOGGER.trace(
                    "class [%s] method [%s] acceptsArgument [%b]",
                    testClass.getName(), testMethod.getName(), acceptsArgument);

            if (acceptsArgument) {
                REFLECTION_UTILS.invoke(
                        testInstance, testMethod, new Object[] {testArgument}, throwableCollector);
            } else {
                REFLECTION_UTILS.invoke(testInstance, testMethod, throwableCollector);
            }

            lockAnnotationUtils.processUnlockAnnotations(testMethod);

            stateMachine.ifTrueThenElse(
                    throwableCollector.isEmpty(), State.EXECUTE_SUCCESS, State.EXECUTE_FAIL);
        }

        // Always run AfterEach methods

        methods = REFLECTION_UTILS.getAfterEachMethods(testClass);
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

        AutoCloseAnnotationUtils.singleton()
                .processAutoCloseAnnotatedFields(
                        testInstance, "@TestEngine.AfterEach", throwableCollector);

        if (throwableCollector.isEmpty()) {
            engineExecutionListener.executionFinished(this, TestExecutionResult.successful());
        } else {
            engineExecutionListener.executionFinished(
                    this, TestExecutionResult.failed(throwableCollector.getFirst().orElse(null)));
        }
    }
}
