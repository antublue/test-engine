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

package org.antublue.test.engine.test.descriptor.parameterized;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.test.descriptor.ExecutableContext;
import org.antublue.test.engine.test.descriptor.ExecutableTestDescriptor;
import org.antublue.test.engine.test.descriptor.Metadata;
import org.antublue.test.engine.test.descriptor.MetadataConstants;
import org.antublue.test.engine.test.descriptor.MetadataSupport;
import org.antublue.test.engine.test.descriptor.util.AutoCloseProcessor;
import org.antublue.test.engine.test.descriptor.util.LockProcessor;
import org.antublue.test.engine.test.descriptor.util.MethodInvoker;
import org.antublue.test.engine.test.descriptor.util.TestDescriptorUtils;
import org.antublue.test.engine.util.Invariant;
import org.antublue.test.engine.util.Invocation;
import org.antublue.test.engine.util.ReflectionUtils;
import org.antublue.test.engine.util.StandardStreams;
import org.antublue.test.engine.util.StopWatch;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.MethodSource;

/** Class to implement a ParameterMethodTestDescriptor */
public class ParameterizedMethodTestDescriptor extends AbstractTestDescriptor
        implements ExecutableTestDescriptor, MetadataSupport {

    private static final ReflectionUtils REFLECTION_UTILS = ReflectionUtils.getSingleton();

    private static final TestDescriptorUtils TEST_DESCRIPTOR_UTILS =
            TestDescriptorUtils.getSingleton();

    private static final LockProcessor LOCK_PROCESSOR = LockProcessor.getSingleton();

    private final Class<?> testClass;
    private final Argument testArgument;
    private final Method testMethod;
    private final StopWatch stopWatch;
    private final Metadata metadata;

    /** Constructor */
    private ParameterizedMethodTestDescriptor(
            UniqueId parentUniqueId, Class<?> testClass, Argument testArgument, Method testMethod) {
        super(
                parentUniqueId.append(
                        ParameterizedMethodTestDescriptor.class.getSimpleName(),
                        testMethod.getName()),
                TEST_DESCRIPTOR_UTILS.getDisplayName(testMethod));
        this.testClass = testClass;
        this.testArgument = testArgument;
        this.testMethod = testMethod;
        this.stopWatch = new StopWatch();
        this.metadata = new Metadata();
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
    public Metadata getMetadata() {
        return metadata;
    }

    private enum State {
        RUN_BEFORE_EACH_METHODS,
        RUN_TEST_METHOD,
        RUN_AFTER_EACH_METHODS,
        RUN_AUTO_CLOSE_FIELDS
    }

    @Override
    public void execute(ExecutionRequest executionRequest, ExecutableContext executableContext) {
        stopWatch.start();

        Object testInstance = executableContext.get(ParameterizedExecutableConstants.TEST_INSTANCE);
        Invariant.check(testInstance != null);

        metadata.put(MetadataConstants.TEST_CLASS, testClass);
        metadata.put(MetadataConstants.TEST_ARGUMENT, testArgument);
        metadata.put(MetadataConstants.TEST_METHOD, testMethod);

        EngineExecutionListener engineExecutionListener =
                executionRequest.getEngineExecutionListener();

        if (executableContext.hasThrowables()) {
            stopWatch.stop();
            metadata.put(MetadataConstants.TEST_DESCRIPTOR_ELAPSED_TIME, stopWatch.elapsedTime());
            metadata.put(MetadataConstants.TEST_DESCRIPTOR_STATUS, MetadataConstants.SKIP);
            engineExecutionListener.executionSkipped(this, "");
            return;
        }

        engineExecutionListener.executionStarted(this);

        AtomicReference<State> state = new AtomicReference<>(State.RUN_BEFORE_EACH_METHODS);

        if (state.get() == State.RUN_BEFORE_EACH_METHODS) {
            Invocation.execute(
                    () -> {
                        try {
                            List<Method> beforeEachMethods =
                                    REFLECTION_UTILS.findMethods(
                                            testInstance.getClass(),
                                            ParameterizedFilters.BEFORE_EACH_METHOD);
                            TEST_DESCRIPTOR_UTILS.sortMethods(
                                    beforeEachMethods, TestDescriptorUtils.Sort.FORWARD);
                            for (Method method : beforeEachMethods) {
                                try {
                                    LOCK_PROCESSOR.processLocks(method);
                                    MethodInvoker.invoke(method, testInstance, testArgument);
                                } finally {
                                    LOCK_PROCESSOR.processUnlocks(method);
                                }
                            }
                            state.set(State.RUN_TEST_METHOD);
                        } catch (Throwable t) {
                            executableContext.addAndProcessThrowable(testClass, t);
                            state.set(State.RUN_AFTER_EACH_METHODS);
                        } finally {
                            StandardStreams.flush();
                        }
                    });
        }

        if (state.get() == State.RUN_TEST_METHOD) {
            try {
                LOCK_PROCESSOR.processLocks(testMethod);
                MethodInvoker.invoke(testMethod, testInstance, testArgument);
            } catch (Throwable t) {
                executableContext.addAndProcessThrowable(testClass, t);
            } finally {
                LOCK_PROCESSOR.processUnlocks(testMethod);
                state.set(State.RUN_AFTER_EACH_METHODS);
                StandardStreams.flush();
            }
        }

        if (state.get() == State.RUN_AFTER_EACH_METHODS) {
            Invocation.execute(
                    () -> {
                        List<Method> afterEachMethods =
                                REFLECTION_UTILS.findMethods(
                                        testInstance.getClass(),
                                        ParameterizedFilters.AFTER_EACH_METHOD);
                        TEST_DESCRIPTOR_UTILS.sortMethods(
                                afterEachMethods, TestDescriptorUtils.Sort.REVERSE);
                        for (Method method : afterEachMethods) {
                            try {
                                LOCK_PROCESSOR.processLocks(method);
                                MethodInvoker.invoke(method, testInstance, testArgument);
                            } catch (Throwable t) {
                                executableContext.addAndProcessThrowable(testClass, t);
                            } finally {
                                LOCK_PROCESSOR.processUnlocks(method);
                                StandardStreams.flush();
                            }
                        }
                        state.set(State.RUN_AUTO_CLOSE_FIELDS);
                    });
        }

        if (state.get() == State.RUN_AUTO_CLOSE_FIELDS) {
            Invocation.execute(
                    () -> {
                        List<Field> fields =
                                REFLECTION_UTILS.findFields(
                                        testClass, ParameterizedFilters.AUTO_CLOSE_FIELDS);
                        for (Field field : fields) {
                            TestEngine.AutoClose annotation =
                                    field.getAnnotation(TestEngine.AutoClose.class);
                            if ("@TestEngine.AfterEach".equals(annotation.lifecycle())) {
                                try {
                                    AutoCloseProcessor.close(testInstance, field);
                                } catch (Throwable t) {
                                    executableContext.addAndProcessThrowable(testClass, t);
                                } finally {
                                    StandardStreams.flush();
                                }
                            }
                        }
                    });
        }

        stopWatch.stop();
        metadata.put(MetadataConstants.TEST_DESCRIPTOR_ELAPSED_TIME, stopWatch.elapsedTime());

        if (executableContext.hasThrowables()) {
            metadata.put(MetadataConstants.TEST_DESCRIPTOR_STATUS, MetadataConstants.FAIL);
            engineExecutionListener.executionFinished(
                    this, TestExecutionResult.failed(executableContext.getThrowables().get(0)));
        } else {
            metadata.put(MetadataConstants.TEST_DESCRIPTOR_STATUS, MetadataConstants.PASS);
            engineExecutionListener.executionFinished(this, TestExecutionResult.successful());
        }

        StandardStreams.flush();
    }

    public static class Builder {

        private TestDescriptor parentTestDescriptor;
        private Class<?> testClass;
        private Argument testArgument;
        private Method testMethod;

        public Builder withParentTestDescriptor(TestDescriptor parentTestDescriptor) {
            this.parentTestDescriptor = parentTestDescriptor;
            return this;
        }

        public Builder withTestClass(Class<?> testClass) {
            this.testClass = testClass;
            return this;
        }

        public Builder withTestArgument(Argument testArgument) {
            this.testArgument = testArgument;
            return this;
        }

        public Builder withTestMethod(Method testMethod) {
            this.testMethod = testMethod;
            return this;
        }

        public TestDescriptor build() {
            try {
                UniqueId testDescriptorUniqueId =
                        parentTestDescriptor
                                .getUniqueId()
                                .append(
                                        ParameterizedMethodTestDescriptor.class.getName(),
                                        testArgument.name());
                TestDescriptor testDescriptor =
                        new ParameterizedMethodTestDescriptor(
                                testDescriptorUniqueId, testClass, testArgument, testMethod);

                parentTestDescriptor.addChild(testDescriptor);

                return testDescriptor;
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }
}
