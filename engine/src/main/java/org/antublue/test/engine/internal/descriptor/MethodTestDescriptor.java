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
import org.antublue.test.engine.internal.TestEngineExecutionContext;
import org.antublue.test.engine.internal.TestEngineLockUtils;
import org.antublue.test.engine.internal.TestEngineReflectionUtils;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.util.ThrowableCollector;
import org.antublue.test.engine.internal.util.ThrowableConsumer;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Class to implement an method descriptor
 */
@SuppressWarnings("PMD.EmptyCatchBlock")
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
    public MethodTestDescriptor(
            UniqueId uniqueId,
            String displayName,
            Class<?> testClass,
            Argument testArgument,
            Method testMethod) {
        super(uniqueId, displayName);
        this.testClass = testClass;
        this.testArgument = testArgument;
        this.testMethod = testMethod;
        this.testMethod.setAccessible(true);
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
     *
     * @param testEngineExecutionContext testEngineExecutionContext
     */
    public void execute(TestEngineExecutionContext testEngineExecutionContext) {
        ThrowableCollector throwableCollector = getThrowableCollector();

        final Object testInstance = testEngineExecutionContext.getTestInstance();
        final Class<?> testClass = testInstance.getClass();
        final String testClassName = testClass.getName();

        EngineExecutionListener engineExecutionListener =
                testEngineExecutionContext.getExecutionRequest().getEngineExecutionListener();

        engineExecutionListener.executionStarted(this);

        try {
            TestEngineReflectionUtils
                    .getBeforeEachMethods(testClass)
                    .forEach((ThrowableConsumer<Method>) method -> {
                        TestEngineLockUtils.processLock(method);
                        LOGGER.trace(
                                "invoking test instance [%s] @TestEngine.BeforeEach method [%s]",
                                testClassName,
                                method.getName());
                        try {
                            method.invoke(testInstance, (Object[]) null);
                        } finally {
                            TestEngineLockUtils.processUnlock(method);
                            flush();
                        }
                    });
        } catch (Throwable t) {
            t = pruneStackTrace(t, testClassName);
            System.out.println("-->");
            t.printStackTrace();
            System.out.println("<--");
            throwableCollector.add(t);
        }

        if (throwableCollector.isEmpty()) {
            try {
                LOGGER.trace(
                        "invoking test instance [%s] @TestEngine.Test method [%s]",
                        testClassName,
                        testMethod.getName());
                try {
                    TestEngineLockUtils.processLock(testMethod);
                    testMethod.invoke(testInstance, (Object[]) null);
                } finally {
                    TestEngineLockUtils.processUnlock(testMethod);
                    flush();
                }
            } catch (Throwable t) {
                t = pruneStackTrace(t, testClassName);
                t.printStackTrace();
                throwableCollector.add(t);
            }
        }

        try {
            TestEngineReflectionUtils
                    .getAfterEachMethods(testClass)
                    .forEach((ThrowableConsumer<Method>) method -> {
                        TestEngineLockUtils.processLock(method);
                        LOGGER.trace(
                                "invoking test instance [%s] @TestEngine.AfterEach method [%s]",
                                testClassName,
                                method.getName());
                        try {
                            method.invoke(testInstance, (Object[]) null);
                        } finally {
                            TestEngineLockUtils.processUnlock(method);
                            flush();
                        }
                    });
        } catch (Throwable t) {
            t = pruneStackTrace(t, testClassName);
            t.printStackTrace();
            throwableCollector.add(t);
        }

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