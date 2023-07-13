/*
 * Copyright (C) 2022-2023 The AntuBLUE test-engine project authors
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
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.descriptor.ArgumentTestDescriptor;
import org.antublue.test.engine.internal.descriptor.ClassTestDescriptor;
import org.antublue.test.engine.internal.descriptor.MethodTestDescriptor;
import org.antublue.test.engine.internal.descriptor.TestDescriptorUtils;
import org.antublue.test.engine.internal.discovery.predicate.TestClassPredicate;
import org.antublue.test.engine.internal.discovery.predicate.TestClassTagPredicate;
import org.antublue.test.engine.internal.discovery.predicate.TestMethodPredicate;
import org.antublue.test.engine.internal.discovery.predicate.TestMethodTagPredicate;
import org.antublue.test.engine.internal.discovery.resolver.ClassNameFiltersPredicate;
import org.antublue.test.engine.internal.discovery.resolver.PackageNameFiltersPredicate;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.DiscoveryFilter;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.EngineDiscoveryListener;
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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Class to implement a TestEngineEngineDiscoveryRequest
 */
public class TestEngineEngineDiscoveryRequest implements EngineDiscoveryRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEngineEngineDiscoveryRequest.class);

    private final TestClassPredicate includeTestClassPredicate;
    private final TestClassPredicate excludeTestClassPredicate;
    private final TestMethodPredicate includeTestMethodPredicate;
    private final TestMethodPredicate excludeTestMethodPredicate;
    private final TestClassTagPredicate includeTestClassTagPredicate;
    private final TestClassTagPredicate excludeTestClassTagPredicate;
    private final TestMethodTagPredicate includeTestMethodTagPredicate;
    private final TestMethodTagPredicate excludeTestMethodTagPredicate;

    private final ConfigurationParameters configurationParameters;
    private final EngineDiscoveryRequest engineDiscoveryRequest;
    private final EngineDescriptor engineDescriptor;

    /**
     * Class to implement a Predicate to determine if a Class is a test class
     */
    private static final Predicate<Class<?>> IS_TEST_CLASS = clazz -> {
        if (clazz.isAnnotationPresent(TestEngine.BaseClass.class)
                || clazz.isAnnotationPresent(TestEngine.Disabled.class)
                || Modifier.isAbstract(clazz.getModifiers())
                || TestEngineReflectionUtils.getTestMethods(clazz).isEmpty()) {
            LOGGER.trace("class [%s] excluded", clazz.getName());
            return false;
        }

        LOGGER.trace("class [%s] included", clazz.getName());
        return true;
    };

    /**
     * Class to implement a Predicate to determine if a Method is a test method
     */
    private static final Predicate<Method> IS_TEST_METHOD = method -> {
        boolean result =
                !method.isAnnotationPresent(TestEngine.Disabled.class)
                && TestEngineReflectionUtils.getTestMethods(method.getDeclaringClass()).contains(method);
        LOGGER.trace("class [%s] = [%b]", method.getDeclaringClass().getName(), result);
        return result;
    };

    /**
     * Constructor
     *
     * @param configurationParameters configurationParameters
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @param engineDescriptor engineDescriptor
     */
    public TestEngineEngineDiscoveryRequest(
            EngineDiscoveryRequest engineDiscoveryRequest,
            ConfigurationParameters configurationParameters,
            EngineDescriptor engineDescriptor) {
        this.engineDiscoveryRequest = engineDiscoveryRequest;
        this.configurationParameters = configurationParameters;
        this.engineDescriptor = engineDescriptor;

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

    @Override
    public <T extends DiscoverySelector> List<T> getSelectorsByType(Class<T> clazz) {
        return engineDiscoveryRequest.getSelectorsByType(clazz);
    }

    @Override
    public <T extends DiscoveryFilter<?>> List<T> getFiltersByType(Class<T> clazz) {
        return engineDiscoveryRequest.getFiltersByType(clazz);
    }

    @Override
    public ConfigurationParameters getConfigurationParameters() {
        return configurationParameters;
    }

    @Override
    public EngineDiscoveryListener getDiscoveryListener() {
        return engineDiscoveryRequest.getDiscoveryListener();
    }

    /**
     * Method to resolve selectors
     */
    public void resolve() {
        LOGGER.trace("resolve()");

        List<ClassNameFilter> classNameFilters = getFiltersByType(ClassNameFilter.class);
        LOGGER.trace("classNameFilters count [%d]", classNameFilters.size());

        ClassNameFiltersPredicate classNameFiltersPredicate = new ClassNameFiltersPredicate(classNameFilters);

        List<PackageNameFilter> packageNameFilters = getFiltersByType(PackageNameFilter.class);
        LOGGER.trace("packageNameFilters count [%d]", packageNameFilters.size());

        PackageNameFiltersPredicate packageNameFiltersPredicate = new PackageNameFiltersPredicate(packageNameFilters);

        final Map<Class<?>, Set<Method>> classMethodSetMap = new LinkedHashMap<>();

        // Resolve selectors

        getSelectorsByType(ClasspathRootSelector.class)
                .forEach(classpathRootSelector -> {
                    LOGGER.trace("ClasspathRootSelector.class");
                    TestEngineReflectionUtils
                            .findAllClasses(classpathRootSelector.getClasspathRoot())
                            .forEach(clazz -> {
                                if (IS_TEST_CLASS.test(clazz)
                                        && packageNameFiltersPredicate.test(clazz)
                                        && classNameFiltersPredicate.test(clazz)) {
                                    classMethodSetMap.put(
                                            clazz,
                                            new LinkedHashSet<>(TestEngineReflectionUtils.getTestMethods(clazz)));
                                }
                            });
                });

        getSelectorsByType(PackageSelector.class)
                .forEach(packageSelector -> {
                    LOGGER.trace("PackageSelector.class");
                    TestEngineReflectionUtils
                            .findAllClasses(packageSelector.getPackageName())
                            .forEach(clazz -> {
                                if (IS_TEST_CLASS.test(clazz)
                                        && packageNameFiltersPredicate.test(clazz)
                                        && classNameFiltersPredicate.test(clazz)) {
                                    classMethodSetMap.put(
                                            clazz,
                                            new LinkedHashSet<>(TestEngineReflectionUtils.getTestMethods(clazz)));
                                }
                            });
                });

       getSelectorsByType(ClassSelector.class)
                .forEach(classSelector -> {
                    LOGGER.trace("ClassSelector.class");
                    Class<?> clazz = classSelector.getJavaClass();
                    if (IS_TEST_CLASS.test(clazz)
                            && packageNameFiltersPredicate.test(clazz)
                            && classNameFiltersPredicate.test(clazz)) {
                        classMethodSetMap.put(
                                clazz,
                                new LinkedHashSet<>(TestEngineReflectionUtils.getTestMethods(clazz)));
                    }
                });

       getSelectorsByType(MethodSelector.class)
                .forEach(methodSelector -> {
                    LOGGER.trace("MethodSelector.class");
                    Class<?> clazz = methodSelector.getJavaClass();
                    if (IS_TEST_METHOD.test(methodSelector.getJavaMethod())
                            && packageNameFiltersPredicate.test(clazz)
                            && classNameFiltersPredicate.test(clazz)) {
                        Set<Method> methods = classMethodSetMap.getOrDefault(clazz, new LinkedHashSet<>());
                        methods.add(methodSelector.getJavaMethod());
                        classMethodSetMap.put(clazz, methods);
                    }
                });

        getSelectorsByType(UniqueIdSelector.class)
                .forEach(uniqueIdSelector -> {
                    LOGGER.trace("UniqueIdSelector.class");
                    UniqueId.Segment segment = uniqueIdSelector.getUniqueId().getLastSegment();
                    if ("class".equals(segment.getType())) {
                        String className = segment.getValue();
                        try {
                            Class<?> clazz = Class.forName(className);
                            if (IS_TEST_CLASS.test(clazz)
                                    && packageNameFiltersPredicate.test(clazz)
                                    && classNameFiltersPredicate.test(clazz)) {
                                Set<Method> methods = classMethodSetMap.getOrDefault(clazz, new LinkedHashSet<>());
                                methods.addAll(TestEngineReflectionUtils.getTestMethods(clazz));
                                classMethodSetMap.put(clazz, methods);
                            }
                        } catch (ClassNotFoundException e) {
                            throw new TestEngineException(String.format("Exception loading class [%s]", className));
                        }
                    } else if ("argument".equals(segment.getType())) {
                        segment = uniqueIdSelector.getUniqueId().removeLastSegment().getLastSegment();
                        String className = segment.getValue();
                        try {
                            Class<?> clazz = Class.forName(className);
                            if (IS_TEST_CLASS.test(clazz)
                                    && packageNameFiltersPredicate.test(clazz)
                                    && classNameFiltersPredicate.test(clazz)) {
                                Set<Method> methods = classMethodSetMap.getOrDefault(clazz, new LinkedHashSet<>());
                                methods.addAll(TestEngineReflectionUtils.getTestMethods(clazz));
                                classMethodSetMap.put(clazz, methods);
                            }
                        } catch (ClassNotFoundException e) {
                            throw new TestEngineException(String.format("Exception loading class [%s]", className));
                        }
                    }
                });

        /*
         * Filter...
         *
         * include/exclude classes
         * include/exclude methods
         * include/exclude class tags
         * include/exclude method tags
         *
         * ... and build the TestDescriptor tree
         */

        Iterator<Map.Entry<Class<?>, Set<Method>>> classMethodMapEntryIterator = classMethodSetMap.entrySet().iterator();
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
                    LOGGER.trace("includeTestClassTagPredicate class [%s] included", clazz.getName());
                } else {
                    LOGGER.trace("includeTestClassTagPredicate class [%s] excluded", clazz.getName());
                    classMethodMapEntryIterator.remove();
                    continue;
                }
            }

            if (excludeTestClassTagPredicate != null) {
                if (excludeTestClassTagPredicate.test(clazz)) {
                    LOGGER.trace("excludeTestClassTagPredicate class [%s] excluded", clazz.getName());
                    classMethodMapEntryIterator.remove();
                    continue;
                } else {
                    LOGGER.trace("excludeTestClassTagPredicate class [%s] included", clazz.getName());
                }
            }

            Iterator<Method> methodIterator = methods.iterator();
            while (methodIterator.hasNext()) {
                Method method = methodIterator.next();

                if (includeTestMethodPredicate != null) {
                    if (includeTestMethodPredicate.test(method)) {
                        LOGGER.trace("includeTestMethodPredicate class [%s] included", method.getName());
                    } else {
                        LOGGER.trace("includeTestMethodPredicate class [%s] excluded", method.getName());
                        methodIterator.remove();
                        continue;
                    }
                }

                if (excludeTestMethodPredicate != null) {
                    if (excludeTestMethodPredicate.test(method)) {
                        LOGGER.trace("excludeTestMethodPredicate class [%s] excluded", method.getName());
                        methodIterator.remove();
                        continue;
                    } else {
                        LOGGER.trace("excludeTestMethodPredicate class [%s] included", method.getName());
                    }
                }

                if (includeTestMethodTagPredicate != null) {
                    if (includeTestMethodTagPredicate.test(method)) {
                        LOGGER.trace("includeTestMethodTagPredicate class [%s] included", method.getName());
                    } else {
                        LOGGER.trace("includeTestMethodTagPredicate class [%s] excluded", method.getName());
                        methodIterator.remove();
                        continue;
                    }
                }

                if (excludeTestMethodTagPredicate != null) {
                    if (excludeTestMethodTagPredicate.test(method)) {
                        LOGGER.trace("excludeTestMethodTagPredicate class [%s] excluded", method.getName());
                        methodIterator.remove();
                    } else {
                        LOGGER.trace("excludeTestMethodTagPredicate class [%s] included", method.getName());
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
                    TestDescriptorUtils.createClassTestDescriptor(classTestDescritporUniqueId, clazz);

            engineDescriptor.addChild(classTestDescriptor);

            List<Argument> arguments = TestEngineReflectionUtils.getArgumentsList(clazz);
            for (Argument argument : arguments) {
                UniqueId argumentUniqueId =
                        classTestDescritporUniqueId.append("argument", argument.name());

                ArgumentTestDescriptor argumentTestDescriptor =
                        TestDescriptorUtils.createArgumentTestDescriptor(
                                argumentUniqueId,
                                clazz,
                                argument);

                classTestDescriptor.addChild(argumentTestDescriptor);

                for (Method method : methods) {
                    UniqueId methodUniqueId =
                            argumentUniqueId.append("method", method.getName());

                    MethodTestDescriptor methodTestDescriptor =
                            TestDescriptorUtils.createMethodTestDescriptor(
                                    methodUniqueId,
                                    clazz,
                                    argument,
                                    method);

                    argumentTestDescriptor.addChild(methodTestDescriptor);
                }
            }
        }
        
        TestDescriptorUtils.logTestDescriptorTree(engineDescriptor);
    }
}
