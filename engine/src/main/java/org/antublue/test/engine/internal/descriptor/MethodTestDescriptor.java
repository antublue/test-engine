/*
 * Copyright (C) 2022-2023 The AntuBLUE test-engine project authors
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

import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.internal.AutoCloseAnnotationUtils;
import org.antublue.test.engine.internal.ExecutorContext;
import org.antublue.test.engine.internal.LockAnnotationUtils;
import org.antublue.test.engine.internal.ReflectionUtils;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.util.MethodUtils;
import org.antublue.test.engine.internal.util.ThrowableCollector;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * Class to implement a method descriptor
 */
@SuppressWarnings({ "PMD.AvoidAccessibilityAlteration", "PMD.EmptyCatchBlock" })
public final class MethodTestDescriptor extends ExtendedAbstractTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodTestDescriptor.class);

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

    /**
     * Method to test the test descriptor
     */
    @Override
    public void execute(ExecutorContext executorContext) {
        LOGGER.trace(
                "execute uniqueId [%s] testClass [%s] testArgument [%s] testMethod [%s]",
                getUniqueId(),
                testClass.getName(),
                testArgument.name(),
                testMethod.getName());

        EngineExecutionListener engineExecutionListener = executorContext.getEngineExecutionListener();
        engineExecutionListener.executionStarted(this);

        ThrowableCollector throwableCollector = new ThrowableCollector();

        List<Method> methods = ReflectionUtils.getBeforeEachMethods(testClass);
        for (Method method : methods) {
            LOGGER.trace(
                    "invoke uniqueId [%s] testClass [%s] testMethod [%s]",
                    getUniqueId(),
                    testClass.getName(),
                    method.getName());

            LockAnnotationUtils.processLockAnnotations(method);

            MethodUtils.invoke(
                    testInstance,
                    method,
                    throwable -> {
                        throwableCollector.add(throwable);
                        throwable.printStackTrace();
                    });

            LockAnnotationUtils.processUnlockAnnotations(method);

            if (throwableCollector.isNotEmpty()) {
                break;
            }
        }

        if (throwableCollector.isEmpty()) {
            LOGGER.trace(
                    "invoke uniqueId [%s] testClass [%s] testMethod [%s]",
                    getUniqueId(),
                    testClass.getName(),
                    testMethod.getName());

            LockAnnotationUtils.processLockAnnotations(testMethod);

            MethodUtils.invoke(
                    testInstance,
                    testMethod,
                    throwable -> {
                        throwableCollector.add(throwable);
                        throwable.printStackTrace();
                    });

            LockAnnotationUtils.processUnlockAnnotations(testMethod);
        }

        methods = ReflectionUtils.getAfterEachMethods(testClass);
        for (Method method : methods) {
            LOGGER.trace(
                    "invoke uniqueId [%s] testClass [%s] testMethod [%s]",
                    getUniqueId(),
                    testClass.getName(),
                    method.getName());

            LockAnnotationUtils.processLockAnnotations(method);

            MethodUtils.invoke(
                    testInstance,
                    method,
                    throwable -> {
                        throwableCollector.add(throwable);
                        throwable.printStackTrace();
                    });

            LockAnnotationUtils.processUnlockAnnotations(method);
        }

        AutoCloseAnnotationUtils
                .processAutoCloseAnnotatedFields(
                        testInstance,
                        "@TestEngine.AfterEach",
                        throwable -> {
                            throwableCollector.add(throwable);
                            throwable.printStackTrace();
                        });

        if (throwableCollector.isEmpty()) {
            engineExecutionListener.executionFinished(this, TestExecutionResult.successful());
        } else {
            engineExecutionListener.executionFinished(
                    this,
                    TestExecutionResult.failed(
                            throwableCollector
                                    .getFirst()
                                    .orElse(null)));
        }
    }
}