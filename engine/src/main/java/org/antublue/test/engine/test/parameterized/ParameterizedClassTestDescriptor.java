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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.exception.TestClassDefinitionException;
import org.antublue.test.engine.test.ExecutableMetadataConstants;
import org.antublue.test.engine.test.ExecutableTestDescriptor;
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
import org.junit.platform.engine.support.descriptor.ClassSource;

/** Class to implement a ParameterClassTestDescriptor */
public class ParameterizedClassTestDescriptor extends ExecutableTestDescriptor {

    private final Class<?> testClass;
    private final StopWatch stopWatch;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param testClass testClass
     */
    private ParameterizedClassTestDescriptor(UniqueId uniqueId, Class<?> testClass) {
        super(uniqueId, TEST_UTILS.getDisplayName(testClass));
        this.testClass = testClass;
        this.stopWatch = new StopWatch();
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(ClassSource.from(testClass));
    }

    @Override
    public Type getType() {
        return Type.CONTAINER_AND_TEST;
    }

    private enum State {
        BEGIN,
        INSTANTIATE,
        PREPARE,
        EXECUTE_OR_SKIP,
        CONCLUDE,
        CLOSE_AUTO_CLOSE_FIELDS,
        END
    }

    @Override
    public void execute(ExecutionRequest executionRequest) {
        stopWatch.start();

        getExecutableMetadata().put(ExecutableMetadataConstants.TEST_CLASS, testClass);

        executionRequest.getEngineExecutionListener().executionStarted(this);

        validate();

        State state = State.BEGIN;
        while (state != null) {
            switch (state) {
                case BEGIN:
                    {
                        state = State.INSTANTIATE;
                        break;
                    }
                case INSTANTIATE:
                    {
                        state = instantiate();
                        break;
                    }
                case PREPARE:
                    {
                        state = prepare();
                        break;
                    }
                case EXECUTE_OR_SKIP:
                    {
                        state = executeOrSkip(executionRequest);
                        break;
                    }
                case CONCLUDE:
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
                            this, TestExecutionResult.failed(getThrowableContext().getFirst()));
        }

        setTestInstance(null);
        StandardStreams.flush();
    }

    private State instantiate() {
        try {
            Constructor<?> constructor = testClass.getDeclaredConstructor((Class<?>[]) null);
            Object testInstance = constructor.newInstance((Object[]) null);
            setTestInstance(testInstance);
            EXTENSION_MANAGER.initialize(testClass);
            EXTENSION_MANAGER.postInstantiateCallback(testInstance, getThrowableContext());
            return State.PREPARE;
        } catch (Throwable t) {
            getThrowableContext().add(testClass, t);
            return State.EXECUTE_OR_SKIP;
        } finally {
            StandardStreams.flush();
        }
    }

    private State prepare() {
        Object testInstance = getTestInstance();
        Invariant.check(testInstance != null);
        try {
            List<Method> prepareMethods =
                    REFLECTION_UTILS.findMethods(
                            testClass, ParameterizedTestFilters.PREPARE_METHOD);
            TEST_UTILS.sortMethods(prepareMethods, TestUtils.Sort.FORWARD);
            for (Method method : prepareMethods) {
                LOCK_PROCESSOR.processLocks(method);
                TEST_UTILS.invoke(method, testInstance, null, getThrowableContext());
                LOCK_PROCESSOR.processUnlocks(method);
                StandardStreams.flush();
                if (!getThrowableContext().isEmpty()) {
                    break;
                }
            }
        } catch (Throwable t) {
            getThrowableContext().add(testClass, t);
        } finally {
            EXTENSION_MANAGER.postPrepareCallback(testInstance, getThrowableContext());
            StandardStreams.flush();
        }
        return State.EXECUTE_OR_SKIP;
    }

    private State executeOrSkip(ExecutionRequest executionRequest) {
        Object testInstance = getTestInstance();
        Invariant.check(testInstance != null);
        getChildren()
                .forEach(
                        testDescriptor -> {
                            if (testDescriptor instanceof ExecutableTestDescriptor) {
                                ((ExecutableTestDescriptor) testDescriptor)
                                        .execute(executionRequest);
                            }
                        });
        return State.CONCLUDE;
    }

    private State conclude() {
        Object testInstance = getTestInstance();
        Invariant.check(testInstance != null);
        List<Method> concludeMethods =
                REFLECTION_UTILS.findMethods(testClass, ParameterizedTestFilters.CONCLUDE_METHOD);
        TEST_UTILS.sortMethods(concludeMethods, TestUtils.Sort.REVERSE);
        for (Method method : concludeMethods) {
            LOCK_PROCESSOR.processLocks(method);
            TEST_UTILS.invoke(method, testInstance, null, getThrowableContext());
            LOCK_PROCESSOR.processUnlocks(method);
            StandardStreams.flush();
        }
        EXTENSION_MANAGER.postConcludeCallback(testInstance, getThrowableContext());
        return State.CLOSE_AUTO_CLOSE_FIELDS;
    }

    private State closeAutoCloseFields() {
        Object testInstance = getTestInstance();
        Invariant.check(testInstance != null);
        AutoCloseProcessor autoCloseProcessor = AutoCloseProcessor.getSingleton();
        List<Field> testFields =
                REFLECTION_UTILS.findFields(
                        testInstance.getClass(), ParameterizedTestFilters.AUTO_CLOSE_FIELDS);
        for (Field testField : testFields) {
            if (testField.isAnnotationPresent(TestEngine.AutoClose.Conclude.class)) {
                autoCloseProcessor.close(testInstance, testField, getThrowableContext());
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
                        testClass,
                        method -> method.isAnnotationPresent(TestEngine.ArgumentSupplier.class));
        if (methods.size() != 1) {
            throw new TestClassDefinitionException(
                    String.format(
                            "Test class [%s] must defined exactly one @TestEngine.ArgumentSupplier"
                                    + " method, %d found",
                            testClass.getName(), methods.size()));
        }

        if (!ParameterizedTestFilters.ARGUMENT_SUPPLIER_METHOD.test(methods.get(0))) {
            throw new TestClassDefinitionException(
                    String.format(
                            "Test class [%s] @TestEngine.ArgumentSupplier method [%s] definition is"
                                    + " invalid",
                            testClass.getName(), methods.get(0).getName()));
        }

        methods =
                REFLECTION_UTILS.findMethods(
                        testClass, method -> method.isAnnotationPresent(TestEngine.Prepare.class));
        for (Method method : methods) {
            if (method.isAnnotationPresent(TestEngine.Disabled.class)) {
                continue;
            }
            if (!ParameterizedTestFilters.PREPARE_METHOD.test(method)) {
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
                        method -> method.isAnnotationPresent(TestEngine.BeforeAll.class));
        for (Method method : methods) {
            if (method.isAnnotationPresent(TestEngine.Disabled.class)) {
                continue;
            }
            if (!ParameterizedTestFilters.BEFORE_ALL_METHOD.test(method)) {
                throw new TestClassDefinitionException(
                        String.format(
                                "Test class [%s] @TestEngine.BeforeAll method [%s] definition is"
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
            if (!ParameterizedTestFilters.BEFORE_EACH_METHOD.test(method)) {
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
            if (!ParameterizedTestFilters.TEST_METHOD.test(method)) {
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
            if (!ParameterizedTestFilters.AFTER_EACH_METHOD.test(method)) {
                TestClassDefinitionException testClassDefinitionException =
                        new TestClassDefinitionException(
                                String.format(
                                        "Test class [%s] @TestEngine.AfterEach method [%s]"
                                                + " definition is invalid",
                                        testClass.getName(), method.getName()));
                testClassDefinitionException.setStackTrace(new StackTraceElement[0]);
                throw testClassDefinitionException;
            }
        }

        methods =
                REFLECTION_UTILS.findMethods(
                        testClass, method -> method.isAnnotationPresent(TestEngine.AfterAll.class));
        for (Method method : methods) {
            if (method.isAnnotationPresent(TestEngine.Disabled.class)) {
                continue;
            }
            if (!ParameterizedTestFilters.AFTER_ALL_METHOD.test(method)) {
                throw new TestClassDefinitionException(
                        String.format(
                                "Test class [%s] @TestEngine.BeforeAll method [%s] definition is"
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
            if (!ParameterizedTestFilters.CONCLUDE_METHOD.test(method)) {
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

        public void build() {
            try {
                UniqueId testDescriptorUniqueId =
                        parentTestDescriptor
                                .getUniqueId()
                                .append(
                                        ParameterizedClassTestDescriptor.class.getName(),
                                        testClass.getName());
                TestDescriptor testDescriptor =
                        new ParameterizedClassTestDescriptor(testDescriptorUniqueId, testClass);
                parentTestDescriptor.addChild(testDescriptor);

                Method testArgumentSupplierMethod =
                        PARAMETERIZED_UTILS.getArumentSupplierMethod(testClass);

                List<Argument> testArguments = PARAMETERIZED_UTILS.getArguments(testClass);
                for (Argument testArgument : testArguments) {
                    testDescriptor.addChild(
                            new ParameterizedArgumentTestDescriptor.Builder()
                                    .setParentTestDescriptor(testDescriptor)
                                    .setTestClass(testClass)
                                    .setTestArgumentSupplierMethod(testArgumentSupplierMethod)
                                    .setTestArgument(testArgument)
                                    .setTestMethodFilter(testMethodFilter)
                                    .build());
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }
}
