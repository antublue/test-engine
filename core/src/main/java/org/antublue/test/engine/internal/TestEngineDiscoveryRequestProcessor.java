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

package org.antublue.test.engine.internal;

import org.antublue.test.engine.TestEngineConstants;
import org.antublue.test.engine.api.Parameter;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.descriptor.ClassTestDescriptor;
import org.antublue.test.engine.internal.descriptor.ParameterTestDescriptor;
import org.antublue.test.engine.internal.descriptor.TestDescriptorFactory;
import org.antublue.test.engine.internal.descriptor.MethodTestDescriptor;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.predicate.TestClassPredicate;
import org.antublue.test.engine.internal.predicate.TestClassTagPredicate;
import org.antublue.test.engine.internal.predicate.TestMethodPredicate;
import org.antublue.test.engine.internal.predicate.TestMethodTagPredicate;
import org.antublue.test.engine.internal.util.Cast;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.discovery.PackageNameFilter;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Class to implement code to discover tests / build test tree
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
    private final TestMethodTagPredicate includeTestMethodTagPredicate;
    private final TestMethodTagPredicate excludeTestMethodTagPredicate;

    /**
     * Predicate to determine if a class is a test class (not abstract, has @TestEngine.Test methods)
     */
    private static final Predicate<Class<?>> IS_TEST_CLASS = clazz -> {
        if (clazz.isAnnotationPresent(TestEngine.BaseClass.class) || clazz.isAnnotationPresent(TestEngine.Disabled.class)) {
            return false;
        }

        int modifiers = clazz.getModifiers();
        return !Modifier.isAbstract(modifiers) && !TestEngineReflectionUtils.getTestMethods(clazz).isEmpty();
    };

    /**
     * Constructor
     */
    public TestEngineDiscoveryRequestProcessor() {
        includeTestClassPredicate =
                TestEngineConfigurationParameters.getInstance()
                        .get(TestEngineConstants.TEST_CLASS_INCLUDE)
                        .map(value -> {
                            LOGGER.trace(String.format("%s [%s]", TestEngineConstants.TEST_CLASS_INCLUDE, value));
                            return value;
                        })
                        .map(TestClassPredicate::of)
                        .orElse(null);

        excludeTestClassPredicate =
                TestEngineConfigurationParameters.getInstance()
                        .get(TestEngineConstants.TEST_CLASS_EXCLUDE)
                        .map(value -> {
                            LOGGER.trace(String.format("%s [%s]", TestEngineConstants.TEST_CLASS_EXCLUDE, value));
                            return value;
                        })
                        .map(TestClassPredicate::of)
                        .orElse(null);

        includeTestMethodPredicate =
                TestEngineConfigurationParameters.getInstance()
                        .get(TestEngineConstants.TEST_METHOD_INCLUDE)
                        .map(value -> {
                            LOGGER.trace(String.format("%s [%s]", TestEngineConstants.TEST_METHOD_INCLUDE, value));
                            return value;
                        })
                        .map(TestMethodPredicate::of)
                        .orElse(null);

        excludeTestMethodPredicate =
                TestEngineConfigurationParameters.getInstance()
                        .get(TestEngineConstants.TEST_METHOD_EXCLUDE)
                        .map(value -> {
                            LOGGER.trace(String.format("%s [%s]", TestEngineConstants.TEST_METHOD_EXCLUDE, value));
                            return value;
                        })
                        .map(TestMethodPredicate::of)
                        .orElse(null);

        includeTestClassTagPredicate =
                TestEngineConfigurationParameters.getInstance()
                        .get(TestEngineConstants.TEST_CLASS_TAG_INCLUDE)
                        .map(value -> {
                            LOGGER.trace(String.format("%s [%s]", TestEngineConstants.TEST_CLASS_TAG_INCLUDE, value));
                            return value;
                        })
                        .map(TestClassTagPredicate::of)
                        .orElse(null);

        excludeTestClassTagPredicate =
                TestEngineConfigurationParameters.getInstance()
                        .get(TestEngineConstants.TEST_CLASS_TAG_EXCLUDE)
                        .map(value -> {
                            LOGGER.trace(String.format("%s [%s]", TestEngineConstants.TEST_CLASS_TAG_EXCLUDE, value));
                            return value;
                        })
                        .map(TestClassTagPredicate::of)
                        .orElse(null);

        includeTestMethodTagPredicate =
                TestEngineConfigurationParameters.getInstance()
                        .get(TestEngineConstants.TEST_METHOD_TAG_INCLUDE)
                        .map(value -> {
                            LOGGER.trace(String.format("%s [%s]", TestEngineConstants.TEST_METHOD_TAG_INCLUDE, value));
                            return value;
                        })
                        .map(TestMethodTagPredicate::of)
                        .orElse(null);

        excludeTestMethodTagPredicate =
                TestEngineConfigurationParameters.getInstance()
                        .get(TestEngineConstants.TEST_METHOD_TAG_EXCLUDE)
                        .map(value -> {
                            LOGGER.trace(String.format("%s [%s]", TestEngineConstants.TEST_METHOD_TAG_EXCLUDE, value));
                            return value;
                        })
                        .map(TestMethodTagPredicate::of)
                        .orElse(null);
    }

    /**
     * Method to process the EngineDiscoveryRequest
     *
     * @param engineDiscoveryRequest
     * @param engineDescriptor
     */
    public void processEngineDiscoveryRequest(EngineDiscoveryRequest engineDiscoveryRequest, EngineDescriptor engineDescriptor) {
        LOGGER.trace("processEngineDiscoveryRequest()");

        try {
            // Process potential selectors
            processClasspathRootSelectors(engineDiscoveryRequest, engineDescriptor);
            processPackageSelectors(engineDiscoveryRequest, engineDescriptor);
            processClassSelectors(engineDiscoveryRequest, engineDescriptor);
            processMethodSelectors(engineDiscoveryRequest, engineDescriptor);
            processUniqueIdSelectors(engineDiscoveryRequest, engineDescriptor);

            // Filter based on package names
            processPackageNameFilters(engineDiscoveryRequest, engineDescriptor);

            // Filter test classes based on class/method predicate
            processTestClassPredicates(engineDescriptor);
            processTestMethodPredicates(engineDescriptor);

            // Filter test classes based on class/method tag predicates
            processTestClassTagPredicates(engineDescriptor);
            processTestMethodTagPredicates(engineDescriptor);

            if (LOGGER.isTraceEnabled()) {
                printTree(engineDescriptor);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            throw new TestEngineException("Exception processing engine discovery request", t);
        }
    }

    private void processClasspathRootSelectors(EngineDiscoveryRequest engineDiscoveryRequest, EngineDescriptor engineDescriptor) throws Throwable {
        LOGGER.trace("processClasspathRootSelectors");

        UniqueId uniqueId = engineDescriptor.getUniqueId();
        Collection<ClasspathRootSelector> classpathRootSelectors = engineDiscoveryRequest.getSelectorsByType(ClasspathRootSelector.class);
        LOGGER.trace("classpathRootSelectors.size() [%d]", classpathRootSelectors.size());

        for (ClasspathRootSelector classpathRootSelector : classpathRootSelectors) {
            URI uri = classpathRootSelector.getClasspathRoot();
            LOGGER.trace("uri [%s]", uri);

            List<Class<?>> classes = new ArrayList<>(ReflectionSupport.findAllClassesInClasspathRoot(uri, IS_TEST_CLASS, name -> true));
            classes.sort(Comparator.comparing(Class::getName));

            for (Class<?> clazz : classes) {
                LOGGER.trace(String.format("  class [%s]", clazz.getName()));

                uniqueId = uniqueId.append("class", clazz.getName());

                ClassTestDescriptor testEngineClassTestDescriptor =
                        TestDescriptorFactory.createTestEngineTestClassTestDescriptor(
                                uniqueId,
                                clazz);

                engineDescriptor.addChild(testEngineClassTestDescriptor);

                List<Parameter> parameters = TestEngineReflectionUtils.getParameters(clazz);
                for (int i = 0; i < parameters.size(); i++) {
                    Parameter parameter = parameters.get(i);
                    uniqueId = uniqueId.append("parameter", String.valueOf(i));

                    ParameterTestDescriptor testEngineParameterTestDescriptor =
                            TestDescriptorFactory.createTestEngineTestParameterTestDescriptor(
                                    uniqueId,
                                    clazz,
                                    parameter);

                    testEngineClassTestDescriptor.addChild(testEngineParameterTestDescriptor);

                    List<Method> methods = TestEngineReflectionUtils.getTestMethods(clazz);
                    for (Method method : methods) {
                        uniqueId = uniqueId.append("method", method.getName());

                        MethodTestDescriptor methodTestDescriptor =
                                TestDescriptorFactory.createTestEngineTestMethodTestDescriptor(
                                        uniqueId,
                                        clazz,
                                        parameter,
                                        method);

                        testEngineParameterTestDescriptor.addChild(methodTestDescriptor);

                        uniqueId = uniqueId.removeLastSegment();
                    }

                    testEngineParameterTestDescriptor.prune();
                    uniqueId = uniqueId.removeLastSegment();
                }

                testEngineClassTestDescriptor.prune();
                uniqueId = uniqueId.removeLastSegment();
            }
        }
    }

    /**
     * Method to process package selectors
     *
     * @param engineDiscoveryRequest
     * @param engineDescriptor
     */
    private void processPackageSelectors(EngineDiscoveryRequest engineDiscoveryRequest, EngineDescriptor engineDescriptor) throws Throwable {
        LOGGER.trace("processPackageSelectors");

        UniqueId uniqueId = engineDescriptor.getUniqueId();
        Collection<PackageSelector> packageSelectors = engineDiscoveryRequest.getSelectorsByType(PackageSelector.class);
        LOGGER.trace("packageSelectors.size() [%d]", packageSelectors.size());

        for (PackageSelector packageSelector : packageSelectors) {
            String packageName = packageSelector.getPackageName();

            List<Class<?>> classes = new ArrayList<>(ReflectionSupport.findAllClassesInPackage(packageName, IS_TEST_CLASS, name -> true));
            classes.sort(Comparator.comparing(Class::getName));

            for (Class<?> clazz : classes) {
                LOGGER.trace(String.format("  class [%s]", clazz.getName()));

                uniqueId = uniqueId.append("class", clazz.getName());

                ClassTestDescriptor testEngineClassTestDescriptor =
                        TestDescriptorFactory.createTestEngineTestClassTestDescriptor(
                                uniqueId,
                                clazz);

                engineDescriptor.addChild(testEngineClassTestDescriptor);

                List<Parameter> parameters = TestEngineReflectionUtils.getParameters(clazz);
                for (int i = 0; i < parameters.size(); i++) {
                    Parameter parameter = parameters.get(i);
                    uniqueId = uniqueId.append("parameter", String.valueOf(i));

                    ParameterTestDescriptor testEngineParameterTestDescriptor =
                            TestDescriptorFactory.createTestEngineTestParameterTestDescriptor(
                                    uniqueId,
                                    clazz,
                                    parameter);

                    testEngineClassTestDescriptor.addChild(testEngineParameterTestDescriptor);

                    List<Method> methods = TestEngineReflectionUtils.getTestMethods(clazz);
                    for (Method method : methods) {
                        uniqueId = uniqueId.append("method", method.getName());

                        MethodTestDescriptor methodTestDescriptor =
                                TestDescriptorFactory.createTestEngineTestMethodTestDescriptor(
                                        uniqueId,
                                        clazz,
                                        parameter,
                                        method);

                        testEngineParameterTestDescriptor.addChild(methodTestDescriptor);

                        uniqueId = uniqueId.removeLastSegment();
                    }

                    testEngineParameterTestDescriptor.prune();
                    uniqueId = uniqueId.removeLastSegment();
                }

                testEngineClassTestDescriptor.prune();
                uniqueId = uniqueId.removeLastSegment();
            }
        }
    }

    /**
     * Method to process class selectors
     *
     * @param engineDiscoveryRequest
     * @param engineDescriptor
     */
    private void processClassSelectors(EngineDiscoveryRequest engineDiscoveryRequest, EngineDescriptor engineDescriptor) throws Throwable {
        LOGGER.trace("processClassSelectors");

        UniqueId uniqueId = engineDescriptor.getUniqueId();
        Collection<ClassSelector> classSelectors = engineDiscoveryRequest.getSelectorsByType(ClassSelector.class);
        LOGGER.trace("classSelectors.size() [%d]", classSelectors.size());

        for (ClassSelector classSelector : classSelectors) {
            Class<?> clazz = classSelector.getJavaClass();
            LOGGER.trace(String.format("  class [%s]", clazz.getName()));

            uniqueId = uniqueId.append("class", clazz.getName());

            ClassTestDescriptor testEngineClassTestDescriptor =
                    TestDescriptorFactory.createTestEngineTestClassTestDescriptor(
                            uniqueId,
                            clazz);

            engineDescriptor.addChild(testEngineClassTestDescriptor);

            List<Parameter> parameters = TestEngineReflectionUtils.getParameters(clazz);
            for (int i = 0; i < parameters.size(); i++) {
                Parameter parameter = parameters.get(i);
                uniqueId = uniqueId.append("parameter", String.valueOf(i));

                ParameterTestDescriptor testEngineParameterTestDescriptor =
                        TestDescriptorFactory.createTestEngineTestParameterTestDescriptor(
                                uniqueId,
                                clazz,
                                parameter);

                testEngineClassTestDescriptor.addChild(testEngineParameterTestDescriptor);

                List<Method> methods = TestEngineReflectionUtils.getTestMethods(clazz);
                for (Method method : methods) {
                    uniqueId = uniqueId.append("method", method.getName());

                    MethodTestDescriptor methodTestDescriptor =
                            TestDescriptorFactory.createTestEngineTestMethodTestDescriptor(
                                    uniqueId,
                                    clazz,
                                    parameter,
                                    method);

                    testEngineParameterTestDescriptor.addChild(methodTestDescriptor);

                    uniqueId = uniqueId.removeLastSegment();
                }

                testEngineParameterTestDescriptor.prune();
                uniqueId = uniqueId.removeLastSegment();
            }
        }
    }

    private void processMethodSelectors(EngineDiscoveryRequest engineDiscoveryRequest, EngineDescriptor engineDescriptor) {
        LOGGER.trace("processMethodSelectors");

        UniqueId uniqueId = engineDescriptor.getUniqueId();
        LOGGER.trace("uniqueId [%s]", uniqueId);

        Collection<MethodSelector> methodSelectors = engineDiscoveryRequest.getSelectorsByType(MethodSelector.class);
        LOGGER.trace("methodSelectors.size() [%d]", methodSelectors.size());
        for (MethodSelector methodSelector : methodSelectors) {
            Class<?> clazz = methodSelector.getJavaClass();
            Method method = methodSelector.getJavaMethod();
            uniqueId = uniqueId.append("class", clazz.getName());

            ClassTestDescriptor testEngineClassTestDescriptor =
                    TestDescriptorFactory.createTestEngineTestClassTestDescriptor(
                            uniqueId,
                            clazz);

            List<Parameter> parameters = TestEngineReflectionUtils.getParameters(clazz);
            for (int i = 0; i < parameters.size(); i++) {
                Parameter parameter = parameters.get(i);
                uniqueId = uniqueId.append("parameter", String.valueOf(i));

                ParameterTestDescriptor testEngineParameterTestDescriptor =
                        TestDescriptorFactory.createTestEngineTestParameterTestDescriptor(
                                uniqueId,
                                clazz,
                                parameter);

                uniqueId = uniqueId.append("method", method.getName());

                MethodTestDescriptor methodTestDescriptor =
                        TestDescriptorFactory.createTestEngineTestMethodTestDescriptor(
                                uniqueId,
                                clazz,
                                parameter,
                                method);

                uniqueId = uniqueId.removeLastSegment();
                testEngineParameterTestDescriptor.addChild(methodTestDescriptor);

                uniqueId = uniqueId.removeLastSegment();
                testEngineClassTestDescriptor.addChild(testEngineParameterTestDescriptor);
            }

            uniqueId = uniqueId.removeLastSegment();

            engineDescriptor.addChild(testEngineClassTestDescriptor);
        }
    }

    /**
     * Method to process uniqueId selectors
     *
     * @param engineDiscoveryRequest
     * @param engineDescriptor
     */
    private void processUniqueIdSelectors(EngineDiscoveryRequest engineDiscoveryRequest, EngineDescriptor engineDescriptor) {
        LOGGER.info("processUniqueIdSelectors");

        String className = null;
        Map<UniqueId, ClassTestDescriptor> classTestDescriptorMap = new LinkedHashMap<>();

        try {
            Collection<UniqueIdSelector> uniqueIdSelectors = engineDiscoveryRequest.getSelectorsByType(UniqueIdSelector.class);
            LOGGER.trace("uniqueIdSelectors.size() [%d]", uniqueIdSelectors.size());

            for (UniqueIdSelector uniqueIdSelector : uniqueIdSelectors) {
                UniqueId selectorUniqueId = uniqueIdSelector.getUniqueId();
                LOGGER.info("selectorUniqueId [%s]", selectorUniqueId);

                UniqueId.Segment segment = selectorUniqueId.getLastSegment();

                if ("parameter".equals(segment.getType())) {
                    LOGGER.info("parameter [%s] selected", segment.getValue());

                    UniqueId classUniqueId = selectorUniqueId.removeLastSegment();
                    UniqueId.Segment classSegment = classUniqueId.getLastSegment();
                    className = classSegment.getValue();
                    LOGGER.info("className [%s]", className);

                    Class<?> clazz = Class.forName(className);

                    ClassTestDescriptor classTestDescriptor;
                    Optional<? extends TestDescriptor> optionalTestDescriptor = engineDescriptor.findByUniqueId(classUniqueId);
                    if (optionalTestDescriptor.isPresent()) {
                        classTestDescriptor = Cast.cast(optionalTestDescriptor.get());
                    } else {
                        classTestDescriptor =
                                TestDescriptorFactory.createTestEngineTestClassTestDescriptor(
                                        classUniqueId,
                                        clazz);
                    }

                    List<Parameter> parameters = TestEngineReflectionUtils.getParameters(clazz);
                    Parameter parameter = parameters.get(Integer.parseInt(segment.getValue()));

                    ParameterTestDescriptor parameterTestDescriptor;

                    optionalTestDescriptor = classTestDescriptor.findByUniqueId(selectorUniqueId);
                    if (optionalTestDescriptor.isPresent()) {
                        parameterTestDescriptor = Cast.cast(optionalTestDescriptor.get());
                    } else {
                        parameterTestDescriptor =
                                TestDescriptorFactory.createTestEngineTestParameterTestDescriptor(
                                        selectorUniqueId,
                                        clazz,
                                        parameter);
                    }

                    List<Method> methods = TestEngineReflectionUtils.getTestMethods(clazz);
                    for (Method method : methods) {
                        UniqueId methodUniqueId = selectorUniqueId.append("method", method.getName());

                        MethodTestDescriptor methodTestDescriptor =
                                TestDescriptorFactory.createTestEngineTestMethodTestDescriptor(
                                        methodUniqueId,
                                        clazz,
                                        parameter,
                                        method);

                        parameterTestDescriptor.addChild(methodTestDescriptor);
                    }

                    classTestDescriptor.addChild(parameterTestDescriptor);
                    engineDescriptor.addChild(classTestDescriptor);

                    /*
                    ParameterTestDescriptor testEngineParameterTestDescriptor =
                            TestDescriptorFactory.createTestEngineTestParameterTestDescriptor(
                                    uniqueId,
                                    clazz,
                                    parameter);
                     */
                }
                /*

                UniqueId parameterUniqueId = selectorUniqueId.removeLastSegment();
                LOGGER.info("parameterUniqueId [%s]", parameterUniqueId);

                UniqueId classUniqueId = parameterUniqueId.removeLastSegment();
                LOGGER.info("classUniqueId [%s]", classUniqueId);

                className = classUniqueId.getLastSegment().getValue();
                LOGGER.info("className [%s]", className);

                /*
                className = clazzSegment.getValue();
                Class clazz = Class.forName(className);

                UniqueId.Segment parameterSegment = uniqueId.removeLastSegment().getLastSegment();
                LOGGER.info("parameterSegment [%s]", parameterSegment.getValue());
                int parameterIndex = Integer.parseInt(parameterSegment.getValue());

                ClassTestDescriptor testEngineClassTestDescriptor =
                        TestDescriptorFactory.createTestEngineTestClassTestDescriptor(
                                uniqueId.removeLastSegment(),
                                clazz);

                List<Parameter> parameters = TestEngineReflectionUtils.getParameters(clazz);
                Parameter parameter = parameters.get(parameterIndex);

                ParameterTestDescriptor testEngineParameterTestDescriptor =
                        TestDescriptorFactory.createTestEngineTestParameterTestDescriptor(
                                uniqueId,
                                clazz,
                                parameter);

                List<Method> methods = TestEngineReflectionUtils.getTestMethods(clazz);
                for (Method method : methods) {
                    MethodTestDescriptor methodTestDescriptor =
                            TestDescriptorFactory.createTestEngineTestMethodTestDescriptor(
                                    uniqueId.append("method", method.getName()),
                                    clazz,
                                    parameter,
                                    method);

                    testEngineParameterTestDescriptor.addChild(methodTestDescriptor);
                }

                testEngineClassTestDescriptor.addChild(testEngineParameterTestDescriptor);
                engineDescriptor.addChild(testEngineClassTestDescriptor);
                */
            }
        } catch (ClassNotFoundException e) {
            throw new TestEngineException(
                    String.format("Class [%s] not found", className),
                    e);
        }
    }

    /**
     * Method to process package name filters
     *
     * @param engineDiscoveryRequest
     * @param engineDescriptor
     */
    private void processPackageNameFilters(EngineDiscoveryRequest engineDiscoveryRequest, EngineDescriptor engineDescriptor) {
        LOGGER.trace("processPackageNameFilters");

        List<? extends PackageNameFilter> packageNameFilters = engineDiscoveryRequest.getFiltersByType(PackageNameFilter.class);
        LOGGER.trace("packageNameFilters size [%d]", packageNameFilters.size());
        for (PackageNameFilter packageNameFilter : packageNameFilters) {
            Set<? extends TestDescriptor> testDescriptors = new LinkedHashSet<>(engineDescriptor.getChildren());
            for (TestDescriptor testDescriptor : testDescriptors) {
                ClassTestDescriptor testEngineClassTestDescriptor = Cast.cast(testDescriptor);
                Set<? extends TestDescriptor> testDescriptors2 = new LinkedHashSet<>(testEngineClassTestDescriptor.getChildren());
                for (TestDescriptor testDescriptor2 : testDescriptors2) {
                    ParameterTestDescriptor testEngineParameterTestDescriptor = Cast.cast(testDescriptor2);
                    Set<? extends TestDescriptor> testDescriptors3 = new LinkedHashSet<>(testDescriptor2.getChildren());
                    for (TestDescriptor testDescriptor3 : testDescriptors3) {
                        MethodTestDescriptor methodTestDescriptor = Cast.cast(testDescriptor3);
                        Class<?> clazz = methodTestDescriptor.getTestClass();
                        String className = clazz.getName();
                        if (packageNameFilter.apply(className).excluded()) {
                            methodTestDescriptor.removeFromHierarchy();
                        }
                    }
                    Class<?> clazz = testEngineParameterTestDescriptor.getTestClass();
                    String className = clazz.getName();
                    if (packageNameFilter.apply(className).excluded()) {
                        testEngineParameterTestDescriptor.removeFromHierarchy();
                    }
                }
                Class<?> clazz = testEngineClassTestDescriptor.getTestClass();
                String className = clazz.getName();
                if (packageNameFilter.apply(className).excluded()) {
                    testEngineClassTestDescriptor.removeFromHierarchy();
                }
            }
        }

        engineDescriptor.prune();
    }

    /**
     * Method to process test class predicates
     *
     * @param engineDescriptor
     */
    private void processTestClassPredicates(EngineDescriptor engineDescriptor) {
        LOGGER.trace("processTestClassPredicates");

        if (includeTestClassPredicate != null) {
            LOGGER.trace("includeTestClassPredicate [%s]", includeTestClassPredicate.getRegex());

            Set<? extends TestDescriptor> children = new LinkedHashSet<>(engineDescriptor.getChildren());
            for (TestDescriptor child : children) {
                if (child instanceof ClassTestDescriptor) {
                    ClassTestDescriptor testEngineClassTestDescriptor = Cast.cast(child);
                    UniqueId uniqueId = testEngineClassTestDescriptor.getUniqueId();
                    Class<?> clazz = testEngineClassTestDescriptor.getTestClass();

                    if (includeTestClassPredicate.test(clazz)) {
                        LOGGER.trace("  accept [%s]", uniqueId);
                    } else {
                        LOGGER.trace("  prune  [%s]", uniqueId);
                        testEngineClassTestDescriptor.removeFromHierarchy();
                    }
                }
            }
        }

        if (excludeTestClassPredicate != null) {
            LOGGER.trace("excludeTestClassPredicate [%s]", excludeTestClassPredicate.getRegex());

            Set<? extends TestDescriptor> children = new LinkedHashSet<>(engineDescriptor.getChildren());
            for (TestDescriptor child : children) {
                if (child instanceof ClassTestDescriptor) {
                    ClassTestDescriptor testEngineClassTestDescriptor = Cast.cast(child);
                    UniqueId uniqueId = testEngineClassTestDescriptor.getUniqueId();
                    Class<?> clazz = testEngineClassTestDescriptor.getTestClass();

                    if (excludeTestClassPredicate.test(clazz)) {
                        LOGGER.trace("  prune  [%s]", uniqueId);
                        testEngineClassTestDescriptor.removeFromHierarchy();
                    } else {
                        LOGGER.trace("  accept [%s]", uniqueId);
                    }
                }
            }
        }
    }

    /**
     * Method to process test method predicates
     *
     * @param engineDescriptor
     */
    private void processTestMethodPredicates(EngineDescriptor engineDescriptor) {
        LOGGER.trace("processTestMethodPredicates");

        if (includeTestMethodPredicate != null) {
            LOGGER.trace("includeTestMethodPredicate [%s]", includeTestMethodPredicate.getRegex());

            Set<? extends TestDescriptor> children = engineDescriptor.getChildren();
            for (TestDescriptor child : children) {
                if (child instanceof ClassTestDescriptor) {
                    Set<? extends TestDescriptor> grandChildren = child.getChildren();
                    for (TestDescriptor grandChild : grandChildren) {
                        if (grandChild instanceof ParameterTestDescriptor) {
                            Set<? extends TestDescriptor> greatGrandChildren = new LinkedHashSet<>(grandChild.getChildren());
                            for (TestDescriptor greatGrandChild : greatGrandChildren) {
                                if (greatGrandChild instanceof MethodTestDescriptor) {
                                    MethodTestDescriptor methodTestDescriptor = Cast.cast(greatGrandChild);
                                    UniqueId uniqueId = methodTestDescriptor.getUniqueId();
                                    Method method = methodTestDescriptor.getTestMethod();

                                    if (includeTestMethodPredicate.test(method)) {
                                        LOGGER.trace("  accept [%s]", uniqueId);
                                    } else {
                                        LOGGER.trace("  prune  [%s]", uniqueId);
                                        methodTestDescriptor.removeFromHierarchy();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (excludeTestMethodPredicate != null) {
            LOGGER.trace("excludeTestMethodPredicate [%s]", excludeTestMethodPredicate.getRegex());

            Set<? extends TestDescriptor> children = engineDescriptor.getChildren();
            for (TestDescriptor child : children) {
                if (child instanceof ClassTestDescriptor) {
                    Set<? extends TestDescriptor> grandChildren = child.getChildren();
                    for (TestDescriptor grandChild : grandChildren) {
                        if (grandChild instanceof ParameterTestDescriptor) {
                            Set<? extends TestDescriptor> greatGrandChildren = new LinkedHashSet<>(grandChild.getChildren());
                            for (TestDescriptor greatGrandChild : greatGrandChildren) {
                                if (greatGrandChild instanceof MethodTestDescriptor) {
                                    MethodTestDescriptor methodTestDescriptor = Cast.cast(greatGrandChild);
                                    UniqueId uniqueId = methodTestDescriptor.getUniqueId();
                                    Method method = methodTestDescriptor.getTestMethod();

                                    if (excludeTestMethodPredicate.test(method)) {
                                        LOGGER.trace("  prune  [%s]", uniqueId);
                                        methodTestDescriptor.removeFromHierarchy();
                                    } else {
                                        LOGGER.trace("  accept [%s]", uniqueId);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Method to process test class tag predicates
     *
     * @param engineDescriptor
     */
    private void processTestClassTagPredicates(EngineDescriptor engineDescriptor) {
        LOGGER.trace("processTestClassTagPredicates");

        if (includeTestClassTagPredicate != null) {
            LOGGER.trace("includeTestClassTagPredicate [%s]", includeTestClassTagPredicate.getRegex());

            Set<? extends TestDescriptor> children = new LinkedHashSet<>(engineDescriptor.getChildren());
            for (TestDescriptor child : children) {
                if (child instanceof ClassTestDescriptor) {
                    ClassTestDescriptor testEngineClassTestDescriptor = Cast.cast(child);
                    UniqueId uniqueId = testEngineClassTestDescriptor.getUniqueId();
                    Class<?> clazz = testEngineClassTestDescriptor.getTestClass();

                    if (includeTestClassTagPredicate.test(clazz)) {
                        LOGGER.trace("  accept [%s]", uniqueId);
                    } else {
                        LOGGER.trace("  prune  [%s]", uniqueId);
                        testEngineClassTestDescriptor.removeFromHierarchy();
                    }
                }
            }
        }

        if (excludeTestClassTagPredicate != null) {
            LOGGER.trace("excludeTestClassTagPredicate [%s]", excludeTestClassTagPredicate.getRegex());

            Set<? extends TestDescriptor> children = new LinkedHashSet<>(engineDescriptor.getChildren());
            for (TestDescriptor child : children) {
                if (child instanceof ClassTestDescriptor) {
                    ClassTestDescriptor testEngineClassTestDescriptor = Cast.cast(child);
                    UniqueId uniqueId = testEngineClassTestDescriptor.getUniqueId();
                    Class<?> clazz = testEngineClassTestDescriptor.getTestClass();

                    if (excludeTestClassTagPredicate.test(clazz)) {
                        LOGGER.trace("  prune  [%s]", uniqueId);
                        testEngineClassTestDescriptor.removeFromHierarchy();
                    } else {
                        LOGGER.trace("  accept [%s]", uniqueId);
                    }
                }
            }
        }
    }

    /**
     * Method to process test method tag predicates
     *
     * @param engineDescriptor
     */
    private void processTestMethodTagPredicates(EngineDescriptor engineDescriptor) {
        LOGGER.trace("processTestMethodTagPredicates");

        if (includeTestMethodTagPredicate != null) {
            LOGGER.trace("includeTestMethodTagPredicate [%s]", includeTestMethodTagPredicate.getRegex());

            Set<? extends TestDescriptor> children = engineDescriptor.getChildren();
            for (TestDescriptor child : children) {
                if (child instanceof ClassTestDescriptor) {
                    Set<? extends TestDescriptor> grandChildren = child.getChildren();
                    for (TestDescriptor grandChild : grandChildren) {
                        if (grandChild instanceof ParameterTestDescriptor) {
                            Set<? extends TestDescriptor> greatGrandChildren = new LinkedHashSet<>(grandChild.getChildren());
                            for (TestDescriptor greatGrandChild : greatGrandChildren) {
                                if (greatGrandChild instanceof MethodTestDescriptor) {
                                    MethodTestDescriptor methodTestDescriptor = Cast.cast(greatGrandChild);
                                    UniqueId uniqueId = methodTestDescriptor.getUniqueId();
                                    Method method = methodTestDescriptor.getTestMethod();

                                    if (includeTestMethodTagPredicate.test(method)) {
                                        LOGGER.trace("  accept [%s]", uniqueId);
                                    } else {
                                        LOGGER.trace("  prune  [%s]", uniqueId);
                                        methodTestDescriptor.removeFromHierarchy();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (excludeTestMethodTagPredicate != null) {
            LOGGER.trace("excludeTestMethodTagPredicate [%s]", excludeTestMethodTagPredicate.getRegex());

            Set<? extends TestDescriptor> children = engineDescriptor.getChildren();
            for (TestDescriptor child : children) {
                if (child instanceof ClassTestDescriptor) {
                    Set<? extends TestDescriptor> grandChildren = child.getChildren();
                    for (TestDescriptor grandChild : grandChildren) {
                        if (grandChild instanceof ParameterTestDescriptor) {
                            Set<? extends TestDescriptor> greatGrandChildren = new LinkedHashSet<>(grandChild.getChildren());
                            for (TestDescriptor greatGrandChild : greatGrandChildren) {
                                if (greatGrandChild instanceof MethodTestDescriptor) {
                                    MethodTestDescriptor methodTestDescriptor = Cast.cast(greatGrandChild);
                                    UniqueId uniqueId = methodTestDescriptor.getUniqueId();
                                    Method method = methodTestDescriptor.getTestMethod();

                                    if (excludeTestMethodTagPredicate.test(method)) {
                                        LOGGER.trace("  prune  [%s]", uniqueId);
                                        methodTestDescriptor.removeFromHierarchy();
                                    } else {
                                        LOGGER.trace("  accept [%s]", uniqueId);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Method to print the test tree
     *
     * @param engineDescriptor
     */
    private static void printTree(EngineDescriptor engineDescriptor) {
        LOGGER.trace("EngineDescriptor - > " + engineDescriptor.getUniqueId());
        Set<? extends TestDescriptor> testDescriptors = engineDescriptor.getChildren();
        for (TestDescriptor testDescriptor : testDescriptors) {
            printTree(testDescriptor, 2);
        }
    }

    /**
     * Method to print a test descriptor
     *
     * @param parentTestDescriptor
     * @param indent
     */
    private static void printTree(TestDescriptor parentTestDescriptor, int indent) {
        if (parentTestDescriptor instanceof ClassTestDescriptor) {
            LOGGER.trace(pad(indent) + "TestEngineClassTestDescriptor - > " + parentTestDescriptor.getUniqueId());
            Set<? extends TestDescriptor> testDescriptors = ((ClassTestDescriptor) parentTestDescriptor).getChildren();
            for (TestDescriptor childTestDescriptor : testDescriptors) {
                printTree(childTestDescriptor, indent + 2);
            }
        } else if (parentTestDescriptor instanceof ParameterTestDescriptor) {
            LOGGER.trace(pad(indent) + "TestEngineParameterTestDescriptor - > " + parentTestDescriptor.getUniqueId());
            Set<? extends TestDescriptor> testDescriptors = ((ParameterTestDescriptor) parentTestDescriptor).getChildren();
            for (TestDescriptor childTestDescriptor : testDescriptors) {
                printTree(childTestDescriptor, indent + 2);
            }
        } else  {
            LOGGER.trace(pad(indent) + "TestEngineTestMethodTestDescriptor - > " + parentTestDescriptor.getUniqueId());
        }
    }

    /**
     * Method to left pad a string with spaces
     *
     * @param length
     * @return
     */
    private static String pad(int length) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }
}
