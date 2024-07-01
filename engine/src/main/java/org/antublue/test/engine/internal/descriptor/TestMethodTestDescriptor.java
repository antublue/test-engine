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

import static java.lang.String.format;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.extension.InvocationExtension;
import org.antublue.test.engine.internal.execution.ExecutionContext;
import org.antublue.test.engine.internal.execution.ExecutionContextConstant;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.support.DisplayNameSupport;
import org.antublue.test.engine.internal.support.MethodSupport;
import org.antublue.test.engine.internal.support.ObjectSupport;
import org.antublue.test.engine.internal.support.OrdererSupport;
import org.antublue.test.engine.internal.util.Predicates;
import org.antublue.test.engine.internal.util.ThrowableCollector;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.util.Preconditions;
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
    private final InvocationExtension invocationExtension;

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
            Argument<?> testArgument,
            InvocationExtension invocationExtension) {
        super(uniqueId, displayName);
        this.testClass = testClass;
        this.beforeEachMethods = beforeEachMethods;
        this.testMethod = testMethod;
        this.afterEachMethods = afterEachMethods;
        this.testArgument = testArgument;
        this.invocationExtension = invocationExtension;
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
    public void execute(ExecutionContext executionContext) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("execute(ExecutionContext executionContext) %s", toString());
        }

        Object testInstance = executionContext.get(ExecutionContextConstant.TEST_INSTANCE);
        Preconditions.notNull(testInstance, "testInstance is null");

        stopWatch.reset();

        getMetadata().put(MetadataTestDescriptorConstants.TEST_CLASS, testClass);
        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_CLASS_DISPLAY_NAME,
                        DisplayNameSupport.getDisplayName(testClass));

        getMetadata().put(MetadataTestDescriptorConstants.TEST_ARGUMENT, testArgument);
        getMetadata().put(MetadataTestDescriptorConstants.TEST_METHOD, testMethod);
        getMetadata()
                .put(MetadataTestDescriptorConstants.TEST_METHOD_DISPLAY_NAME, getDisplayName());

        executionContext.getExecutionRequest().getEngineExecutionListener().executionStarted(this);

        throwableCollector.execute(() -> beforeEach(executionContext));
        if (throwableCollector.isEmpty()) {
            throwableCollector.execute(() -> test(executionContext));
        }
        throwableCollector.execute(() -> afterEach(executionContext));

        stopWatch.stop();

        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_DESCRIPTOR_ELAPSED_TIME,
                        stopWatch.elapsedNanoseconds());

        List<Throwable> throwables = collectThrowables();
        if (throwables.isEmpty()) {
            getMetadata()
                    .put(
                            MetadataTestDescriptorConstants.TEST_DESCRIPTOR_STATUS,
                            MetadataTestDescriptorConstants.PASS);
            executionContext
                    .getExecutionRequest()
                    .getEngineExecutionListener()
                    .executionFinished(this, TestExecutionResult.successful());
        } else {
            getMetadata()
                    .put(
                            MetadataTestDescriptorConstants.TEST_DESCRIPTOR_STATUS,
                            MetadataTestDescriptorConstants.FAIL);
            executionContext
                    .getExecutionRequest()
                    .getEngineExecutionListener()
                    .executionFinished(this, throwableCollector.toTestExecutionResult());
        }
    }

    @Override
    public void skip(ExecutionContext executionContext) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("skip(ExecutionContext executionContext) %s", toString());
        }

        stopWatch.reset();

        getMetadata().put(MetadataTestDescriptorConstants.TEST_CLASS, testClass);
        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_CLASS_DISPLAY_NAME,
                        DisplayNameSupport.getDisplayName(testClass));
        getMetadata().put(MetadataTestDescriptorConstants.TEST_ARGUMENT, testArgument);
        getMetadata().put(MetadataTestDescriptorConstants.TEST_METHOD, testMethod);
        getMetadata()
                .put(MetadataTestDescriptorConstants.TEST_METHOD_DISPLAY_NAME, getDisplayName());
        getMetadata().put(MetadataTestDescriptorConstants.TEST_DESCRIPTOR_ELAPSED_TIME, 0);
        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_DESCRIPTOR_STATUS,
                        MetadataTestDescriptorConstants.SKIP);

        executionContext
                .getExecutionRequest()
                .getEngineExecutionListener()
                .executionSkipped(
                        this,
                        format(
                                "Argument [%s] test method [%s] skipped",
                                testArgument, testMethod.getName()));
    }

    @Override
    public String toString() {
        return getClass().getName()
                + "{ "
                + "testClass ["
                + testClass.getName()
                + "]"
                + " beforeEachMethods ["
                + ObjectSupport.toString(beforeEachMethods)
                + "]"
                + " testMethod ["
                + testMethod.getName()
                + "] afterEachMethods ["
                + ObjectSupport.toString(afterEachMethods)
                + "]"
                + " }";
    }

    private void beforeEach(ExecutionContext executionContext) throws Throwable {
        Object testInstance = executionContext.get(ExecutionContextConstant.TEST_INSTANCE);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "beforeEach() testClass [%s] testInstance [%s]",
                    testInstance.getClass().getName(), testInstance);
        }

        ThrowableCollector localThrowableCollector = new ThrowableCollector();

        localThrowableCollector.execute(
                () ->
                        invocationExtension.beforeInvocationCallback(
                                TestEngine.BeforeEach.class, testInstance, null),
                () -> {
                    for (Method method : beforeEachMethods) {
                        MethodSupport.invoke(testInstance, method);
                    }
                },
                () ->
                        invocationExtension.afterInvocationCallback(
                                TestEngine.BeforeEach.class,
                                testInstance,
                                null,
                                localThrowableCollector.getFirst()));

        throwableCollector.getThrowables().addAll(localThrowableCollector.getThrowables());
    }

    private void test(ExecutionContext executionContext) throws Throwable {
        Object testInstance = executionContext.get(ExecutionContextConstant.TEST_INSTANCE);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "test() testClass [%s] testInstance [%s] method [%s]",
                    testInstance.getClass().getName(), testInstance, testMethod);
        }

        ThrowableCollector localThrowableCollector = new ThrowableCollector();

        localThrowableCollector.execute(
                () ->
                        invocationExtension.beforeInvocationCallback(
                                TestEngine.Test.class, testInstance, testMethod),
                () -> MethodSupport.invoke(testInstance, testMethod),
                () ->
                        invocationExtension.afterInvocationCallback(
                                TestEngine.Test.class,
                                testInstance,
                                testMethod,
                                localThrowableCollector.getFirst()));

        throwableCollector.getThrowables().addAll(localThrowableCollector.getThrowables());
    }

    private void afterEach(ExecutionContext executionContext) throws Throwable {
        Object testInstance = executionContext.get(ExecutionContextConstant.TEST_INSTANCE);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "afterEach() testClass [%s] testInstance [%s]",
                    testInstance.getClass().getName(), testInstance);
        }

        ThrowableCollector localThrowableCollector = new ThrowableCollector();

        localThrowableCollector.execute(
                () ->
                        invocationExtension.beforeInvocationCallback(
                                TestEngine.AfterEach.class, testInstance, null),
                () -> {
                    for (Method method : afterEachMethods) {
                        MethodSupport.invoke(testInstance, method);
                    }
                },
                () ->
                        invocationExtension.afterInvocationCallback(
                                TestEngine.AfterEach.class,
                                testInstance,
                                null,
                                localThrowableCollector.getFirst()));

        throwableCollector.getThrowables().addAll(localThrowableCollector.getThrowables());
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
            Argument<?> testArgument,
            InvocationExtension invocationExtension) {
        Preconditions.notNull(parentUniqueId, "parentUniqueId is null");
        Preconditions.notNull(testClass, "testClass is null");
        Preconditions.notNull(testMethod, "testMethod is null");
        Preconditions.notNull(testArgument, "testArgument is null");

        UniqueId uniqueId =
                parentUniqueId.append(
                        TestMethodTestDescriptor.class.getName(), testMethod.getName());

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("uniqueId [%s]", uniqueId);
        }

        String displayName = DisplayNameSupport.getDisplayName(testMethod);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("displayName [%s]", displayName);
        }

        List<Method> beforeEachMethods =
                MethodSupport.findMethods(
                        testClass, Predicates.BEFORE_EACH_METHOD, HierarchyTraversalMode.TOP_DOWN);

        beforeEachMethods =
                OrdererSupport.orderTestMethods(beforeEachMethods, HierarchyTraversalMode.TOP_DOWN);

        if (LOGGER.isTraceEnabled() && !beforeEachMethods.isEmpty()) {
            beforeEachMethods.forEach(
                    method -> LOGGER.trace("beforeEachMethods method [%s]", method));
        }

        List<Method> afterEachMethods =
                MethodSupport.findMethods(
                        testClass, Predicates.AFTER_EACH_METHOD, HierarchyTraversalMode.BOTTOM_UP);

        afterEachMethods =
                OrdererSupport.orderTestMethods(afterEachMethods, HierarchyTraversalMode.BOTTOM_UP);

        if (LOGGER.isTraceEnabled() && !afterEachMethods.isEmpty()) {
            afterEachMethods.forEach(
                    method -> LOGGER.trace("afterEachMethods method [%s]", method));
        }

        return new TestMethodTestDescriptor(
                uniqueId,
                displayName,
                testClass,
                beforeEachMethods,
                testMethod,
                afterEachMethods,
                testArgument,
                invocationExtension);
    }
}
