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

import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.internal.TestDescriptorUtils;
import org.antublue.test.engine.internal.TestEngineException;
import org.antublue.test.engine.internal.TestEngineReflectionUtils;
import org.antublue.test.engine.internal.descriptor.ArgumentTestDescriptor;
import org.antublue.test.engine.internal.descriptor.ClassTestDescriptor;
import org.antublue.test.engine.internal.descriptor.MethodTestDescriptor;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.util.Cast;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Class to resolve a UniqueIdSelector
 */
public class UniqueIdSelectorResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(UniqueIdSelectorResolver.class);

    /**
     * Method to resolve a UniqueIdSelector
     *
     * @param uniqueIdSelector uniqueIdSelector
     * @param engineDescriptor engineDescriptor
     */
    public void resolve(UniqueIdSelector uniqueIdSelector, EngineDescriptor engineDescriptor) {
        LOGGER.trace("resolve [%s]", uniqueIdSelector.getUniqueId());

        String className = null;

        try {
            UniqueId selectorUniqueId = uniqueIdSelector.getUniqueId();
            LOGGER.trace("selectorUniqueId [%s]", selectorUniqueId);

            UniqueId.Segment segment = selectorUniqueId.getLastSegment();

            if ("argument".equals(segment.getType())) {
                LOGGER.trace("argument [%s] selected", segment.getValue());

                UniqueId classTestDescriptorUniqueId = selectorUniqueId.removeLastSegment();
                UniqueId.Segment classSegment = classTestDescriptorUniqueId.getLastSegment();
                className = classSegment.getValue();
                LOGGER.trace("className [%s]", className);

                Class<?> clazz = Class.forName(className);

                ClassTestDescriptor classTestDescriptor =
                        engineDescriptor
                                .findByUniqueId(classTestDescriptorUniqueId)
                                .map((Function<TestDescriptor, ClassTestDescriptor>) Cast::cast)
                                .orElseGet(() ->
                                        TestDescriptorUtils.createClassTestDescriptor(classTestDescriptorUniqueId, clazz));

                List<Argument> arguments = TestEngineReflectionUtils.getArguments(clazz);
                Argument argument = arguments.get(Integer.parseInt(segment.getValue()));

                ArgumentTestDescriptor argumentTestDescriptor =
                        classTestDescriptor
                                .findByUniqueId(selectorUniqueId)
                                .map((Function<TestDescriptor, ArgumentTestDescriptor>) Cast::cast)
                                .orElseGet(() ->
                                        TestDescriptorUtils.createArgumentTestDescriptor(
                                                selectorUniqueId,
                                                clazz,
                                                argument));

                TestEngineReflectionUtils
                        .getTestMethods(clazz)
                        .forEach(method -> {
                            UniqueId methodTestDescriptorUniqueId =
                                    selectorUniqueId.append("method", method.getName());

                            MethodTestDescriptor methodTestDescriptor =
                                    TestDescriptorUtils.createMethodTestDescriptor(
                                            methodTestDescriptorUniqueId,
                                            clazz,
                                            argument,
                                            method);

                            argumentTestDescriptor.addChild(methodTestDescriptor);
                        });

                classTestDescriptor.addChild(argumentTestDescriptor);
                engineDescriptor.addChild(classTestDescriptor);
            } else if ("class".equals(segment.getType())) {
                className = segment.getValue();
                LOGGER.trace("className [%s]", className);

                Class<?> clazz = Class.forName(className);

                ClassTestDescriptor classTestDescriptor =
                        engineDescriptor
                                .findByUniqueId(selectorUniqueId)
                                .map((Function<TestDescriptor, ClassTestDescriptor>) Cast::cast)
                                .orElseGet(() ->
                                        TestDescriptorUtils.createClassTestDescriptor(
                                        selectorUniqueId,
                                        clazz));

                AtomicInteger index = new AtomicInteger();
                TestEngineReflectionUtils
                        .getArguments(clazz)
                        .forEach(argument -> {
                            UniqueId argumentTestDescriptorUniqueId =
                                    selectorUniqueId.append(
                                            "argument",
                                            String.valueOf(index.getAndIncrement()));

                            ArgumentTestDescriptor argumentTestDescriptor =
                                    TestDescriptorUtils.createArgumentTestDescriptor(
                                            argumentTestDescriptorUniqueId,
                                            clazz,
                                            argument);

                            TestEngineReflectionUtils
                                    .getTestMethods(clazz)
                                    .forEach(method -> {
                                        UniqueId methodUniqueId =
                                                argumentTestDescriptorUniqueId.append(
                                                        "method",
                                                        method.getName());

                                        MethodTestDescriptor methodTestDescriptor =
                                                TestDescriptorUtils.createMethodTestDescriptor(
                                                        methodUniqueId,
                                                        clazz,
                                                        argument,
                                                        method);

                                        argumentTestDescriptor.addChild(methodTestDescriptor);
                                    });

                            classTestDescriptor.addChild(argumentTestDescriptor);
                        });

                engineDescriptor.addChild(classTestDescriptor);
            }
        } catch (ClassNotFoundException e) {
            throw new TestEngineException(
                    String.format("Class [%s] not found", className),
                    e);
        }
    }
}
