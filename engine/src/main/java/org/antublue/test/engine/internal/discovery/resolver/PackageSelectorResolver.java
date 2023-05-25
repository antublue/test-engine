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

import org.antublue.test.engine.internal.TestDescriptorUtils;
import org.antublue.test.engine.internal.TestEngineReflectionUtils;
import org.antublue.test.engine.internal.descriptor.ArgumentTestDescriptor;
import org.antublue.test.engine.internal.descriptor.ClassTestDescriptor;
import org.antublue.test.engine.internal.descriptor.MethodTestDescriptor;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassNameFilter;
import org.junit.platform.engine.discovery.PackageNameFilter;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class to resolve a PackageSelector
 */
public class PackageSelectorResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(PackageSelectorResolver.class);

    /**
     * Method to resolve a PackageSelector
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @param engineDescriptor engineDescriptor
     * @param packageSelector packageSelector
     */
    public void resolve(EngineDiscoveryRequest engineDiscoveryRequest, EngineDescriptor engineDescriptor, PackageSelector packageSelector) {
        LOGGER.trace("resolve [%s]", packageSelector.getPackageName());

        final UniqueId engineDescriptorUniqueId = engineDescriptor.getUniqueId();
        String packageName = packageSelector.getPackageName();

        List<ClassNameFilter> classNameFilters =
                engineDiscoveryRequest.getFiltersByType(ClassNameFilter.class);

        LOGGER.trace("classNameFilters count [%d]", classNameFilters.size());

        List<PackageNameFilter> packageNameFilters =
                engineDiscoveryRequest.getFiltersByType(PackageNameFilter.class);

        LOGGER.trace("packageNameFilters count [%d]", packageNameFilters.size());

        new ArrayList<>(ReflectionSupport.findAllClassesInPackage(packageName, classFilter -> true, classNameFilter -> true))
                .stream()
                //.filter(new PackageNameFiltersPredicate(packageNameFilters))
                //.filter(new ClassNameFiltersPredicate(classNameFilters))
                .sorted(Comparator.comparing(Class::getName))
                .forEach(clazz -> {
                    LOGGER.trace("  class [%s]", clazz.getName());

                    UniqueId classTestDescriptorUniqueId =
                            engineDescriptorUniqueId.append("class", clazz.getName());

                    ClassTestDescriptor testEngineClassTestDescriptor =
                            TestDescriptorUtils.createClassTestDescriptor(
                                    classTestDescriptorUniqueId,
                                    clazz);

                    engineDescriptor.addChild(testEngineClassTestDescriptor);

                    final AtomicInteger index = new AtomicInteger();
                    TestEngineReflectionUtils
                            .getArgumentsList(clazz)
                            .forEach(argument -> {
                                UniqueId argumentTestDescriptorUniqueId =
                                        classTestDescriptorUniqueId
                                                .append(
                                                        "argument",
                                                        String.valueOf(index.getAndIncrement()));

                                ArgumentTestDescriptor testEngineArgumentTestDescriptor =
                                        TestDescriptorUtils.createArgumentTestDescriptor(
                                                argumentTestDescriptorUniqueId,
                                                clazz,
                                                argument);

                                testEngineClassTestDescriptor.addChild(testEngineArgumentTestDescriptor);

                                TestEngineReflectionUtils
                                        .getTestMethods(clazz)
                                        .forEach(method -> {
                                            UniqueId methodTestDescriptorUniqueId =
                                                    argumentTestDescriptorUniqueId
                                                            .append("method", method.getName());

                                            MethodTestDescriptor methodTestDescriptor =
                                                    TestDescriptorUtils.createMethodTestDescriptor(
                                                            methodTestDescriptorUniqueId,
                                                            clazz,
                                                            argument,
                                                            method);

                                            testEngineArgumentTestDescriptor.addChild(methodTestDescriptor);
                                        });

                                testEngineClassTestDescriptor.addChild(testEngineArgumentTestDescriptor);
                                testEngineArgumentTestDescriptor.prune();
                            });
                        testEngineClassTestDescriptor.prune();
                    });
    }
}
