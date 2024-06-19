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
import java.util.function.Consumer;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.metadata.MetadataConstants;
import org.antublue.test.engine.internal.support.ArgumentAnnotationSupport;
import org.antublue.test.engine.internal.support.DisplayNameSupport;
import org.antublue.test.engine.internal.support.OrdererSupport;
import org.antublue.test.engine.internal.support.RandomAnnotationSupport;
import org.antublue.test.engine.internal.util.Predicates;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;

/** Class to implement a ArgumentTestDescriptor */
public class ArgumentTestDescriptor extends ExecutableTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArgumentTestDescriptor.class);

    private final Class<?> testClass;
    private final Argument<?> testArgument;
    private final List<Method> beforeAllMethods;
    private final List<Method> afterAllMethods;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     * @param testClass testClass
     * @param beforeAllMethods beforeAllMethods
     * @param afterAllMethods afterAllMethods
     * @param testArgument testArgument
     */
    public ArgumentTestDescriptor(
            UniqueId uniqueId,
            String displayName,
            Class<?> testClass,
            List<Method> beforeAllMethods,
            List<Method> afterAllMethods,
            Argument<?> testArgument) {
        super(uniqueId, displayName);
        this.testClass = testClass;
        this.testArgument = testArgument;
        this.beforeAllMethods = beforeAllMethods;
        this.afterAllMethods = afterAllMethods;
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(ClassSource.from(testClass));
    }

    @Override
    public Type getType() {
        return Type.CONTAINER_AND_TEST;
    }

    @Override
    public void execute(ExecutionRequest executionRequest) {
        LOGGER.trace("execute(ExecutionRequest executionRequest)");

        getStopWatch().reset();

        setExecutionRequest(executionRequest);

        Object testInstance = getParent(ExecutableTestDescriptor.class).getTestInstance();
        Preconditions.notNull(testInstance, "testInstance is null");
        setTestInstance(testInstance);

        getMetadata().put(MetadataConstants.TEST_CLASS, testClass);
        getMetadata()
                .put(
                        MetadataConstants.TEST_CLASS_DISPLAY_NAME,
                        DisplayNameSupport.getDisplayName(testClass));

        getMetadata().put(MetadataConstants.TEST_ARGUMENT, testArgument);

        executionRequest.getEngineExecutionListener().executionStarted(this);

        ThrowableCollector throwableCollector = getThrowableCollector();

        throwableCollector.execute(this::setArgumentFields);
        if (throwableCollector.isEmpty()) {
            throwableCollector.execute(this::setRandomFields);
            if (throwableCollector.isEmpty()) {
                throwableCollector.execute(this::beforeAllMethods);
                if (throwableCollector.isEmpty()) {
                    execute();
                }
                throwableCollector.execute(this::afterAllMethods);
            }
            throwableCollector.execute(this::clearRandomFields);
        }
        throwableCollector.execute(this::clearArgumentFields);

        getStopWatch().stop();

        getMetadata()
                .put(
                        MetadataConstants.TEST_DESCRIPTOR_ELAPSED_TIME,
                        getStopWatch().elapsedNanoseconds());

        if (getThrowableCollector().isEmpty()) {
            getMetadata().put(MetadataConstants.TEST_DESCRIPTOR_STATUS, MetadataConstants.PASS);
            executionRequest
                    .getEngineExecutionListener()
                    .executionFinished(this, TestExecutionResult.successful());
        } else {
            /*
            getParent(ClassTestDescriptor.class)
                    .getThrowableCollector()
                    .add(
                            new TestClassFailedException(
                                    format(
                                            "Exception testing test class [%s]",
                                            getDisplayName(testClass))));
             */
            getMetadata().put(MetadataConstants.TEST_DESCRIPTOR_STATUS, MetadataConstants.FAIL);
            executionRequest
                    .getEngineExecutionListener()
                    .executionFinished(
                            this,
                            TestExecutionResult.failed(getThrowableCollector().getThrowable()));
        }
    }

    private void setArgumentFields() throws Throwable {
        LOGGER.trace(
                "setArgumentFields() testClass [%s] testInstance [%s] testArgument [%s]",
                getTestInstance().getClass().getName(), getTestInstance(), testArgument);

        ArgumentAnnotationSupport.setArgumentFields(getTestInstance(), testArgument);
    }

    private void setRandomFields() throws Throwable {
        LOGGER.trace(
                "setRandomFields() testClass [%s] testInstance [%s] testArgument [%s]",
                getTestInstance().getClass().getName(), getTestInstance(), testArgument);

        RandomAnnotationSupport.setRandomFields(getTestInstance());
    }

    private void beforeAllMethods() throws Throwable {
        LOGGER.trace(
                "beforeAllMethods() testClass [%s] testInstance [%s]",
                getTestInstance().getClass().getName(), getTestInstance());

        for (Method method : beforeAllMethods) {
            LOGGER.trace(
                    "beforeAllMethods() testClass [%s] testInstance [%s] method [%s]",
                    getTestInstance().getClass().getName(), getTestInstance(), method);
            method.invoke(getTestInstance());
        }
    }

    private void execute() {
        LOGGER.trace(
                "execute() testClass [%s] testInstance [%s]",
                getTestInstance().getClass().getName(), getTestInstance());

        getChildren()
                .forEach(
                        (Consumer<TestDescriptor>)
                                testDescriptor -> {
                                    if (testDescriptor instanceof ExecutableTestDescriptor) {
                                        ((ExecutableTestDescriptor) testDescriptor)
                                                .execute(getExecutionRequest());
                                    }
                                });
    }

    private void afterAllMethods() throws Throwable {
        LOGGER.trace(
                "afterAllMethods() testClass [%s] testInstance [%s]",
                getTestInstance().getClass().getName(), getTestInstance());

        for (Method method : afterAllMethods) {
            LOGGER.trace(
                    "afterAllMethods() testClass [%s] testInstance [%s] method [%s]",
                    getTestInstance().getClass().getName(), getTestInstance(), method);
            method.invoke(getTestInstance());
        }
    }

    private void clearRandomFields() throws Throwable {
        LOGGER.trace(
                "clearRandomFields() testClass [%s] testInstance [%s]",
                getTestInstance().getClass().getName(), getTestInstance());

        RandomAnnotationSupport.clearRandomFields(getTestInstance());
    }

    private void clearArgumentFields() throws Throwable {
        LOGGER.trace(
                "clearArgumentFields() testClass [%s] testInstance [%s]",
                getTestInstance().getClass().getName(), getTestInstance());

        ArgumentAnnotationSupport.setArgumentFields(getTestInstance(), null);
    }

    /**
     * Method to create an ArgumentTestDescriptor
     *
     * @param parentUniqueId parentUniqueId
     * @param testClass testClass
     * @param testArgument testArgument
     * @param testArgumentIndex testArgumentIndex
     * @return an ArgumentTestDescriptor
     */
    public static ArgumentTestDescriptor create(
            UniqueId parentUniqueId,
            Class<?> testClass,
            Argument<?> testArgument,
            int testArgumentIndex) {
        Preconditions.notNull(parentUniqueId, "parentUniqueId is null");
        Preconditions.notNull(testClass, "testClass is null");
        Preconditions.notNull(testArgument, "testArgument is null");

        UniqueId uniqueId =
                parentUniqueId.append(
                        ArgumentTestDescriptor.class.getName(),
                        testArgumentIndex + "/" + testArgument.getName());

        LOGGER.trace("uniqueId [%s]", uniqueId);

        String displayName = testArgument.getName();

        LOGGER.trace("displayName [%s]", displayName);

        List<Method> beforeAllMethods =
                ReflectionSupport.findMethods(
                        testClass, Predicates.BEFORE_ALL_METHOD, HierarchyTraversalMode.TOP_DOWN);

        if (!beforeAllMethods.isEmpty() && LOGGER.isTraceEnabled()) {
            beforeAllMethods.forEach(method -> LOGGER.trace("beforeAll method [%s]", method));
        }

        beforeAllMethods =
                OrdererSupport.orderTestMethods(beforeAllMethods, HierarchyTraversalMode.TOP_DOWN);

        List<Method> afterAllMethods =
                ReflectionSupport.findMethods(
                        testClass, Predicates.AFTER_ALL_METHOD, HierarchyTraversalMode.BOTTOM_UP);

        if (!afterAllMethods.isEmpty() && LOGGER.isTraceEnabled()) {
            afterAllMethods.forEach(method -> LOGGER.trace("afterAll method [%s]", method));
        }

        afterAllMethods =
                OrdererSupport.orderTestMethods(afterAllMethods, HierarchyTraversalMode.BOTTOM_UP);

        return new ArgumentTestDescriptor(
                uniqueId, displayName, testClass, beforeAllMethods, afterAllMethods, testArgument);
    }
}
