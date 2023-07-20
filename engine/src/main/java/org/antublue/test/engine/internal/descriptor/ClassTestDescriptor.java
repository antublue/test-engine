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

import org.antublue.test.engine.internal.TestEngineAutoCloseUtils;
import org.antublue.test.engine.internal.TestEngineExecutorContext;
import org.antublue.test.engine.internal.TestEngineLockUtils;
import org.antublue.test.engine.internal.TestEngineReflectionUtils;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.util.MethodUtils;
import org.antublue.test.engine.internal.util.ObjectUtils;
import org.antublue.test.engine.internal.util.ThrowableCollector;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;

import java.lang.reflect.Method;
import java.util.List;
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
     */
    @Override
    public void execute(TestEngineExecutorContext testEngineExecutorContext) {
        LOGGER.trace("execute uniqueId [%s] testClass [%s]", getUniqueId(), testClass.getName());

        EngineExecutionListener engineExecutionListener = testEngineExecutorContext.getEngineExecutionListener();
        engineExecutionListener.executionStarted(this);

        ThrowableCollector throwableCollector = testEngineExecutorContext.getThrowableCollector();

        ObjectUtils.instantiate(testClass, o -> testInstance = o, throwableCollector::add);

        if (throwableCollector.isEmpty()) {
            List<Method> methods = TestEngineReflectionUtils.getPrepareMethods(testClass);
            for (Method method : methods) {
                LOGGER.trace(
                        "invoke uniqueId [%s] testClass [%s] testMethod [%s]",
                        getUniqueId(),
                        testClass.getName(),
                        method.getName());

                TestEngineLockUtils.processLockAnnotations(method);

                MethodUtils.invoke(
                        testInstance,
                        method,
                        throwable -> {
                            throwableCollector.add(throwable);
                            throwable.printStackTrace();
                        });

                TestEngineLockUtils.processUnlockAnnotations(method);

                if (throwableCollector.isNotEmpty()) {
                    break;
                }
            }
        }

        List<ArgumentTestDescriptor> argumentTestDescriptors = getChildren(ArgumentTestDescriptor.class);

        if (throwableCollector.isEmpty()) {
            argumentTestDescriptors
                    .forEach(argumentTestDescriptor -> {
                        argumentTestDescriptor.setTestInstance(testInstance);
                        argumentTestDescriptor.execute(testEngineExecutorContext);
                    } );
        } else {
            argumentTestDescriptors
                    .forEach(argumentTestDescriptor -> {
                        LOGGER.trace(
                                "skip uniqueId [%s] testClass [%s] testArgument [%s]",
                                argumentTestDescriptor.getUniqueId(),
                                testClass.getName(),
                                argumentTestDescriptor.getTestArgument().name());

                        argumentTestDescriptor.skip(testEngineExecutorContext);
                    });
        }

        if (testInstance != null) {
            List<Method> methods = TestEngineReflectionUtils.getConcludeMethods(testClass);
            for (Method method : methods) {
                LOGGER.trace(
                        "invoke uniqueId [%s] testClass [%s] testMethod [%s]",
                        getUniqueId(),
                        testClass.getName(),
                        method.getName());

                TestEngineLockUtils.processLockAnnotations(method);

                MethodUtils.invoke(
                        testInstance,
                        method,
                        throwable -> {
                            throwableCollector.add(throwable);
                            throwable.printStackTrace();
                        });

                TestEngineLockUtils.processUnlockAnnotations(method);
            }

            TestEngineAutoCloseUtils
                    .processAutoCloseAnnotatedFields(
                            testInstance,
                            "@TestEngine.Conclude",
                            throwable -> {
                                throwableCollector.add(throwable);
                                throwable.printStackTrace();
                            });
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

        testInstance = null;
        testEngineExecutorContext.complete();
    }
}