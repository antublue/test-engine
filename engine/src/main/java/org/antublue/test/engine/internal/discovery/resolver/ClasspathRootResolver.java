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

package org.antublue.test.engine.internal.discovery.resolver;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.TestDescriptorUtils;
import org.antublue.test.engine.internal.TestEngineReflectionUtils;
import org.antublue.test.engine.internal.descriptor.ArgumentTestDescriptor;
import org.antublue.test.engine.internal.descriptor.ClassTestDescriptor;
import org.antublue.test.engine.internal.descriptor.MethodTestDescriptor;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.util.Throwables;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassNameFilter;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.PackageNameFilter;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class to resolve a ClasspathRootSelector
 */
public class ClasspathRootResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClasspathRootResolver.class);

    /**
     * Method to resolve a ClasspathRootSelector
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @param engineDescriptor engineDescriptor
     * @param classpathRootSelector classpathRootSelector
     */
    public void resolve(
            EngineDiscoveryRequest engineDiscoveryRequest,
            EngineDescriptor engineDescriptor,
            ClasspathRootSelector classpathRootSelector) {
        LOGGER.trace("resolve [%s]", classpathRootSelector);

        final UniqueId engineDescriptorUniqueId = engineDescriptor.getUniqueId();
        URI uri = classpathRootSelector.getClasspathRoot();
        LOGGER.trace("uri [%s]", uri);

        List<ClassNameFilter> classNameFilters =
                engineDiscoveryRequest.getFiltersByType(ClassNameFilter.class);

        LOGGER.trace("classNameFilters count [%d]", classNameFilters.size());

        List<PackageNameFilter> packageNameFilters =
                engineDiscoveryRequest.getFiltersByType(PackageNameFilter.class);

        LOGGER.trace("packageNameFilters count [%d]", packageNameFilters.size());

        try {
            ReflectionUtils
                    .findAllClassesInClasspathRoot(uri, classFilter -> true, classNameFilter -> true)
                    .stream()
                    .filter(new PackageNameFiltersPredicate(packageNameFilters))
                    .filter(new ClassNameFiltersPredicate(classNameFilters))
                    .filter(clazz -> !TestEngineReflectionUtils.getTestMethods(clazz).isEmpty())
                    .filter(clazz ->
                            !(Modifier.isAbstract(clazz.getModifiers())
                                    || clazz.isAnnotationPresent(TestEngine.BaseClass.class)
                                    || clazz.isAnnotationPresent(TestEngine.Disabled.class)
                                    || TestEngineReflectionUtils.getTestMethods(clazz).isEmpty()))
                    .sorted(Comparator.comparing(TestEngineReflectionUtils::getDisplayName))
                    .forEach(clazz -> {
                        LOGGER.trace("  class [%s]", clazz.getName());

                        final UniqueId classTestDescriptorUniqueId =
                                engineDescriptorUniqueId.append("class", clazz.getName());

                        ClassTestDescriptor classTestDescriptor =
                                TestDescriptorUtils.createClassTestDescriptor(
                                        classTestDescriptorUniqueId,
                                        clazz);

                        engineDescriptor.addChild(classTestDescriptor);

                        AtomicInteger index = new AtomicInteger();
                        TestEngineReflectionUtils
                                .getArgumentsList(clazz)
                                .forEach(argument -> {
                                    UniqueId argumentTestDescriptorUniqueId =
                                            classTestDescriptorUniqueId.append(
                                                    "argument",
                                                    String.valueOf(index.getAndIncrement()));

                                    ArgumentTestDescriptor argumentTestDescriptor =
                                            TestDescriptorUtils.createArgumentTestDescriptor(
                                                    argumentTestDescriptorUniqueId,
                                                    clazz,
                                                    argument);

                                    classTestDescriptor.addChild(argumentTestDescriptor);

                                    TestEngineReflectionUtils
                                            .getTestMethods(clazz)
                                            .forEach(method -> {
                                                UniqueId methodTestDescriptorUniqueId =
                                                        argumentTestDescriptorUniqueId.append("method", method.getName());

                                                MethodTestDescriptor methodTestDescriptor =
                                                        TestDescriptorUtils.createMethodTestDescriptor(
                                                                methodTestDescriptorUniqueId,
                                                                clazz,
                                                                argument,
                                                                method);

                                                argumentTestDescriptor.addChild(methodTestDescriptor);
                                            });

                                    argumentTestDescriptor.prune();
                                });

                        classTestDescriptor.prune();
                    });
        } catch (Throwable t) {
            Throwables.throwIfUnchecked(t);
        }
    }
}
