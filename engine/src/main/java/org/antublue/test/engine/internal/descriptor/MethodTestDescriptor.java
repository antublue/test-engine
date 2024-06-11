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
import org.antublue.test.engine.api.Named;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.exception.TestArgumentFailedException;
import org.antublue.test.engine.exception.TestEngineException;
import org.antublue.test.engine.internal.MetadataConstants;
import org.antublue.test.engine.internal.predicate.AnnotationMethodPredicate;
import org.antublue.test.engine.internal.util.StandardStreams;
import org.antublue.test.engine.internal.util.ThrowableCollector;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;

/** Class to implement a MethodTestDescriptor */
public class MethodTestDescriptor extends ExecutableTestDescriptor {

    private final Class<?> testClass;
    private final Named<?> testArgument;
    private final List<Method> beforeEachMethods;
    private final Method testMethod;
    private final List<Method> afterEachMethods;

    /** Constructor */
    private MethodTestDescriptor(Builder builder) {
        super(builder.uniqueId, builder.displayName);
        this.testClass = builder.testClass;
        this.testArgument = builder.testArgument;
        this.beforeEachMethods = builder.beforeEachMethods;
        this.testMethod = builder.testMethod;
        this.afterEachMethods = builder.afterEachMethods;
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
        getStopWatch().reset();

        setExecutionRequest(executionRequest);

        Object testInstance = getParent(ExecutableTestDescriptor.class).getTestInstance();
        Preconditions.notNull(testInstance, "testInstance is null");
        setTestInstance(testInstance);

        getMetadata().put(MetadataConstants.TEST_CLASS, testClass);
        getMetadata().put(MetadataConstants.TEST_ARGUMENT, testArgument);
        getMetadata().put(MetadataConstants.TEST_METHOD, testMethod);

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
            getParent(ArgumentTestDescriptor.class)
                    .getThrowableCollector()
                    .add(
                            new TestArgumentFailedException(
                                    format(
                                            "Exception testing test argument name [%s]",
                                            testArgument.getName())));
            getMetadata().put(MetadataConstants.TEST_DESCRIPTOR_STATUS, MetadataConstants.FAIL);
            executionRequest
                    .getEngineExecutionListener()
                    .executionFinished(
                            this,
                            TestExecutionResult.failed(
                                    getThrowableCollector().getThrowables().get(0)));
        }

        StandardStreams.flush();
    }

    private void beforeEach() throws Throwable {
        for (Method method : beforeEachMethods) {
            method.setAccessible(true);
            method.invoke(getTestInstance());
        }
    }

    private void test() throws Throwable {
        getTestMethod().setAccessible(true);
        getTestMethod().invoke(getTestInstance());
    }

    private void afterEach() throws Throwable {
        for (Method method : afterEachMethods) {
            method.setAccessible(true);
            method.invoke(getTestInstance());
        }
    }

    /** Class to implement a Builder */
    public static class Builder {

        private Class<?> testClass;
        private Named<?> testArgument;
        private Method testMethod;

        private UniqueId uniqueId;
        private String displayName;
        private List<Method> beforeEachMethods;
        private List<Method> afterEachMethods;

        /**
         * Method to set the test class
         *
         * @param testClass testClass
         * @return this
         */
        public Builder setTestClass(Class<?> testClass) {
            this.testClass = testClass;
            return this;
        }

        /**
         * Method to set the test argument index and test argument
         *
         * @param testArgument testArgument
         * @return this
         */
        public Builder setTestArgument(Named<?> testArgument) {
            this.testArgument = testArgument;
            return this;
        }

        /**
         * Method to set the test method
         *
         * @param testMethod testMethod
         * @return this
         */
        public Builder setTestMethod(Method testMethod) {
            this.testMethod = testMethod;
            return this;
        }

        /**
         * Method to build the test descriptor and any children
         *
         * @param parentTestDescriptor parentTestDescriptor
         */
        public void build(TestDescriptor parentTestDescriptor) {
            try {
                uniqueId =
                        parentTestDescriptor
                                .getUniqueId()
                                .append(MethodTestDescriptor.class.getName(), testMethod.getName());

                displayName = getDisplayName(testMethod);

                beforeEachMethods =
                        ReflectionSupport.findMethods(
                                testClass,
                                AnnotationMethodPredicate.of(TestEngine.BeforeEach.class),
                                HierarchyTraversalMode.TOP_DOWN);

                beforeEachMethods =
                        orderTestMethods(beforeEachMethods, HierarchyTraversalMode.TOP_DOWN);

                afterEachMethods =
                        ReflectionSupport.findMethods(
                                testClass,
                                AnnotationMethodPredicate.of(TestEngine.AfterEach.class),
                                HierarchyTraversalMode.BOTTOM_UP);

                afterEachMethods =
                        orderTestMethods(afterEachMethods, HierarchyTraversalMode.BOTTOM_UP);

                TestDescriptor testDescriptor = new MethodTestDescriptor(this);

                parentTestDescriptor.addChild(testDescriptor);
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable t) {
                throw new TestEngineException(t);
            }
        }
    }
}
