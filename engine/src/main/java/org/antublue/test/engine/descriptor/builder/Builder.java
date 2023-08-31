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

package org.antublue.test.engine.descriptor.builder;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.antublue.test.engine.TestEngineUtils;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.configuration.Constants;
import org.antublue.test.engine.descriptor.ArgumentTestDescriptor;
import org.antublue.test.engine.descriptor.ClassTestDescriptor;
import org.antublue.test.engine.descriptor.MethodTestDescriptor;
import org.antublue.test.engine.descriptor.TestDescriptorUtils;
import org.antublue.test.engine.descriptor.builder.predicate.ClassNameFiltersPredicate;
import org.antublue.test.engine.descriptor.builder.predicate.PackageNameFiltersPredicate;
import org.antublue.test.engine.descriptor.builder.predicate.TestClassPredicate;
import org.antublue.test.engine.descriptor.builder.predicate.TestClassTagPredicate;
import org.antublue.test.engine.descriptor.builder.predicate.TestMethodPredicate;
import org.antublue.test.engine.descriptor.builder.predicate.TestMethodTagPredicate;
import org.antublue.test.engine.exception.TestEngineException;
import org.antublue.test.engine.logger.Logger;
import org.antublue.test.engine.logger.LoggerFactory;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassNameFilter;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.discovery.PackageNameFilter;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

/** Class to implement a resolver to build the test descriptor tree */
@SuppressWarnings("PMD.NPathComplexity")
public class Builder {

    private static final Logger LOGGER = LoggerFactory.getLogger(Builder.class);

    private static final TestEngineUtils TEST_ENGINE_REFLECTION_UTILS =
            TestEngineUtils.singleton();

    private TestClassPredicate includeTestClassPredicate;
    private TestClassPredicate excludeTestClassPredicate;
    private TestMethodPredicate includeTestMethodPredicate;
    private TestMethodPredicate excludeTestMethodPredicate;
    private TestClassTagPredicate includeTestClassTagPredicate;
    private TestClassTagPredicate excludeTestClassTagPredicate;
    private TestMethodTagPredicate includeTestMethodTagPredicate;
    private TestMethodTagPredicate excludeTestMethodTagPredicate;
    private ClassNameFiltersPredicate classNameFiltersPredicate;
    private PackageNameFiltersPredicate packageNameFiltersPredicate;
    private Map<Class<?>, Set<Method>> classMethodSetMap;

    private ConfigurationParameters configurationParameters;
    private EngineDiscoveryRequest engineDiscoveryRequest;
    private EngineDescriptor engineDescriptor;

    /**
     * Method to process test classes / test methods
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @param configurationParameters configurationParameters
     * @param engineDescriptor engineDescriptor
     */
    public void build(
            EngineDiscoveryRequest engineDiscoveryRequest,
            ConfigurationParameters configurationParameters,
            EngineDescriptor engineDescriptor) {
        this.engineDiscoveryRequest = engineDiscoveryRequest;
        this.configurationParameters = configurationParameters;
        this.engineDescriptor = engineDescriptor;

        configure();
        build();
        filter();
    }

    /** Method to configure the resolver */
    private void configure() {
        LOGGER.trace("configure");

        configureIncludeTestClassPredicate();
        configureExcludeTestClassPredicate();
        configureIncludeTestMethodPredicate();
        configureExcludeTestMethodPredicate();
        configureIncludeTestClassTagPredicate();
        configureExcludeTestClassTagPredicate();
        configureIncludeTestMethodTagPredicate();
        configureExcludeTestMethodTagPredicate();
        configureClassNameFiltersPredicate();
        configurePackageNameFiltersPredicate();
    }

    /** Method to configure the include test class Predicate */
    private void configureIncludeTestClassPredicate() {
        LOGGER.trace("configureIncludeTestClassPredicate");

        includeTestClassPredicate =
                configurationParameters
                        .get(Constants.TEST_CLASS_INCLUDE)
                        .map(
                                value -> {
                                    LOGGER.trace("%s [%s]", Constants.TEST_CLASS_INCLUDE, value);
                                    return value;
                                })
                        .map(TestClassPredicate::of)
                        .orElse(null);
    }

    /** Method to configure the exclude test class Predicate */
    private void configureExcludeTestClassPredicate() {
        LOGGER.trace("configureExcludeTestClassPredicate");

        excludeTestClassPredicate =
                configurationParameters
                        .get(Constants.TEST_CLASS_EXCLUDE)
                        .map(
                                value -> {
                                    LOGGER.trace("%s [%s]", Constants.TEST_CLASS_EXCLUDE, value);
                                    return value;
                                })
                        .map(TestClassPredicate::of)
                        .orElse(null);
    }

    /** Method to configure the include test method Predicate */
    private void configureIncludeTestMethodPredicate() {
        LOGGER.trace("configureIncludeTestMethodPredicate");

        includeTestMethodPredicate =
                configurationParameters
                        .get(Constants.TEST_METHOD_INCLUDE)
                        .map(
                                value -> {
                                    LOGGER.trace("%s [%s]", Constants.TEST_METHOD_INCLUDE, value);
                                    return value;
                                })
                        .map(TestMethodPredicate::of)
                        .orElse(null);
    }

    /** Method to configure the exclude test method Predicate */
    private void configureExcludeTestMethodPredicate() {
        LOGGER.trace("configureExcludeTestMethodPredicate");

        excludeTestMethodPredicate =
                configurationParameters
                        .get(Constants.TEST_METHOD_EXCLUDE)
                        .map(
                                value -> {
                                    LOGGER.trace("%s [%s]", Constants.TEST_METHOD_EXCLUDE, value);
                                    return value;
                                })
                        .map(TestMethodPredicate::of)
                        .orElse(null);
    }

    /** Method to configure the include test class tag Predicate */
    private void configureIncludeTestClassTagPredicate() {
        LOGGER.trace("configureIncludeTestClassTagPredicate");

        includeTestClassTagPredicate =
                configurationParameters
                        .get(Constants.TEST_CLASS_TAG_INCLUDE)
                        .map(
                                value -> {
                                    LOGGER.trace(
                                            "%s [%s]", Constants.TEST_CLASS_TAG_INCLUDE, value);
                                    return value;
                                })
                        .map(TestClassTagPredicate::of)
                        .orElse(null);
    }

    /** Method to configure the exclude test class tag Predicate */
    private void configureExcludeTestClassTagPredicate() {
        LOGGER.trace("configureExcludeTestClassTagPredicate");

        excludeTestClassTagPredicate =
                configurationParameters
                        .get(Constants.TEST_CLASS_TAG_EXCLUDE)
                        .map(
                                value -> {
                                    LOGGER.trace(
                                            "%s [%s]", Constants.TEST_CLASS_TAG_EXCLUDE, value);
                                    return value;
                                })
                        .map(TestClassTagPredicate::of)
                        .orElse(null);
    }

    /** Method to configure the include test method tag Predicate */
    private void configureIncludeTestMethodTagPredicate() {
        LOGGER.trace("configureIncludeTestMethodTagPredicate");

        includeTestMethodTagPredicate =
                configurationParameters
                        .get(Constants.TEST_METHOD_TAG_INCLUDE)
                        .map(
                                value -> {
                                    LOGGER.trace(
                                            "%s [%s]", Constants.TEST_METHOD_TAG_INCLUDE, value);
                                    return value;
                                })
                        .map(TestMethodTagPredicate::of)
                        .orElse(null);
    }

    /** Method to configure the exclude test method tag Predicate */
    private void configureExcludeTestMethodTagPredicate() {
        LOGGER.trace("configureExcludeTestMethodTagPredicate");

        excludeTestMethodTagPredicate =
                configurationParameters
                        .get(Constants.TEST_METHOD_TAG_EXCLUDE)
                        .map(
                                value -> {
                                    LOGGER.trace(
                                            "%s [%s]", Constants.TEST_METHOD_TAG_EXCLUDE, value);
                                    return value;
                                })
                        .map(TestMethodTagPredicate::of)
                        .orElse(null);
    }

    /** Method to configure the class name filters Predicate */
    private void configureClassNameFiltersPredicate() {
        LOGGER.trace("configureClassNameFiltersPredicate");

        List<ClassNameFilter> classNameFilters =
                engineDiscoveryRequest.getFiltersByType(ClassNameFilter.class);

        LOGGER.trace("classNameFilters count [%d]", classNameFilters.size());

        classNameFiltersPredicate = new ClassNameFiltersPredicate(classNameFilters);
    }

    /** Method to configure the package name filters Predicate */
    private void configurePackageNameFiltersPredicate() {
        LOGGER.trace("configurePackageNameFiltersPredicate");

        List<PackageNameFilter> packageNameFilters =
                engineDiscoveryRequest.getFiltersByType(PackageNameFilter.class);

        LOGGER.trace("packageNameFilters count [%d]", packageNameFilters.size());

        packageNameFiltersPredicate = new PackageNameFiltersPredicate(packageNameFilters);
    }

    /** Method to process selectors */
    private void build() {
        LOGGER.trace("process");

        classMethodSetMap = new LinkedHashMap<>();

        processClasspathRootSelectors();
        processPackageSelectors();
        processClassSelectors();
        processMethodSelectors();
        processUniqueIdSelectors();
    }

    /** Method to process ClasspathRootSelectors */
    private void processClasspathRootSelectors() {
        LOGGER.trace("processClasspathRootSelectors");

        engineDiscoveryRequest
                .getSelectorsByType(ClasspathRootSelector.class)
                .forEach(
                        classpathRootSelector -> {
                            TEST_ENGINE_REFLECTION_UTILS
                                    .findAllTestClasses(classpathRootSelector.getClasspathRoot())
                                    .forEach(
                                            clazz -> {
                                                if (packageNameFiltersPredicate.test(clazz)
                                                        && classNameFiltersPredicate.test(clazz)) {
                                                    classMethodSetMap.put(
                                                            clazz,
                                                            new LinkedHashSet<>(
                                                                    TEST_ENGINE_REFLECTION_UTILS
                                                                            .getTestMethods(
                                                                                    clazz)));
                                                }
                                            });
                        });
    }

    /** Method to process PackageSelector */
    private void processPackageSelectors() {
        LOGGER.trace("processPackageSelectors");

        engineDiscoveryRequest
                .getSelectorsByType(PackageSelector.class)
                .forEach(
                        packageSelector -> {
                            TEST_ENGINE_REFLECTION_UTILS
                                    .findAllTestClasses(packageSelector.getPackageName())
                                    .forEach(
                                            clazz -> {
                                                if (packageNameFiltersPredicate.test(clazz)
                                                        && classNameFiltersPredicate.test(clazz)) {
                                                    classMethodSetMap.put(
                                                            clazz,
                                                            new LinkedHashSet<>(
                                                                    TEST_ENGINE_REFLECTION_UTILS
                                                                            .getTestMethods(
                                                                                    clazz)));
                                                }
                                            });
                        });
    }

    /** Method to process ClassSelectors */
    private void processClassSelectors() {
        LOGGER.trace("processClassSelectors");

        engineDiscoveryRequest
                .getSelectorsByType(ClassSelector.class)
                .forEach(
                        classSelector -> {
                            Class<?> clazz = classSelector.getJavaClass();
                            if (packageNameFiltersPredicate.test(clazz)
                                    && classNameFiltersPredicate.test(clazz)
                                    && TEST_ENGINE_REFLECTION_UTILS.isTestClass(clazz)) {
                                classMethodSetMap.put(
                                        clazz,
                                        new LinkedHashSet<>(
                                                TEST_ENGINE_REFLECTION_UTILS.getTestMethods(
                                                        clazz)));
                            }
                        });
    }

    /** Method to process MethodSelectors */
    private void processMethodSelectors() {
        LOGGER.trace("processMethodSelectors");

        engineDiscoveryRequest
                .getSelectorsByType(MethodSelector.class)
                .forEach(
                        methodSelector -> {
                            Class<?> clazz = methodSelector.getJavaClass();
                            if (packageNameFiltersPredicate.test(clazz)
                                    && classNameFiltersPredicate.test(clazz)
                                    && TEST_ENGINE_REFLECTION_UTILS.isTestMethod(
                                            methodSelector.getJavaMethod())) {
                                Set<Method> methods =
                                        classMethodSetMap.getOrDefault(
                                                clazz, new LinkedHashSet<>());
                                methods.add(methodSelector.getJavaMethod());
                                classMethodSetMap.put(clazz, methods);
                            }
                        });
    }

    /** Method to process UniqueIdSelectors */
    private void processUniqueIdSelectors() {
        LOGGER.trace("processUniqueIdSelectors");

        engineDiscoveryRequest
                .getSelectorsByType(UniqueIdSelector.class)
                .forEach(
                        uniqueIdSelector -> {
                            UniqueId.Segment segment =
                                    uniqueIdSelector.getUniqueId().getLastSegment();
                            if ("class".equals(segment.getType())) {
                                String className = segment.getValue();
                                try {
                                    Class<?> clazz = Class.forName(className);
                                    if (packageNameFiltersPredicate.test(clazz)
                                            && classNameFiltersPredicate.test(clazz)
                                            && TEST_ENGINE_REFLECTION_UTILS.isTestClass(clazz)) {
                                        Set<Method> methods =
                                                classMethodSetMap.getOrDefault(
                                                        clazz, new LinkedHashSet<>());
                                        methods.addAll(
                                                TEST_ENGINE_REFLECTION_UTILS.getTestMethods(clazz));
                                        classMethodSetMap.put(clazz, methods);
                                    }
                                } catch (ClassNotFoundException e) {
                                    throw new TestEngineException(
                                            String.format(
                                                    "Exception loading class [%s]", className));
                                }
                            } else if ("argument".equals(segment.getType())) {
                                segment =
                                        uniqueIdSelector
                                                .getUniqueId()
                                                .removeLastSegment()
                                                .getLastSegment();
                                String className = segment.getValue();
                                try {
                                    Class<?> clazz = Class.forName(className);
                                    if (packageNameFiltersPredicate.test(clazz)
                                            && classNameFiltersPredicate.test(clazz)
                                            && TEST_ENGINE_REFLECTION_UTILS.isTestClass(clazz)) {
                                        Set<Method> methods =
                                                classMethodSetMap.getOrDefault(
                                                        clazz, new LinkedHashSet<>());
                                        methods.addAll(
                                                TEST_ENGINE_REFLECTION_UTILS.getTestMethods(clazz));
                                        classMethodSetMap.put(clazz, methods);
                                    }
                                } catch (ClassNotFoundException e) {
                                    throw new TestEngineException(
                                            String.format(
                                                    "Exception loading class [%s]", className));
                                }
                            }
                        });
    }

    /** Method to filters */
    private void filter() {
        LOGGER.trace("filter");

        Iterator<Map.Entry<Class<?>, Set<Method>>> classMethodMapEntryIterator =
                classMethodSetMap.entrySet().iterator();

        while (classMethodMapEntryIterator.hasNext()) {
            Map.Entry<Class<?>, Set<Method>> entry = classMethodMapEntryIterator.next();
            Class<?> clazz = entry.getKey();
            Set<Method> methods = entry.getValue();

            if (includeTestClassPredicate != null) {
                if (includeTestClassPredicate.test(clazz)) {
                    LOGGER.trace("includeTestClassPredicate class [%s] included", clazz.getName());
                } else {
                    LOGGER.trace("includeTestClassPredicate class [%s] excluded", clazz.getName());
                    classMethodMapEntryIterator.remove();
                    continue;
                }
            }

            if (excludeTestClassPredicate != null) {
                if (excludeTestClassPredicate.test(clazz)) {
                    LOGGER.trace("excludeTestClassPredicate class [%s] excluded", clazz.getName());
                    classMethodMapEntryIterator.remove();
                    continue;
                } else {
                    LOGGER.trace("excludeTestClassPredicate class [%s] included", clazz.getName());
                }
            }

            if (includeTestClassTagPredicate != null) {
                if (includeTestClassTagPredicate.test(clazz)) {
                    LOGGER.trace(
                            "includeTestClassTagPredicate class [%s] included", clazz.getName());
                } else {
                    LOGGER.trace(
                            "includeTestClassTagPredicate class [%s] excluded", clazz.getName());
                    classMethodMapEntryIterator.remove();
                    continue;
                }
            }

            if (excludeTestClassTagPredicate != null) {
                if (excludeTestClassTagPredicate.test(clazz)) {
                    LOGGER.trace(
                            "excludeTestClassTagPredicate class [%s] excluded", clazz.getName());
                    classMethodMapEntryIterator.remove();
                    continue;
                } else {
                    LOGGER.trace(
                            "excludeTestClassTagPredicate class [%s] included", clazz.getName());
                }
            }

            Iterator<Method> methodIterator = methods.iterator();
            while (methodIterator.hasNext()) {
                Method method = methodIterator.next();

                if (includeTestMethodPredicate != null) {
                    if (includeTestMethodPredicate.test(method)) {
                        LOGGER.trace(
                                "includeTestMethodPredicate class [%s] included", method.getName());
                    } else {
                        LOGGER.trace(
                                "includeTestMethodPredicate class [%s] excluded", method.getName());
                        methodIterator.remove();
                        continue;
                    }
                }

                if (excludeTestMethodPredicate != null) {
                    if (excludeTestMethodPredicate.test(method)) {
                        LOGGER.trace(
                                "excludeTestMethodPredicate class [%s] excluded", method.getName());
                        methodIterator.remove();
                        continue;
                    } else {
                        LOGGER.trace(
                                "excludeTestMethodPredicate class [%s] included", method.getName());
                    }
                }

                if (includeTestMethodTagPredicate != null) {
                    if (includeTestMethodTagPredicate.test(method)) {
                        LOGGER.trace(
                                "includeTestMethodTagPredicate class [%s] included",
                                method.getName());
                    } else {
                        LOGGER.trace(
                                "includeTestMethodTagPredicate class [%s] excluded",
                                method.getName());
                        methodIterator.remove();
                        continue;
                    }
                }

                if (excludeTestMethodTagPredicate != null) {
                    if (excludeTestMethodTagPredicate.test(method)) {
                        LOGGER.trace(
                                "excludeTestMethodTagPredicate class [%s] excluded",
                                method.getName());
                        methodIterator.remove();
                    } else {
                        LOGGER.trace(
                                "excludeTestMethodTagPredicate class [%s] included",
                                method.getName());
                    }
                }
            }

            if (methods.isEmpty()) {
                LOGGER.trace("class [%s] has no test methods, ignoring", clazz.getName());
                classMethodMapEntryIterator.remove();
                continue;
            }

            LOGGER.trace("building test descriptor tree for class [%s]", clazz.getName());

            UniqueId classTestDescritporUniqueId =
                    engineDescriptor.getUniqueId().append("class", clazz.getName());

            ClassTestDescriptor classTestDescriptor =
                    TestDescriptorUtils.createClassTestDescriptor(
                            classTestDescritporUniqueId, clazz);

            engineDescriptor.addChild(classTestDescriptor);

            List<Argument> arguments = TEST_ENGINE_REFLECTION_UTILS.getArguments(clazz);
            for (Argument argument : arguments) {
                UniqueId argumentUniqueId =
                        classTestDescritporUniqueId.append("argument", argument.name());

                ArgumentTestDescriptor argumentTestDescriptor =
                        TestDescriptorUtils.createArgumentTestDescriptor(
                                argumentUniqueId, clazz, argument);

                classTestDescriptor.addChild(argumentTestDescriptor);

                for (Method method : methods) {
                    UniqueId methodUniqueId = argumentUniqueId.append("method", method.getName());

                    MethodTestDescriptor methodTestDescriptor =
                            TestDescriptorUtils.createMethodTestDescriptor(
                                    methodUniqueId, clazz, method, argument);

                    argumentTestDescriptor.addChild(methodTestDescriptor);
                }
            }
        }

        TestDescriptorUtils.logTestDescriptorTree(engineDescriptor);
    }
}
