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

package org.antublue.test.engine.internal.discovery;

import org.antublue.test.engine.TestEngineConstants;
import org.antublue.test.engine.internal.TestEngineConfiguration;
import org.antublue.test.engine.internal.descriptor.ArgumentTestDescriptor;
import org.antublue.test.engine.internal.descriptor.ClassTestDescriptor;
import org.antublue.test.engine.internal.descriptor.MethodTestDescriptor;
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
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class to implement code to discover tests / build test descriptor tree
 * <br>
 * The current paradigm is to build a complete test descriptor tree,
 * then remove filter (remove) test descriptor
 */
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
     * Constructor
     */
    public TestEngineDiscoveryRequestResolver() {
        classpathRootResolver = new ClasspathRootResolver();
        packageSelectorResolver = new PackageSelectorResolver();
        classSelectorResolver = new ClassSelectorResolver();
        methodSelectorResolver = new MethodSelectorResolver();
        uniqueIdSelectorResolver = new UniqueIdSelectorResolver();

        includeTestClassPredicate =
                TestEngineConfiguration
                        .getInstance()
                        .get(TestEngineConstants.TEST_CLASS_INCLUDE)
                        .map(value -> {
                            LOGGER.trace("%s [%s]", TestEngineConstants.TEST_CLASS_INCLUDE, value);
                            return value;
                        })
                        .map(TestClassPredicate::of)
                        .orElse(null);

        excludeTestClassPredicate =
                TestEngineConfiguration
                        .getInstance()
                        .get(TestEngineConstants.TEST_CLASS_EXCLUDE)
                        .map(value -> {
                            LOGGER.trace("%s [%s]", TestEngineConstants.TEST_CLASS_EXCLUDE, value);
                            return value;
                        })
                        .map(TestClassPredicate::of)
                        .orElse(null);

        includeTestMethodPredicate =
                TestEngineConfiguration
                        .getInstance()
                        .get(TestEngineConstants.TEST_METHOD_INCLUDE)
                        .map(value -> {
                            LOGGER.trace("%s [%s]", TestEngineConstants.TEST_METHOD_INCLUDE, value);
                            return value;
                        })
                        .map(TestMethodPredicate::of)
                        .orElse(null);

        excludeTestMethodPredicate =
                TestEngineConfiguration
                        .getInstance()
                        .get(TestEngineConstants.TEST_METHOD_EXCLUDE)
                        .map(value -> {
                            LOGGER.trace("%s [%s]", TestEngineConstants.TEST_METHOD_EXCLUDE, value);
                            return value;
                        })
                        .map(TestMethodPredicate::of)
                        .orElse(null);

        includeTestClassTagPredicate =
                TestEngineConfiguration
                        .getInstance()
                        .get(TestEngineConstants.TEST_CLASS_TAG_INCLUDE)
                        .map(value -> {
                            LOGGER.trace("%s [%s]", TestEngineConstants.TEST_CLASS_TAG_INCLUDE, value);
                            return value;
                        })
                        .map(TestClassTagPredicate::of)
                        .orElse(null);

        excludeTestClassTagPredicate =
                TestEngineConfiguration
                        .getInstance()
                        .get(TestEngineConstants.TEST_CLASS_TAG_EXCLUDE)
                        .map(value -> {
                            LOGGER.trace("%s [%s]", TestEngineConstants.TEST_CLASS_TAG_EXCLUDE, value);
                            return value;
                        })
                        .map(TestClassTagPredicate::of)
                        .orElse(null);

        includeTestMethodTagPredicate =
                TestEngineConfiguration
                        .getInstance()
                        .get(TestEngineConstants.TEST_METHOD_TAG_INCLUDE)
                        .map(value -> {
                            LOGGER.trace("%s [%s]", TestEngineConstants.TEST_METHOD_TAG_INCLUDE, value);
                            return value;
                        })
                        .map(TestMethodTagPredicate::of)
                        .orElse(null);

        excludeTestMethodTagPredicate =
                TestEngineConfiguration
                        .getInstance()
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
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @param engineDescriptor engineDescriptor
     */
    public void resolve(EngineDiscoveryRequest engineDiscoveryRequest, EngineDescriptor engineDescriptor) {
        LOGGER.trace("resolve(EngineDiscoveryRequest, EngineDescriptor)");

        // Resolve selectors

        engineDiscoveryRequest
                .getSelectorsByType(ClasspathRootSelector.class)
                .stream()
                .sorted(Comparator.comparing(ClasspathRootSelector::getClasspathRoot))
                .collect(Collectors.toList())
                .forEach(classpathRootSelector ->
                        classpathRootResolver.resolve(
                                engineDiscoveryRequest, engineDescriptor, classpathRootSelector));

        engineDiscoveryRequest
                .getSelectorsByType(PackageSelector.class)
                .stream()
                .sorted(Comparator.comparing(PackageSelector::getPackageName))
                .collect(Collectors.toList())
                .forEach(packageSelector ->
                        packageSelectorResolver.resolve(engineDiscoveryRequest, engineDescriptor, packageSelector));

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

        /*
         * TODO refactor code to use a visitor pattern to apply
         *      the predicate filters or possibly do it during the resolution phase
         */

        // Filter test classes based on class/method predicate
        processTestClassPredicates(engineDescriptor);
        processTestMethodPredicates(engineDescriptor);

        // Filter test classes based on class/method tag predicates
        processTestClassTagPredicates(engineDescriptor);
        processTestMethodTagPredicates(engineDescriptor);
    }

    /**
     * Method to process test class predicates
     *
     * @param engineDescriptor engineDescriptor
     */
    private void processTestClassPredicates(EngineDescriptor engineDescriptor) {
        LOGGER.trace("processTestClassPredicates");

        if (includeTestClassPredicate != null) {
            LOGGER.trace("includeTestClassPredicate [%s]", includeTestClassPredicate.getRegex());
            // TODO refactor to use forEach
            Set<? extends TestDescriptor> children = new LinkedHashSet<>(engineDescriptor.getChildren());
            for (TestDescriptor child : children) {
                if (child instanceof ClassTestDescriptor) {
                    ClassTestDescriptor classTestDescriptor = Cast.cast(child);
                    UniqueId classTestDescriptorUniqueId = classTestDescriptor.getUniqueId();
                    Class<?> clazz = classTestDescriptor.getTestClass();
                    if (includeTestClassPredicate.test(clazz)) {
                        LOGGER.trace("  accept [%s]", classTestDescriptorUniqueId);
                    } else {
                        LOGGER.trace("  prune  [%s]", classTestDescriptorUniqueId);
                        classTestDescriptor.removeFromHierarchy();
                    }
                }
            }
        }

        if (excludeTestClassPredicate != null) {
            LOGGER.trace("excludeTestClassPredicate [%s]", excludeTestClassPredicate.getRegex());
            // TODO refactor to use forEach
            Set<? extends TestDescriptor> children = new LinkedHashSet<>(engineDescriptor.getChildren());
            for (TestDescriptor child : children) {
                if (child instanceof ClassTestDescriptor) {
                    ClassTestDescriptor classTestDescriptor = Cast.cast(child);
                    UniqueId classTestDescriptorUniqueId = classTestDescriptor.getUniqueId();
                    Class<?> clazz = classTestDescriptor.getTestClass();
                    if (excludeTestClassPredicate.test(clazz)) {
                        LOGGER.trace("  prune  [%s]", classTestDescriptorUniqueId);
                        classTestDescriptor.removeFromHierarchy();
                    } else {
                        LOGGER.trace("  accept [%s]", classTestDescriptorUniqueId);
                    }
                }
            }
        }
    }

    /**
     * Method to process test method predicates
     *
     * @param engineDescriptor engineDescriptor
     */
    private void processTestMethodPredicates(EngineDescriptor engineDescriptor) {
        LOGGER.trace("processTestMethodPredicates");

        if (includeTestMethodPredicate != null) {
            LOGGER.trace("includeTestMethodPredicate [%s]", includeTestMethodPredicate.getRegex());
            // TODO refactor to use forEach
            Set<? extends TestDescriptor> children = engineDescriptor.getChildren();
            for (TestDescriptor child : children) {
                if (child instanceof ClassTestDescriptor) {
                    Set<? extends TestDescriptor> grandChildren = child.getChildren();
                    for (TestDescriptor grandChild : grandChildren) {
                        if (grandChild instanceof ArgumentTestDescriptor) {
                            Set<? extends TestDescriptor> greatGrandChildren = new LinkedHashSet<>(grandChild.getChildren());
                            for (TestDescriptor greatGrandChild : greatGrandChildren) {
                                if (greatGrandChild instanceof MethodTestDescriptor) {
                                    MethodTestDescriptor methodTestDescriptor = Cast.cast(greatGrandChild);
                                    UniqueId methodTestDescriptorUniqueId = methodTestDescriptor.getUniqueId();
                                    Method method = methodTestDescriptor.getTestMethod();
                                    if (includeTestMethodPredicate.test(method)) {
                                        LOGGER.trace("  accept [%s]", methodTestDescriptorUniqueId);
                                    } else {
                                        LOGGER.trace("  prune  [%s]", methodTestDescriptorUniqueId);
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
            // TODO refactor to use forEach
            Set<? extends TestDescriptor> children = engineDescriptor.getChildren();
            for (TestDescriptor child : children) {
                if (child instanceof ClassTestDescriptor) {
                    Set<? extends TestDescriptor> grandChildren = child.getChildren();
                    for (TestDescriptor grandChild : grandChildren) {
                        if (grandChild instanceof ArgumentTestDescriptor) {
                            Set<? extends TestDescriptor> greatGrandChildren = new LinkedHashSet<>(grandChild.getChildren());
                            for (TestDescriptor greatGrandChild : greatGrandChildren) {
                                if (greatGrandChild instanceof MethodTestDescriptor) {
                                    MethodTestDescriptor methodTestDescriptor = Cast.cast(greatGrandChild);
                                    UniqueId methodTestDescriptorUniqueId = methodTestDescriptor.getUniqueId();
                                    Method method = methodTestDescriptor.getTestMethod();
                                    if (excludeTestMethodPredicate.test(method)) {
                                        LOGGER.trace("  prune  [%s]", methodTestDescriptorUniqueId);
                                        methodTestDescriptor.removeFromHierarchy();
                                    } else {
                                        LOGGER.trace("  accept [%s]", methodTestDescriptorUniqueId);
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
     * @param engineDescriptor engineDescriptor
     */
    private void processTestClassTagPredicates(EngineDescriptor engineDescriptor) {
        LOGGER.trace("processTestClassTagPredicates");

        if (includeTestClassTagPredicate != null) {
            LOGGER.trace("includeTestClassTagPredicate [%s]", includeTestClassTagPredicate.getRegex());
            Set<? extends TestDescriptor> children = new LinkedHashSet<>(engineDescriptor.getChildren());
            // TODO refactor to use forEach
            for (TestDescriptor child : children) {
                if (child instanceof ClassTestDescriptor) {
                    ClassTestDescriptor testEngineClassTestDescriptor = Cast.cast(child);
                    UniqueId classTestDescriptorUniqueId = testEngineClassTestDescriptor.getUniqueId();
                    Class<?> clazz = testEngineClassTestDescriptor.getTestClass();
                    if (includeTestClassTagPredicate.test(clazz)) {
                        LOGGER.trace("  accept [%s]", classTestDescriptorUniqueId);
                    } else {
                        LOGGER.trace("  prune  [%s]", classTestDescriptorUniqueId);
                        testEngineClassTestDescriptor.removeFromHierarchy();
                    }
                }
            }
        }

        if (excludeTestClassTagPredicate != null) {
            LOGGER.trace("excludeTestClassTagPredicate [%s]", excludeTestClassTagPredicate.getRegex());
            // TODO refactor to use forEach
            Set<? extends TestDescriptor> children = new LinkedHashSet<>(engineDescriptor.getChildren());
            for (TestDescriptor child : children) {
                if (child instanceof ClassTestDescriptor) {
                    ClassTestDescriptor testEngineClassTestDescriptor = Cast.cast(child);
                    UniqueId classTestDescriptorUniqueId = testEngineClassTestDescriptor.getUniqueId();
                    Class<?> clazz = testEngineClassTestDescriptor.getTestClass();
                    if (excludeTestClassTagPredicate.test(clazz)) {
                        LOGGER.trace("  prune  [%s]", classTestDescriptorUniqueId);
                        testEngineClassTestDescriptor.removeFromHierarchy();
                    } else {
                        LOGGER.trace("  accept [%s]", classTestDescriptorUniqueId);
                    }
                }
            }
        }
    }

    /**
     * Method to process test method tag predicates
     *
     * @param engineDescriptor engineDescriptor
     */
    private void processTestMethodTagPredicates(EngineDescriptor engineDescriptor) {
        LOGGER.trace("processTestMethodTagPredicates");

        if (includeTestMethodTagPredicate != null) {
            LOGGER.trace("includeTestMethodTagPredicate [%s]", includeTestMethodTagPredicate.getRegex());
            // TODO refactor to use forEach
            Set<? extends TestDescriptor> children = engineDescriptor.getChildren();
            for (TestDescriptor child : children) {
                if (child instanceof ClassTestDescriptor) {
                    Set<? extends TestDescriptor> grandChildren = child.getChildren();
                    for (TestDescriptor grandChild : grandChildren) {
                        if (grandChild instanceof ArgumentTestDescriptor) {
                            Set<? extends TestDescriptor> greatGrandChildren = new LinkedHashSet<>(grandChild.getChildren());
                            for (TestDescriptor greatGrandChild : greatGrandChildren) {
                                if (greatGrandChild instanceof MethodTestDescriptor) {
                                    MethodTestDescriptor methodTestDescriptor = Cast.cast(greatGrandChild);
                                    UniqueId methodTestDescriptorUniqueId = methodTestDescriptor.getUniqueId();
                                    Method method = methodTestDescriptor.getTestMethod();
                                    if (includeTestMethodTagPredicate.test(method)) {
                                        LOGGER.trace("  accept [%s]", methodTestDescriptorUniqueId);
                                    } else {
                                        LOGGER.trace("  prune  [%s]", methodTestDescriptorUniqueId);
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
            // TODO refactor to use forEach
            Set<? extends TestDescriptor> children = engineDescriptor.getChildren();
            for (TestDescriptor child : children) {
                if (child instanceof ClassTestDescriptor) {
                    Set<? extends TestDescriptor> grandChildren = child.getChildren();
                    for (TestDescriptor grandChild : grandChildren) {
                        if (grandChild instanceof ArgumentTestDescriptor) {
                            Set<? extends TestDescriptor> greatGrandChildren = new LinkedHashSet<>(grandChild.getChildren());
                            for (TestDescriptor greatGrandChild : greatGrandChildren) {
                                if (greatGrandChild instanceof MethodTestDescriptor) {
                                    MethodTestDescriptor methodTestDescriptor = Cast.cast(greatGrandChild);
                                    UniqueId methodTestDescriptorUniqueId = methodTestDescriptor.getUniqueId();
                                    Method method = methodTestDescriptor.getTestMethod();
                                    if (excludeTestMethodTagPredicate.test(method)) {
                                        LOGGER.trace("  prune  [%s]", methodTestDescriptorUniqueId);
                                        methodTestDescriptor.removeFromHierarchy();
                                    } else {
                                        LOGGER.trace("  accept [%s]", methodTestDescriptorUniqueId);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
