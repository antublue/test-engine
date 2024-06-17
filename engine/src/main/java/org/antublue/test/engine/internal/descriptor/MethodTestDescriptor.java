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
import org.antublue.test.engine.internal.MetadataConstants;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.reflection.OrdererUtils;
import org.antublue.test.engine.internal.util.DisplayNameUtils;
import org.antublue.test.engine.internal.util.Predicates;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;

/** Class to implement a MethodTestDescriptor */
public class MethodTestDescriptor extends ExecutableTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodTestDescriptor.class);

    private final Class<?> testClass;
    private final List<Method> beforeEachMethods;
    private final Method testMethod;
    private final List<Method> afterEachMethods;

    public MethodTestDescriptor(
            UniqueId uniqueId,
            String displayName,
            Class<?> testClass,
            List<Method> beforeEachMethods,
            Method testMethod,
            List<Method> afterEachMethods) {
        super(uniqueId, displayName);
        this.testClass = testClass;
        this.beforeEachMethods = beforeEachMethods;
        this.testMethod = testMethod;
        this.afterEachMethods = afterEachMethods;
    }

    @Override
    public Type getType() {
        return Type.TEST;
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(MethodSource.from(testMethod));
    }

    public Method getTestMethod() {
        return testMethod;
    }

    public String getTag() {
        return getTag(testMethod);
    }

    @Override
    public void execute(ExecutionRequest executionRequest) {
        LOGGER.trace("execute(ExecutionRequest executionRequest)");

        getStopWatch().reset();

        executionRequest.getEngineExecutionListener().executionStarted(this);

        setExecutionRequest(executionRequest);

        Object testInstance = getParent(ExecutableTestDescriptor.class).getTestInstance();
        Preconditions.notNull(testInstance, "testInstance is null");
        setTestInstance(testInstance);

        getMetadata().put(MetadataConstants.TEST_CLASS, testClass);
        getMetadata()
                .put(
                        MetadataConstants.TEST_CLASS_DISPLAY_NAME,
                        DisplayNameUtils.getDisplayName(testClass));
        getMetadata().put(MetadataConstants.TEST_METHOD, testMethod);
        getMetadata().put(MetadataConstants.TEST_METHOD_DISPLAY_NAME, getDisplayName());

        ThrowableCollector throwableCollector = getThrowableCollector();

        throwableCollector.execute(this::beforeEach);
        if (getThrowableCollector().isEmpty()) {
            throwableCollector.execute(this::test);
        }
        throwableCollector.execute(this::afterEach);

        setExecutionRequest(null);
        setTestInstance(null);

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
            getMetadata().put(MetadataConstants.TEST_DESCRIPTOR_STATUS, MetadataConstants.FAIL);

            /*
            getParent(ArgumentTestDescriptor.class)
                    .getThrowableCollector()
                    .add(
                            new TestArgumentFailedException(
                                    format(
                                            "Exception testing test argument name [%s]",
                                            testArgument.getName())));
             */

            executionRequest
                    .getEngineExecutionListener()
                    .executionFinished(
                            this,
                            TestExecutionResult.failed(getThrowableCollector().getThrowable()));
        }
    }

    private void beforeEach() throws Throwable {
        LOGGER.trace(
                "beforeEach() testClass [%s] testInstance [%s]",
                getTestInstance().getClass().getName(), getTestInstance());

        for (Method method : beforeEachMethods) {
            LOGGER.trace(
                    "beforeEach() testClass [%s] testInstance [%s] method [%s]",
                    getTestInstance().getClass().getName(), getTestInstance(), method);
            method.invoke(getTestInstance());
        }
    }

    private void test() throws Throwable {
        LOGGER.trace(
                "test() testClass [%s] testInstance [%s] method [%s]",
                getTestInstance().getClass().getName(), getTestInstance(), getTestMethod());
        getTestMethod().invoke(getTestInstance());
    }

    private void afterEach() throws Throwable {
        LOGGER.trace(
                "afterEach() testClass [%s] testInstance [%s]",
                getTestInstance().getClass().getName(), getTestInstance());

        for (Method method : afterEachMethods) {
            LOGGER.trace(
                    "afterEach() testClass [%s] testInstance [%s] method [%s]",
                    getTestInstance().getClass().getName(), getTestInstance(), method);
            method.invoke(getTestInstance());
        }
    }

    /**
     * Method to create a MethodTestDescriptor
     *
     * @param parentUniqueId parentUniqueId
     * @param testClass testClass
     * @param testMethod testMethod
     * @return a MethodTestDescriptor
     */
    public static MethodTestDescriptor of(
            UniqueId parentUniqueId, Class<?> testClass, Method testMethod) {
        UniqueId uniqueId =
                parentUniqueId.append(MethodTestDescriptor.class.getName(), testMethod.getName());

        String displayName = DisplayNameUtils.getDisplayName(testMethod);

        List<Method> beforeEachMethods =
                ReflectionSupport.findMethods(
                        testClass, Predicates.BEFORE_EACH_METHOD, HierarchyTraversalMode.TOP_DOWN);

        beforeEachMethods =
                OrdererUtils.orderTestMethods(beforeEachMethods, HierarchyTraversalMode.TOP_DOWN);

        List<Method> afterEachMethods =
                ReflectionSupport.findMethods(
                        testClass, Predicates.AFTER_EACH_METHOD, HierarchyTraversalMode.BOTTOM_UP);

        afterEachMethods =
                OrdererUtils.orderTestMethods(afterEachMethods, HierarchyTraversalMode.BOTTOM_UP);

        return new MethodTestDescriptor(
                uniqueId, displayName, testClass, beforeEachMethods, testMethod, afterEachMethods);
    }
}
