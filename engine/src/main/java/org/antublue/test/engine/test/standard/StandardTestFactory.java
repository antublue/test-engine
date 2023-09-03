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

import java.lang.reflect.Method;
import org.antublue.test.engine.test.TestDescriptorFactory;
import org.antublue.test.engine.util.ReflectionUtils;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

/** Class to implement a StandardTestDescriptorFactory */
public class StandardTestFactory implements TestDescriptorFactory {

    private static final ReflectionUtils REFLECTION_UTILS = ReflectionUtils.getSingleton();

    @Override
    public void discover(
            EngineDiscoveryRequest engineDiscoveryRequest, EngineDescriptor engineDescriptor) {
        engineDiscoveryRequest
                .getSelectorsByType(ClasspathRootSelector.class)
                .forEach(
                        classpathRootSelector -> discover(classpathRootSelector, engineDescriptor));

        engineDiscoveryRequest
                .getSelectorsByType(PackageSelector.class)
                .forEach(packageSelector -> discover(packageSelector, engineDescriptor));

        engineDiscoveryRequest
                .getSelectorsByType(ClassSelector.class)
                .forEach(classSelector -> discover(classSelector, engineDescriptor));

        engineDiscoveryRequest
                .getSelectorsByType(MethodSelector.class)
                .forEach(methodSelector -> discover(methodSelector, engineDescriptor));
    }

    /**
     * Method to process a ClasspathRootSelector
     *
     * @param classpathRootSelector classpathRootSelector
     * @param engineDescriptor engineDescriptor
     */
    private void discover(
            ClasspathRootSelector classpathRootSelector, EngineDescriptor engineDescriptor) {
        REFLECTION_UTILS
                .findAllClasses(
                        classpathRootSelector.getClasspathRoot(), StandardFilters.TEST_CLASS)
                .forEach(
                        testClass ->
                                new StandardClassTestDescriptor.Builder()
                                        .withParentTestDescriptor(engineDescriptor)
                                        .withTestClass(testClass)
                                        .build());
    }

    /**
     * Method to process a PackageSelector
     *
     * @param packageSelector packageSelector
     * @param engineDescriptor engineDescriptor
     */
    private static void discover(
            PackageSelector packageSelector, EngineDescriptor engineDescriptor) {
        REFLECTION_UTILS
                .findAllClasses(packageSelector.getPackageName(), StandardFilters.TEST_CLASS)
                .forEach(
                        testClass -> {
                            if (StandardFilters.TEST_CLASS.test(testClass)) {
                                new StandardClassTestDescriptor.Builder()
                                        .withParentTestDescriptor(engineDescriptor)
                                        .withTestClass(testClass)
                                        .build();
                            }
                        });
    }

    /**
     * Method to process a ClassSelector
     *
     * @param classSelector classSelector
     * @param engineDescriptor engineDescriptor
     */
    private static void discover(ClassSelector classSelector, EngineDescriptor engineDescriptor) {
        Class<?> testClass = classSelector.getJavaClass();
        if (StandardFilters.TEST_CLASS.test(testClass)) {
            engineDescriptor.addChild(
                    new StandardClassTestDescriptor.Builder()
                            .withParentTestDescriptor(engineDescriptor)
                            .withTestClass(testClass)
                            .build());
        }
    }

    /**
     * Method to process a MethodSelector
     *
     * @param methodSelector methodSelector
     * @param engineDescriptor engineDescriptor
     */
    private static void discover(MethodSelector methodSelector, EngineDescriptor engineDescriptor) {
        Class<?> testClass = methodSelector.getJavaClass();
        Method testMethod = methodSelector.getJavaMethod();
        if (StandardFilters.TEST_CLASS.test(testClass)
                && StandardFilters.TEST_METHOD.test(testMethod)) {
            new StandardMethodTestDescriptor.Builder()
                    .withParentTestDescriptor(engineDescriptor)
                    .withTestClass(testClass)
                    .withTestMethod(testMethod)
                    .build();
        }
    }
}