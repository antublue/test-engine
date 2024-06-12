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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.antublue.test.engine.Constants;
import org.antublue.test.engine.api.Configuration;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.ContextImpl;
import org.antublue.test.engine.internal.Metadata;
import org.antublue.test.engine.internal.MetadataSupport;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.util.StopWatch;
import org.antublue.test.engine.internal.util.ThrowableCollector;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.util.ClassUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

/** Abstract class to implement an ExecutableTestDescriptor */
@SuppressWarnings("PMD.EmptyCatchBlock")
public abstract class ExecutableTestDescriptor extends AbstractTestDescriptor
        implements MetadataSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutableTestDescriptor.class);

    private static final Configuration CONFIGURATION = ContextImpl.getInstance().getConfiguration();

    private static long THREAD_THROTTLE_MILLISECONDS = 0;

    private static final boolean shortTestClassNames;

    private static final DefaultMethodOrderTopDownComparator
            DEFAULT_METHOD_ORDER_TOP_DOWN_COMPARATOR = new DefaultMethodOrderTopDownComparator();

    private static final DefaultMethodOrderBottomUpComparator
            DEFAULT_METHOD_ORDER_BOTTOM_UP_COMPARATOR = new DefaultMethodOrderBottomUpComparator();

    private static final TestEngineOrderAnnotationMethodComparator
            TEST_ENGINE_ORDER_ANNOTATION_COMPARATOR =
                    new TestEngineOrderAnnotationMethodComparator();

    private static final MethodNameComparator METHOD_NAME_COMPARATOR = new MethodNameComparator();

    static {
        CONFIGURATION
                .getProperty(Constants.THREAD_THROTTLE_MILLISECONDS)
                .ifPresent(
                        s -> {
                            try {
                                THREAD_THROTTLE_MILLISECONDS = Long.parseLong(s);
                            } catch (Throwable t) {
                                LOGGER.warn(
                                        Constants.THREAD_THROTTLE_MILLISECONDS
                                                + " [%s] is invalid, ignoring",
                                        s);
                            }
                        });

        Optional<String> optional = CONFIGURATION.getProperty(Constants.TEST_CLASS_NAME_FORMAT);
        shortTestClassNames = optional.filter("short"::equalsIgnoreCase).isPresent();
    }

    private final ThrowableCollector throwableCollector;
    private final Metadata metadata;
    private final StopWatch stopWatch;
    private ExecutionRequest executionRequest;
    private Object testInstance;

    protected ExecutableTestDescriptor(UniqueId uniqueId, String displayName) {
        super(uniqueId, displayName);

        throwableCollector = new ThrowableCollector();
        metadata = new Metadata();
        stopWatch = new StopWatch();
    }

    protected void setExecutionRequest(ExecutionRequest executionRequest) {
        this.executionRequest = executionRequest;
    }

    protected ExecutionRequest getExecutionRequest() {
        return executionRequest;
    }

    protected <T> T getParent(Class<T> clazz) {
        Optional<TestDescriptor> optional = getParent();
        Preconditions.condition(optional.isPresent(), "parent is null");
        return clazz.cast(optional.get());
    }

    protected StopWatch getStopWatch() {
        return stopWatch;
    }

    protected void setTestInstance(Object testInstance) {
        this.testInstance = testInstance;
    }

    protected Object getTestInstance() {
        return testInstance;
    }

    protected ThrowableCollector getThrowableCollector() {
        return throwableCollector;
    }

    @Override
    public Metadata getMetadata() {
        return metadata;
    }

    /**
     * Method to execute the test descriptor
     *
     * @param executionRequest executionRequest
     */
    public abstract void execute(ExecutionRequest executionRequest);

    public void skip(ExecutionRequest executionRequest) {
        getChildren()
                .forEach(
                        testDescriptor -> {
                            if (testDescriptor instanceof ExecutableTestDescriptor) {
                                ((ExecutableTestDescriptor) testDescriptor).skip(executionRequest);
                            }
                        });
    }

    // Common static methods

    /**
     * Method to get a test class tag value
     *
     * @param annotatedElement annotatedElement
     * @return the tag value
     */
    protected static String getTag(AnnotatedElement annotatedElement) {
        String tagValue = null;

        TestEngine.Tag annotation = annotatedElement.getAnnotation(TestEngine.Tag.class);
        if (annotation != null) {
            String tag = annotation.tag();
            if (tag != null && !tag.trim().isEmpty()) {
                tagValue = tag.trim();
            }
        }

        return tagValue;
    }

    /**
     * Method to get a test method display name
     *
     * @param testClass testClass
     * @return the display name
     */
    protected static String getDisplayName(Class<?> testClass) {
        String displayName = testClass.getName();

        TestEngine.DisplayName annotation = testClass.getAnnotation(TestEngine.DisplayName.class);
        if (annotation != null) {
            String name = annotation.name();
            if (name != null && !name.trim().isEmpty()) {
                displayName = name.trim();
            }
        } else if (shortTestClassNames) {
            String[] tokens = testClass.getName().split("\\.");
            if (tokens.length < 2) {
                displayName = testClass.getName();
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < tokens.length - 2; i++) {
                    stringBuilder.append(tokens[i].charAt(0)).append('.');
                }
                stringBuilder
                        .append(tokens[tokens.length - 2])
                        .append('.')
                        .append(tokens[tokens.length - 1]);
                displayName = stringBuilder.toString();
            }
        }

        return displayName;
    }

    /**
     * Method to get a test method display name
     *
     * @param testMethod testMethod
     * @return the display name
     */
    protected static String getDisplayName(Method testMethod) {
        String displayName = testMethod.getName();

        TestEngine.DisplayName annotation = testMethod.getAnnotation(TestEngine.DisplayName.class);
        if (annotation != null) {
            String name = annotation.name();
            if (name != null && !name.trim().isEmpty()) {
                displayName = name.trim();
            }
        }

        return displayName;
    }

    /**
     * Method to order test methods within the hierarchy, keeping the groups by component type /
     * declaring class
     *
     * @param testMethods testMethods
     * @return the list of methods ordered
     */
    protected static List<Method> orderTestMethods(
            List<Method> testMethods, HierarchyTraversalMode hierarchyTraversalMode) {
        // The code assumes that the list of already ordered by component type / declaring class

        // Group methods based on component type / declaring class
        Map<Class<?>, List<Method>> methodMap = new LinkedHashMap<>();
        for (Method method : testMethods) {
            Class<?> componentType = method.getDeclaringClass().getComponentType();
            if (componentType == null) {
                componentType = method.getDeclaringClass();
            }
            List<Method> methods = methodMap.computeIfAbsent(componentType, k -> new ArrayList<>());
            methods.add(method);
        }

        Comparator<Method> methodComparator = DEFAULT_METHOD_ORDER_TOP_DOWN_COMPARATOR;
        if (hierarchyTraversalMode == HierarchyTraversalMode.BOTTOM_UP) {
            methodComparator = DEFAULT_METHOD_ORDER_BOTTOM_UP_COMPARATOR;
        }

        // Sort methods for each group and add them to the list
        for (Map.Entry<Class<?>, List<Method>> entry : methodMap.entrySet()) {
            entry.getValue().sort(methodComparator);
        }

        List<Method> orderedMethods = new ArrayList<>();
        for (Class<?> key : methodMap.keySet()) {
            List<Method> methods = methodMap.get(key);
            orderedMethods.addAll(methods);
        }

        return orderedMethods;
    }

    private static class DefaultMethodOrderTopDownComparator implements Comparator<Method> {

        @Override
        public int compare(Method method1, Method method2) {
            int comparison = TEST_ENGINE_ORDER_ANNOTATION_COMPARATOR.compare(method1, method2);
            if (comparison == 0) {
                comparison = METHOD_NAME_COMPARATOR.compare(method1, method2);
            }
            return comparison;
        }
    }

    private static class DefaultMethodOrderBottomUpComparator implements Comparator<Method> {

        @Override
        public int compare(Method method1, Method method2) {
            int comparison = TEST_ENGINE_ORDER_ANNOTATION_COMPARATOR.compare(method1, method2);
            if (comparison == 0) {
                comparison = -METHOD_NAME_COMPARATOR.compare(method1, method2);
            }
            return comparison;
        }
    }

    /** Class to order methods based on @TestEngine.Order annotation */
    private static class TestEngineOrderAnnotationMethodComparator implements Comparator<Method> {

        private static final int DEFAULT_ORDER = Integer.MAX_VALUE;

        @Override
        public int compare(Method o1, Method o2) {
            int o1Order = DEFAULT_ORDER;
            TestEngine.Order o1Annotation = o1.getAnnotation(TestEngine.Order.class);
            if (o1Annotation != null) {
                o1Order = o1Annotation.order();
            }

            int o2Order = DEFAULT_ORDER;
            TestEngine.Order o2Annotation = o2.getAnnotation(TestEngine.Order.class);
            if (o2Annotation != null) {
                o2Order = o2Annotation.order();
            }

            return Integer.compare(o1Order, o2Order);
        }
    }

    /** Class to order methods based on method name, then parameter types */
    private static class MethodNameComparator implements Comparator<Method> {

        @Override
        public int compare(Method method1, Method method2) {
            int comparison = method1.getName().compareTo(method2.getName());
            if (comparison == 0) {
                comparison =
                        ClassUtils.nullSafeToString(method1.getParameterTypes())
                                .compareTo(
                                        ClassUtils.nullSafeToString(method2.getParameterTypes()));
            }
            return comparison;
        }
    }
}
