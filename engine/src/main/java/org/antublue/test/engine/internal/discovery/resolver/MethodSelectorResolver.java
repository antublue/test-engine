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
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/**
 * Class to resolve a MethodSelector
 */
@SuppressWarnings("unchecked")
public class MethodSelectorResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodSelectorResolver.class);

    /**
     * Predicate to determine if a class is a valid test class
     */
    private static final Predicate<Class<?>> IS_TEST_CLASS = clazz -> {
        if (clazz.isAnnotationPresent(TestEngine.BaseClass.class)
                || clazz.isAnnotationPresent(TestEngine.Disabled.class)) {
            return false;
        }

        int modifiers = clazz.getModifiers();
        return !Modifier.isAbstract(modifiers) && !TestEngineReflectionUtils.getTestMethods(clazz).isEmpty();
    };

    private static final Predicate<Method> IS_TEST_METHOD = new Predicate<Method>() {
        @Override
        public boolean test(Method method) {
            return TestEngineReflectionUtils.getTestMethods(method.getDeclaringClass()).contains(method);
        }
    };

    /**
     * Method to resolve a MethodSelector
     *
     * @param methodSelector methodSelector
     * @param engineDescriptor engineDescriptor
     */
    public void resolve(MethodSelector methodSelector, EngineDescriptor engineDescriptor) {
        LOGGER.trace("resolve [%s]", methodSelector.getJavaMethod().getName());

        UniqueId engineDescriptorUniqueId = engineDescriptor.getUniqueId();
        Class<?> clazz = methodSelector.getJavaClass();
        LOGGER.trace("  class [%s]", clazz.getName());

        if (!IS_TEST_CLASS.test(clazz)) {
            return;
        }

        Method method = methodSelector.getJavaMethod();
        LOGGER.trace("  class [%s]", clazz.getName());

        if (!IS_TEST_METHOD.test(method)) {
            return;
        }

        UniqueId classTestDescriptorUniqueId = engineDescriptorUniqueId.append("class", clazz.getName());

        ClassTestDescriptor classTestDescriptor =
                TestDescriptorUtils.createClassTestDescriptor(
                        classTestDescriptorUniqueId,
                        clazz);

        final AtomicInteger index = new AtomicInteger();
        TestEngineReflectionUtils
                .getArguments(clazz)
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

                    UniqueId methodTestDescriptorUniqueId =
                            argumentTestDescriptorUniqueId.append("method", method.getName());

                    MethodTestDescriptor methodTestDescriptor =
                            TestDescriptorUtils.createMethodTestDescriptor(
                                    methodTestDescriptorUniqueId,
                                    clazz,
                                    argument,
                                    method);

                    argumentTestDescriptor.addChild(methodTestDescriptor);
                    classTestDescriptor.addChild(argumentTestDescriptor);
                    engineDescriptor.addChild(classTestDescriptor);
                });
    }
}
