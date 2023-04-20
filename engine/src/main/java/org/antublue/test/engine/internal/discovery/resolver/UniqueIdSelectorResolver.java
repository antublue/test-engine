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
import org.antublue.test.engine.internal.descriptor.RunnableClassTestDescriptor;
import org.antublue.test.engine.internal.descriptor.RunnableMethodTestDescriptor;
import org.antublue.test.engine.internal.descriptor.RunnableParameterTestDescriptor;
import org.antublue.test.engine.internal.TestDescriptorUtils;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;

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
        LOGGER.trace("resolve [%s]", uniqueIdSelector.getUniqueId());

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

                RunnableClassTestDescriptor classTestDescriptor =
                        engineDescriptor
                                .findByUniqueId(classUniqueId)
                                .map((Function<TestDescriptor, RunnableClassTestDescriptor>) testDescriptor ->
                                        (RunnableClassTestDescriptor) testDescriptor)
                                .orElseGet(() ->
                                        TestDescriptorUtils.createClassTestDescriptor(classUniqueId, clazz));

                List<Parameter> parameters = TestEngineReflectionUtils.getParameters(clazz);
                Parameter parameter = parameters.get(Integer.parseInt(segment.getValue()));

                RunnableParameterTestDescriptor parameterTestDescriptor =
                        classTestDescriptor
                                .findByUniqueId(selectorUniqueId)
                                .map((Function<TestDescriptor, RunnableParameterTestDescriptor>) testDescriptor ->
                                        (RunnableParameterTestDescriptor) testDescriptor)
                                .orElseGet(() ->
                                        TestDescriptorUtils.createParameterTestDescriptor(
                                                selectorUniqueId,
                                                clazz,
                                                parameter));

                List<Method> methods = TestEngineReflectionUtils.getTestMethods(clazz);
                for (Method method : methods) {
                    UniqueId methodUniqueId = selectorUniqueId.append("method", method.getName());

                    RunnableMethodTestDescriptor methodTestDescriptor =
                            TestDescriptorUtils.createMethodTestDescriptor(
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

                RunnableClassTestDescriptor classTestDescriptor =
                        engineDescriptor
                                .findByUniqueId(selectorUniqueId)
                                .map((Function<TestDescriptor, RunnableClassTestDescriptor>) testDescriptor ->
                                        (RunnableClassTestDescriptor) testDescriptor)
                                .orElseGet(() ->
                                        TestDescriptorUtils.createClassTestDescriptor(
                                        selectorUniqueId,
                                        clazz));


                List<Parameter> parameters = TestEngineReflectionUtils.getParameters(clazz);
                for (int i = 0; i < parameters.size(); i++) {
                    Parameter parameter = parameters.get(i);
                    UniqueId parameterUniqueId = selectorUniqueId.append("parameter", String.valueOf(i));

                    RunnableParameterTestDescriptor parameterTestDescriptor =
                            TestDescriptorUtils.createParameterTestDescriptor(
                                    parameterUniqueId,
                                    clazz,
                                    parameter);

                    List<Method> methods = TestEngineReflectionUtils.getTestMethods(clazz);
                    for (Method method : methods) {
                        UniqueId methodUniqueId = parameterUniqueId.append("method", method.getName());

                        RunnableMethodTestDescriptor methodTestDescriptor =
                                TestDescriptorUtils.createMethodTestDescriptor(
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