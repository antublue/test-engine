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
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.exception.TestArgumentFailedException;
import org.antublue.test.engine.test.ExecutableMetadata;
import org.antublue.test.engine.test.ExecutableMetadataConstants;
import org.antublue.test.engine.test.ExecutableTestDescriptor;
import org.antublue.test.engine.test.ThrowableContext;
import org.antublue.test.engine.test.util.AutoCloseProcessor;
import org.antublue.test.engine.test.util.TestUtils;
import org.antublue.test.engine.util.Invariant;
import org.antublue.test.engine.util.StandardStreams;
import org.antublue.test.engine.util.StopWatch;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;

/** Class to implement a ParameterMethodTestDescriptor */
public class ParameterizedMethodTestDescriptor extends ExecutableTestDescriptor {

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
                TEST_UTILS.getDisplayName(testMethod));
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
        BEGIN,
        BEFORE_EACH,
        POST_BEFORE_EACH,
        PRE_TEST,
        TEST,
        POST_TEST,
        AFTER_EACH,
        POST_AFTER_EACH,
        CLOSE_AUTO_CLOSE_FIELDS,
        END
    }

    @Override
    public void execute(ExecutionRequest executionRequest) {
        stopWatch.start();

        Object testInstance = getParent(ExecutableTestDescriptor.class).getTestInstance();
        Invariant.check(testInstance != null);
        setTestInstance(testInstance);

        getExecutableMetadata().put(ExecutableMetadataConstants.TEST_CLASS, testClass);
        getExecutableMetadata().put(ExecutableMetadataConstants.TEST_ARGUMENT, testArgument);
        getExecutableMetadata().put(ExecutableMetadataConstants.TEST_METHOD, testMethod);

        if (!getThrowableContext().isEmpty()) {
            stopWatch.stop();
            getExecutableMetadata()
                    .put(
                            ExecutableMetadataConstants.TEST_DESCRIPTOR_ELAPSED_TIME,
                            stopWatch.elapsedTime());
            getExecutableMetadata()
                    .put(
                            ExecutableMetadataConstants.TEST_DESCRIPTOR_STATUS,
                            ExecutableMetadataConstants.SKIP);
            executionRequest.getEngineExecutionListener().executionSkipped(this, "");
            return;
        }

        executionRequest.getEngineExecutionListener().executionStarted(this);

        State state = State.BEGIN;
        while (state != null && state != State.END) {
            switch (state) {
                case BEGIN:
                    {
                        state = State.BEFORE_EACH;
                        break;
                    }
                case BEFORE_EACH:
                    {
                        state = beforeEach();
                        break;
                    }
                case POST_BEFORE_EACH:
                    {
                        state = postBeforeEach();
                        break;
                    }
                case PRE_TEST:
                    {
                        state = preTest();
                        break;
                    }
                case TEST:
                    {
                        state = test();
                        break;
                    }
                case POST_TEST:
                    {
                        state = postTest();
                        break;
                    }
                case AFTER_EACH:
                    {
                        state = afterEach();
                        break;
                    }
                case POST_AFTER_EACH:
                    {
                        state = postAfterEach();
                        break;
                    }
                case CLOSE_AUTO_CLOSE_FIELDS:
                    {
                        state = closeAutoCloseFields();
                        break;
                    }
                default:
                    {
                        state = null;
                    }
            }
        }

        stopWatch.stop();
        getExecutableMetadata()
                .put(
                        ExecutableMetadataConstants.TEST_DESCRIPTOR_ELAPSED_TIME,
                        stopWatch.elapsedTime());

        if (getThrowableContext().isEmpty()) {
            getExecutableMetadata()
                    .put(
                            ExecutableMetadataConstants.TEST_DESCRIPTOR_STATUS,
                            ExecutableMetadataConstants.PASS);
            executionRequest
                    .getEngineExecutionListener()
                    .executionFinished(this, TestExecutionResult.successful());
        } else {
            getParent(ParameterizedArgumentTestDescriptor.class)
                    .getThrowableContext()
                    .add(
                            getTestInstance().getClass(),
                            new TestArgumentFailedException(
                                    String.format(
                                            "Exception testing test argument name [%s]",
                                            testArgument.name())));
            getExecutableMetadata()
                    .put(
                            ExecutableMetadataConstants.TEST_DESCRIPTOR_STATUS,
                            ExecutableMetadataConstants.FAIL);
            executionRequest
                    .getEngineExecutionListener()
                    .executionFinished(
                            this,
                            TestExecutionResult.failed(
                                    getThrowableContext().getThrowables().get(0)));
        }
        StandardStreams.flush();
    }

    private State beforeEach() {
        Object testInstance = getTestInstance();
        Invariant.check(testInstance != null);
        ThrowableContext throwableContext = getThrowableContext();
        try {
            List<Method> beforeEachMethods =
                    REFLECTION_UTILS.findMethods(
                            testInstance.getClass(), ParameterizedTestFilters.BEFORE_EACH_METHOD);
            TEST_UTILS.sortMethods(beforeEachMethods, TestUtils.Sort.FORWARD);
            for (Method method : beforeEachMethods) {
                LOCK_PROCESSOR.processLocks(method);
                TEST_UTILS.invoke(method, testInstance, testArgument, throwableContext);
                LOCK_PROCESSOR.processUnlocks(method);
                StandardStreams.flush();
                if (!throwableContext.isEmpty()) {
                    break;
                }
            }
        } catch (Throwable t) {
            throwableContext.add(testClass, t);
        } finally {
            StandardStreams.flush();
        }
        return State.POST_BEFORE_EACH;
    }

    private State postBeforeEach() {
        Object testInstance = getTestInstance();
        Invariant.check(testInstance != null);
        EXTENSION_MANAGER.postBeforeEachCallback(testInstance, testArgument, getThrowableContext());
        if (getThrowableContext().isEmpty()) {
            return State.PRE_TEST;
        } else {
            return State.AFTER_EACH;
        }
    }

    private State preTest() {
        Object testInstance = getTestInstance();
        Invariant.check(testInstance != null);
        ThrowableContext throwableContext = getThrowableContext();
        EXTENSION_MANAGER.preTestCallback(testInstance, testArgument, testMethod, throwableContext);
        if (throwableContext.isEmpty()) {
            return State.TEST;
        } else {
            return State.POST_TEST;
        }
    }

    private State test() {
        Object testInstance = getTestInstance();
        Invariant.check(testInstance != null);
        ThrowableContext throwableContext = getThrowableContext();
        LOCK_PROCESSOR.processLocks(testMethod);
        TEST_UTILS.invoke(testMethod, testInstance, testArgument, throwableContext);
        LOCK_PROCESSOR.processUnlocks(testMethod);
        StandardStreams.flush();
        return State.POST_TEST;
    }

    private State postTest() {
        Object testInstance = getTestInstance();
        Invariant.check(testInstance != null);
        ThrowableContext throwableContext = getThrowableContext();
        EXTENSION_MANAGER.postAfterTestCallback(
                testInstance, testArgument, testMethod, throwableContext);
        return State.AFTER_EACH;
    }

    private State afterEach() {
        Object testInstance = getTestInstance();
        Invariant.check(testInstance != null);
        ThrowableContext throwableContext = getThrowableContext();
        List<Method> afterEachMethods =
                REFLECTION_UTILS.findMethods(
                        testInstance.getClass(), ParameterizedTestFilters.AFTER_EACH_METHOD);
        TEST_UTILS.sortMethods(afterEachMethods, TestUtils.Sort.REVERSE);
        for (Method method : afterEachMethods) {
            LOCK_PROCESSOR.processLocks(method);
            TEST_UTILS.invoke(method, testInstance, testArgument, throwableContext);
            LOCK_PROCESSOR.processUnlocks(method);
            StandardStreams.flush();
        }
        return State.POST_AFTER_EACH;
    }

    private State postAfterEach() {
        Object testInstance = getTestInstance();
        Invariant.check(testInstance != null);
        ThrowableContext throwableContext = getThrowableContext();
        EXTENSION_MANAGER.postAfterEachCallback(testInstance, testArgument, throwableContext);
        return State.CLOSE_AUTO_CLOSE_FIELDS;
    }

    private State closeAutoCloseFields() {
        Object testInstance = getTestInstance();
        Invariant.check(testInstance != null);
        ThrowableContext throwableContext = getThrowableContext();
        AutoCloseProcessor autoCloseProcessor = AutoCloseProcessor.getSingleton();
        List<Field> testFields =
                REFLECTION_UTILS.findFields(testClass, ParameterizedTestFilters.AUTO_CLOSE_FIELDS);
        for (Field testField : testFields) {
            if (testField.isAnnotationPresent(TestEngine.AutoClose.AfterEach.class)) {
                autoCloseProcessor.close(testInstance, testField, throwableContext);
                StandardStreams.flush();
            }
        }
        return State.END;
    }

    public static class Builder {

        private TestDescriptor parentTestDescriptor;
        private Class<?> testClass;
        private Argument testArgument;
        private Method testMethod;

        public Builder setParentTestDescriptor(TestDescriptor parentTestDescriptor) {
            this.parentTestDescriptor = parentTestDescriptor;
            return this;
        }

        public Builder setTestClass(Class<?> testClass) {
            this.testClass = testClass;
            return this;
        }

        public Builder setTestArgument(Argument testArgument) {
            this.testArgument = testArgument;
            return this;
        }

        public Builder setTestMethod(Method testMethod) {
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
