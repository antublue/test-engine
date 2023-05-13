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

import org.antublue.test.engine.api.Parameter;
import org.antublue.test.engine.internal.TestEngineExecutionContext;
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
 * Class to implement an extended parameter test descriptor
 */
public final class ParameterTestDescriptor extends ExtendedAbstractTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParameterTestDescriptor.class);

    private final Class<?> testClass;
    private final Parameter testParameter;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     * @param testClass testClass
     * @param testParameter testParameter
     */
    public ParameterTestDescriptor(UniqueId uniqueId, String displayName, Class<?> testClass, Parameter testParameter) {
        super(uniqueId, displayName);
        this.testClass = testClass;
        this.testParameter = testParameter;
    }

    /**
     * Method to get the TestSource
     *
     * @return the return value
     */
    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(MethodSource.from(TestEngineReflectionUtils.getParameterSupplierMethod(testClass)));
    }

    /**
     * Method to get the test descriptor Type
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
     * Method to get the test parameter
     * @return the return value
     */
    public Parameter getTestParameter() {
        return testParameter;
    }

    /**
     * Method to test the test descriptor
     *
     * @param testEngineExecutionContext testEngineExecutionContext
     */
    public void execute(TestEngineExecutionContext testEngineExecutionContext) {
        ThrowableCollector throwableCollector = getThrowableCollector();

        EngineExecutionListener engineExecutionListener =
                testEngineExecutionContext.getExecutionRequest().getEngineExecutionListener();

        engineExecutionListener.executionStarted(this);

        final Object testInstance = testEngineExecutionContext.getTestInstance();
        final Class<?> testClass = testInstance.getClass();
        final String testClassName = testClass.getName();

        try {
            testEngineExecutionContext.setTestInstance(testInstance);

            LOGGER.trace("injecting [%s] @TestEngine.Parameter field ...", testClassName);

            try {
                 TestEngineReflectionUtils.getParameterField(testClass).set(testInstance, testParameter);
            } finally {
                flush();
            }
        } catch (Throwable t) {
            t = pruneStackTrace(t, testClassName);
            t.printStackTrace();
            throwableCollector.add(t);
        }

        if (throwableCollector.isEmpty()) {
            try {
                TestEngineReflectionUtils
                        .getBeforeAllMethods(testClass)
                        .forEach((ThrowableConsumer<Method>) method -> {
                            LOGGER.trace(
                                    "invoking [%s] @TestEngine.BeforeAll method [%s] ...",
                                    testClassName,
                                    method.getName());
                            try {
                                method.invoke(testInstance, (Object[]) null);
                            } finally {
                                flush();
                            }
                        });
            } catch (Throwable t) {
                t = pruneStackTrace(t, testClassName);
                t.printStackTrace();
                throwableCollector.add(t);
            }

            if (!throwableCollector.isEmpty()) {
                getChildren(MethodTestDescriptor.class)
                        .forEach(methodTestDescriptor -> methodTestDescriptor.skip(testEngineExecutionContext));
            }

        } else {
            getChildren(MethodTestDescriptor.class)
                    .forEach(methodTestDescriptor -> methodTestDescriptor.skip(testEngineExecutionContext));
        }

        if (throwableCollector.isEmpty()) {
            getChildren(MethodTestDescriptor.class)
                    .forEach(methodTestDescriptor -> methodTestDescriptor.execute(testEngineExecutionContext));
        }

        try {
            TestEngineReflectionUtils
                    .getAfterAllMethods(testClass)
                    .forEach((ThrowableConsumer<Method>) method -> {
                        LOGGER.trace(
                                "invoking [%s] @TestEngine.AfterAll method [%s] ...",
                                testClassName,
                                method.getName());
                        try {
                            method.invoke(testInstance, (Object[]) null);
                        } finally {
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