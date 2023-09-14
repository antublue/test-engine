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

package org.antublue.test.engine.internal.test.descriptor.parameterized;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.antublue.test.engine.exception.TestEngineException;
import org.antublue.test.engine.internal.test.descriptor.TestDescriptorFactory;
import org.antublue.test.engine.internal.test.util.ReflectionUtils;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.discovery.PackageNameFilter;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

/** Class to implement a ParameterizedTestDescriptorFactory */
public class ParameterizedTestFactory implements TestDescriptorFactory {

    private static final ReflectionUtils REFLECTION_UTILS = ReflectionUtils.getSingleton();

    @Override
    public void discover(
            EngineDiscoveryRequest engineDiscoveryRequest, EngineDescriptor engineDescriptor) {

        engineDiscoveryRequest
                .getSelectorsByType(ClasspathRootSelector.class)
                .forEach(
                        classpathRootSelector ->
                                discover(
                                        engineDiscoveryRequest,
                                        classpathRootSelector,
                                        engineDescriptor));

        engineDiscoveryRequest
                .getSelectorsByType(PackageSelector.class)
                .forEach(
                        packageSelector ->
                                discover(
                                        engineDiscoveryRequest, packageSelector, engineDescriptor));

        engineDiscoveryRequest
                .getSelectorsByType(ClassSelector.class)
                .forEach(
                        classSelector ->
                                discover(engineDiscoveryRequest, classSelector, engineDescriptor));

        engineDiscoveryRequest
                .getSelectorsByType(MethodSelector.class)
                .forEach(
                        classSelector ->
                                discover(engineDiscoveryRequest, classSelector, engineDescriptor));

        List<UniqueIdSelector> uniqueIdSelectors =
                engineDiscoveryRequest.getSelectorsByType(UniqueIdSelector.class);
        discover(uniqueIdSelectors, engineDescriptor);
    }

    /**
     * Method to process a ClasspathRootSelector
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @param classpathRootSelector classpathRootSelector
     * @param engineDescriptor engineDescriptor
     */
    private void discover(
            EngineDiscoveryRequest engineDiscoveryRequest,
            ClasspathRootSelector classpathRootSelector,
            EngineDescriptor engineDescriptor) {
        REFLECTION_UTILS
                .findAllClasses(
                        classpathRootSelector.getClasspathRoot(),
                        ParameterizedTestFilters.TEST_CLASS)
                .forEach(
                        testClass -> {
                            if (accept(engineDiscoveryRequest, testClass)) {
                                new ParameterizedClassTestDescriptor.Builder()
                                        .setParentTestDescriptor(engineDescriptor)
                                        .setTestClass(testClass)
                                        .build();
                            }
                        });
    }

    /**
     * Method to process a PackageSelector
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @param packageSelector packageSelector
     * @param engineDescriptor engineDescriptor
     */
    private void discover(
            EngineDiscoveryRequest engineDiscoveryRequest,
            PackageSelector packageSelector,
            EngineDescriptor engineDescriptor) {
        REFLECTION_UTILS
                .findAllClasses(
                        packageSelector.getPackageName(), ParameterizedTestFilters.TEST_CLASS)
                .forEach(
                        testClass -> {
                            if (ParameterizedTestFilters.TEST_CLASS.test(testClass)
                                    && accept(engineDiscoveryRequest, testClass)) {
                                new ParameterizedClassTestDescriptor.Builder()
                                        .setParentTestDescriptor(engineDescriptor)
                                        .setTestClass(testClass)
                                        .build();
                            }
                        });
    }

    /**
     * Method to process a ClassSelector
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @param classSelector classSelector
     * @param engineDescriptor engineDescriptor
     */
    private void discover(
            EngineDiscoveryRequest engineDiscoveryRequest,
            ClassSelector classSelector,
            EngineDescriptor engineDescriptor) {
        Class<?> testClass = classSelector.getJavaClass();
        if (ParameterizedTestFilters.TEST_CLASS.test(testClass)
                && accept(engineDiscoveryRequest, testClass)) {
            new ParameterizedClassTestDescriptor.Builder()
                    .setParentTestDescriptor(engineDescriptor)
                    .setTestClass(testClass)
                    .build();
        }
    }

    /**
     * Method to process a List of UniqueId selectors
     *
     * @param uniqueIdSelectors uniqueIdSelectors
     * @param engineDescriptor engineDescriptor
     */
    private void discover(
            List<UniqueIdSelector> uniqueIdSelectors, EngineDescriptor engineDescriptor) {
        for (UniqueIdSelector uniqueIdSelector : uniqueIdSelectors) {
            UniqueId uniqueId = uniqueIdSelector.getUniqueId();
            List<UniqueId.Segment> uniqueIdSegments = uniqueId.getSegments();
            String testClassName = uniqueIdSegments.get(1).getValue();
            Class<?> testClass = null;

            try {
                testClass = Thread.currentThread().getContextClassLoader().loadClass(testClassName);
            } catch (Throwable t) {
                throw new TestEngineException(
                        String.format("Exception loading test class [%s]", testClassName));
            }

            new ParameterizedClassTestDescriptor.Builder()
                    .setParentTestDescriptor(engineDescriptor)
                    .setTestClass(testClass)
                    .build();
        }

        Set<UniqueId> uniqueIds = new LinkedHashSet<>();
        for (UniqueIdSelector uniqueIdSelector : uniqueIdSelectors) {
            uniqueIds.add(uniqueIdSelector.getUniqueId());
        }

        if (!uniqueIds.isEmpty()) {
            filter(uniqueIds, engineDescriptor);
        }
    }

    private void filter(Set<UniqueId> uniqueIds, TestDescriptor testDescriptor) {
        if (testDescriptor instanceof EngineDescriptor) {
            for (TestDescriptor child : testDescriptor.getChildren()) {
                filter(uniqueIds, child);
            }
            return;
        }

        if (testDescriptor instanceof ParameterizedClassTestDescriptor) {
            Set<TestDescriptor> testDescriptorsToRemove = new LinkedHashSet<>();
            for (TestDescriptor child : testDescriptor.getChildren()) {
                if (!uniqueIds.contains(child.getUniqueId())) {
                    testDescriptorsToRemove.add(child);
                }
            }
            for (TestDescriptor testDescriptorToRemove : testDescriptorsToRemove) {
                testDescriptor.removeChild(testDescriptorToRemove);
            }
        }
    }

    /**
     * Method to process engine discovery request filters
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @param clazz class
     * @return true if the class should be accepted, else false
     */
    public static boolean accept(EngineDiscoveryRequest engineDiscoveryRequest, Class<?> clazz) {
        List<PackageNameFilter> packageNameFilters =
                engineDiscoveryRequest.getFiltersByType(PackageNameFilter.class);
        for (PackageNameFilter packageNameFilter : packageNameFilters) {
            FilterResult filterResult = packageNameFilter.apply(clazz.getPackage().getName());
            if (filterResult.excluded()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Method to process a MethodSelector
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @param methodSelector methodSelector
     * @param engineDescriptor engineDescriptor
     */
    private static void discover(
            EngineDiscoveryRequest engineDiscoveryRequest,
            MethodSelector methodSelector,
            EngineDescriptor engineDescriptor) {
        Class<?> testClass = methodSelector.getJavaClass();
        Method testMethod = methodSelector.getJavaMethod();
        if (ParameterizedTestFilters.TEST_CLASS.test(testClass)
                && ParameterizedTestFilters.TEST_METHOD.test(testMethod)
                && accept(engineDiscoveryRequest, testClass)) {
            new ParameterizedClassTestDescriptor.Builder()
                    .setParentTestDescriptor(engineDescriptor)
                    .setTestClass(testClass)
                    .setTestMethodFilter(new TestMethodFilter(testMethod))
                    .build();
        }
    }

    /** Class to implement a TestMethodFilter */
    private static class TestMethodFilter implements Predicate<Method> {

        private final Method testMethod;

        /**
         * Constructor
         *
         * @param testMethod testMethod
         */
        public TestMethodFilter(Method testMethod) {
            this.testMethod = testMethod;
        }

        @Override
        public boolean test(Method testMethod) {
            return this.testMethod.equals(testMethod);
        }
    }
}