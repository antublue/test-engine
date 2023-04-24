/*
 * Copyright 2022-2023 Douglas Hoard
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

import org.antublue.test.engine.api.Parameter;
import org.antublue.test.engine.internal.TestEngineReflectionUtils;
import org.antublue.test.engine.internal.TestExecutionContext;
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
 * Class to implement a Runnable test method descriptor
 */
public final class RunnableMethodTestDescriptor extends AbstractRunnableTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunnableMethodTestDescriptor.class);

    private final Class<?> testClass;
    private final Parameter testParameter;
    private final Method testMethod;

    /**
     * Constructor
     *
     * @param uniqueId
     * @param displayName
     * @param testClass
     * @param testParameter
     * @param testMethod
     */
    public RunnableMethodTestDescriptor(
            UniqueId uniqueId,
            String displayName,
            Class<?> testClass,
            Parameter testParameter,
            Method testMethod) {
        super(uniqueId, displayName);
        this.testClass = testClass;
        this.testParameter = testParameter;
        this.testMethod = testMethod;
        this.testMethod.setAccessible(true);
    }

    /**
     * Method to get the TestSource
     *
     * @return
     */
    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(MethodSource.from(testMethod));
    }

    /**
     * Method to get the test descriptor Type
     *
     * @return
     */
    @Override
    public Type getType() {
        return Type.TEST;
    }

    /**
     * Method to return whether the test descriptor is a test
     *
     * @return
     */
    @Override
    public boolean isTest() {
        return true;
    }

    /**
     * Method to return whether the test descriptor is a container
     *
     * @return
     */
    @Override
    public boolean isContainer() {
        return false;
    }

    /**
     * Method to get the test class
     *
     * @return
     */
    public Class<?> getTestClass() {
        return testClass;
    }

    /**
     * Method to get the test parameter
     *
     * @return
     */
    public Parameter getTestParameter() {
        return testParameter;
    }

    /**
     * Method to get the test method
     *
     * @return
     */
    public Method getTestMethod() {
        return testMethod;
    }

    /**
     * Method to run the test descriptor
     * <br>
     * The TestExecutionContext must be set prior to the call
     */
    public void run() {
        TestExecutionContext testExecutionContext = getTestExecutionContext();
        ThrowableCollector throwableCollector = getThrowableCollector();

        EngineExecutionListener engineExecutionListener =
                testExecutionContext.getExecutionRequest().getEngineExecutionListener();

        engineExecutionListener.executionStarted(this);

        final Object testInstance = testExecutionContext.getTestInstance();
        final Class<?> testClass = testInstance.getClass();
        final String testClassName = testClass.getName();

        try {
            LOGGER.trace("invoking [%s] @TestEngine.BeforeEach methods ...", testClassName);

            TestEngineReflectionUtils
                    .getBeforeEachMethods(testClass)
                    .forEach((ThrowableConsumer<Method>) method -> {
                        LOGGER.trace(
                                "invoking [%s] @TestEngine.BeforeEach method [%s] ...",
                                testClassName,
                                method.getName());
                        try {
                            method.invoke(testInstance, (Object[]) null);
                        } finally {
                            flush();
                        }
                    });
        } catch (Throwable t) {
            throwableCollector.add(t);
            resolve(t).printStackTrace();
        }

        if (throwableCollector.isEmpty()) {
            try {
                LOGGER.trace("invoking [%s] @TestEngine.Test method [%s] ...", testClassName, testMethod.getName());
                try {
                    testMethod.invoke(testInstance, (Object[]) null);
                } finally {
                    flush();
                }
            } catch (Throwable t) {
                throwableCollector.add(t);
                resolve(t).printStackTrace();
            }
        }

        try {
            LOGGER.trace("invoking [%s] @TestEngine.AfterEach methods ...", testClassName);

            TestEngineReflectionUtils
                    .getAfterEachMethods(testClass)
                    .forEach((ThrowableConsumer<Method>) method -> {
                        LOGGER.trace(
                                "invoking [%s] @TestEngine.AfterEach method [%s] ...",
                                testClassName,
                                method.getName());
                        try {
                            method.invoke(testInstance, (Object[]) null);
                        } finally {
                            flush();
                        }
                    });
        } catch (Throwable t) {
            throwableCollector.add(t);
            resolve(t).printStackTrace();
        }

        if (throwableCollector.isEmpty()) {
            engineExecutionListener.executionFinished(this, TestExecutionResult.successful());
        } else {
            engineExecutionListener.executionFinished(this, TestExecutionResult.failed(throwableCollector.getFirst().get()));
        }
    }
}
