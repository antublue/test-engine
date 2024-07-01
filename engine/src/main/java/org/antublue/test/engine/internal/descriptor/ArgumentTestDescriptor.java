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
import java.util.function.Consumer;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.extension.InvocationInterceptor;
import org.antublue.test.engine.internal.execution.ExecutionContext;
import org.antublue.test.engine.internal.execution.ExecutionContextConstant;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.support.ArgumentAnnotationSupport;
import org.antublue.test.engine.internal.support.DisplayNameSupport;
import org.antublue.test.engine.internal.support.MethodSupport;
import org.antublue.test.engine.internal.support.ObjectSupport;
import org.antublue.test.engine.internal.support.OrdererSupport;
import org.antublue.test.engine.internal.support.RandomAnnotationSupport;
import org.antublue.test.engine.internal.util.Predicates;
import org.antublue.test.engine.internal.util.ThrowableCollector;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;

/** Class to implement a ArgumentTestDescriptor */
public class ArgumentTestDescriptor extends ExecutableTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArgumentTestDescriptor.class);

    private final Class<?> testClass;
    private final Argument<?> testArgument;
    private final List<Method> beforeAllMethods;
    private final List<Method> afterAllMethods;
    private final InvocationInterceptor invocationInterceptor;

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
            Argument<?> testArgument,
            InvocationInterceptor invocationInterceptor) {
        super(uniqueId, displayName);
        this.testClass = testClass;
        this.testArgument = testArgument;
        this.beforeAllMethods = beforeAllMethods;
        this.afterAllMethods = afterAllMethods;
        this.invocationInterceptor = invocationInterceptor;
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
    public void execute(ExecutionContext executionContext) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("execute(ExecutionContext executionContext)");
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

        executionContext.getExecutionRequest().getEngineExecutionListener().executionStarted(this);

        throwableCollector.execute(() -> setArgumentFields(executionContext));
        if (throwableCollector.isEmpty()) {
            throwableCollector.execute(() -> setRandomFields(executionContext));
            if (throwableCollector.isEmpty()) {
                throwableCollector.execute(() -> beforeAllMethods(executionContext));
                if (throwableCollector.isEmpty()) {
                    doExecute(executionContext);
                } else {
                    doSkip(executionContext);
                }
                throwableCollector.execute(() -> afterAllMethods(executionContext));
            }
            throwableCollector.execute(() -> clearRandomFields(executionContext));
        }
        throwableCollector.execute(() -> clearArgumentFields(executionContext));

        stopWatch.stop();

        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_DESCRIPTOR_ELAPSED_TIME,
                        stopWatch.elapsedNanoseconds());

        List<Throwable> throwables = collectThrowables();
        if (throwableCollector.isEmpty()) {
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
                    .executionFinished(this, TestExecutionResult.failed(throwables.get(0)));
        }
    }

    @Override
    public void skip(ExecutionContext executionContext) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("skip(ExecutionContext executionContext)");
        }

        stopWatch.reset();

        getMetadata().put(MetadataTestDescriptorConstants.TEST_CLASS, testClass);
        getMetadata()
                .put(MetadataTestDescriptorConstants.TEST_CLASS_DISPLAY_NAME, getDisplayName());
        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_DESCRIPTOR_STATUS,
                        MetadataTestDescriptorConstants.SKIP);

        getChildren()
                .forEach(
                        (Consumer<TestDescriptor>)
                                testDescriptor -> {
                                    if (testDescriptor instanceof ExecutableTestDescriptor) {
                                        ((ExecutableTestDescriptor) testDescriptor)
                                                .skip(executionContext);
                                    }
                                });
        stopWatch.stop();

        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_DESCRIPTOR_ELAPSED_TIME,
                        stopWatch.elapsedNanoseconds());

        executionContext
                .getExecutionRequest()
                .getEngineExecutionListener()
                .executionSkipped(this, format("Argument [%s] skipped", testArgument));
    }

    @Override
    public String toString() {
        return getClass().getName()
                + "{ "
                + "testClass ["
                + testClass.getName()
                + "]"
                + " beforeAllMethods ["
                + ObjectSupport.toString(beforeAllMethods)
                + "]"
                + " afterAllMethods ["
                + ObjectSupport.toString(afterAllMethods)
                + "]"
                + " }";
    }

    private void setArgumentFields(ExecutionContext executionContext) throws Throwable {
        Object testInstance = executionContext.get(ExecutionContextConstant.TEST_INSTANCE);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "setArgumentFields() testClass [%s] testInstance [%s] testArgument [%s]",
                    testInstance.getClass().getName(), testInstance, testArgument);
        }

        ArgumentAnnotationSupport.setArgumentFields(testInstance, testArgument);
    }

    private void setRandomFields(ExecutionContext executionContext) throws Throwable {
        Object testInstance = executionContext.get(ExecutionContextConstant.TEST_INSTANCE);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "setRandomFields() testClass [%s] testInstance [%s] testArgument [%s]",
                    testInstance.getClass().getName(), testInstance, testArgument);
        }

        RandomAnnotationSupport.setRandomFields(testInstance);
    }

    private void beforeAllMethods(ExecutionContext executionContext) throws Throwable {
        Object testInstance = executionContext.get(ExecutionContextConstant.TEST_INSTANCE);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "beforeAllMethods() testClass [%s] testInstance [%s]",
                    testInstance.getClass().getName(), testInstance);
        }

        ThrowableCollector localThrowableCollector = new ThrowableCollector();

        localThrowableCollector.execute(
                () ->
                        invocationInterceptor.beforeInvocationCallback(
                                TestEngine.BeforeAll.class, testInstance, null),
                () -> {
                    for (Method method : beforeAllMethods) {
                        MethodSupport.invoke(testInstance, method);
                    }
                },
                () ->
                        invocationInterceptor.afterInvocationCallback(
                                TestEngine.BeforeAll.class,
                                testInstance,
                                null,
                                localThrowableCollector.getFirst()));

        throwableCollector.getThrowables().addAll(localThrowableCollector.getThrowables());
    }

    private void doExecute(ExecutionContext executionContext) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("execute() testClass [%s]", testClass.getName());
        }

        getChildren()
                .forEach(
                        (Consumer<TestDescriptor>)
                                testDescriptor -> {
                                    if (testDescriptor instanceof TestMethodTestDescriptor) {
                                        ExecutableTestDescriptor executableTestDescriptor =
                                                (ExecutableTestDescriptor) testDescriptor;
                                        executableTestDescriptor.execute(executionContext);
                                    }
                                });
    }

    private void doSkip(ExecutionContext executionContext) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("skip() testClass [%s]", testClass.getName());
        }

        stopWatch.stop();

        getChildren()
                .forEach(
                        (Consumer<TestDescriptor>)
                                testDescriptor -> {
                                    if (testDescriptor instanceof TestMethodTestDescriptor) {
                                        ExecutableTestDescriptor executableTestDescriptor =
                                                (ExecutableTestDescriptor) testDescriptor;
                                        executableTestDescriptor.skip(executionContext);
                                    }
                                });

        stopWatch.reset();

        getMetadata().put(MetadataTestDescriptorConstants.TEST_CLASS, testClass);
        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_CLASS_DISPLAY_NAME,
                        DisplayNameSupport.getDisplayName(testClass));

        getMetadata().put(MetadataTestDescriptorConstants.TEST_ARGUMENT, testArgument);
    }

    private void afterAllMethods(ExecutionContext executionContext) throws Throwable {
        Object testInstance = executionContext.get(ExecutionContextConstant.TEST_INSTANCE);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "afterAllMethods() testClass [%s] testInstance [%s]",
                    testInstance.getClass().getName(), testInstance);
        }

        ThrowableCollector localThrowableCollector = new ThrowableCollector();

        localThrowableCollector.execute(
                () ->
                        invocationInterceptor.beforeInvocationCallback(
                                TestEngine.AfterAll.class, testInstance, null),
                () -> {
                    for (Method method : afterAllMethods) {
                        MethodSupport.invoke(testInstance, method);
                    }
                },
                () ->
                        invocationInterceptor.afterInvocationCallback(
                                TestEngine.AfterAll.class,
                                testInstance,
                                null,
                                localThrowableCollector.getFirst()));

        throwableCollector.getThrowables().addAll(localThrowableCollector.getThrowables());
    }

    private void clearRandomFields(ExecutionContext executionContext) throws Throwable {
        Object testInstance = executionContext.get(ExecutionContextConstant.TEST_INSTANCE);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "clearRandomFields() testClass [%s] testInstance [%s]",
                    testInstance.getClass().getName(), testInstance);
        }

        RandomAnnotationSupport.clearRandomFields(testInstance);
    }

    private void clearArgumentFields(ExecutionContext executionContext) throws Throwable {
        Object testInstance = executionContext.get(ExecutionContextConstant.TEST_INSTANCE);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "clearArgumentFields() testClass [%s] testInstance [%s]",
                    testInstance.getClass().getName(), testInstance);
        }

        ArgumentAnnotationSupport.setArgumentFields(testInstance, null);
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
            int testArgumentIndex,
            InvocationInterceptor invocationInterceptor) {
        Preconditions.notNull(parentUniqueId, "parentUniqueId is null");
        Preconditions.notNull(testClass, "testClass is null");
        Preconditions.notNull(testArgument, "testArgument is null");

        UniqueId uniqueId =
                parentUniqueId.append(
                        ArgumentTestDescriptor.class.getName(),
                        testArgumentIndex + "/" + testArgument.getName());

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("uniqueId [%s]", uniqueId);
        }

        String displayName = testArgument.getName();

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("displayName [%s]", displayName);
        }

        List<Method> beforeAllMethods =
                MethodSupport.findMethods(
                        testClass, Predicates.BEFORE_ALL_METHOD, HierarchyTraversalMode.TOP_DOWN);

        if (LOGGER.isTraceEnabled() && !beforeAllMethods.isEmpty()) {
            beforeAllMethods.forEach(method -> LOGGER.trace("beforeAll method [%s]", method));
        }

        beforeAllMethods =
                OrdererSupport.orderTestMethods(beforeAllMethods, HierarchyTraversalMode.TOP_DOWN);

        List<Method> afterAllMethods =
                MethodSupport.findMethods(
                        testClass, Predicates.AFTER_ALL_METHOD, HierarchyTraversalMode.BOTTOM_UP);

        if (LOGGER.isTraceEnabled() && !afterAllMethods.isEmpty()) {
            afterAllMethods.forEach(method -> LOGGER.trace("afterAll method [%s]", method));
        }

        afterAllMethods =
                OrdererSupport.orderTestMethods(afterAllMethods, HierarchyTraversalMode.BOTTOM_UP);

        return new ArgumentTestDescriptor(
                uniqueId,
                displayName,
                testClass,
                beforeAllMethods,
                afterAllMethods,
                testArgument,
                invocationInterceptor);
    }
}
