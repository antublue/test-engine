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
import org.antublue.test.engine.internal.descriptor.ClassTestDescriptor;
import org.antublue.test.engine.internal.descriptor.MethodTestDescriptor;
import org.antublue.test.engine.internal.descriptor.ParameterTestDescriptor;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/**
 * Class to resolve a ClasspathRootSelector
 */
public class ClasspathRootResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClasspathRootResolver.class);

    /**
     * Predicate to determine if a class is a valid test class
     */
    private static final Predicate<Class<?>> IS_TEST_CLASS = clazz -> {
        if (clazz.isAnnotationPresent(TestEngine.BaseClass.class)
                || clazz.isAnnotationPresent(TestEngine.Disabled.class)) {
            return false;
        }

        return !Modifier.isAbstract(clazz.getModifiers()) && !TestEngineReflectionUtils.getTestMethods(clazz).isEmpty();
    };

    /**
     * Method to resolve a ClasspathRootSelector
     *
     * @param classpathRootSelector classpathRootSelector
     * @param engineDescriptor engineDescriptor
     */
    public void resolve(ClasspathRootSelector classpathRootSelector, EngineDescriptor engineDescriptor) {
        LOGGER.trace("resolve [%s]", classpathRootSelector);

        final UniqueId engineDescriptorUniqueId = engineDescriptor.getUniqueId();
        URI uri = classpathRootSelector.getClasspathRoot();
        LOGGER.trace("uri [%s]", uri);

        new ArrayList<>(ReflectionUtils.findAllClassesInClasspathRoot(uri, IS_TEST_CLASS, name -> true))
                .stream()
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
                            .getParameters(clazz)
                            .forEach(parameter -> {
                                UniqueId parameterTestDescriptorUniqueId =
                                        classTestDescriptorUniqueId.append(
                                                "parameter",
                                                String.valueOf(index.getAndIncrement()));

                                ParameterTestDescriptor parameterTestDescriptor =
                                        TestDescriptorUtils.createParameterTestDescriptor(
                                                parameterTestDescriptorUniqueId,
                                                clazz,
                                                parameter);

                                classTestDescriptor.addChild(parameterTestDescriptor);

                                TestEngineReflectionUtils
                                        .getTestMethods(clazz)
                                        .forEach(method -> {
                                            UniqueId methodTestDescriptorUniqueId =
                                                    parameterTestDescriptorUniqueId.append("method", method.getName());

                                            MethodTestDescriptor methodTestDescriptor =
                                                    TestDescriptorUtils.createMethodTestDescriptor(
                                                            methodTestDescriptorUniqueId,
                                                            clazz,
                                                            parameter,
                                                            method);

                                            parameterTestDescriptor.addChild(methodTestDescriptor);
                                        });

                                parameterTestDescriptor.prune();
                            });

                    classTestDescriptor.prune();
                });
    }
}
