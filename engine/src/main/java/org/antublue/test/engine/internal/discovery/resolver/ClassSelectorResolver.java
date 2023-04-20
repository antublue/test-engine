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
import org.antublue.test.engine.internal.TestEngineReflectionUtils;
import org.antublue.test.engine.internal.descriptor.RunnableClassTestDescriptor;
import org.antublue.test.engine.internal.descriptor.RunnableMethodTestDescriptor;
import org.antublue.test.engine.internal.descriptor.RunnableParameterTestDescriptor;
import org.antublue.test.engine.internal.TestDescriptorUtils;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Class to resolve a ClassSelector
 */
public class ClassSelectorResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassSelectorResolver.class);

    /**
     * Method to resolve a ClassSelector
     *
     * @param classSelector
     * @param engineDescriptor
     */
    public void resolve(ClassSelector classSelector, EngineDescriptor engineDescriptor) {
        LOGGER.trace("resolve [%s]", classSelector.getClassName());

        UniqueId uniqueId = engineDescriptor.getUniqueId();
        Class<?> clazz = classSelector.getJavaClass();
        LOGGER.trace("  class [%s]", clazz.getName());

        uniqueId = uniqueId.append("class", clazz.getName());

        RunnableClassTestDescriptor testEngineClassTestDescriptor =
                TestDescriptorUtils.createClassTestDescriptor(
                        uniqueId,
                        clazz);

        engineDescriptor.addChild(testEngineClassTestDescriptor);

        List<Parameter> parameters = TestEngineReflectionUtils.getParameters(clazz);
        for (int i = 0; i < parameters.size(); i++) {
            Parameter parameter = parameters.get(i);
            uniqueId = uniqueId.append("parameter", String.valueOf(i));

            RunnableParameterTestDescriptor testEngineParameterTestDescriptor =
                    TestDescriptorUtils.createParameterTestDescriptor(
                            uniqueId,
                            clazz,
                            parameter);

            testEngineClassTestDescriptor.addChild(testEngineParameterTestDescriptor);

            List<Method> methods = TestEngineReflectionUtils.getTestMethods(clazz);
            for (Method method : methods) {
                uniqueId = uniqueId.append("method", method.getName());

                RunnableMethodTestDescriptor methodTestDescriptor =
                        TestDescriptorUtils.createMethodTestDescriptor(
                                uniqueId,
                                clazz,
                                parameter,
                                method);

                testEngineParameterTestDescriptor.addChild(methodTestDescriptor);

                uniqueId = uniqueId.removeLastSegment();
            }

            testEngineParameterTestDescriptor.prune();
            uniqueId = uniqueId.removeLastSegment();
        }
    }
}