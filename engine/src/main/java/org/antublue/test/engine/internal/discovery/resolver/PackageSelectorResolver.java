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

package org.antublue.test.engine.internal.discovery.resolver;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.TestDescriptorUtils;
import org.antublue.test.engine.internal.TestEngineReflectionUtils;
import org.antublue.test.engine.internal.descriptor.RunnableClassTestDescriptor;
import org.antublue.test.engine.internal.descriptor.RunnableMethodTestDescriptor;
import org.antublue.test.engine.internal.descriptor.RunnableParameterTestDescriptor;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/**
 * Class to resolve a PackageSelector
 */
public class PackageSelectorResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(PackageSelectorResolver.class);

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
     * Method to resolve a PackageSelector
     *
     * @param packageSelector
     * @param engineDescriptor
     */
    public void resolve(PackageSelector packageSelector, EngineDescriptor engineDescriptor) {
        LOGGER.trace("resolve [%s]", packageSelector.getPackageName());

        final UniqueId engineDescriptorUniqueId = engineDescriptor.getUniqueId();
        String packageName = packageSelector.getPackageName();

        new ArrayList<>(ReflectionSupport.findAllClassesInPackage(packageName, IS_TEST_CLASS, name -> true))
                .stream()
                .sorted(Comparator.comparing(Class::getName))
                .forEach(clazz -> {
                    LOGGER.trace("  class [%s]", clazz.getName());

                    UniqueId classDescriptorUniqueId =
                            engineDescriptorUniqueId.append("class", clazz.getName());

                    RunnableClassTestDescriptor testEngineClassTestDescriptor =
                            TestDescriptorUtils.createClassTestDescriptor(
                                    classDescriptorUniqueId,
                                    clazz);

                    engineDescriptor.addChild(testEngineClassTestDescriptor);

                    final AtomicInteger index = new AtomicInteger();
                    TestEngineReflectionUtils
                            .getParameters(clazz)
                            .forEach(parameter -> {
                                UniqueId parameterDescriptorUniqueId =
                                        classDescriptorUniqueId
                                                .append("parameter", String.valueOf(index.get()));

                                RunnableParameterTestDescriptor testEngineParameterTestDescriptor =
                                        TestDescriptorUtils.createParameterTestDescriptor(
                                                parameterDescriptorUniqueId,
                                                clazz,
                                                parameter);

                                testEngineClassTestDescriptor.addChild(testEngineParameterTestDescriptor);

                                TestEngineReflectionUtils
                                        .getTestMethods(clazz)
                                        .forEach(method -> {
                                            UniqueId uniqueId =
                                                    parameterDescriptorUniqueId
                                                            .append("method", method.getName());

                                            RunnableMethodTestDescriptor methodTestDescriptor =
                                                    TestDescriptorUtils.createMethodTestDescriptor(
                                                            uniqueId,
                                                            clazz,
                                                            parameter,
                                                            method);

                                            testEngineParameterTestDescriptor.addChild(methodTestDescriptor);
                                        });

                                testEngineClassTestDescriptor.addChild(testEngineParameterTestDescriptor);
                                testEngineParameterTestDescriptor.prune();
                                index.incrementAndGet();
                            });
                        testEngineClassTestDescriptor.prune();
                    });
    }
}
