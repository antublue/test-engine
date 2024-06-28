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
import org.antublue.test.engine.internal.execution.ExecutionContext;
import org.antublue.test.engine.internal.execution.ExecutionContextConstant;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.support.DisplayNameSupport;
import org.antublue.test.engine.internal.support.MethodSupport;
import org.antublue.test.engine.internal.support.ObjectSupport;
import org.antublue.test.engine.internal.support.OrdererSupport;
import org.antublue.test.engine.internal.support.RandomAnnotationSupport;
import org.antublue.test.engine.internal.util.Predicates;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.util.Preconditions;
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
    public void execute(ExecutionContext executionContext) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("skip(ExecutionContext executionContext) %s", toString());
        }

        stopWatch.reset();

        getMetadata().put(MetadataTestDescriptorConstants.TEST_CLASS, testClass);
        getMetadata()
                .put(MetadataTestDescriptorConstants.TEST_CLASS_DISPLAY_NAME, getDisplayName());

        executionContext.getExecutionRequest().getEngineExecutionListener().executionStarted(this);

        throwableCollector.execute(() -> setRandomFields());
        if (throwableCollector.isEmpty()) {
            throwableCollector.execute(() -> createTestInstance(executionContext));
            if (throwableCollector.isEmpty()) {
                throwableCollector.execute(() -> prepare(executionContext));
                if (throwableCollector.isEmpty()) {
                    doExecute(executionContext);
                } else {
                    doSkip(executionContext);
                }
                throwableCollector.execute(() -> conclude(executionContext));
            }
            throwableCollector.execute(() -> destroyTestInstance(executionContext));
        }
        throwableCollector.execute(() -> clearRandomFields());

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
                    .executionFinished(this, TestExecutionResult.failed(throwables.get(0)));
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
                .executionSkipped(this, "Skipped");
    }

    @Override
    public String toString() {
        return getClass().getName()
                + "{ "
                + "testClass ["
                + testClass.getName()
                + "]"
                + " prepareMethods ["
                + ObjectSupport.toString(prepareMethods)
                + "]"
                + " concludeMethods ["
                + ObjectSupport.toString(concludeMethods)
                + "]"
                + " }";
    }

    private void setRandomFields() throws Throwable {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("setRandomFields() testClass [%s]", testClass.getName());
        }

        RandomAnnotationSupport.setRandomFields(testClass);
    }

    private void prepare(ExecutionContext executionContext) throws Throwable {
        Object testInstance = executionContext.get(ExecutionContextConstant.TEST_INSTANCE);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "prepare() testClass [%s] testInstance [%s]",
                    testClass.getName(), testInstance);
        }

        for (Method method : prepareMethods) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(
                        "prepare() testClass [%s] testInstance [%s] method [%s]",
                        testClass.getName(), testInstance, method);
            }

            method.invoke(testInstance);
        }
    }

    private void createTestInstance(ExecutionContext executionContext) throws Throwable {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("createTestInstance() testClass [%s]", testClass.getName());
        }

        Object testInstance =
                testClass.getDeclaredConstructor((Class<?>[]) null).newInstance((Object[]) null);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "createTestInstance() testClass [%s] testInstance [%s]",
                    testClass.getName(), testInstance);
        }

        executionContext.put(ExecutionContextConstant.TEST_INSTANCE, testInstance);
    }

    private void doExecute(ExecutionContext executionContext) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("doExecute() testClass [%s]", testClass.getName());
        }

        getChildren()
                .forEach(
                        (Consumer<TestDescriptor>)
                                testDescriptor -> {
                                    if (testDescriptor instanceof ArgumentTestDescriptor) {
                                        ExecutableTestDescriptor executableTestDescriptor =
                                                (ExecutableTestDescriptor) testDescriptor;
                                        executableTestDescriptor.execute(executionContext);
                                    }
                                });
    }

    private void doSkip(ExecutionContext executionContext) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("doSkip() testClass [%s]", testClass.getName());
        }

        getMetadata().put(MetadataTestDescriptorConstants.TEST_CLASS, testClass);
        getMetadata()
                .put(MetadataTestDescriptorConstants.TEST_CLASS_DISPLAY_NAME, getDisplayName());

        executionContext
                .getExecutionRequest()
                .getEngineExecutionListener()
                .executionSkipped(this, "Skipped");

        getChildren()
                .forEach(
                        (Consumer<TestDescriptor>)
                                testDescriptor -> {
                                    if (testDescriptor instanceof ArgumentTestDescriptor) {
                                        ExecutableTestDescriptor executableTestDescriptor =
                                                (ExecutableTestDescriptor) testDescriptor;
                                        executableTestDescriptor.skip(executionContext);
                                    }
                                });
    }

    private void clearRandomFields() throws Throwable {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("clearRandomFields() testClass [%s]", testClass.getName());
        }

        RandomAnnotationSupport.clearRandomFields(testClass);
    }

    private void conclude(ExecutionContext executionContext) throws Throwable {
        Object testInstance = executionContext.get(ExecutionContextConstant.TEST_INSTANCE);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "conclude() testClass [%s] testInstance [%s]",
                    testClass.getName(), testInstance);
        }

        for (Method method : concludeMethods) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(
                        "conclude() testClass [%s] testInstance [%s] method [%s]",
                        testClass.getName(), testInstance, method);
            }

            method.invoke(testInstance);
        }
    }

    private void destroyTestInstance(ExecutionContext executionContext) {
        Object testInstance = executionContext.remove(ExecutionContextConstant.TEST_INSTANCE);
        LOGGER.trace("destroyTestInstance() testClass [%s]", testClass.getName(), testInstance);
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

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("uniqueId [%s]", uniqueId);
        }

        String displayName = DisplayNameSupport.getDisplayName(testClass);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("displayName [%s]", displayName);
        }

        List<Method> prepareMethods =
                MethodSupport.findMethods(
                        testClass, Predicates.PREPARE_METHOD, HierarchyTraversalMode.TOP_DOWN);

        prepareMethods =
                OrdererSupport.orderTestMethods(prepareMethods, HierarchyTraversalMode.TOP_DOWN);

        if (LOGGER.isTraceEnabled() && !prepareMethods.isEmpty()) {
            prepareMethods.forEach(method -> LOGGER.trace("prepare method [%s]", method));
        }

        List<Method> concludeMethods =
                MethodSupport.findMethods(
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
