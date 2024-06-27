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
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
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

/** Class to implement a ClassTestDescriptor */
@SuppressWarnings("PMD.UnusedPrivateMethod")
public class ClassTestDescriptor extends ExecutableTestDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassTestDescriptor.class);

    private final Class<?> testClass;
    private final List<Method> prepareMethods;
    private final List<Method> concludeMethods;
    private ExecutionRequest executionRequest;
    private Object testInstance;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     * @param testClass testClass
     * @param prepareMethods prepareMethods
     * @param concludeMethods concludeMethods
     */
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

    @Override
    public void execute(ExecutionRequest executionRequest, Object testInstance) {
        LOGGER.trace("execute(ExecutionRequest executionRequest)");

        stopWatch.reset();

        getMetadata().put(MetadataTestDescriptorConstants.TEST_CLASS, testClass);
        getMetadata()
                .put(MetadataTestDescriptorConstants.TEST_CLASS_DISPLAY_NAME, getDisplayName());

        this.executionRequest = executionRequest;

        executionRequest.getEngineExecutionListener().executionStarted(this);

        throwableCollector.execute(this::setRandomFields);
        if (throwableCollector.isEmpty()) {
            throwableCollector.execute(this::createTestInstance);
            if (throwableCollector.isEmpty()) {
                throwableCollector.execute(this::prepare);
                if (throwableCollector.isEmpty()) {
                    execute();
                } else {
                    skip();
                }
                throwableCollector.execute(this::conclude);
            }
            throwableCollector.execute(this::destroyTestInstance);
        }
        throwableCollector.execute(this::clearRandomFields);

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
            executionRequest
                    .getEngineExecutionListener()
                    .executionFinished(this, TestExecutionResult.successful());
        } else {
            getMetadata()
                    .put(
                            MetadataTestDescriptorConstants.TEST_DESCRIPTOR_STATUS,
                            MetadataTestDescriptorConstants.FAIL);
            executionRequest
                    .getEngineExecutionListener()
                    .executionFinished(this, TestExecutionResult.failed(throwables.get(0)));
        }
    }

    public void skip(ExecutionRequest executionRequest) {
        LOGGER.trace("skip(ExecutionRequest executionRequest)");

        stopWatch.reset();

        getMetadata().put(MetadataTestDescriptorConstants.TEST_CLASS, testClass);
        getMetadata()
                .put(MetadataTestDescriptorConstants.TEST_CLASS_DISPLAY_NAME, getDisplayName());
        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_DESCRIPTOR_ELAPSED_TIME,
                        stopWatch.elapsedNanoseconds());
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
                                                .skip(executionRequest);
                                    }
                                });

        stopWatch.stop();

        getMetadata()
                .put(
                        MetadataTestDescriptorConstants.TEST_DESCRIPTOR_ELAPSED_TIME,
                        stopWatch.elapsedNanoseconds());

        executionRequest.getEngineExecutionListener().executionSkipped(this, "Skipped");
    }

    private void setRandomFields() throws Throwable {
        LOGGER.trace("setRandomFields() testClass [%s]", testClass.getName());

        RandomAnnotationSupport.setRandomFields(testClass);
    }

    private void prepare() throws Throwable {
        LOGGER.trace(
                "prepare() testClass [%s] testInstance [%s]", testClass.getName(), testInstance);

        for (Method method : prepareMethods) {
            LOGGER.trace(
                    "prepare() testClass [%s] testInstance [%s] method [%s]",
                    testClass.getName(), testInstance, method);
            method.invoke(testInstance);
        }
    }

    private void createTestInstance() throws Throwable {
        LOGGER.trace("createTestInstance() testClass [%s]", testClass.getName());

        testInstance =
                testClass.getDeclaredConstructor((Class<?>[]) null).newInstance((Object[]) null);

        LOGGER.trace(
                "createTestInstance() testClass [%s] testInstance [%s]",
                testClass.getName(), testInstance);
    }

    private void execute() {
        LOGGER.trace("execute() testClass [%s]", testClass.getName());

        getChildren()
                .forEach(
                        (Consumer<TestDescriptor>)
                                testDescriptor -> {
                                    if (testDescriptor instanceof ArgumentTestDescriptor) {
                                        ExecutableTestDescriptor executableTestDescriptor =
                                                (ExecutableTestDescriptor) testDescriptor;
                                        executableTestDescriptor.execute(
                                                executionRequest, testInstance);
                                    }
                                });
    }

    private void skip() {
        LOGGER.trace("skip() testClass [%s]", testClass.getName());

        getMetadata().put(MetadataTestDescriptorConstants.TEST_CLASS, testClass);
        getMetadata()
                .put(MetadataTestDescriptorConstants.TEST_CLASS_DISPLAY_NAME, getDisplayName());

        executionRequest.getEngineExecutionListener().executionSkipped(this, "Skipped");

        getChildren()
                .forEach(
                        (Consumer<TestDescriptor>)
                                testDescriptor -> {
                                    if (testDescriptor instanceof ArgumentTestDescriptor) {
                                        ExecutableTestDescriptor executableTestDescriptor =
                                                (ExecutableTestDescriptor) testDescriptor;
                                        executableTestDescriptor.skip(executionRequest);
                                    }
                                });
    }

    private void clearRandomFields() throws Throwable {
        LOGGER.trace("clearRandomFields() testClass [%s]", testClass.getName());

        RandomAnnotationSupport.clearRandomFields(testClass);
    }

    private void conclude() throws Throwable {
        LOGGER.trace(
                "conclude() testClass [%s] testInstance [%s]", testClass.getName(), testInstance);

        for (Method method : concludeMethods) {
            LOGGER.trace(
                    "conclude() testClass [%s] testInstance [%s] method [%s]",
                    testClass.getName(), testInstance, method);
            method.invoke(testInstance);
        }
    }

    private void destroyTestInstance() {
        LOGGER.trace("destroyTestInstance() testClass [%s]", testClass.getName(), testInstance);

        testInstance = null;
    }

    /**
     * Method to create a ClassTestDescriptor
     *
     * @param parentUniqueId parentUniqueId
     * @param testClass testClass
     * @return a ClassTestDescriptor
     */
    public static ClassTestDescriptor create(UniqueId parentUniqueId, Class<?> testClass) {
        Preconditions.notNull(parentUniqueId, "parentUniqueId is null");
        Preconditions.notNull(testClass, "testClass is null");

        UniqueId uniqueId =
                parentUniqueId.append(ClassTestDescriptor.class.getName(), testClass.getName());

        LOGGER.trace("uniqueId [%s]", uniqueId);

        String displayName = DisplayNameSupport.getDisplayName(testClass);

        LOGGER.trace("displayName [%s]", displayName);

        List<Method> prepareMethods =
                ReflectionSupport.findMethods(
                        testClass, Predicates.PREPARE_METHOD, HierarchyTraversalMode.TOP_DOWN);

        prepareMethods =
                OrdererSupport.orderTestMethods(prepareMethods, HierarchyTraversalMode.TOP_DOWN);

        if (LOGGER.isTraceEnabled() && !prepareMethods.isEmpty()) {
            prepareMethods.forEach(method -> LOGGER.trace("prepare method [%s]", method));
        }

        List<Method> concludeMethods =
                ReflectionSupport.findMethods(
                        testClass, Predicates.CONCLUDE_METHOD, HierarchyTraversalMode.BOTTOM_UP);

        concludeMethods =
                OrdererSupport.orderTestMethods(concludeMethods, HierarchyTraversalMode.TOP_DOWN);

        if (LOGGER.isTraceEnabled() && !concludeMethods.isEmpty()) {
            concludeMethods.forEach(method -> LOGGER.trace("conclude method [%s]", method));
        }

        return new ClassTestDescriptor(
                uniqueId, displayName, testClass, prepareMethods, concludeMethods);
    }
}
