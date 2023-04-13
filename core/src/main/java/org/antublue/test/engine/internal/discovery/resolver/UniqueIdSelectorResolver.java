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

import org.antublue.test.engine.api.Parameter;
import org.antublue.test.engine.internal.TestEngineException;
import org.antublue.test.engine.internal.TestEngineReflectionUtils;
import org.antublue.test.engine.internal.descriptor.ClassTestDescriptor;
import org.antublue.test.engine.internal.descriptor.MethodTestDescriptor;
import org.antublue.test.engine.internal.descriptor.ParameterTestDescriptor;
import org.antublue.test.engine.internal.descriptor.TestDescriptorFactory;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.util.Cast;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * Class to resolve a UniqueIdSelector
 */
public class UniqueIdSelectorResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(UniqueIdSelectorResolver.class);

    /**
     * Method to resolve a UniqueIdSelector
     *
     * @param uniqueIdSelector
     * @param engineDescriptor
     */
    public void resolve(UniqueIdSelector uniqueIdSelector, EngineDescriptor engineDescriptor) {
        LOGGER.trace("resolve");

        String className = null;

        try {
            UniqueId selectorUniqueId = uniqueIdSelector.getUniqueId();
            LOGGER.trace("selectorUniqueId [%s]", selectorUniqueId);

            UniqueId.Segment segment = selectorUniqueId.getLastSegment();

            if ("parameter".equals(segment.getType())) {
                LOGGER.trace("parameter [%s] selected", segment.getValue());

                UniqueId classUniqueId = selectorUniqueId.removeLastSegment();
                UniqueId.Segment classSegment = classUniqueId.getLastSegment();
                className = classSegment.getValue();
                LOGGER.trace("className [%s]", className);

                Class<?> clazz = Class.forName(className);

                ClassTestDescriptor classTestDescriptor;
                Optional<? extends TestDescriptor> optionalTestDescriptor = engineDescriptor.findByUniqueId(classUniqueId);
                if (optionalTestDescriptor.isPresent()) {
                    classTestDescriptor = Cast.cast(optionalTestDescriptor.get());
                } else {
                    classTestDescriptor =
                            TestDescriptorFactory.creaateClassTestDescriptor(
                                    classUniqueId,
                                    clazz);
                }

                List<Parameter> parameters = TestEngineReflectionUtils.getParameters(clazz);
                Parameter parameter = parameters.get(Integer.parseInt(segment.getValue()));

                ParameterTestDescriptor parameterTestDescriptor;

                optionalTestDescriptor = classTestDescriptor.findByUniqueId(selectorUniqueId);
                if (optionalTestDescriptor.isPresent()) {
                    parameterTestDescriptor = Cast.cast(optionalTestDescriptor.get());
                } else {
                    parameterTestDescriptor =
                            TestDescriptorFactory.createParameterTestDescriptor(
                                    selectorUniqueId,
                                    clazz,
                                    parameter);
                }

                List<Method> methods = TestEngineReflectionUtils.getTestMethods(clazz);
                for (Method method : methods) {
                    UniqueId methodUniqueId = selectorUniqueId.append("method", method.getName());

                    MethodTestDescriptor methodTestDescriptor =
                            TestDescriptorFactory.createMethodTestDescriptor(
                                    methodUniqueId,
                                    clazz,
                                    parameter,
                                    method);

                    parameterTestDescriptor.addChild(methodTestDescriptor);
                }

                classTestDescriptor.addChild(parameterTestDescriptor);
                engineDescriptor.addChild(classTestDescriptor);
            } else if ("class".equals(segment.getType())) {
                className = segment.getValue();
                LOGGER.trace("className [%s]", className);

                Class<?> clazz = Class.forName(className);

                ClassTestDescriptor classTestDescriptor;
                Optional<? extends TestDescriptor> optionalTestDescriptor = engineDescriptor.findByUniqueId(selectorUniqueId);
                if (optionalTestDescriptor.isPresent()) {
                    classTestDescriptor = Cast.cast(optionalTestDescriptor.get());
                } else {
                    classTestDescriptor =
                            TestDescriptorFactory.creaateClassTestDescriptor(
                                    selectorUniqueId,
                                    clazz);
                }

                List<Parameter> parameters = TestEngineReflectionUtils.getParameters(clazz);
                for (int i = 0; i < parameters.size(); i++) {
                    Parameter parameter = parameters.get(i);
                    UniqueId parameterUniqueId = selectorUniqueId.append("parameter", String.valueOf(i));

                    ParameterTestDescriptor parameterTestDescriptor =
                            TestDescriptorFactory.createParameterTestDescriptor(
                                    parameterUniqueId,
                                    clazz,
                                    parameter);

                    List<Method> methods = TestEngineReflectionUtils.getTestMethods(clazz);
                    for (Method method : methods) {
                        UniqueId methodUniqueId = parameterUniqueId.append("method", method.getName());

                        MethodTestDescriptor methodTestDescriptor =
                                TestDescriptorFactory.createMethodTestDescriptor(
                                        methodUniqueId,
                                        clazz,
                                        parameter,
                                        method);

                        parameterTestDescriptor.addChild(methodTestDescriptor);
                    }

                    classTestDescriptor.addChild(parameterTestDescriptor);
                }

                engineDescriptor.addChild(classTestDescriptor);
            }
        } catch (ClassNotFoundException e) {
            throw new TestEngineException(
                    String.format("Class [%s] not found", className),
                    e);
        }
    }
}