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

import org.antublue.test.engine.TestEngine;
import org.antublue.test.engine.api.Parameter;
import org.antublue.test.engine.descriptor.TestEngineClassTestDescriptor;
import org.antublue.test.engine.descriptor.TestEngineParameterTestDescriptor;
import org.antublue.test.engine.descriptor.TestEngineTestMethodTestDescriptor;
import org.antublue.test.engine.support.logger.Logger;
import org.antublue.test.engine.support.logger.LoggerFactory;
import org.antublue.test.engine.support.predicate.TestClassPredicate;
import org.antublue.test.engine.support.predicate.TestClassTagPredicate;
import org.antublue.test.engine.support.predicate.TestMethodPredicate;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.DirectorySelector;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.discovery.PackageNameFilter;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class to implement a code to discover tests
 */
@SuppressWarnings("unchecked")
public class TestEngineDiscoveryRequestProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEngineDiscoveryRequestProcessor.class);

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

    public TestEngineDiscoveryRequestProcessor() {
        String includeTestClassPredicateRegex =
                TestEngineConfigurationParameters.getInstance()
                        .get("antublue.test.engine.test.class.include")
                        .orElse(null);

        LOGGER.trace(String.format("antublue.test.engine.test.class.include [%s]", includeTestClassPredicateRegex));

        if (includeTestClassPredicateRegex != null) {
            includeTestClassPredicate = TestClassPredicate.of(includeTestClassPredicateRegex);
        } else {
            includeTestClassPredicate = null;
        }

        String excludeTestClassPredicateRegex =
                TestEngineConfigurationParameters.getInstance()
                        .get("antublue.test.engine.test.class.exclude")
                        .orElse(null);

        LOGGER.trace(String.format("antublue.test.engine.test.class.exclude [%s]", excludeTestClassPredicateRegex));

        if (excludeTestClassPredicateRegex != null) {
            excludeTestClassPredicate = TestClassPredicate.of(excludeTestClassPredicateRegex);
        } else {
            excludeTestClassPredicate = null;
        }

        String includeTestMethodPredicateRegex =
                TestEngineConfigurationParameters.getInstance()
                        .get("antublue.test.engine.test.method.include")
                        .orElse(null);

        LOGGER.trace(String.format("antublue.test.engine.test.method.include [%s]", includeTestMethodPredicateRegex));

        if (includeTestMethodPredicateRegex != null) {
            includeTestMethodPredicate = TestMethodPredicate.of(includeTestMethodPredicateRegex);
        } else {
            includeTestMethodPredicate = null;
        }

        String excludeTestMethodPredicateRegex =
                TestEngineConfigurationParameters.getInstance()
                        .get("antublue.test.engine.test.method.exclude")
                        .orElse(null);

        LOGGER.trace(String.format("antublue.test.engine.test.method.exclude [%s]", excludeTestMethodPredicateRegex));

        if (excludeTestMethodPredicateRegex != null) {
            excludeTestMethodPredicate = TestMethodPredicate.of(excludeTestMethodPredicateRegex);
        } else {
            excludeTestMethodPredicate = null;
        }

        String includeTestClassTagsRegex =
                TestEngineConfigurationParameters.getInstance()
                        .get("antublue.test.engine.test.class.tag.include")
                        .orElse(null);

        LOGGER.trace(String.format("antublue.test.engine.test.class.tag.include [%s]", includeTestClassTagsRegex));

        if (includeTestClassTagsRegex != null) {
            includeTestClassTagPredicate = TestClassTagPredicate.of(includeTestClassTagsRegex);
        } else {
            includeTestClassTagPredicate = null;
        }

        String excludeTestClassTagsRegex =
                TestEngineConfigurationParameters.getInstance()
                        .get("antublue.test.engine.test.class.tag.exclude")
                        .orElse(null);

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
    public void processDiscoveryRequest(EngineDiscoveryRequest engineDiscoveryRequest, EngineDescriptor engineDescriptor) {
        LOGGER.trace("processDiscoveryRequest()");

        // Test class to test method list mapping, sorted by test class name
        Map<Class<?>, Collection<Method>> testClassToMethodMap = new TreeMap<>(Comparator.comparing(Class::getName));

        // For each all classes, add all test methods
        processClasspathRootSelector(engineDiscoveryRequest, testClassToMethodMap);

        // For each test package that was selected, add all test methods
        processPackageSelector(engineDiscoveryRequest, testClassToMethodMap);

        // For each test class selected, add all test methods
        processClassSelector(engineDiscoveryRequest, testClassToMethodMap);

        // For each test method that was selected, add the test class and method
        processMethodSelector(engineDiscoveryRequest, testClassToMethodMap);

        // For a specific test argument selection
        processUniqueIdSelector(engineDiscoveryRequest, engineDescriptor, testClassToMethodMap);

        // For a specific directory selection
        processDirectorySelector(engineDiscoveryRequest, engineDescriptor, testClassToMethodMap);

        // Process package name filters
        processPackageNameFilters(engineDiscoveryRequest, engineDescriptor, testClassToMethodMap);

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

        processTestDescriptor(engineDescriptor, testClassToMethodMap);
    }

    private void processClasspathRootSelector(EngineDiscoveryRequest engineDiscoveryRequest, Map<Class<?>, Collection<Method>> testClassToMethodMap) {
        LOGGER.trace("resolveClasspathRoot()");

        List<? extends DiscoverySelector> discoverySelectorList = engineDiscoveryRequest.getSelectorsByType(ClasspathRootSelector.class);
        LOGGER.trace(String.format("discoverySelectorList size [%d]", discoverySelectorList.size()));

        for (DiscoverySelector discoverySelector : discoverySelectorList) {
            LOGGER.trace("discoverySelector.class [%s]", discoverySelector.getClass());

            URI uri = ((ClasspathRootSelector) discoverySelector).getClasspathRoot();
            LOGGER.trace(String.format("uri [%s]", uri));

            List<Class<?>> classList = ReflectionSupport.findAllClassesInClasspathRoot(uri, IS_TEST_CLASS, name -> true);
            for (Class<?> clazz : classList) {
                LOGGER.trace(String.format("  class [%s]", clazz.getName()));
                testClassToMethodMap.putIfAbsent(clazz, TestEngineReflectionUtils.getTestMethods(clazz));
            }
        }
    }

    private void processPackageSelector(EngineDiscoveryRequest engineDiscoveryRequest, Map<Class<?>, Collection<Method>> testClassToMethodMap) {
        LOGGER.trace("resolvePackageSelector()");

        List<? extends DiscoverySelector> discoverySelectorList = engineDiscoveryRequest.getSelectorsByType(PackageSelector.class);
        LOGGER.trace("discoverySelectorList size [%d]", discoverySelectorList.size());

        for (DiscoverySelector discoverySelector : discoverySelectorList) {
            String packageName = ((PackageSelector) discoverySelector).getPackageName();
            LOGGER.trace("packageName [%s]", packageName);

            List<Class<?>> classList = ReflectionSupport.findAllClassesInPackage(packageName, IS_TEST_CLASS, name -> true);
            for (Class<?> clazz : classList) {
                LOGGER.trace(String.format("  test class [%s]", clazz.getName()));
                testClassToMethodMap.putIfAbsent(clazz, TestEngineReflectionUtils.getTestMethods(clazz));
            }
        }
    }

    private void processClassSelector(EngineDiscoveryRequest engineDiscoveryRequest, Map<Class<?>, Collection<Method>> testClassToMethodMap) {
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

    private void processMethodSelector(EngineDiscoveryRequest engineDiscoveryRequest, Map<Class<?>, Collection<Method>> testClassToMethodMap) {
        LOGGER.trace("resolveMethodSelector()");

        List<? extends DiscoverySelector> discoverySelectorList = engineDiscoveryRequest.getSelectorsByType(UniqueIdSelector.class);
        LOGGER.trace(String.format("discoverySelectorList size [%d]", discoverySelectorList.size()));

        discoverySelectorList = engineDiscoveryRequest.getSelectorsByType(MethodSelector.class);
        LOGGER.trace(String.format("discoverySelectorList size [%d]", discoverySelectorList.size()));

        for (DiscoverySelector discoverySelector : discoverySelectorList) {
            MethodSelector methodSelector = (MethodSelector) discoverySelector;
            Class<?> c = ((MethodSelector) discoverySelector).getJavaClass();
            LOGGER.trace("c " + c);
            Method method = ((MethodSelector) discoverySelector).getJavaMethod();
            LOGGER.trace("method " + method.getName());
            Class<?> clazz = method.getDeclaringClass();

            if ((c != null) && (c != clazz)) {
                continue;
            }

            if (IS_TEST_METHOD.test(method)) {
                LOGGER.trace(String.format("  test class [%s] @TestEngine.Test method [%s]", clazz.getName(), method.getName()));
                Collection<Method> methods = testClassToMethodMap.computeIfAbsent(clazz, k -> new ArrayList<>());

                methods.add(method);
            }
        }
    }

    private void processUniqueIdSelector(EngineDiscoveryRequest engineDiscoveryRequest, EngineDescriptor engineDescriptor, Map<Class<?>, Collection<Method>> testClassToMethodMap) {
        LOGGER.trace("resolveUniqueIdSelector()");

        List<? extends DiscoverySelector> discoverySelectorList = engineDiscoveryRequest.getSelectorsByType(UniqueIdSelector.class);
        LOGGER.trace(String.format("discoverySelectorList size [%d]", discoverySelectorList.size()));

        for (DiscoverySelector discoverySelector : discoverySelectorList) {
            UniqueIdSelector uniqueIdSelector = (UniqueIdSelector) discoverySelector;
            UniqueId uniqueId = uniqueIdSelector.getUniqueId();
            LOGGER.trace("uniqueId [" + uniqueId + "]");

            String classpath = System.getProperty("java.class.path");
            String[] classpathEntries = classpath.split(File.pathSeparator);
            for (String classPathEntry : classpathEntries) {
                URI uri = new File(classPathEntry).getAbsoluteFile().toURI();
                List<Class<?>> classList = ReflectionSupport.findAllClassesInClasspathRoot(uri, IS_TEST_CLASS, name -> true);
                for (Class<?> clazz : classList) {
                    testClassToMethodMap.putIfAbsent(clazz, TestEngineReflectionUtils.getTestMethods(clazz));
                }
            }

            EngineDescriptor tempEngineDescriptor =
                    new EngineDescriptor(UniqueId.forEngine(TestEngine.ENGINE_ID), TestEngine.ENGINE_ID);

            processTestDescriptor(tempEngineDescriptor, testClassToMethodMap);

            testClassToMethodMap.clear();

            Optional<? extends TestDescriptor> optionalTestDescriptor = tempEngineDescriptor.findByUniqueId(uniqueId);
            if (optionalTestDescriptor.isPresent()) {
                LOGGER.trace("found testDescriptor");

                TestDescriptor tempTestDescriptor =  optionalTestDescriptor.get();
                if (tempTestDescriptor instanceof TestEngineClassTestDescriptor) {
                    engineDescriptor.addChild(tempTestDescriptor);
                } else {
                    TestDescriptor testDescriptor = ((TestEngineParameterTestDescriptor) optionalTestDescriptor.get()).copy();
                    LOGGER.trace("testDescriptor -> " + testDescriptor.getUniqueId());

                    TestDescriptor parentTestDescriptor = ((TestEngineClassTestDescriptor) testDescriptor.getParent().get()).copy();
                    LOGGER.trace("  testDescriptor -> " + parentTestDescriptor.getUniqueId());

                    parentTestDescriptor.addChild(testDescriptor);
                    engineDescriptor.addChild(parentTestDescriptor);
                }
            }
        }
    }

    private static void processDirectorySelector(EngineDiscoveryRequest engineDiscoveryRequest, EngineDescriptor engineDescriptor, Map<Class<?>, Collection<Method>> testClassToMethodMap) {
        LOGGER.trace("resolveDirectorySelector()");

        List<? extends DiscoverySelector> discoverySelectorList = engineDiscoveryRequest.getSelectorsByType(DirectorySelector.class);
        LOGGER.trace("discoverySelectorList size [%d]", discoverySelectorList.size());

        for (DiscoverySelector discoverySelector : discoverySelectorList) {
            File directory = ((DirectorySelector) discoverySelector).getDirectory();
            LOGGER.trace("directory [%s]", directory.getAbsolutePath());
        }
    }

    private static void processPackageNameFilters(EngineDiscoveryRequest engineDiscoveryRequest, EngineDescriptor engineDescriptor, Map<Class<?>, Collection<Method>> testClassToMethodMap) {
        LOGGER.trace("processPackageNameFilters()");

        List<? extends PackageNameFilter> packageNameFilters = engineDiscoveryRequest.getFiltersByType(PackageNameFilter.class);
        LOGGER.trace("packageNameFilters size [%d]", packageNameFilters.size());

        Map<Class<?>, Collection<Method>> tempTestClassToMethodMap = new LinkedHashMap<>(testClassToMethodMap);

        for (PackageNameFilter packageNameFilter : packageNameFilters) {
            for (Map.Entry<Class<?>, Collection<Method>> entry : tempTestClassToMethodMap.entrySet()) {
                Class<?> clazz = entry.getKey();
                String className = clazz.getName();
                LOGGER.trace("className [%s]", className);

                FilterResult filterResult = packageNameFilter.apply(entry.getKey().getName());
                if (filterResult.excluded()) {
                    LOGGER.trace("excluded [true]");
                    testClassToMethodMap.remove(entry.getKey());
                } else {
                    LOGGER.trace("excluded [false]");
                }
            }
        }
    }

    private void processTestDescriptor(
            TestDescriptor testDescriptor,
            Map<Class<?>, Collection<Method>> testClassToMethodMap) {
        LOGGER.trace("processSelectors()");

        UniqueId uniqueId = testDescriptor.getUniqueId();

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

                validateParameterFieldsAndOrMethods(testClass);

                // Build the test descriptor tree if we have test parameters
                // i.e. Tests with an empty set of parameters will be ignored

                uniqueId = uniqueId.append("class", testClass.getName());

                TestEngineClassTestDescriptor testClassTestDescriptor =
                        new TestEngineClassTestDescriptor(
                                uniqueId,
                                testClass.getName(),
                                testClass);

                Collection<Parameter> testParameters = getTestParameters(testClass);

                int index = 0;
                for (Parameter testParameter : testParameters) {
                    String testParameterName = testParameter.name();

                    // We use generic value of "parameter-" + index since parameter names are not unique
                    uniqueId = uniqueId.append("parameter", "parameter-" + index);

                    TestEngineParameterTestDescriptor testEngineParameterTestDescriptor =
                            new TestEngineParameterTestDescriptor(
                                    uniqueId,
                                    testParameterName,
                                    testClass,
                                    testParameter);

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
                                        testParameter,
                                        testMethod);

                        testEngineParameterTestDescriptor.addChild(testEngineTestMethodTestDescriptor);

                        uniqueId = uniqueId.removeLastSegment();
                    }

                    if (testEngineParameterTestDescriptor.getChildren().size() > 0) {
                        testClassTestDescriptor.addChild(testEngineParameterTestDescriptor);
                    }

                    uniqueId = uniqueId.removeLastSegment();
                    index++;
                }

                if (testClassTestDescriptor.getChildren().size() > 0) {
                    testDescriptor.addChild(testClassTestDescriptor);
                }

                uniqueId = uniqueId.removeLastSegment();
            }
        } catch (Throwable t) {
            throw new TestEngineException("Exception in TestEngine", t);
        }
    }

    private static void validateParameterFieldsAndOrMethods(Class<?> testClass) {
        Collection<Field> parameterFields = TestEngineReflectionUtils.getParameterFields(testClass);
        LOGGER.trace(String.format("test class [%s] parameter field count [%d]", testClass.getName(), parameterFields.size()));

        Collection<Method> parameterMethods = TestEngineReflectionUtils.getParameterMethods(testClass);
        LOGGER.trace(String.format("test class [%s] parameter method count [%d]", testClass.getName(), parameterMethods.size()));

        int count = parameterMethods.size() + parameterFields.size();
        LOGGER.trace(String.format("test class [%s] parameter field + method count [%d]", testClass.getName(), count));

        // Validate parameter method + field count
        if (count == 0) {
            throw new TestClassConfigurationException(
                    String.format(
                            "Test class [%s] must declare a @TestEngine.Parameter field or @TestEngine.Parameter method",
                            testClass.getName()));
        }
    }

    private static Collection<Parameter> getTestParameters(Class<?> testClass) throws Throwable {
        Collection<Parameter> testParameters;

        // Get the parameter supplier methods
        Collection<Method> parameterSupplierMethods = TestEngineReflectionUtils.getParameterSupplierMethod(testClass);
        LOGGER.trace(String.format("test class [%s] parameter supplier method count [%d]", testClass.getName(), parameterSupplierMethods.size()));

        // Validate parameter supplier method count
        if (parameterSupplierMethods.isEmpty()) {
            throw new TestClassConfigurationException(
                    String.format(
                            "Test class [%s] must declare a @TestEngine.ParameterSupplier method",
                            testClass.getName()));
        }

        // Validate parameter supplier method count
        if (parameterSupplierMethods.size() > 1) {
            throw new TestClassConfigurationException(
                    String.format(
                            "Test class [%s] declares more than one @TestEngine.ParameterSupplier method",
                            testClass.getName()));
        }

        // Get parameters from the parameter supplier method
        try {
            Stream<Parameter> parameterSupplierMethodStream =
                    (Stream<Parameter>) parameterSupplierMethods
                            .stream()
                            .findFirst()
                            .get()
                            .invoke(null, (Object[]) null);

            if (parameterSupplierMethodStream == null) {
                throw new TestClassConfigurationException(
                        String.format(
                                "Test class [%s] @TestEngine.ParameterSupplier Stream is null",
                                testClass.getName()));
            }

            testParameters = parameterSupplierMethodStream.collect(Collectors.toList());
        } catch (ClassCastException e) {
            throw new TestClassConfigurationException(
                    String.format(
                            "Test class [%s] @TestEngine.ParameterSupplier method must return a Stream<Parameter>",
                            testClass.getName()),
                    e);
        }

        LOGGER.trace("test class parameter count [%d]", testParameters.size());

        // Validate we have test parameters
        if (testParameters.isEmpty()) {
            throw new TestClassConfigurationException(
                    String.format(
                            "Test class [%s] @TestEngine.ParameterSupplier Stream is empty",
                            testClass.getName()));
        }

        return testParameters;
    }
}
