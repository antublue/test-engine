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

package org.antublue.test.engine.test.standard;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.exception.TestClassDefinitionException;
import org.antublue.test.engine.test.ExecutableMetadata;
import org.antublue.test.engine.test.ExecutableMetadataConstants;
import org.antublue.test.engine.test.ExecutableTestDescriptor;
import org.antublue.test.engine.test.ThrowableContext;
import org.antublue.test.engine.test.parameterized.ParameterizedTestFilters;
import org.antublue.test.engine.test.util.AutoCloseProcessor;
import org.antublue.test.engine.test.util.RandomFieldInjector;
import org.antublue.test.engine.test.util.TestUtils;
import org.antublue.test.engine.util.Invariant;
import org.antublue.test.engine.util.StandardStreams;
import org.antublue.test.engine.util.StopWatch;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;

/** Class to implement a ParameterClassTestDescriptor */
public class StandardClassTestDescriptor extends ExecutableTestDescriptor {

    private final Class<?> testClass;
    private final StopWatch stopWatch;
    private final ExecutableMetadata executableMetadata;

    /**
     * Constructor
     *
     * @param parentUniqueId parentUniqueId
     * @param testClass testClass
     */
    public StandardClassTestDescriptor(UniqueId parentUniqueId, Class<?> testClass) {
        super(
                parentUniqueId.append(
                        StandardClassTestDescriptor.class.getSimpleName(), testClass.getName()),
                TEST_UTILS.getDisplayName(testClass));
        this.testClass = testClass;
        this.stopWatch = new StopWatch();
        this.executableMetadata = new ExecutableMetadata();
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
    public ExecutableMetadata getExecutableMetadata() {
        return executableMetadata;
    }

    private enum State {
        BEGIN,
        SET_RANDOM_FIELDS,
        PREPARE_METHODS,
        EXECUTE_OR_SKIP,
        CONCLUDE_METHODS,
        CLOSE_AUTO_CLOSE_FIELDS,
        END
    }

    @Override
    public void execute(ExecutionRequest executionRequest) {
        stopWatch.start();

        validate();

        getExecutableMetadata().put(ExecutableMetadataConstants.TEST_CLASS, testClass);

        executionRequest.getEngineExecutionListener().executionStarted(this);

        State state = State.BEGIN;
        while (state != null) {
            switch (state) {
                case BEGIN:
                    {
                        state = instantiate();
                        break;
                    }
                case SET_RANDOM_FIELDS:
                    {
                        state = injectRandomFields();
                        break;
                    }
                case PREPARE_METHODS:
                    {
                        state = prepare();
                        break;
                    }
                case EXECUTE_OR_SKIP:
                    {
                        state = executeOrSkip(executionRequest);
                        break;
                    }
                case CONCLUDE_METHODS:
                    {
                        state = conclude();
                        break;
                    }
                case CLOSE_AUTO_CLOSE_FIELDS:
                    {
                        state = closeAutoCloseFields();
                        break;
                    }
                case END:
                default:
                    {
                        state = null;
                    }
            }
        }

        setTestInstance(null);

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

    private State instantiate() {
        ThrowableContext throwableContext = getThrowableContext();

        try {
            Constructor<?> constructor = testClass.getDeclaredConstructor((Class<?>[]) null);
            Object testInstance = constructor.newInstance((Object[]) null);
            setTestInstance(testInstance);
            EXTENSION_MANAGER.initialize(testClass);
            EXTENSION_MANAGER.postCreateTestInstance(testInstance, throwableContext);
            return State.SET_RANDOM_FIELDS;
        } catch (Throwable t) {
            throwableContext.add(testClass, t);
            return State.EXECUTE_OR_SKIP;
        } finally {
            StandardStreams.flush();
        }
    }

    private State injectRandomFields() {
        Object testInstance = getTestInstance();
        Invariant.check(testInstance != null);
        ThrowableContext throwableContext = getThrowableContext();

        try {
            List<Field> fields =
                    REFLECTION_UTILS.findFields(testClass, ParameterizedTestFilters.RANDOM_FIELD);
            for (Field field : fields) {
                RandomFieldInjector.inject(testInstance, field);
            }
            return State.PREPARE_METHODS;
        } catch (Throwable t) {
            throwableContext.add(testClass, t);
            return State.EXECUTE_OR_SKIP;
        } finally {
            StandardStreams.flush();
        }
    }

    private State prepare() {
        Object testInstance = getTestInstance();
        Invariant.check(testInstance != null);
        ThrowableContext throwableContext = getThrowableContext();
        List<Method> prepareMethods =
                REFLECTION_UTILS.findMethods(testClass, StandardTestFilters.PREPARE_METHOD);
        TEST_UTILS.sortMethods(prepareMethods, TestUtils.Sort.FORWARD);
        for (Method method : prepareMethods) {
            LOCK_PROCESSOR.processLocks(method);
            TEST_UTILS.invoke(method, testInstance, null, throwableContext);
            LOCK_PROCESSOR.processUnlocks(method);
            if (!throwableContext.isEmpty()) {
                break;
            }
        }
        EXTENSION_MANAGER.prepare(testInstance, throwableContext);
        return State.EXECUTE_OR_SKIP;
    }

    private State executeOrSkip(ExecutionRequest executionRequest) {
        Object testInstance = getTestInstance();
        Invariant.check(testInstance != null);
        getChildren()
                .forEach(
                        (Consumer<TestDescriptor>)
                                testDescriptor -> {
                                    if (testDescriptor instanceof ExecutableTestDescriptor) {
                                        ((ExecutableTestDescriptor) testDescriptor)
                                                .execute(executionRequest);
                                    }
                                });

        return State.CONCLUDE_METHODS;
    }

    private State conclude() {
        Object testInstance = getTestInstance();
        Invariant.check(testInstance != null);
        ThrowableContext throwableContext = getThrowableContext();
        List<Method> concludeMethods =
                REFLECTION_UTILS.findMethods(testClass, ParameterizedTestFilters.CONCLUDE_METHOD);
        TEST_UTILS.sortMethods(concludeMethods, TestUtils.Sort.REVERSE);

        for (Method method : concludeMethods) {
            LOCK_PROCESSOR.processLocks(method);
            TEST_UTILS.invoke(method, testInstance, null, throwableContext);
            LOCK_PROCESSOR.processUnlocks(method);
            StandardStreams.flush();
        }
        EXTENSION_MANAGER.conclude(testInstance, throwableContext);
        return State.CLOSE_AUTO_CLOSE_FIELDS;
    }

    private State closeAutoCloseFields() {
        Object testInstance = getTestInstance();
        Invariant.check(testInstance != null);
        ThrowableContext throwableContext = getThrowableContext();
        AutoCloseProcessor autoCloseProcessor = AutoCloseProcessor.getSingleton();
        List<Field> testFields =
                REFLECTION_UTILS.findFields(testClass, StandardTestFilters.AUTO_CLOSE_FIELDS);
        for (Field testField : testFields) {
            if (testField.isAnnotationPresent(TestEngine.AutoClose.Conclude.class)) {
                autoCloseProcessor.close(testInstance, testField, throwableContext);
                StandardStreams.flush();
            }
        }
        return State.END;
    }

    private void validate() {
        try {
            testClass.getDeclaredConstructor((Class<?>[]) null);
        } catch (Throwable t) {
            throw new TestClassDefinitionException(
                    String.format(
                            "Test class [%s] must have a no-argument constructor",
                            testClass.getName()));
        }

        List<Method> methods =
                REFLECTION_UTILS.findMethods(
                        testClass, method -> method.isAnnotationPresent(TestEngine.Prepare.class));
        for (Method method : methods) {
            if (method.isAnnotationPresent(TestEngine.Disabled.class)) {
                continue;
            }
            if (!StandardTestFilters.PREPARE_METHOD.test(method)) {
                throw new TestClassDefinitionException(
                        String.format(
                                "Test class [%s] @TestEngine.Prepare method [%s] definition is"
                                        + " invalid",
                                testClass.getName(), method.getName()));
            }
        }

        methods =
                REFLECTION_UTILS.findMethods(
                        testClass,
                        method -> method.isAnnotationPresent(TestEngine.BeforeEach.class));
        for (Method method : methods) {
            if (method.isAnnotationPresent(TestEngine.Disabled.class)) {
                continue;
            }
            if (!StandardTestFilters.BEFORE_EACH_METHOD.test(method)) {
                throw new TestClassDefinitionException(
                        String.format(
                                "Test class [%s] @TestEngine.BeforeEach method [%s] definition is"
                                        + " invalid",
                                testClass.getName(), method.getName()));
            }
        }

        methods =
                REFLECTION_UTILS.findMethods(
                        testClass, method -> method.isAnnotationPresent(TestEngine.Test.class));
        for (Method method : methods) {
            if (method.isAnnotationPresent(TestEngine.Disabled.class)) {
                continue;
            }
            if (!StandardTestFilters.TEST_METHOD.test(method)) {
                throw new TestClassDefinitionException(
                        String.format(
                                "Test class [%s] @TestEngine.Test method [%s] definition is"
                                        + " invalid",
                                testClass.getName(), method.getName()));
            }
        }

        methods =
                REFLECTION_UTILS.findMethods(
                        testClass,
                        method -> method.isAnnotationPresent(TestEngine.AfterEach.class));
        for (Method method : methods) {
            if (method.isAnnotationPresent(TestEngine.Disabled.class)) {
                continue;
            }
            if (!StandardTestFilters.AFTER_EACH_METHOD.test(method)) {
                throw new TestClassDefinitionException(
                        String.format(
                                "Test class [%s] @TestEngine.AfterEach method [%s] definition is"
                                        + " invalid",
                                testClass.getName(), method.getName()));
            }
        }

        methods =
                REFLECTION_UTILS.findMethods(
                        testClass, method -> method.isAnnotationPresent(TestEngine.Conclude.class));
        for (Method method : methods) {
            if (method.isAnnotationPresent(TestEngine.Disabled.class)) {
                continue;
            }
            if (!StandardTestFilters.CONCLUDE_METHOD.test(method)) {
                throw new TestClassDefinitionException(
                        String.format(
                                "Test class [%s] @TestEngine.Conclude method [%s] definition is"
                                        + " invalid",
                                testClass.getName(), method.getName()));
            }
        }
    }

    public static class Builder {

        private TestDescriptor parentTestDescriptor;
        private Class<?> testClass;
        private Predicate<Method> testMethodFilter;

        public Builder setParentTestDescriptor(TestDescriptor parentTestDescriptor) {
            this.parentTestDescriptor = parentTestDescriptor;
            return this;
        }

        public Builder setTestClass(Class<?> testClass) {
            this.testClass = testClass;
            return this;
        }

        public Builder setTestMethodFilter(Predicate<Method> testMethodFilter) {
            this.testMethodFilter = testMethodFilter;
            return this;
        }

        public TestDescriptor build() {
            try {
                UniqueId testDescriptorUniqueId =
                        parentTestDescriptor
                                .getUniqueId()
                                .append(
                                        StandardClassTestDescriptor.class.getName(),
                                        testClass.getName());
                TestDescriptor testDescriptor =
                        new StandardClassTestDescriptor(testDescriptorUniqueId, testClass);
                parentTestDescriptor.addChild(testDescriptor);

                List<Method> testMethods =
                        REFLECTION_UTILS.findMethods(testClass, StandardTestFilters.TEST_METHOD);
                TEST_UTILS.sortMethods(testMethods, TestUtils.Sort.FORWARD);
                for (Method testMethod : testMethods) {
                    if (testMethodFilter == null || testMethodFilter.test(testMethod)) {
                        new StandardMethodTestDescriptor.Builder()
                                .setParentTestDescriptor(testDescriptor)
                                .setTestClass(testClass)
                                .setTestMethod(testMethod)
                                .build();
                    }
                }

                return testDescriptor;
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }
}
