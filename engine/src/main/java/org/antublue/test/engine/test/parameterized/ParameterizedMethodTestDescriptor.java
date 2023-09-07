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

package org.antublue.test.engine.test.parameterized;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.test.ExecutableContext;
import org.antublue.test.engine.test.ExecutableMetadata;
import org.antublue.test.engine.test.ExecutableMetadataConstants;
import org.antublue.test.engine.test.ExecutableMetadataSupport;
import org.antublue.test.engine.test.ExecutableTestDescriptor;
import org.antublue.test.engine.test.ThrowableContext;
import org.antublue.test.engine.test.extension.ExtensionProcessor;
import org.antublue.test.engine.test.util.AutoCloseProcessor;
import org.antublue.test.engine.test.util.LockProcessor;
import org.antublue.test.engine.test.util.MethodInvoker;
import org.antublue.test.engine.test.util.TestUtils;
import org.antublue.test.engine.util.Invariant;
import org.antublue.test.engine.util.ReflectionUtils;
import org.antublue.test.engine.util.Singleton;
import org.antublue.test.engine.util.StandardStreams;
import org.antublue.test.engine.util.StopWatch;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.MethodSource;

/** Class to implement a ParameterMethodTestDescriptor */
public class ParameterizedMethodTestDescriptor extends AbstractTestDescriptor
        implements ExecutableTestDescriptor, ExecutableMetadataSupport {

    private static final ReflectionUtils REFLECTION_UTILS = Singleton.get(ReflectionUtils.class);

    private static final TestUtils TEST_DESCRIPTOR_UTILS = Singleton.get(TestUtils.class);

    private static final ExtensionProcessor EXTENSION_PROCESSOR =
            Singleton.get(ExtensionProcessor.class);

    private static final LockProcessor LOCK_PROCESSOR = Singleton.get(LockProcessor.class);

    private final Class<?> testClass;
    private final Argument testArgument;
    private final Method testMethod;
    private final StopWatch stopWatch;
    private final ExecutableMetadata executableMetadata;

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
        this.executableMetadata = new ExecutableMetadata();
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
    public ExecutableMetadata getExecutableMetadata() {
        return executableMetadata;
    }

    private enum State {
        BEFORE_EACH_METHODS,
        BEFORE_EACH_CALLBACK_METHODS,
        BEFORE_TEST_CALLBACK_METHODS,
        TEST_METHOD,
        AFTER_TEST_CALLBACK_METHODS,
        AFTER_EACH_METHODS,
        CLOSE_AUTO_CLOSE_FIELDS,
        AFTER_EACH_CALLBACK_METHODS
    }

    @Override
    public void execute(ExecutableContext executableContext) {
        stopWatch.start();

        ThrowableContext throwableContext = executableContext.getThrowableContext();

        Object testInstance = executableContext.getTestInstance();
        Invariant.check(testInstance != null);

        executableMetadata.put(ExecutableMetadataConstants.TEST_CLASS, testClass);
        executableMetadata.put(ExecutableMetadataConstants.TEST_ARGUMENT, testArgument);
        executableMetadata.put(ExecutableMetadataConstants.TEST_METHOD, testMethod);

        if (!executableContext.getThrowableContext().isEmpty()) {
            stopWatch.stop();
            executableMetadata.put(
                    ExecutableMetadataConstants.TEST_DESCRIPTOR_ELAPSED_TIME,
                    stopWatch.elapsedTime());
            executableMetadata.put(
                    ExecutableMetadataConstants.TEST_DESCRIPTOR_STATUS,
                    ExecutableMetadataConstants.SKIP);
            executableContext
                    .getExecutionRequest()
                    .getEngineExecutionListener()
                    .executionSkipped(this, "");
            return;
        }

        executableContext.getExecutionRequest().getEngineExecutionListener().executionStarted(this);

        AtomicReference<State> state = new AtomicReference<>(State.BEFORE_EACH_METHODS);

        if (state.get() == State.BEFORE_EACH_METHODS) {
            try {
                List<Method> beforeEachMethods =
                        REFLECTION_UTILS.findMethods(
                                testInstance.getClass(),
                                ParameterizedTestFilters.BEFORE_EACH_METHOD);
                TEST_DESCRIPTOR_UTILS.sortMethods(beforeEachMethods, TestUtils.Sort.FORWARD);
                for (Method method : beforeEachMethods) {
                    try {
                        LOCK_PROCESSOR.processLocks(method);
                        MethodInvoker.invoke(method, testInstance, testArgument);
                    } finally {
                        LOCK_PROCESSOR.processUnlocks(method);
                    }
                }
            } catch (Throwable t) {
                throwableContext.add(testClass, t);
            } finally {
                state.set(State.BEFORE_EACH_CALLBACK_METHODS);
                StandardStreams.flush();
            }
        }

        if (state.get() == State.BEFORE_EACH_CALLBACK_METHODS) {
            EXTENSION_PROCESSOR.beforeEachCallbackMethods(
                    testClass, testArgument, testInstance, executableContext.getThrowableContext());
            if (executableContext.getThrowableContext().isEmpty()) {
                state.set(State.BEFORE_TEST_CALLBACK_METHODS);
            } else {
                state.set(State.AFTER_EACH_METHODS);
            }
        }

        if (state.get() == State.BEFORE_TEST_CALLBACK_METHODS) {
            EXTENSION_PROCESSOR.beforeTestCallbacks(
                    testClass,
                    testArgument,
                    testMethod,
                    testInstance,
                    executableContext.getThrowableContext());
            if (executableContext.getThrowableContext().isEmpty()) {
                state.set(State.TEST_METHOD);
            } else {
                state.set(State.AFTER_TEST_CALLBACK_METHODS);
            }
        }

        if (state.get() == State.TEST_METHOD) {
            try {
                LOCK_PROCESSOR.processLocks(testMethod);
                MethodInvoker.invoke(testMethod, testInstance, testArgument);
            } catch (Throwable t) {
                throwableContext.add(testClass, t);
            } finally {
                LOCK_PROCESSOR.processUnlocks(testMethod);
                state.set(State.AFTER_TEST_CALLBACK_METHODS);
                StandardStreams.flush();
            }
        }

        if (state.get() == State.AFTER_TEST_CALLBACK_METHODS) {
            EXTENSION_PROCESSOR.afterTestCallbacks(
                    testClass,
                    testArgument,
                    testMethod,
                    testInstance,
                    executableContext.getThrowableContext());
            state.set(State.AFTER_EACH_METHODS);
        }

        if (state.get() == State.AFTER_EACH_METHODS) {
            List<Method> afterEachMethods =
                    REFLECTION_UTILS.findMethods(
                            testInstance.getClass(), ParameterizedTestFilters.AFTER_EACH_METHOD);
            TEST_DESCRIPTOR_UTILS.sortMethods(afterEachMethods, TestUtils.Sort.REVERSE);
            for (Method method : afterEachMethods) {
                try {
                    LOCK_PROCESSOR.processLocks(method);
                    MethodInvoker.invoke(method, testInstance, testArgument);
                } catch (Throwable t) {
                    throwableContext.add(testClass, t);
                } finally {
                    LOCK_PROCESSOR.processUnlocks(method);
                    StandardStreams.flush();
                }
            }
            state.set(State.AFTER_EACH_CALLBACK_METHODS);
        }

        if (state.get() == State.AFTER_EACH_CALLBACK_METHODS) {
            EXTENSION_PROCESSOR.afterEachCallbacks(
                    testClass, testArgument, testInstance, executableContext.getThrowableContext());

            state.set(State.CLOSE_AUTO_CLOSE_FIELDS);
        }

        AutoCloseProcessor autoCloseProcessor = Singleton.get(AutoCloseProcessor.class);

        if (state.get() == State.CLOSE_AUTO_CLOSE_FIELDS) {
            List<Field> testFields =
                    REFLECTION_UTILS.findFields(
                            testClass, ParameterizedTestFilters.AUTO_CLOSE_FIELDS);
            for (Field testField : testFields) {
                if (testField.isAnnotationPresent(TestEngine.AutoClose.AfterEach.class)) {
                    autoCloseProcessor.close(testInstance, testField, throwableContext);
                    StandardStreams.flush();
                }
            }
            state.set(State.AFTER_EACH_CALLBACK_METHODS);
        }

        stopWatch.stop();
        executableMetadata.put(
                ExecutableMetadataConstants.TEST_DESCRIPTOR_ELAPSED_TIME, stopWatch.elapsedTime());

        if (executableContext.getThrowableContext().isEmpty()) {
            executableMetadata.put(
                    ExecutableMetadataConstants.TEST_DESCRIPTOR_STATUS,
                    ExecutableMetadataConstants.PASS);
            executableContext
                    .getExecutionRequest()
                    .getEngineExecutionListener()
                    .executionFinished(this, TestExecutionResult.successful());
        } else {
            executableMetadata.put(
                    ExecutableMetadataConstants.TEST_DESCRIPTOR_STATUS,
                    ExecutableMetadataConstants.FAIL);
            executableContext
                    .getExecutionRequest()
                    .getEngineExecutionListener()
                    .executionFinished(
                            this,
                            TestExecutionResult.failed(
                                    executableContext
                                            .getThrowableContext()
                                            .getThrowables()
                                            .get(0)));
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
