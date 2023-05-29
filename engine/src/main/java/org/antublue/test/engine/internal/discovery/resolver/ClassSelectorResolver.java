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

import org.antublue.test.engine.internal.TestClassConfigurationException;
import org.antublue.test.engine.internal.TestDescriptorUtils;
import org.antublue.test.engine.internal.TestEngineReflectionUtils;
import org.antublue.test.engine.internal.descriptor.ArgumentTestDescriptor;
import org.antublue.test.engine.internal.descriptor.ClassTestDescriptor;
import org.antublue.test.engine.internal.descriptor.MethodTestDescriptor;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class to resolve a ClassSelector
 */
public class ClassSelectorResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassSelectorResolver.class);

    private final Map<Integer, Class<?>> orderToClassMap;

    public ClassSelectorResolver() {
        orderToClassMap = new LinkedHashMap<>();
    }

    /**
     * Method to resolve a ClassSelector
     *
     * @param classSelector classSelector
     * @param engineDescriptor engineDescriptor
     */
    public void resolve(ClassSelector classSelector, EngineDescriptor engineDescriptor) {
        LOGGER.trace("resolve [%s]", classSelector.getClassName());

        UniqueId engineDescriptorUniqueId = engineDescriptor.getUniqueId();
        Class<?> clazz = classSelector.getJavaClass();
        LOGGER.trace("  class [%s]", clazz.getName());

        if (!IsTestClassPredicate.INSTANCE.test(clazz)) {
            return;
        }

        Integer order = TestEngineReflectionUtils.getClassOrder(clazz);
        if (order != null) {
            if (orderToClassMap.containsKey(order)) {
                Class<?> existingClass = orderToClassMap.get(order);
                throw new TestClassConfigurationException(
                        String.format(
                                "Test class [%s] (or superclass) and test class [%s] (or superclass) contain duplicate @TestEngine.Order(%d) class annotation",
                                existingClass.getName(),
                                clazz.getName(),
                                order));
            } else {
                orderToClassMap.put(order, clazz);
            }
        }

        UniqueId classTestDescriptorUniqueId = engineDescriptorUniqueId.append("class", clazz.getName());

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
                            classTestDescriptorUniqueId.append(
                                    "argument",
                                    String.valueOf(index.getAndIncrement()));

                    ArgumentTestDescriptor argumentTestDescriptor =
                            TestDescriptorUtils.createArgumentTestDescriptor(
                                    argumentTestDescriptorUniqueId,
                                    clazz,
                                    argument);

                    testEngineClassTestDescriptor.addChild(argumentTestDescriptor);

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
    }
}
