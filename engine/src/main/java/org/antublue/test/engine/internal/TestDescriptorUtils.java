/*
 * Copyright 2022-2023 Douglas Hoard
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

package org.antublue.test.engine.internal;

import org.antublue.test.engine.api.Parameter;
import org.antublue.test.engine.internal.descriptor.ClassTestDescriptor;
import org.antublue.test.engine.internal.descriptor.MethodTestDescriptor;
import org.antublue.test.engine.internal.descriptor.ParameterTestDescriptor;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.util.Switch;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public final class TestDescriptorUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestDescriptorUtils.class);

    /**
     * Constructor
     */
    private TestDescriptorUtils() {
        // DO NOTHING
    }

    /**
     * Method to create a ClassTestDescriptor
     *
     * @param uniqueId
     * @param clazz
     * @return
     */
    public static ClassTestDescriptor createClassTestDescriptor(
            UniqueId uniqueId, Class<?> clazz) {
        return new ClassTestDescriptor(
                uniqueId,
                TestEngineReflectionUtils.getDisplayName(clazz),
                clazz);
    }

    /**
     * Method to create a ParameterTestDescriptor
     *
     * @param uniqueId
     * @param clazz
     * @param parameter
     * @return
     */
    public static ParameterTestDescriptor createParameterTestDescriptor(
            UniqueId uniqueId, Class<?> clazz, Parameter parameter) {
        return new ParameterTestDescriptor(uniqueId, parameter.name(), clazz, parameter);
    }

    /**
     * Method to create a MethodTestDescriptor
     *
     * @param uniqueId
     * @param clazz
     * @param parameter
     * @param method
     * @return
     */
    public static MethodTestDescriptor createMethodTestDescriptor(
            UniqueId uniqueId, Class<?> clazz, Parameter parameter, Method method) {
        return new MethodTestDescriptor(
                uniqueId,
                TestEngineReflectionUtils.getDisplayName(method),
                clazz,
                parameter,
                method);
    }

    /**
     *
     * @param testDescriptor
     */
    public static void trace(TestDescriptor testDescriptor) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("--------------------");
            LOGGER.trace("Test descriptor tree");
            LOGGER.trace("--------------------");
            trace(testDescriptor, 0);
            LOGGER.trace("------------------------");
        }
    }

    /**
     * Method to log the test descriptor tree hierarchy
     *
     * @param testDescriptor
     * @param indent
     */
    private static void trace(TestDescriptor testDescriptor, int indent) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            stringBuilder.append(" ");
        }

        Switch.switchType(testDescriptor,
                Switch.switchCase(
                        MethodTestDescriptor.class,
                        testMethodTestDescriptor ->
                                stringBuilder
                                        .append("method -> ")
                                        .append(testMethodTestDescriptor.getUniqueId())
                                        .append("()")),
                Switch.switchCase(
                        ParameterTestDescriptor.class,
                        testEngineParameterTestDescriptor ->
                                stringBuilder
                                        .append("parameter -> ")
                                        .append(testEngineParameterTestDescriptor.getUniqueId())),
                Switch.switchCase(
                        ClassTestDescriptor.class,
                        testClassTestDescriptor ->
                                stringBuilder
                                        .append("class -> ")
                                        .append(testClassTestDescriptor.getUniqueId())),
                Switch.switchCase(
                        EngineDescriptor.class,
                        engineDescriptor ->
                                stringBuilder
                                        .append("engine -> ")
                                        .append(engineDescriptor.getDisplayName())));

        LOGGER.trace(stringBuilder.toString());

        testDescriptor
                .getChildren()
                .forEach((Consumer<TestDescriptor>) testDescriptor1 -> trace(testDescriptor1, indent + 2));
    }
}
