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
import org.junit.platform.engine.support.descriptor.ClassSource;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Class to implement a class test descriptor
 */
@SuppressWarnings("PMD.EmptyCatchBlock")
public final class ClassTestDescriptor extends ExtendedAbstractTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassTestDescriptor.class);

    private final Class<?> testClass;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     * @param testClass testClass
     */
    ClassTestDescriptor(UniqueId uniqueId, String displayName, Class<?> testClass) {
        super(uniqueId, displayName);
        this.testClass = testClass;
    }

    /**
     * Method to get the TestSource
     *
     * @return the return value
     */
    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(ClassSource.from(testClass));
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
     * Method to test the test descriptor
     *
     * @param testEngineExecutionContext testEngineExecutionContext
     */
    public void execute(TestEngineExecutionContext testEngineExecutionContext) {
        ThrowableCollector throwableCollector = getThrowableCollector();

        String testClassName = testClass.getName();
        Object testInstance = null;

        EngineExecutionListener engineExecutionListener =
                testEngineExecutionContext.getExecutionRequest().getEngineExecutionListener();

        engineExecutionListener.executionStarted(this);

        try {
            LOGGER.trace("instantiating test class [%s]", testClassName);
            try {
                testInstance = testClass.getDeclaredConstructor((Class<?>[]) null).newInstance((Object[]) null);
                testEngineExecutionContext.setTestInstance(testInstance);
            } finally {
                flush();
            }

            final Object finalTestInstance = testInstance;

            TestEngineReflectionUtils
                    .getPrepareMethods(testClass)
                    .forEach((ThrowableConsumer<Method>) method -> {
                        LOGGER.trace(
                                "invoking test instance [%s] @TestEngine.Prepare method [%s]",
                                testClassName,
                                method.getName());
                        try {
                            TestEngineLockUtils.processLock(method);
                            method.invoke(finalTestInstance, (Object[]) null);
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
            getChildren(ArgumentTestDescriptor.class)
                    .forEach(argumentTestDescriptor -> argumentTestDescriptor.execute(testEngineExecutionContext));
        } else {
            getChildren(ArgumentTestDescriptor.class)
                    .forEach(argumentTestDescriptor -> argumentTestDescriptor.skip(testEngineExecutionContext));
        }

        try {
            if (testInstance != null) {
                final Object finalTestInstance = testInstance;

                TestEngineReflectionUtils
                        .getConcludeMethods(testClass)
                        .forEach((ThrowableConsumer<Method>) method -> {
                            LOGGER.trace(
                                    "invoking test instance [%s] @TestEngine.Conclude method [%s]",
                                    testClassName,
                                    method.getName());
                            try {
                                TestEngineLockUtils.processLock(method);
                                method.invoke(finalTestInstance, (Object[]) null);
                            } finally {
                                TestEngineLockUtils.processUnlock(method);
                                flush();
                            }
                        });
            }
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

        testEngineExecutionContext.setTestInstance(null);
        testEngineExecutionContext.getCountDownLatch().countDown();
    }
}