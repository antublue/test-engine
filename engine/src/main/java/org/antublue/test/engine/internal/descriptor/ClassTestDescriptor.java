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
import org.antublue.test.engine.internal.annotation.RandomAnnotationUtils;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.reflection.OrdererUtils;
import org.antublue.test.engine.internal.util.DisplayNameUtils;
import org.antublue.test.engine.internal.util.Predicates;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;

/** Class to implement a ClassTestDescriptor */
@SuppressWarnings("PMD.UnusedPrivateMethod")
public class ClassTestDescriptor extends ExecutableTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassTestDescriptor.class);

    private final Class<?> testClass;
    private final List<Method> prepareMethods;
    private final List<Method> concludeMethods;

    private ClassTestDescriptor(
            UniqueId uniqueId,
            String displayName,
            Class<?> testClass,
            List<Method> prepareMethods,
            List<Method> concludeMethods) {
        super(uniqueId, displayName);
        this.testClass = testClass;
        this.prepareMethods = prepareMethods;
        this.concludeMethods = concludeMethods;
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(ClassSource.from(testClass));
    }

    @Override
    public Type getType() {
        return Type.CONTAINER_AND_TEST;
    }

    public Class<?> getTestClass() {
        return testClass;
    }

    public String getTag() {
        return getTag(testClass);
    }

    @Override
    public void execute(ExecutionRequest executionRequest) {
        LOGGER.trace("execute(ExecutionRequest executionRequest)");

        getStopWatch().reset();

        getMetadata().put(MetadataConstants.TEST_CLASS, testClass);
        getMetadata().put(MetadataConstants.TEST_CLASS_DISPLAY_NAME, getDisplayName());

        setExecutionRequest(executionRequest);

        executionRequest.getEngineExecutionListener().executionStarted(this);

        ThrowableCollector throwableCollector = getThrowableCollector();

        throwableCollector.execute(this::setRandomFields);
        if (throwableCollector.isEmpty()) {
            throwableCollector.execute(this::createTestInstance);
            if (throwableCollector.isEmpty()) {
                throwableCollector.execute(this::prepare);
                if (throwableCollector.isEmpty()) {
                    execute();
                } else {
                    skip(executionRequest);
                }
                throwableCollector.execute(this::conclude);
            }
            throwableCollector.execute(this::destroyTestInstance);
        }
        throwableCollector.execute(this::clearRandomFields);

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

            executionRequest
                    .getEngineExecutionListener()
                    .executionFinished(
                            this,
                            TestExecutionResult.failed(getThrowableCollector().getThrowable()));
        }
    }

    private void setRandomFields() throws Throwable {
        LOGGER.trace("setRandomFields() testClass [%s]", getTestClass().getName());

        RandomAnnotationUtils.injectRandomFields(getTestClass());
    }

    private void prepare() throws Throwable {
        LOGGER.trace(
                "prepare() testClass [%s] testInstance [%s]",
                getTestClass().getName(), getTestInstance());

        for (Method method : prepareMethods) {
            LOGGER.trace(
                    "prepare() testClass [%s] testInstance [%s] method [%s]",
                    getTestClass().getName(), getTestInstance(), method);
            method.invoke(getTestInstance());
        }
    }

    private void createTestInstance() throws Throwable {
        LOGGER.trace("createTestInstance() testClass [%s]", getTestClass().getName());

        setTestInstance(
                getTestClass()
                        .getDeclaredConstructor((Class<?>[]) null)
                        .newInstance((Object[]) null));

        LOGGER.trace(
                "createTestInstance() testClass [%s] testInstance [%s]",
                getTestClass().getName(), getTestInstance());
    }

    private void execute() {
        LOGGER.trace("execute() testClass [%s]", getTestClass().getName());

        getChildren()
                .forEach(
                        testDescriptor -> {
                            if (testDescriptor instanceof ExecutableTestDescriptor) {
                                ((ExecutableTestDescriptor) testDescriptor)
                                        .execute(getExecutionRequest());
                            }
                        });
    }

    private void clearRandomFields() throws Throwable {
        LOGGER.trace("clearRandomFields() testClass [%s]", getTestClass().getName());

        RandomAnnotationUtils.clearRandomFields(getTestClass());
    }

    private void conclude() throws Throwable {
        LOGGER.trace(
                "conclude() testClass [%s] testInstance [%s]",
                getTestClass().getName(), getTestInstance());

        for (Method method : concludeMethods) {
            LOGGER.trace(
                    "conclude() testClass [%s] testInstance [%s] method [%s]",
                    getTestClass().getName(), getTestInstance(), method);
            method.invoke(getTestInstance());
        }
    }

    private void destroyTestInstance() {
        LOGGER.trace(
                "destroyTestInstance() testClass [%s]",
                getTestClass().getName(), getTestInstance());

        setTestInstance(null);
    }

    /**
     * Method to create a ClassTestDescriptor
     *
     * @param parentUniqueId parentUniqueId
     * @param testClass testClass
     * @return a ClassTestDescriptor
     */
    public static ClassTestDescriptor of(UniqueId parentUniqueId, Class<?> testClass) {
        UniqueId uniqueId =
                parentUniqueId.append(ClassTestDescriptor.class.getName(), testClass.getName());

        LOGGER.trace("uniqueId [%s]", uniqueId);

        String displayName = DisplayNameUtils.getDisplayName(testClass);

        LOGGER.trace("displayName [%s]", displayName);

        List<Method> prepareMethods =
                ReflectionSupport.findMethods(
                        testClass, Predicates.PREPARE_METHOD, HierarchyTraversalMode.TOP_DOWN);

        prepareMethods =
                OrdererUtils.orderTestMethods(prepareMethods, HierarchyTraversalMode.TOP_DOWN);

        if (!prepareMethods.isEmpty() && LOGGER.isTraceEnabled()) {
            prepareMethods.forEach(method -> LOGGER.trace("prepare method [%s]", method));
        }

        List<Method> concludeMethods =
                ReflectionSupport.findMethods(
                        testClass, Predicates.CONCLUDE_METHOD, HierarchyTraversalMode.BOTTOM_UP);

        concludeMethods =
                OrdererUtils.orderTestMethods(concludeMethods, HierarchyTraversalMode.TOP_DOWN);

        if (!concludeMethods.isEmpty() && LOGGER.isTraceEnabled()) {
            concludeMethods.forEach(method -> LOGGER.trace("conclude method [%s]", method));
        }

        return new ClassTestDescriptor(
                uniqueId, displayName, testClass, prepareMethods, concludeMethods);
    }
}
