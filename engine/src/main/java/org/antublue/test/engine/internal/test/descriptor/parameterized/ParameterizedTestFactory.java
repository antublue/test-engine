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
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.exception.TestClassDefinitionException;
import org.antublue.test.engine.internal.test.descriptor.TestDescriptorFactory;
import org.antublue.test.engine.internal.test.descriptor.filter.AnnotationMethodFilter;
import org.antublue.test.engine.internal.test.util.TestUtils;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
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
public class ParameterizedTestFactory implements TestDescriptorFactory {

    @Override
    public void discover(
            EngineDiscoveryRequest engineDiscoveryRequest, EngineDescriptor engineDescriptor) {
        Set<Class<?>> classes = new LinkedHashSet<>();
        Map<Class<?>, List<Argument>> classArgumentMap = new LinkedHashMap<>();
        Map<Class<?>, List<Method>> classMethodMap = new LinkedHashMap<>();

        engineDiscoveryRequest
                .getSelectorsByType(ClasspathRootSelector.class)
                .forEach(
                        classpathRootSelector -> {
                            try {
                                List<Class<?>> javaClasses = ReflectionSupport.findAllClassesInClasspathRoot(classpathRootSelector.getClasspathRoot(), ParameterizedTestPredicates.TEST_CLASS, className -> true);
                                for (Class<?> javaClass : javaClasses) {
                                    // Class -> Argument mappings
                                    List<Argument> arguments = getArguments(javaClass);
                                    for (Argument argument : arguments) {
                                        classArgumentMap.computeIfAbsent(javaClass, c -> new ArrayList<>()).add(argument);
                                    }

                                    // Class -> Method mappings
                                    List<Method> javaMethods = ReflectionSupport.findMethods(javaClass, ParameterizedTestPredicates.TEST_METHOD, HierarchyTraversalMode.TOP_DOWN);
                                    javaMethods  = TestUtils.orderTestMethods(javaMethods, HierarchyTraversalMode.TOP_DOWN);
                                    for (Method javaMethod : javaMethods) {
                                        classMethodMap.computeIfAbsent(javaClass, c -> new ArrayList<>()).add(javaMethod);
                                    }

                                    classes.add(javaClass);
                                }
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                        });

        engineDiscoveryRequest
                .getSelectorsByType(PackageSelector.class)
                .forEach(
                        packageSelector -> {
                            try {
                                String packageName = packageSelector.getPackageName();
                                List<Class<?>> javaClasses = ReflectionSupport.findAllClassesInPackage(packageName, ParameterizedTestPredicates.TEST_CLASS, p -> true);
                                for (Class<?> javaClass : javaClasses) {
                                    if (ParameterizedTestPredicates.TEST_CLASS.test(javaClass)) {
                                        // Class -> Argument mappings
                                        List<Argument> arguments = getArguments(javaClass);
                                        for (Argument argument : arguments) {
                                            classArgumentMap.computeIfAbsent(javaClass, c -> new ArrayList<>()).add(argument);
                                        }

                                        // Class -> Method mappings
                                        List<Method> javaMethods = ReflectionSupport.findMethods(javaClass, ParameterizedTestPredicates.TEST_METHOD, HierarchyTraversalMode.TOP_DOWN);
                                        javaMethods  = TestUtils.orderTestMethods(javaMethods, HierarchyTraversalMode.TOP_DOWN);
                                        for (Method javaMethod : javaMethods) {
                                            classMethodMap.computeIfAbsent(javaClass, c -> new ArrayList<>()).add(javaMethod);
                                        }

                                        classes.add(javaClass);
                                    }
                                }
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                        }
                );

        engineDiscoveryRequest
                .getSelectorsByType(ClassSelector.class)
                        .forEach(
                                classSelector -> {
                                    try {
                                        Class<?> javaClass = classSelector.getJavaClass();
                                        if (ParameterizedTestPredicates.TEST_CLASS.test(javaClass)) {
                                            // Class -> Argument mappings
                                            List<Argument> arguments = getArguments(javaClass);
                                            for (Argument argument : arguments) {
                                                classArgumentMap.computeIfAbsent(javaClass, c -> new ArrayList<>()).add(argument);
                                            }

                                            // Class -> Method mappings
                                            List<Method> javaMethods = ReflectionSupport.findMethods(javaClass, ParameterizedTestPredicates.TEST_METHOD, HierarchyTraversalMode.TOP_DOWN);
                                            javaMethods  = TestUtils.orderTestMethods(javaMethods, HierarchyTraversalMode.TOP_DOWN);
                                            for (Method javaMethod : javaMethods) {
                                                classMethodMap.computeIfAbsent(javaClass, c -> new ArrayList<>()).add(javaMethod);
                                            }

                                            classes.add(javaClass);
                                        }
                                    } catch (Throwable t) {
                                        t.printStackTrace();
                                    }
                                }
                        );

        engineDiscoveryRequest
                .getSelectorsByType(MethodSelector.class)
                .forEach(
                         methodSelector -> {
                             try {
                                 Class<?> javaClass = methodSelector.getJavaClass();
                                 Method javaMethod = methodSelector.getJavaMethod();

                                 if (ParameterizedTestPredicates.TEST_CLASS.test(javaClass)
                                         && ParameterizedTestPredicates.TEST_METHOD.test(javaMethod)) {
                                     // Class -> Argument mappings
                                     List<Argument> arguments = getArguments(javaClass);
                                     for (Argument argument : arguments) {
                                         classArgumentMap.computeIfAbsent(javaClass, c -> new ArrayList<>()).add(argument);
                                     }

                                     classMethodMap.computeIfAbsent(javaClass, c -> new ArrayList<>()).add(javaMethod);

                                     classes.add(javaClass);
                                 }
                             } catch (Throwable t) {
                                 t.printStackTrace();
                             }
                         });

        // FIX UniqueIdSelector
        engineDiscoveryRequest.getSelectorsByType(UniqueIdSelector.class)
                .forEach(uniqueIdSelector -> {
                    try {
                        UniqueId uniqueId = uniqueIdSelector.getUniqueId();
                        List<UniqueId.Segment> segments = uniqueId.getSegments();
                        Class<?> javaClass = null;
                        Method javaMethod = null;

                        switch (segments.size()) {
                            case 3: {
                                String className = segments.get(1).getValue();
                                javaClass = Thread.currentThread().getContextClassLoader().loadClass(className);
                                if (!ParameterizedTestPredicates.TEST_CLASS.test(javaClass)) {
                                    break;
                                }

                                String methodName = segments.get(2).getValue();
                                List<Method> javaMethods =
                                        ReflectionSupport.findMethods(
                                                javaClass,
                                                ParameterizedTestPredicates.TEST_METHOD,
                                                HierarchyTraversalMode.BOTTOM_UP);
                                for (Method method :  javaMethods) {
                                    if (method.getName().equals(methodName)) {
                                        javaMethod = method;
                                        break;
                                    }
                                }

                                if (javaClass != null && javaMethod != null) {
                                    // Class -> Argument mappings
                                    List<Argument> arguments = getArguments(javaClass);
                                    for (Argument argument : arguments) {
                                        classArgumentMap.computeIfAbsent(javaClass, c -> new ArrayList<>()).add(argument);
                                    }

                                    classMethodMap.computeIfAbsent(javaClass, c -> new ArrayList<>()).add(javaMethod);

                                    classes.add(javaClass);
                                }
                                break;
                            }
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                });

        for (Class<?> clazz : classes) {
                new ParameterizedClassTestDescriptor
                        .Builder()
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

    public static List<Argument> getArguments(Class<?> testClass) throws Throwable {
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
}
