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

package org.antublue.test.engine.internal.test.descriptor.standard;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.antublue.test.engine.exception.TestEngineException;
import org.antublue.test.engine.internal.test.descriptor.TestDescriptorFactory;
import org.antublue.test.engine.internal.test.util.TestUtils;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

/** Class to implement a ParameterizedTestDescriptorFactory */
@SuppressWarnings("unchecked")
public class StandardTestFactory implements TestDescriptorFactory {

    @Override
    public void discover(
            EngineDiscoveryRequest engineDiscoveryRequest, EngineDescriptor engineDescriptor) {
        Set<Class<?>> classes = new LinkedHashSet<>();
        Map<Class<?>, List<Method>> classMethodMap = new LinkedHashMap<>();

        List<? extends DiscoverySelector> discoverySelectors =
                engineDiscoveryRequest.getSelectorsByType(ClasspathRootSelector.class);
        for (DiscoverySelector discoverySelector : discoverySelectors) {
            try {
                ClasspathRootSelector classpathRootSelector =
                        (ClasspathRootSelector) discoverySelector;

                List<Class<?>> javaClasses =
                        ReflectionSupport.findAllClassesInClasspathRoot(
                                classpathRootSelector.getClasspathRoot(),
                                StandardTestPredicates.TEST_CLASS,
                                className -> true);

                for (Class<?> javaClass : javaClasses) {
                    // Class -> Method mappings
                    List<Method> javaMethods =
                            ReflectionSupport.findMethods(
                                    javaClass,
                                    StandardTestPredicates.TEST_METHOD,
                                    HierarchyTraversalMode.TOP_DOWN);
                    javaMethods =
                            TestUtils.orderTestMethods(
                                    javaMethods, HierarchyTraversalMode.TOP_DOWN);
                    for (Method javaMethod : javaMethods) {
                        classMethodMap
                                .computeIfAbsent(javaClass, c -> new ArrayList<>())
                                .add(javaMethod);
                    }

                    classes.add(javaClass);
                }
            } catch (Throwable t) {
                throw new TestEngineException("Exception processing ClasspathRootSelector", t);
            }
        }

        discoverySelectors = engineDiscoveryRequest.getSelectorsByType(PackageSelector.class);
        for (DiscoverySelector discoverySelector : discoverySelectors) {
            try {
                PackageSelector packageSelector = (PackageSelector) discoverySelector;
                String packageName = packageSelector.getPackageName();

                List<Class<?>> javaClasses =
                        ReflectionSupport.findAllClassesInPackage(
                                packageName, StandardTestPredicates.TEST_CLASS, p -> true);

                for (Class<?> javaClass : javaClasses) {
                    if (StandardTestPredicates.TEST_CLASS.test(javaClass)) {
                        // Class -> Method mappings
                        List<Method> javaMethods =
                                ReflectionSupport.findMethods(
                                        javaClass,
                                        StandardTestPredicates.TEST_METHOD,
                                        HierarchyTraversalMode.TOP_DOWN);
                        javaMethods =
                                TestUtils.orderTestMethods(
                                        javaMethods, HierarchyTraversalMode.TOP_DOWN);
                        for (Method javaMethod : javaMethods) {
                            classMethodMap
                                    .computeIfAbsent(javaClass, c -> new ArrayList<>())
                                    .add(javaMethod);
                        }

                        classes.add(javaClass);
                    }
                }
            } catch (Throwable t) {
                throw new TestEngineException("Exception processing PackageSelector", t);
            }
        }

        discoverySelectors = engineDiscoveryRequest.getSelectorsByType(ClassSelector.class);
        for (DiscoverySelector discoverySelector : discoverySelectors) {
            try {
                ClassSelector classSelector = (ClassSelector) discoverySelector;
                Class<?> javaClass = classSelector.getJavaClass();

                if (StandardTestPredicates.TEST_CLASS.test(javaClass)) {
                    // Class -> Method mappings
                    List<Method> javaMethods =
                            ReflectionSupport.findMethods(
                                    javaClass,
                                    StandardTestPredicates.TEST_METHOD,
                                    HierarchyTraversalMode.TOP_DOWN);
                    javaMethods =
                            TestUtils.orderTestMethods(
                                    javaMethods, HierarchyTraversalMode.TOP_DOWN);
                    for (Method javaMethod : javaMethods) {
                        classMethodMap
                                .computeIfAbsent(javaClass, c -> new ArrayList<>())
                                .add(javaMethod);
                    }

                    classes.add(javaClass);
                }
            } catch (Throwable t) {
                throw new TestEngineException("Exception processing ClassSelector", t);
            }
        }

        discoverySelectors = engineDiscoveryRequest.getSelectorsByType(MethodSelector.class);
        for (DiscoverySelector discoverySelector : discoverySelectors) {
            try {
                MethodSelector methodSelector = (MethodSelector) discoverySelector;
                Class<?> javaClass = methodSelector.getJavaClass();
                Method javaMethod = methodSelector.getJavaMethod();

                if (StandardTestPredicates.TEST_CLASS.test(javaClass)
                        && StandardTestPredicates.TEST_METHOD.test(javaMethod)) {
                    classMethodMap
                            .computeIfAbsent(javaClass, c -> new ArrayList<>())
                            .add(javaMethod);

                    classes.add(javaClass);
                }
            } catch (Throwable t) {
                throw new TestEngineException("Exception processing MethodSelector", t);
            }
        }

        discoverySelectors = engineDiscoveryRequest.getSelectorsByType(UniqueIdSelector.class);
        for (DiscoverySelector discoverySelector : discoverySelectors) {
            try {
                UniqueIdSelector uniqueIdSelector = (UniqueIdSelector) discoverySelector;

                UniqueId uniqueId = uniqueIdSelector.getUniqueId();
                List<UniqueId.Segment> segments = uniqueId.getSegments();

                Class<?> javaClass = null;
                Method javaMethod = null;

                for (UniqueId.Segment segment : segments) {
                    String segmentType = segment.getType();

                    if (segmentType.equals(StandardClassTestDescriptor.class.getName())) {
                        String javaClassName = segment.getValue();
                        javaClass =
                                Thread.currentThread()
                                        .getContextClassLoader()
                                        .loadClass(javaClassName);
                    } else if (segmentType.equals(StandardMethodTestDescriptor.class.getName())) {
                        String javaMethodName = segment.getValue();
                        List<Method> javaMethods =
                                ReflectionSupport.findMethods(
                                        javaClass,
                                        StandardTestPredicates.TEST_METHOD,
                                        HierarchyTraversalMode.BOTTOM_UP);

                        for (Method method : javaMethods) {
                            if (method.getName().equals(javaMethodName)) {
                                javaMethod = method;
                                break;
                            }
                        }
                    }
                }

                if (javaClass != null) {
                    if (javaMethod != null) {
                        classMethodMap
                                .computeIfAbsent(javaClass, c -> new ArrayList<>())
                                .add(javaMethod);
                    } else {
                        List<Method> javaMethods =
                                ReflectionSupport.findMethods(
                                        javaClass,
                                        StandardTestPredicates.TEST_METHOD,
                                        HierarchyTraversalMode.BOTTOM_UP);

                        classMethodMap
                                .computeIfAbsent(javaClass, c -> new ArrayList<>())
                                .addAll(javaMethods);
                    }

                    classes.add(javaClass);
                }

            } catch (Throwable t) {
                throw new TestEngineException("Exception processing UniqueIdSelector", t);
            }
        }

        for (Class<?> clazz : classes) {
            new StandardClassTestDescriptor.Builder()
                    .setTestClass(clazz)
                    .setTestMethods(classMethodMap.get(clazz))
                    .build(engineDescriptor);
        }
    }
}
