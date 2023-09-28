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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.MethodProcessor;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.exception.TestClassDefinitionException;
import org.antublue.test.engine.exception.TestEngineException;
import org.antublue.test.engine.internal.test.descriptor.TestDescriptorFactory;
import org.antublue.test.engine.internal.test.descriptor.filter.AnnotationMethodFilter;
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
@SuppressWarnings({"unchecked", "PMD.AvoidAccessibilityAlteration"})
public class ParameterizedTestFactory implements TestDescriptorFactory {

    @Override
    public void discover(
            EngineDiscoveryRequest engineDiscoveryRequest, EngineDescriptor engineDescriptor) {
        Set<Class<?>> classes = new LinkedHashSet<>();
        Map<Class<?>, List<Argument>> classArgumentMap = new LinkedHashMap<>();
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
                                ParameterizedTestPredicates.TEST_CLASS,
                                className -> true);

                for (Class<?> javaClass : javaClasses) {
                    // Class -> Argument mappings
                    List<Argument> arguments = getArguments(javaClass);

                    classArgumentMap
                            .computeIfAbsent(javaClass, c -> new ArrayList<>())
                            .addAll(arguments);

                    // Class -> Method mappings
                    List<Method> javaMethods =
                            ReflectionSupport.findMethods(
                                    javaClass,
                                    ParameterizedTestPredicates.TEST_METHOD,
                                    HierarchyTraversalMode.TOP_DOWN);

                    javaMethods =
                            TestUtils.orderTestMethods(
                                    javaMethods, HierarchyTraversalMode.TOP_DOWN);

                    MethodProcessor methodProcessor = getMethodOrderer(javaClass);
                    if (methodProcessor != null) {
                        methodProcessor.process(javaClass, javaMethods);
                    }

                    classMethodMap
                            .computeIfAbsent(javaClass, c -> new ArrayList<>())
                            .addAll(javaMethods);

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
                                packageName, ParameterizedTestPredicates.TEST_CLASS, p -> true);

                for (Class<?> javaClass : javaClasses) {
                    if (ParameterizedTestPredicates.TEST_CLASS.test(javaClass)) {
                        // Class -> Argument mappings
                        List<Argument> arguments = getArguments(javaClass);

                        classArgumentMap
                                .computeIfAbsent(javaClass, c -> new ArrayList<>())
                                .addAll(arguments);

                        // Class -> Method mappings
                        List<Method> javaMethods =
                                ReflectionSupport.findMethods(
                                        javaClass,
                                        ParameterizedTestPredicates.TEST_METHOD,
                                        HierarchyTraversalMode.TOP_DOWN);

                        javaMethods =
                                TestUtils.orderTestMethods(
                                        javaMethods, HierarchyTraversalMode.TOP_DOWN);

                        MethodProcessor methodProcessor = getMethodOrderer(javaClass);
                        if (methodProcessor != null) {
                            methodProcessor.process(javaClass, javaMethods);
                        }

                        classMethodMap
                                .computeIfAbsent(javaClass, c -> new ArrayList<>())
                                .addAll(javaMethods);

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

                if (ParameterizedTestPredicates.TEST_CLASS.test(javaClass)) {
                    // Class -> Argument mappings
                    List<Argument> arguments = getArguments(javaClass);

                    classArgumentMap
                            .computeIfAbsent(javaClass, c -> new ArrayList<>())
                            .addAll(arguments);

                    // Class -> Method mappings
                    List<Method> javaMethods =
                            ReflectionSupport.findMethods(
                                    javaClass,
                                    ParameterizedTestPredicates.TEST_METHOD,
                                    HierarchyTraversalMode.TOP_DOWN);

                    javaMethods =
                            TestUtils.orderTestMethods(
                                    javaMethods, HierarchyTraversalMode.TOP_DOWN);

                    MethodProcessor methodProcessor = getMethodOrderer(javaClass);
                    if (methodProcessor != null) {
                        methodProcessor.process(javaClass, javaMethods);
                    }

                    classMethodMap
                            .computeIfAbsent(javaClass, c -> new ArrayList<>())
                            .addAll(javaMethods);

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

                if (ParameterizedTestPredicates.TEST_CLASS.test(javaClass)
                        && ParameterizedTestPredicates.TEST_METHOD.test(javaMethod)) {
                    // Class -> Argument mappings
                    List<Argument> arguments = getArguments(javaClass);

                    classArgumentMap
                            .computeIfAbsent(javaClass, c -> new ArrayList<>())
                            .addAll(arguments);

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
                int argumentIndex = -1;
                Method javaMethod = null;

                for (UniqueId.Segment segment : segments) {
                    String segmentType = segment.getType();

                    if (segmentType.equals(ParameterizedClassTestDescriptor.class.getName())) {
                        String javaClassName = segment.getValue();
                        javaClass =
                                Thread.currentThread()
                                        .getContextClassLoader()
                                        .loadClass(javaClassName);
                    } else if (segmentType.equals(
                            ParameterizedArgumentTestDescriptor.class.getName())) {
                        String value = segment.getValue();
                        if (value.indexOf("/") > 0) {
                            argumentIndex =
                                    Integer.parseInt(value.substring(0, value.indexOf("/")));
                        }
                    } else if (segmentType.equals(
                            ParameterizedMethodTestDescriptor.class.getName())) {
                        String javaMethodName = segment.getValue();
                        List<Method> javaMethods =
                                ReflectionSupport.findMethods(
                                        javaClass,
                                        ParameterizedTestPredicates.TEST_METHOD,
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
                    classes.add(javaClass);

                    List<Argument> arguments = getArguments(javaClass);
                    if (argumentIndex != -1) {
                        classArgumentMap
                                .computeIfAbsent(javaClass, c -> new ArrayList<>())
                                .add(arguments.get(argumentIndex));
                    } else {
                        classArgumentMap
                                .computeIfAbsent(javaClass, c -> new ArrayList<>())
                                .addAll(arguments);
                    }

                    if (javaMethod != null) {
                        classMethodMap
                                .computeIfAbsent(javaClass, c -> new ArrayList<>())
                                .add(javaMethod);
                    } else {
                        List<Method> javaMethods =
                                ReflectionSupport.findMethods(
                                        javaClass,
                                        ParameterizedTestPredicates.TEST_METHOD,
                                        HierarchyTraversalMode.TOP_DOWN);

                        javaMethods =
                                TestUtils.orderTestMethods(
                                        javaMethods, HierarchyTraversalMode.TOP_DOWN);

                        MethodProcessor methodProcessor = getMethodOrderer(javaClass);
                        if (methodProcessor != null) {
                            methodProcessor.process(javaClass, javaMethods);
                        }

                        classMethodMap
                                .computeIfAbsent(javaClass, c -> new ArrayList<>())
                                .addAll(javaMethods);
                    }
                }

            } catch (Throwable t) {
                throw new TestEngineException("Exception processing UniqueIdSelector", t);
            }
        }

        for (Class<?> clazz : classes) {
            new ParameterizedClassTestDescriptor.Builder()
                    .setTestClass(clazz)
                    .setTestArguments(classArgumentMap.get(clazz))
                    .setTestMethods(classMethodMap.get(clazz))
                    .build(engineDescriptor);
        }
    }

    private static Method getArumentSupplierMethod(Class<?> testClass) {
        List<Method> methods =
                ReflectionSupport.findMethods(
                        testClass,
                        AnnotationMethodFilter.of(TestEngine.ArgumentSupplier.class),
                        HierarchyTraversalMode.BOTTOM_UP);

        Method method = methods.get(0);
        method.setAccessible(true);

        return method;
    }

    private static List<Argument> getArguments(Class<?> testClass) throws Throwable {
        List<Argument> testArguments = new ArrayList<>();

        Object object = getArumentSupplierMethod(testClass).invoke(null, (Object[]) null);
        if (object instanceof Stream) {
            Stream<Argument> stream = (Stream<Argument>) object;
            stream.forEach(testArguments::add);
        } else if (object instanceof Iterable) {
            ((Iterable<Argument>) object).forEach(testArguments::add);
        } else {
            throw new TestClassDefinitionException(
                    String.format(
                            "Exception getting arguments for test class [%s]",
                            testClass.getName()));
        }

        return testArguments;
    }

    private static Method getMethodOrdererSupplierMethod(Class<?> testClass) throws Throwable {
        Method method = null;

        List<Method> methods =
                ReflectionSupport.findMethods(
                        testClass,
                        AnnotationMethodFilter.of(TestEngine.MethodProcessorSupplier.class),
                        HierarchyTraversalMode.BOTTOM_UP);

        if (!methods.isEmpty()) {
            method = methods.get(0);
            method.setAccessible(true);
        }

        return method;
    }

    private static MethodProcessor getMethodOrderer(Class<?> testClass) throws Throwable {
        MethodProcessor methodProcessor = null;

        Method methodOrdererMethod = getMethodOrdererSupplierMethod(testClass);
        if (methodOrdererMethod != null) {
            Object object = methodOrdererMethod.invoke(null, (Object[]) null);
            if (object instanceof MethodProcessor) {
                methodProcessor = (MethodProcessor) object;
            } else {
                throw new TestClassDefinitionException(
                        String.format(
                                "Exception getting method orderer for test class [%s]",
                                testClass.getName()));
            }
        }

        return methodProcessor;
    }
}
