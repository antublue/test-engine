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
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.metadata.MetadataConstants;
import org.antublue.test.engine.internal.support.DisplayNameSupport;
import org.antublue.test.engine.internal.support.OrdererSupport;
import org.antublue.test.engine.internal.util.Predicates;
import org.antublue.test.engine.internal.util.ThrowableCollector;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;

/** Class to implement a MethodTestDescriptor */
public class TestMethodTestDescriptor extends ExecutableTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestMethodTestDescriptor.class);

    private final Class<?> testClass;
    private final Argument<?> testArgument;
    private final List<Method> beforeEachMethods;
    private final Method testMethod;
    private final List<Method> afterEachMethods;
    private Object testInstance;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     * @param testClass testClass
     * @param beforeEachMethods beforeEachMethods
     * @param testMethod testMethod
     * @param afterEachMethods afterEachMethods
     * @param testArgument testArgument
     */
    public TestMethodTestDescriptor(
            UniqueId uniqueId,
            String displayName,
            Class<?> testClass,
            List<Method> beforeEachMethods,
            Method testMethod,
            List<Method> afterEachMethods,
            Argument<?> testArgument) {
        super(uniqueId, displayName);
        this.testClass = testClass;
        this.beforeEachMethods = beforeEachMethods;
        this.testMethod = testMethod;
        this.afterEachMethods = afterEachMethods;
        this.testArgument = testArgument;
    }

    @Override
    public Type getType() {
        return Type.TEST;
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(MethodSource.from(testMethod));
    }

    @Override
    public void execute(ExecutionRequest executionRequest, Object testInstance) {
        LOGGER.trace("execute(ExecutionRequest executionRequest)");
        Preconditions.notNull(testInstance, "testInstance is null");

        this.testInstance = testInstance;

        getStopWatch().reset();

        getMetadata().put(MetadataConstants.TEST_CLASS, testClass);
        getMetadata()
                .put(
                        MetadataConstants.TEST_CLASS_DISPLAY_NAME,
                        DisplayNameSupport.getDisplayName(testClass));

        getMetadata().put(MetadataConstants.TEST_ARGUMENT, testArgument);
        getMetadata().put(MetadataConstants.TEST_METHOD, testMethod);
        getMetadata().put(MetadataConstants.TEST_METHOD_DISPLAY_NAME, getDisplayName());

        executionRequest.getEngineExecutionListener().executionStarted(this);

        ThrowableCollector throwableCollector = getThrowableCollector();

        throwableCollector.execute(this::beforeEach);
        if (throwableCollector.isEmpty()) {
            throwableCollector.execute(this::test);
        }
        throwableCollector.execute(this::afterEach);

        getStopWatch().stop();

        getMetadata()
                .put(
                        MetadataConstants.TEST_DESCRIPTOR_ELAPSED_TIME,
                        getStopWatch().elapsedNanoseconds());

        List<Throwable> throwables = collectThrowables();
        if (throwables.isEmpty()) {
            getMetadata().put(MetadataConstants.TEST_DESCRIPTOR_STATUS, MetadataConstants.PASS);
            executionRequest
                    .getEngineExecutionListener()
                    .executionFinished(this, TestExecutionResult.successful());
        } else {
            getMetadata().put(MetadataConstants.TEST_DESCRIPTOR_STATUS, MetadataConstants.FAIL);
            executionRequest
                    .getEngineExecutionListener()
                    .executionFinished(this, getThrowableCollector().toTestExecutionResult());
        }
    }

    public void skip(ExecutionRequest executionRequest) {
        LOGGER.trace("skip(ExecutionRequest executionRequest)");

        getStopWatch().stop();

        getMetadata().put(MetadataConstants.TEST_CLASS, testClass);
        getMetadata()
                .put(
                        MetadataConstants.TEST_CLASS_DISPLAY_NAME,
                        DisplayNameSupport.getDisplayName(testClass));
        getMetadata().put(MetadataConstants.TEST_ARGUMENT, testArgument);
        getMetadata().put(MetadataConstants.TEST_METHOD, testMethod);
        getMetadata().put(MetadataConstants.TEST_METHOD_DISPLAY_NAME, getDisplayName());
        getMetadata()
                .put(
                        MetadataConstants.TEST_DESCRIPTOR_ELAPSED_TIME,
                        getStopWatch().elapsedNanoseconds());
        getMetadata().put(MetadataConstants.TEST_DESCRIPTOR_STATUS, MetadataConstants.SKIP);

        executionRequest.getEngineExecutionListener().executionSkipped(this, "Skipped");
    }

    private void beforeEach() throws Throwable {
        LOGGER.trace(
                "beforeEach() testClass [%s] testInstance [%s]",
                testInstance.getClass().getName(), testInstance);

        for (Method method : beforeEachMethods) {
            LOGGER.trace(
                    "beforeEach() testClass [%s] testInstance [%s] method [%s]",
                    testInstance.getClass().getName(), testInstance, method);
            method.invoke(testInstance);
        }
    }

    private void test() throws Throwable {
        LOGGER.trace(
                "test() testClass [%s] testInstance [%s] method [%s]",
                testInstance.getClass().getName(), testInstance, testMethod);
        testMethod.invoke(testInstance);
    }

    private void afterEach() throws Throwable {
        LOGGER.trace(
                "afterEach() testClass [%s] testInstance [%s]",
                testInstance.getClass().getName(), testInstance);

        for (Method method : afterEachMethods) {
            LOGGER.trace(
                    "afterEach() testClass [%s] testInstance [%s] method [%s]",
                    testInstance.getClass().getName(), testInstance, method);
            method.invoke(testInstance);
        }
    }

    /**
     * Method to create a MethodTestDescriptor
     *
     * @param parentUniqueId parentUniqueId
     * @param testClass testClass
     * @param testMethod testMethod
     * @param testArgument testArgument
     * @return a MethodTestDescriptor
     */
    public static TestMethodTestDescriptor create(
            UniqueId parentUniqueId,
            Class<?> testClass,
            Method testMethod,
            Argument<?> testArgument) {
        Preconditions.notNull(parentUniqueId, "parentUniqueId is null");
        Preconditions.notNull(testClass, "testClass is null");
        Preconditions.notNull(testMethod, "testMethod is null");
        Preconditions.notNull(testArgument, "testArgument is null");

        UniqueId uniqueId =
                parentUniqueId.append(
                        TestMethodTestDescriptor.class.getName(), testMethod.getName());

        String displayName = DisplayNameSupport.getDisplayName(testMethod);

        List<Method> beforeEachMethods =
                ReflectionSupport.findMethods(
                        testClass, Predicates.BEFORE_EACH_METHOD, HierarchyTraversalMode.TOP_DOWN);

        beforeEachMethods =
                OrdererSupport.orderTestMethods(beforeEachMethods, HierarchyTraversalMode.TOP_DOWN);

        List<Method> afterEachMethods =
                ReflectionSupport.findMethods(
                        testClass, Predicates.AFTER_EACH_METHOD, HierarchyTraversalMode.BOTTOM_UP);

        afterEachMethods =
                OrdererSupport.orderTestMethods(afterEachMethods, HierarchyTraversalMode.BOTTOM_UP);

        return new TestMethodTestDescriptor(
                uniqueId,
                displayName,
                testClass,
                beforeEachMethods,
                testMethod,
                afterEachMethods,
                testArgument);
    }
}
