/*
 * Copyright 2023 Douglas Hoard
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
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.descriptor.ClassTestDescriptor;
import org.antublue.test.engine.internal.descriptor.MethodTestDescriptor;
import org.antublue.test.engine.internal.descriptor.ParameterTestDescriptor;
import org.antublue.test.engine.internal.discovery.resolver.ClassSelectorResolver;
import org.antublue.test.engine.internal.discovery.resolver.ClasspathRootResolver;
import org.antublue.test.engine.internal.discovery.resolver.MethodSelectorResolver;
import org.antublue.test.engine.internal.discovery.resolver.PackageSelectorResolver;
import org.antublue.test.engine.internal.discovery.resolver.UniqueIdSelectorResolver;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.predicate.TestClassPredicate;
import org.antublue.test.engine.internal.predicate.TestClassTagPredicate;
import org.antublue.test.engine.internal.predicate.TestMethodPredicate;
import org.antublue.test.engine.internal.predicate.TestMethodTagPredicate;
import org.antublue.test.engine.internal.util.Cast;
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
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Class to implement code to discover tests / build test tree
 */
@SuppressWarnings("unchecked")
public class TestEngineDiscoveryRequestResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEngineDiscoveryRequestResolver.class);

    private final ClasspathRootResolver classpathRootResolver;
    private final PackageSelectorResolver packageSelectorResolver;
    private final ClassSelectorResolver classSelectorResolver;
    private final MethodSelectorResolver methodSelectorResolver;
    private final UniqueIdSelectorResolver uniqueIdSelectorResolver;

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
    public TestEngineDiscoveryRequestResolver() {
        classpathRootResolver = new ClasspathRootResolver();
        packageSelectorResolver = new PackageSelectorResolver();
        classSelectorResolver = new ClassSelectorResolver();
        methodSelectorResolver = new MethodSelectorResolver();
        uniqueIdSelectorResolver = new UniqueIdSelectorResolver();

        includeTestClassPredicate =
                TestEngineConfigurationParameters.getInstance()
                        .get(TestEngineConstants.TEST_CLASS_INCLUDE)
                        .map(value -> {
                            LOGGER.trace("%s [%s]", TestEngineConstants.TEST_CLASS_INCLUDE, value);
                            return value;
                        })
                        .map(TestClassPredicate::of)
                        .orElse(null);

        excludeTestClassPredicate =
                TestEngineConfigurationParameters.getInstance()
                        .get(TestEngineConstants.TEST_CLASS_EXCLUDE)
                        .map(value -> {
                            LOGGER.trace("%s [%s]", TestEngineConstants.TEST_CLASS_EXCLUDE, value);
                            return value;
                        })
                        .map(TestClassPredicate::of)
                        .orElse(null);

        includeTestMethodPredicate =
                TestEngineConfigurationParameters.getInstance()
                        .get(TestEngineConstants.TEST_METHOD_INCLUDE)
                        .map(value -> {
                            LOGGER.trace("%s [%s]", TestEngineConstants.TEST_METHOD_INCLUDE, value);
                            return value;
                        })
                        .map(TestMethodPredicate::of)
                        .orElse(null);

        excludeTestMethodPredicate =
                TestEngineConfigurationParameters.getInstance()
                        .get(TestEngineConstants.TEST_METHOD_EXCLUDE)
                        .map(value -> {
                            LOGGER.trace("%s [%s]", TestEngineConstants.TEST_METHOD_EXCLUDE, value);
                            return value;
                        })
                        .map(TestMethodPredicate::of)
                        .orElse(null);

        includeTestClassTagPredicate =
                TestEngineConfigurationParameters.getInstance()
                        .get(TestEngineConstants.TEST_CLASS_TAG_INCLUDE)
                        .map(value -> {
                            LOGGER.trace("%s [%s]", TestEngineConstants.TEST_CLASS_TAG_INCLUDE, value);
                            return value;
                        })
                        .map(TestClassTagPredicate::of)
                        .orElse(null);

        excludeTestClassTagPredicate =
                TestEngineConfigurationParameters.getInstance()
                        .get(TestEngineConstants.TEST_CLASS_TAG_EXCLUDE)
                        .map(value -> {
                            LOGGER.trace("%s [%s]", TestEngineConstants.TEST_CLASS_TAG_EXCLUDE, value);
                            return value;
                        })
                        .map(TestClassTagPredicate::of)
                        .orElse(null);

        includeTestMethodTagPredicate =
                TestEngineConfigurationParameters.getInstance()
                        .get(TestEngineConstants.TEST_METHOD_TAG_INCLUDE)
                        .map(value -> {
                            LOGGER.trace("%s [%s]", TestEngineConstants.TEST_METHOD_TAG_INCLUDE, value);
                            return value;
                        })
                        .map(TestMethodTagPredicate::of)
                        .orElse(null);

        excludeTestMethodTagPredicate =
                TestEngineConfigurationParameters.getInstance()
                        .get(TestEngineConstants.TEST_METHOD_TAG_EXCLUDE)
                        .map(value -> {
                            LOGGER.trace("%s [%s]", TestEngineConstants.TEST_METHOD_TAG_EXCLUDE, value);
                            return value;
                        })
                        .map(TestMethodTagPredicate::of)
                        .orElse(null);
    }

    /**
     * Method to process an EngineDiscoveryRequest
     *
     * @param engineDiscoveryRequest
     * @param engineDescriptor
     */
    public void resolve(EngineDiscoveryRequest engineDiscoveryRequest, EngineDescriptor engineDescriptor) {
        LOGGER.trace("resolve()");

        try {
            // Resolve selectors

            engineDiscoveryRequest
                    .getSelectorsByType(ClasspathRootSelector.class)
                    .stream()
                    .sorted(Comparator.comparing(o -> o.getClasspathRoot()))
                    .collect(Collectors.toList())
                    .forEach(classpathRootSelector ->
                            classpathRootResolver.resolve(classpathRootSelector, engineDescriptor));

            engineDiscoveryRequest
                    .getSelectorsByType(PackageSelector.class)
                    .stream()
                    .sorted(Comparator.comparing(o -> o.getPackageName()))
                    .collect(Collectors.toList())
                    .forEach(packageSelector -> packageSelectorResolver.resolve(packageSelector, engineDescriptor));

            engineDiscoveryRequest
                    .getSelectorsByType(ClassSelector.class)
                    .stream()
                    .sorted(Comparator.comparing(o -> o.getJavaClass().getName()))
                    .collect(Collectors.toList())
                    .forEach(classSelector -> classSelectorResolver.resolve(classSelector, engineDescriptor));

            engineDiscoveryRequest
                    .getSelectorsByType(MethodSelector.class)
                    .stream()
                    .sorted(Comparator.comparing(o -> o.getJavaMethod().getName()))
                    .collect(Collectors.toList())
                    .forEach(methodSelector -> methodSelectorResolver.resolve(methodSelector, engineDescriptor));

            engineDiscoveryRequest
                    .getSelectorsByType(UniqueIdSelector.class)
                    .stream()
                    .sorted(Comparator.comparing(o -> o.getUniqueId().toString()))
                    .collect(Collectors.toList())
                    .forEach(uniqueIdSelector -> uniqueIdSelectorResolver.resolve(uniqueIdSelector, engineDescriptor));

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
            LOGGER.trace(pad(indent) + "ClassTestDescriptor - > " + parentTestDescriptor.getUniqueId());
            Set<? extends TestDescriptor> testDescriptors = ((ClassTestDescriptor) parentTestDescriptor).getChildren();
            for (TestDescriptor childTestDescriptor : testDescriptors) {
                printTree(childTestDescriptor, indent + 2);
            }
        } else if (parentTestDescriptor instanceof ParameterTestDescriptor) {
            LOGGER.trace(pad(indent) + "ParameterTestDescriptor - > " + parentTestDescriptor.getUniqueId());
            Set<? extends TestDescriptor> testDescriptors = ((ParameterTestDescriptor) parentTestDescriptor).getChildren();
            for (TestDescriptor childTestDescriptor : testDescriptors) {
                printTree(childTestDescriptor, indent + 2);
            }
        } else  {
            LOGGER.trace(pad(indent) + "MethodTestDescriptor - > " + parentTestDescriptor.getUniqueId());
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
