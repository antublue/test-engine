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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

@SuppressWarnings("unchecked")
public final class RunnableParameterTestDescriptor extends AbstractRunnableTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunnableParameterTestDescriptor.class);

    private final Class<?> testClass;
    private final Parameter testParameter;

    public RunnableParameterTestDescriptor(UniqueId uniqueId, String displayName, Class<?> testClass, Parameter testParameter) {
        super(uniqueId, displayName);
        this.testClass = testClass;
        this.testParameter = testParameter;
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(MethodSource.from(TestEngineReflectionUtils.getParameterSupplierMethod(testClass)));
    }

    @Override
    public Type getType() {
        return Type.CONTAINER;
    }

    @Override
    public boolean isTest() {
        return false;
    }

    @Override
    public boolean isContainer() {
        return true;
    }

    /**
     * Method to run the test descriptor
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
            testExecutionContext.setTestInstance(testInstance);

            LOGGER.trace("injecting [%s] @TestEngine.Parameter fields ...", testClassName);

            TestEngineReflectionUtils
                    .getParameterFields(testClass)
                    .forEach((ThrowableConsumer<Field>) field -> {
                        LOGGER.trace(
                                "injecting [%s] @TestEngine.Parameter field [%s] ...",
                                testClassName,
                                field.getName());
                        try {
                            field.set(testInstance, testParameter);
                        } finally {
                            flush();
                        }
                    });

            LOGGER.trace("invoking [%s] @TestEngine.Parameter methods ...", testClassName);

            TestEngineReflectionUtils
                    .getParameterMethods(testClass)
                    .forEach((ThrowableConsumer<Method>) method -> {
                        LOGGER.trace(
                                "invoking [%s] @TestEngine.Parameter method [%s] ...",
                                testClassName,
                                method.getName());
                        try {
                            method.invoke(testInstance, testParameter);
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

                getChildren(RunnableMethodTestDescriptor.class)
                        .forEach(executableMethodTestDescriptor -> {
                            executableMethodTestDescriptor.setTestExecutionContext(testExecutionContext);
                            executableMethodTestDescriptor.run();
                            throwableCollector.addAll(executableMethodTestDescriptor.getThrowableCollector());
                        });
            } catch (Throwable t) {
                throwableCollector.add(t);
                resolve(t).printStackTrace();
            }
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
            throwableCollector.add(t);
            resolve(t).printStackTrace();
        }

        if (throwableCollector.isEmpty()) {
            engineExecutionListener.executionFinished(this, TestExecutionResult.successful());
        } else {
            engineExecutionListener.executionFinished(this, TestExecutionResult.failed(throwableCollector.getFirst().get()));
        }
    }

    public Class<?> getTestClass() {
        return testClass;
    }

    public Parameter getTestParameter() {
        return testParameter;
    }
}
