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
import org.junit.platform.engine.support.descriptor.ClassSource;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Class to implement a class test descriptor
 */
public final class ClassTestDescriptor extends ExtendedAbstractTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassTestDescriptor.class);

    private final Class<?> testClass;

    /**
     * Constructor
     *
     * @param uniqueId
     * @param displayName
     * @param testClass
     */
    public ClassTestDescriptor(UniqueId uniqueId, String displayName, Class<?> testClass) {
        super(uniqueId, displayName);
        this.testClass = testClass;
    }

    /**
     * Method to get the TestSource
     *
     * @return
     */
    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(ClassSource.from(testClass));
    }

    /**
     * Method to get the test descriptor Type
     *
     * @return
     */
    @Override
    public Type getType() {
        return Type.CONTAINER;
    }

    /**
     * Method to return whether the test descriptor is a test
     *
     * @return
     */
    @Override
    public boolean isTest() {
        return false;
    }

    /**
     * Method to return whether the test descriptor is a container
     *
     * @return
     */
    @Override
    public boolean isContainer() {
        return true;
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
     * Method to test the test descriptor
     *
     * @param testExecutionContext
     */
    public void test(TestExecutionContext testExecutionContext) {
        ThrowableCollector throwableCollector = getThrowableCollector();

        EngineExecutionListener engineExecutionListener =
                testExecutionContext.getExecutionRequest().getEngineExecutionListener();

        engineExecutionListener.executionStarted(this);

        String testClassName = testClass.getName();
        Object testInstance;

        try {
            testInstance = testClass.getDeclaredConstructor((Class<?>[]) null).newInstance((Object[]) null);
            testExecutionContext.setTestInstance(testInstance);

            LOGGER.trace("invoking [%s] @TestEngine.BeforeClass methods ...", testClassName);

            TestEngineReflectionUtils
                    .getBeforeClassMethods(testClass)
                    .forEach((ThrowableConsumer<Method>) method -> {
                        LOGGER.trace(
                                String.format(
                                        "invoking [%s] @TestEngine.BeforeClass method [%s] ...",
                                        testClassName,
                                        method.getName()));

                        try {
                            method.invoke(null, (Object[]) null);
                        } finally {
                            flush();
                        }
                    });
        } catch (Throwable t) {
            throwableCollector.add(t);
            resolve(t).printStackTrace();
        }

        if (throwableCollector.isEmpty()) {
            getChildren(ParameterTestDescriptor.class)
                    .forEach(parameterTestDescriptor -> {
                        parameterTestDescriptor.test(testExecutionContext);
                        throwableCollector.addAll(parameterTestDescriptor.getThrowableCollector());
                    });
        } else {
            getChildren(ParameterTestDescriptor.class)
                    .forEach(parameterTestDescriptor -> parameterTestDescriptor.skip(testExecutionContext));
        }

        try {
            LOGGER.trace("invoking [%s] @TestEngine.AfterClass methods ...", testClassName);

            TestEngineReflectionUtils
                    .getAfterClassMethods(testClass)
                    .forEach((ThrowableConsumer<Method>) method -> {
                        LOGGER.trace(
                                String.format(
                                        "invoking [%s] @TestEngine.AfterClass method [%s] ...",
                                        testClassName,
                                        method.getName()));

                        try {
                            method.invoke(null, (Object[]) null);
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

        testExecutionContext.setTestInstance(null);

        testExecutionContext.getCountDownLatch().countDown();
    }
}
