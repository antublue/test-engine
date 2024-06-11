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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Named;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.exception.TestClassDefinitionException;
import org.antublue.test.engine.exception.TestEngineException;
import org.antublue.test.engine.internal.ExtensionManager;
import org.antublue.test.engine.internal.predicate.AnnotationMethodPredicate;
import org.antublue.test.engine.internal.predicate.TestClassPredicate;
import org.antublue.test.engine.internal.predicate.TestMethodPredicate;
import org.antublue.test.engine.internal.util.TestUtils;
import org.antublue.test.engine.internal.util.ThrowableContext;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

/** Class to implement a EngineDescriptorFactory */
@SuppressWarnings("PMD.AvoidAccessibilityAlteration")
public class EngineDescriptorFactory {

    private static final TestUtils TEST_UTILS = TestUtils.getInstance();

    private static final ExtensionManager EXTENSION_MANAGER = ExtensionManager.getInstance();

    public static EngineDescriptorFactory getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Method to create an EngineDescriptor
     *
     * @param uniqueId uniqueId
     * @param name name
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @return an EngineDescriptor
     */
    public EngineDescriptor createEngineDescriptor(
            UniqueId uniqueId, String name, EngineDiscoveryRequest engineDiscoveryRequest) {
        EngineDescriptor engineDescriptor = new EngineDescriptor(uniqueId, name);
        discover(engineDiscoveryRequest, engineDescriptor);
        return engineDescriptor;
    }

    private void discover(
            EngineDiscoveryRequest engineDiscoveryRequest, EngineDescriptor engineDescriptor) {
        Set<Class<?>> classes = new LinkedHashSet<>();
        Map<Class<?>, List<Named<?>>> classArgumentMap = new LinkedHashMap<>();
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
                                TestClassPredicate.TEST_CLASS_PREDICATE,
                                className -> true);

                for (Class<?> javaClass : javaClasses) {
                    // Class -> Argument mappings
                    List<Named<?>> testArguments = getArguments(javaClass);

                    classArgumentMap
                            .computeIfAbsent(javaClass, c -> new ArrayList<>())
                            .addAll(testArguments);

                    // Class -> Method mappings
                    List<Method> javaMethods =
                            ReflectionSupport.findMethods(
                                    javaClass,
                                    TestMethodPredicate.TEST_METHOD_PREDICATE,
                                    HierarchyTraversalMode.TOP_DOWN);

                    javaMethods =
                            TEST_UTILS.orderTestMethods(
                                    javaMethods, HierarchyTraversalMode.TOP_DOWN);

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
                                packageName, TestClassPredicate.TEST_CLASS_PREDICATE, p -> true);

                for (Class<?> javaClass : javaClasses) {
                    // Class -> Argument mappings
                    List<Named<?>> testArguments = getArguments(javaClass);

                    classArgumentMap
                            .computeIfAbsent(javaClass, c -> new ArrayList<>())
                            .addAll(testArguments);

                    // Class -> Method mappings
                    List<Method> javaMethods =
                            ReflectionSupport.findMethods(
                                    javaClass,
                                    TestMethodPredicate.TEST_METHOD_PREDICATE,
                                    HierarchyTraversalMode.TOP_DOWN);

                    javaMethods =
                            TEST_UTILS.orderTestMethods(
                                    javaMethods, HierarchyTraversalMode.TOP_DOWN);

                    classMethodMap
                            .computeIfAbsent(javaClass, c -> new ArrayList<>())
                            .addAll(javaMethods);

                    classes.add(javaClass);
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

                if (TestClassPredicate.TEST_CLASS_PREDICATE.test(javaClass)) {
                    // Class -> Argument mappings
                    List<Named<?>> testArguments = getArguments(javaClass);

                    classArgumentMap
                            .computeIfAbsent(javaClass, c -> new ArrayList<>())
                            .addAll(testArguments);

                    // Class -> Method mappings
                    List<Method> javaMethods =
                            ReflectionSupport.findMethods(
                                    javaClass,
                                    TestMethodPredicate.TEST_METHOD_PREDICATE,
                                    HierarchyTraversalMode.TOP_DOWN);

                    javaMethods =
                            TEST_UTILS.orderTestMethods(
                                    javaMethods, HierarchyTraversalMode.TOP_DOWN);

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

                if (TestClassPredicate.TEST_CLASS_PREDICATE.test(javaClass)
                        && TestMethodPredicate.TEST_METHOD_PREDICATE.test(javaMethod)) {
                    // Class -> Argument mappings
                    List<Named<?>> testArguments = getArguments(javaClass);

                    classArgumentMap
                            .computeIfAbsent(javaClass, c -> new ArrayList<>())
                            .addAll(testArguments);

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
                int testArgumentIndex = -1;
                Method javaMethod = null;

                for (UniqueId.Segment segment : segments) {
                    String segmentType = segment.getType();

                    if (segmentType.equals(ClassTestDescriptor.class.getName())) {
                        String javaClassName = segment.getValue();
                        javaClass =
                                Thread.currentThread()
                                        .getContextClassLoader()
                                        .loadClass(javaClassName);
                    } else if (segmentType.equals(ArgumentTestDescriptor.class.getName())) {
                        String value = segment.getValue();
                        if (value.indexOf("/") > 0) {
                            testArgumentIndex =
                                    Integer.parseInt(value.substring(0, value.indexOf("/")));
                        }
                    } else if (segmentType.equals(MethodTestDescriptor.class.getName())) {
                        Preconditions.notNull(javaClass, "javaClass is null, uniqueId errors");

                        String javaMethodName = segment.getValue();
                        List<Method> javaMethods =
                                ReflectionSupport.findMethods(
                                        javaClass,
                                        TestMethodPredicate.TEST_METHOD_PREDICATE,
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

                    List<Named<?>> testArguments = getArguments(javaClass);
                    if (testArgumentIndex != -1) {
                        classArgumentMap
                                .computeIfAbsent(javaClass, c -> new ArrayList<>())
                                .add(testArguments.get(testArgumentIndex));
                    } else {
                        classArgumentMap
                                .computeIfAbsent(javaClass, c -> new ArrayList<>())
                                .addAll(testArguments);
                    }

                    if (javaMethod != null) {
                        classMethodMap
                                .computeIfAbsent(javaClass, c -> new ArrayList<>())
                                .add(javaMethod);
                    } else {
                        List<Method> javaMethods =
                                ReflectionSupport.findMethods(
                                        javaClass,
                                        TestMethodPredicate.TEST_METHOD_PREDICATE,
                                        HierarchyTraversalMode.TOP_DOWN);

                        javaMethods =
                                TEST_UTILS.orderTestMethods(
                                        javaMethods, HierarchyTraversalMode.TOP_DOWN);

                        classMethodMap
                                .computeIfAbsent(javaClass, c -> new ArrayList<>())
                                .addAll(javaMethods);
                    }
                }

            } catch (Throwable t) {
                throw new TestEngineException("Exception processing UniqueIdSelector", t);
            }
        }

        try {
            for (Class<?> clazz : classes) {
                List<Named<?>> arguments = classArgumentMap.get(clazz);

                ThrowableContext throwableContext = new ThrowableContext();
                EXTENSION_MANAGER.postTestArgumentDiscoveryCallback(
                        clazz, arguments, throwableContext);
                throwableContext.throwFirst();

                List<Method> testMethods = classMethodMap.get(clazz);

                throwableContext.clear();
                EXTENSION_MANAGER.postTestMethodDiscoveryCallback(
                        clazz, testMethods, throwableContext);
                throwableContext.throwFirst();

                new ClassTestDescriptor.Builder()
                        .setTestClass(clazz)
                        .setTestArguments(classArgumentMap.get(clazz))
                        .setTestMethods(classMethodMap.get(clazz))
                        .build(engineDescriptor);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            throw new TestEngineException(t);
        }
    }

    private static Method getArumentSupplierMethod(Class<?> testClass) {
        List<Method> methods =
                ReflectionSupport.findMethods(
                        testClass,
                        AnnotationMethodPredicate.of(TestEngine.ArgumentSupplier.class),
                        HierarchyTraversalMode.BOTTOM_UP);

        Method method = methods.get(0);
        method.setAccessible(true);

        return method;
    }

    private static List<Named<?>> getArguments(Class<?> testClass) throws Throwable {
        List<Named<?>> testArguments = new ArrayList<>();

        Object object = getArumentSupplierMethod(testClass).invoke(null, (Object[]) null);
        if (!(object instanceof Stream || object instanceof Iterable)) {
            throw new TestClassDefinitionException(
                    format("Exception getting arguments for test class [%s]", testClass.getName()));
        }

        Iterator<?> iterator;
        if (object instanceof Stream) {
            Stream<?> stream = (Stream<?>) object;
            iterator = stream.iterator();
        } else {
            Iterable<?> iterable = (Iterable<?>) object;
            iterator = iterable.iterator();
        }

        long index = 0;
        while (iterator.hasNext()) {
            Object o = iterator.next();
            if (o instanceof Named<?>) {
                testArguments.add((Named<?>) o);
            } else {
                testArguments.add(Named.of("[" + index + "]", o));
            }
            index++;
        }

        return testArguments;
    }

    /** Class to hold the singleton instance */
    private static final class SingletonHolder {

        /** The singleton instance */
        private static final EngineDescriptorFactory INSTANCE = new EngineDescriptorFactory();
    }
}
