/*
 * Copyright 2022-2023 Douglas Hoard
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

package org.antublue.test.engine.support;

import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.descriptor.TestEngineClassTestDescriptor;
import org.antublue.test.engine.descriptor.TestEngineArgumentTestDescriptor;
import org.antublue.test.engine.descriptor.TestEngineTestMethodTestDescriptor;
import org.antublue.test.engine.support.logger.Logger;
import org.antublue.test.engine.support.logger.LoggerFactory;
import org.antublue.test.engine.support.predicate.TestClassPredicate;
import org.antublue.test.engine.support.predicate.TestClassTagPredicate;
import org.antublue.test.engine.support.predicate.TestMethodPredicate;
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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class to implement a code to discover tests
 */
@SuppressWarnings("unchecked")
public class TestEngineDiscoverySelectorResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEngineDiscoverySelectorResolver.class);

    private final TestClassPredicate includeTestClassPredicate;
    private final TestClassPredicate excludeTestClassPredicate;
    private final TestMethodPredicate includeTestMethodPredicate;
    private final TestMethodPredicate excludeTestMethodPredicate;
    private final TestClassTagPredicate includeTestClassTagPredicate;
    private final TestClassTagPredicate excludeTestClassTagPredicate;

    /**
     * Predicate to determine if a class is a test class (not abstract, has @TestEngine.Test methods)
     */
    private static final Predicate<Class<?>> IS_TEST_CLASS =
            clazz -> !Modifier.isAbstract(clazz.getModifiers()) && !TestEngineReflectionUtils.getTestMethods(clazz).isEmpty();

    /**
     * Predicate to determine if a method is a test method (declared class contains the method)
     */
    private static final Predicate<Method> IS_TEST_METHOD =
            method -> TestEngineReflectionUtils.getTestMethods(method.getDeclaringClass()).contains(method);

    public TestEngineDiscoverySelectorResolver() {
        String includeTestClassPredicateRegex =
                TestEngineConfiguration.getConfigurationValue(
                        "antublue.test.engine.test.class.include");

        LOGGER.trace(String.format("antublue.test.engine.test.class.include [%s]", includeTestClassPredicateRegex));

        if (includeTestClassPredicateRegex != null) {
            includeTestClassPredicate = TestClassPredicate.of(includeTestClassPredicateRegex);
        } else {
            includeTestClassPredicate = null;
        }

        String excludeTestClassPredicateRegex =
                TestEngineConfiguration.getConfigurationValue(
                        "antublue.test.engine.test.class.exclude");

        LOGGER.trace(String.format("antublue.test.engine.test.class.exclude [%s]", excludeTestClassPredicateRegex));

        if (excludeTestClassPredicateRegex != null) {
            excludeTestClassPredicate = TestClassPredicate.of(excludeTestClassPredicateRegex);
        } else {
            excludeTestClassPredicate = null;
        }

        String includeTestMethodPredicateRegex =
                TestEngineConfiguration.getConfigurationValue(
                        "antublue.test.engine.test.method.include");

        LOGGER.trace(String.format("antublue.test.engine.test.method.include [%s]", includeTestMethodPredicateRegex));

        if (includeTestMethodPredicateRegex != null) {
            includeTestMethodPredicate = TestMethodPredicate.of(includeTestMethodPredicateRegex);
        } else {
            includeTestMethodPredicate = null;
        }

        String excludeTestMethodPredicateRegex =
                TestEngineConfiguration.getConfigurationValue(
                        "antublue.test.engine.test.method.exclude");

        LOGGER.trace(String.format("antublue.test.engine.test.method.exclude [%s]", excludeTestMethodPredicateRegex));

        if (excludeTestMethodPredicateRegex != null) {
            excludeTestMethodPredicate = TestMethodPredicate.of(excludeTestMethodPredicateRegex);
        } else {
            excludeTestMethodPredicate = null;
        }

        String includeTestClassTagsRegex =
                TestEngineConfiguration.getConfigurationValue(
                        "antublue.test.engine.test.class.tag.include");

        LOGGER.trace(String.format("antublue.test.engine.test.class.tag.include [%s]", includeTestClassTagsRegex));

        if (includeTestClassTagsRegex != null) {
            includeTestClassTagPredicate = TestClassTagPredicate.of(includeTestClassTagsRegex);
        } else {
            includeTestClassTagPredicate = null;
        }

        String excludeTestClassTagsRegex =
                TestEngineConfiguration.getConfigurationValue(
                        "antublue.test.engine.test.class.tag.exclude");

        LOGGER.trace(String.format("antublue.test.engine.test.class.tag.exclude [%s]", excludeTestClassTagsRegex));

        if (excludeTestClassTagsRegex != null) {
            excludeTestClassTagPredicate = TestClassTagPredicate.of(excludeTestClassTagsRegex);
        } else {
            excludeTestClassTagPredicate = null;
        }
    }

    /**
     * Method to resolve test classes / methods, adding them to the EngineDescriptor
     *
     * @param engineDiscoveryRequest
     * @param engineDescriptor
     */
    public void resolveSelectors(EngineDiscoveryRequest engineDiscoveryRequest, EngineDescriptor engineDescriptor) {
        LOGGER.info("resolveSelectors()");

        // Test class to test method list mapping, sorted by test class name
        Map<Class<?>, Collection<Method>> testClassToMethodMap = new TreeMap<>(Comparator.comparing(Class::getName));

        // For each all classes, add all test methods
        resolveClasspathRoot(engineDiscoveryRequest, testClassToMethodMap);

        // For each test package that was selected, add all test methods
        resolvePackageSelector(engineDiscoveryRequest, testClassToMethodMap);

        // For each test class selected, add all test methods
        resolveClassSelector(engineDiscoveryRequest, testClassToMethodMap);

        // For each test method that was selected, add the test class and method
        resolveMethodSelector(engineDiscoveryRequest, testClassToMethodMap);

        // For test
        resolveUniqueIdSelector(engineDiscoveryRequest, testClassToMethodMap);

        if (includeTestClassPredicate != null) {
            Map<Class<?>, Collection<Method>> workingTestClassToMethodMap = new HashMap<>(testClassToMethodMap);
            for (Class<?> clazz : workingTestClassToMethodMap.keySet()) {
                if (!includeTestClassPredicate.test(clazz)) {
                    testClassToMethodMap.remove(clazz);
                }
            }
        }

        if (excludeTestClassPredicate != null) {
            Map<Class<?>, Collection<Method>> workingTestClassToMethodMap = new HashMap<>(testClassToMethodMap);
            for (Class<?> clazz : workingTestClassToMethodMap.keySet()) {
                if (excludeTestClassPredicate.test(clazz)) {
                    testClassToMethodMap.remove(clazz);
                }
            }
        }

        if (includeTestMethodPredicate != null) {
            Map<Class<?>, Collection<Method>> workingTestClassToMethodMap = new HashMap<>(testClassToMethodMap);
            for (Class<?> clazz : workingTestClassToMethodMap.keySet()) {
                Collection<Method> methods = new ArrayList<>(testClassToMethodMap.get(clazz));
                methods.removeIf(method -> !includeTestMethodPredicate.test(method));

                if (methods.isEmpty()) {
                    testClassToMethodMap.remove(clazz);
                } else {
                    testClassToMethodMap.put(clazz, methods);
                }
            }
        }

        if (excludeTestMethodPredicate != null) {
            Map<Class<?>, Collection<Method>> workingTestClassToMethodMap = new HashMap<>(testClassToMethodMap);
            for (Class<?> clazz : workingTestClassToMethodMap.keySet()) {
                Collection<Method> methods = new ArrayList<>(workingTestClassToMethodMap.get(clazz));
                methods.removeIf(excludeTestMethodPredicate);

                if (methods.isEmpty()) {
                    testClassToMethodMap.remove(clazz);
                } else {
                    testClassToMethodMap.put(clazz, methods);
                }
            }
        }

        if (includeTestClassTagPredicate != null) {
            Map<Class<?>, Collection<Method>> workingTestClassToMethodMap = new HashMap<>(testClassToMethodMap);
            for (Class<?> clazz : workingTestClassToMethodMap.keySet()) {
                if (!includeTestClassTagPredicate.test(clazz)) {
                    testClassToMethodMap.remove(clazz);
                }
            }
        }

        if (excludeTestClassTagPredicate != null) {
            Map<Class<?>, Collection<Method>> workingTestClassToMethodMap = new HashMap<>(testClassToMethodMap);
            for (Class<?> clazz : workingTestClassToMethodMap.keySet()) {
                if (excludeTestClassTagPredicate.test(clazz)) {
                    testClassToMethodMap.remove(clazz);
                }
            }
        }

        processSelectors(engineDescriptor, testClassToMethodMap);
    }

    private void resolveClasspathRoot(EngineDiscoveryRequest engineDiscoveryRequest, Map<Class<?>, Collection<Method>> testClassToMethodMap) {
        LOGGER.trace("resolveClasspathRoot()");

        List<? extends DiscoverySelector> discoverySelectorList = engineDiscoveryRequest.getSelectorsByType(ClasspathRootSelector.class);
        LOGGER.trace(String.format("discoverySelectorList size [%d]", discoverySelectorList.size()));

        for (DiscoverySelector discoverySelector : discoverySelectorList) {
            URI uri = ((ClasspathRootSelector) discoverySelector).getClasspathRoot();
            LOGGER.trace(String.format("uri [%s]", uri));

            List<Class<?>> classList = ReflectionSupport.findAllClassesInClasspathRoot(uri, IS_TEST_CLASS, name -> true);

            for (Class<?> clazz : classList) {
                LOGGER.trace(String.format("  class [%s]", clazz.getName()));
                testClassToMethodMap.putIfAbsent(clazz, TestEngineReflectionUtils.getTestMethods(clazz));
            }
        }
    }

    private void resolvePackageSelector(EngineDiscoveryRequest engineDiscoveryRequest, Map<Class<?>, Collection<Method>> testClassToMethodMap) {
        LOGGER.trace("resolvePackageSelector()");

        List<? extends DiscoverySelector> discoverySelectorList = engineDiscoveryRequest.getSelectorsByType(PackageSelector.class);
        LOGGER.trace("discoverySelectorList size [%d]", discoverySelectorList.size());

        for (DiscoverySelector discoverySelector : discoverySelectorList) {
            String packageName = ((PackageSelector) discoverySelector).getPackageName();
            List<Class<?>> classList = ReflectionSupport.findAllClassesInPackage(packageName, IS_TEST_CLASS, name -> true);

            for (Class<?> clazz : classList) {
                LOGGER.trace(String.format("  test class [%s]", clazz.getName()));
                testClassToMethodMap.putIfAbsent(clazz, TestEngineReflectionUtils.getTestMethods(clazz));
            }
        }
    }

    private void resolveClassSelector(EngineDiscoveryRequest engineDiscoveryRequest, Map<Class<?>, Collection<Method>> testClassToMethodMap) {
        LOGGER.trace("resolveClassSelector()");

        List<? extends DiscoverySelector> discoverySelectorList = engineDiscoveryRequest.getSelectorsByType(ClassSelector.class);
        LOGGER.trace("discoverySelectorList size [%d]", discoverySelectorList.size());

        for (DiscoverySelector discoverySelector : discoverySelectorList) {
            Class<?> clazz = ((ClassSelector) discoverySelector).getJavaClass();

            if (IS_TEST_CLASS.test(clazz)) {
                LOGGER.trace(String.format("  test class [%s]", clazz.getName()));
                testClassToMethodMap.putIfAbsent(clazz, TestEngineReflectionUtils.getTestMethods(clazz));
            } else {
                LOGGER.trace(String.format("  skipping [%s]", clazz.getName()));
            }
        }
    }

    private void resolveMethodSelector(EngineDiscoveryRequest engineDiscoveryRequest, Map<Class<?>, Collection<Method>> testClassToMethodMap) {
        LOGGER.trace("resolveMethodSelector()");

        List<? extends DiscoverySelector> discoverySelectorList = engineDiscoveryRequest.getSelectorsByType(MethodSelector.class);
        LOGGER.trace(String.format("discoverySelectorList size [%d]", discoverySelectorList.size()));

        for (DiscoverySelector discoverySelector : discoverySelectorList) {
            Method method = ((MethodSelector) discoverySelector).getJavaMethod();
            Class<?> clazz = method.getDeclaringClass();

            if (IS_TEST_METHOD.test(method)) {
                LOGGER.trace(String.format("  test class [%s] @TestEngine.Test method [%s]", clazz.getName(), method.getName()));
                Collection<Method> methods = testClassToMethodMap.computeIfAbsent(clazz, k -> new ArrayList<>());

                methods.add(method);
            }
        }
    }

    private void resolveUniqueIdSelector(EngineDiscoveryRequest engineDiscoveryRequest, Map<Class<?>, Collection<Method>> testClassToMethodMap) {
        LOGGER.info("resolveUniqueIdSelector()");
        
        List<? extends DiscoverySelector> discoverySelectorList = engineDiscoveryRequest.getSelectorsByType(UniqueIdSelector.class);
        LOGGER.info(String.format("discoverySelectorList size [%d]", discoverySelectorList.size()));

        for (DiscoverySelector discoverySelector : discoverySelectorList) {
            UniqueIdSelector uniqueIdSelector = (UniqueIdSelector) discoverySelector;
            UniqueId uniqueId = uniqueIdSelector.getUniqueId();
            LOGGER.info("uniqueId " + uniqueId);

            /*
            Class<?> clazz = method.getDeclaringClass();

            if (IS_TEST_METHOD.test(method)) {
                LOGGER.trace(String.format("  test class [%s] @TestEngine.Test method [%s]", clazz.getName(), method.getName()));
                Collection<Method> methods = testClassToMethodMap.computeIfAbsent(clazz, k -> new ArrayList<>());

                methods.add(method);
            }
            */
        }
    }

    private void processSelectors(
            EngineDescriptor engineDescriptor,
            Map<Class<?>, Collection<Method>> testClassToMethodMap) {
        LOGGER.info("processSelectors()");
        LOGGER.info("testClassToMethodMap.size() [" + testClassToMethodMap.size() + "]");

        UniqueId uniqueId = engineDescriptor.getUniqueId();

        try {
            for (Class<?> testClass : testClassToMethodMap.keySet()) {
                LOGGER.trace(String.format("test class [%s]", testClass.getName()));

                if (TestEngineReflectionUtils.isBaseClass(testClass)) {
                    LOGGER.trace(String.format("test class [%s] is a base class not meant for execution", testClass.getName()));
                    continue;
                }

                if (TestEngineReflectionUtils.isDisabled(testClass)) {
                    LOGGER.trace(String.format("test class [%s] is disabled", testClass.getName()));
                    continue;
                }

                LOGGER.trace(String.format("processing test class [%s]", testClass.getName()));

                // Get the arguments methods
                Collection<Method> argumentsMethods = TestEngineReflectionUtils.getArgumentsMethods(testClass);
                LOGGER.trace(String.format("test class [%s] arguments method count [%d]", testClass.getName(), argumentsMethods.size()));

                // Validate arguments method count
                if (argumentsMethods.isEmpty()) {
                    throw new TestClassConfigurationException(
                            String.format(
                                    "Test class [%s] must declare a @TestEngine.Arguments method",
                                    testClass.getName()));
                }

                // Validate arguments method count
                if (argumentsMethods.size() > 1) {
                    throw new TestClassConfigurationException(
                            String.format(
                                    "Test class [%s] declares more than one @TestEngine.Argument method",
                                    testClass.getName()));
                }

                // Get arguments from the arguments method
                Collection<Argument> testArguments;

                try {
                    Stream<Argument> testArgumentStream =
                            (Stream<Argument>) argumentsMethods
                                    .stream()
                                    .findFirst()
                                    .get()
                                    .invoke(null, (Object[]) null);

                    if (testArgumentStream == null) {
                        throw new TestClassConfigurationException(
                                String.format(
                                        "Test class [%s] @TestEngine.Arguments Stream is null",
                                        testClass.getName()));
                    }

                    testArguments = testArgumentStream.collect(Collectors.toList());
                } catch (ClassCastException e) {
                    throw new TestClassConfigurationException(
                            String.format(
                                    "Test class [%s] @TestEngine.Arguments method must return a Stream<Argument>",
                                    testClass.getName()),
                            e);
                }

                LOGGER.trace("test class argument count [%d]", testArguments.size());

                // Validate we have
                if (testArguments.isEmpty()) {
                    throw new TestClassConfigurationException(
                            String.format(
                                    "Test class [%s] @TestEngine.Arguments Stream is empty",
                                    testClass.getName()));
                }

                Collection<Method> argumentMethods = TestEngineReflectionUtils.getArgumentMethods(testClass);
                LOGGER.trace(String.format("test class [%s] argument method count [%d]", testClass.getName(), argumentMethods.size()));

                if (argumentMethods.isEmpty()) {
                    throw new TestClassConfigurationException(
                            String.format(
                                    "Test class [%s] must declare a @TestEngine.Argument method",
                                    testClass.getName()));
                }

                if (argumentMethods.size() > 1) {
                    throw new TestClassConfigurationException(
                            String.format(
                                    "Test class [%s] declares more than one @TestEngine.Argument method",
                                    testClass.getName()));
                }

                // Build the test descriptor tree if we have test arguments
                // i.e. Tests with an empty set of arguments will be ignored

                uniqueId = uniqueId.append("class", testClass.getName());

                TestEngineClassTestDescriptor testClassTestDescriptor =
                        new TestEngineClassTestDescriptor(
                                uniqueId,
                                testClass.getName(),
                                testClass);

                List<Argument> testArgumentList = new ArrayList<>(testArguments);
                for (Argument testArgument : testArgumentList) {
                    String testArgumentName = testArgument.name();
                    uniqueId = uniqueId.append("argument", testArgumentName);

                    TestEngineArgumentTestDescriptor testEngineArgumentTestDescriptor =
                            new TestEngineArgumentTestDescriptor(
                                    uniqueId,
                                    testArgumentName,
                                    testClass,
                                    testArgument);

                    for (Method testMethod : testClassToMethodMap.get(testClass)) {
                        if (TestEngineReflectionUtils.isDisabled(testMethod)) {
                            LOGGER.trace(
                                    String.format(
                                            "test class [%s] test method [%s] is disabled",
                                            testClass.getName(),
                                            testMethod.getName()));
                            continue;
                        }

                        uniqueId = uniqueId.append("method", testMethod.getName());

                        TestEngineTestMethodTestDescriptor testEngineTestMethodTestDescriptor =
                                new TestEngineTestMethodTestDescriptor(
                                        uniqueId,
                                        testMethod.getName(),
                                        testClass,
                                        testArgument,
                                        testMethod);

                        testEngineArgumentTestDescriptor.addChild(testEngineTestMethodTestDescriptor);

                        uniqueId = uniqueId.removeLastSegment();
                    }

                    if (testEngineArgumentTestDescriptor.getChildren().size() > 0) {
                        testClassTestDescriptor.addChild(testEngineArgumentTestDescriptor);
                    }

                    uniqueId = uniqueId.removeLastSegment();
                }

                if (testClassTestDescriptor.getChildren().size() > 0) {
                    engineDescriptor.addChild(testClassTestDescriptor);
                }

                uniqueId = uniqueId.removeLastSegment();
            }
        } catch (Throwable t) {
            throw new TestEngineException("Exception in TestEngine", t);
        }
    }
}
