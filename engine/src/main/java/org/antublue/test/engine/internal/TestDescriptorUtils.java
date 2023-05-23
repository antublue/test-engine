/*
 * Copyright (C) 2022-2023 The AntuBLUE test-engine project authors
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

import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.internal.descriptor.ArgumentTestDescriptor;
import org.antublue.test.engine.internal.descriptor.ClassTestDescriptor;
import org.antublue.test.engine.internal.descriptor.MethodTestDescriptor;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.util.Switch;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

import java.lang.reflect.Method;

/**
 * Class to contain TestDescriptor utility methods
 */
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
     * @param uniqueId uniqueId
     * @param clazz clazz
     * @return the return value
     */
    public static ClassTestDescriptor createClassTestDescriptor(
            UniqueId uniqueId, Class<?> clazz) {
        return new ClassTestDescriptor(
                uniqueId,
                TestEngineReflectionUtils.getDisplayName(clazz),
                clazz);
    }

    /**
     * Method to create an ArgumentTestDescriptor
     *
     * @param uniqueId uniqueId
     * @param clazz clazz
     * @param argument argument
     * @return the return value
     */
    public static ArgumentTestDescriptor createArgumentTestDescriptor(
            UniqueId uniqueId, Class<?> clazz, Argument argument) {
        if (TestEngineReflectionUtils.getArgumentSupplierMethod(clazz) == null) {
            throw new TestClassConfigurationException(
                    String.format(
                            "Test class [%s] must declare a static @TestEngine.ArgumentSupplier method",
                            clazz.getName()));
        }

        if (TestEngineReflectionUtils.getArgumentField(clazz) == null) {
            throw new TestClassConfigurationException(
                    String.format(
                            "Test class [%s] must declare a @TestEngine.Argument field",
                            clazz.getName()));
        }

        return new ArgumentTestDescriptor(uniqueId, argument.name(), clazz, argument);
    }

    /**
     * Method to create a MethodTestDescriptor
     *
     * @param uniqueId uniqueId
     * @param clazz clazz
     * @param argument argument
     * @param method method
     * @return the return value
     */
    public static MethodTestDescriptor createMethodTestDescriptor(
            UniqueId uniqueId, Class<?> clazz, Argument argument, Method method) {
        return new MethodTestDescriptor(
                uniqueId,
                TestEngineReflectionUtils.getDisplayName(method),
                clazz,
                argument,
                method);
    }

    /**
     *
     * @param testDescriptor testDescriptor
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
     * @param testDescriptor testDescriptor
     * @param indent indent
     */
    private static void trace(TestDescriptor testDescriptor, int indent) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            stringBuilder.append(" ");
        }

        Switch.switchType(testDescriptor,
                Switch.switchCase(
                        MethodTestDescriptor.class,
                        methodTestDescriptor ->
                                stringBuilder
                                        .append("method -> ")
                                        .append(methodTestDescriptor.getUniqueId())
                                        .append("()")),
                Switch.switchCase(
                        ArgumentTestDescriptor.class,
                        argumentTestDescriptor ->
                                stringBuilder
                                        .append("argument -> ")
                                        .append(argumentTestDescriptor.getUniqueId())),
                Switch.switchCase(
                        ClassTestDescriptor.class,
                        classTestDescriptor ->
                                stringBuilder
                                        .append("class -> ")
                                        .append(classTestDescriptor.getUniqueId())),
                Switch.switchCase(
                        EngineDescriptor.class,
                        engineDescriptor ->
                                stringBuilder
                                        .append("engine -> ")
                                        .append(engineDescriptor.getDisplayName())));

        LOGGER.trace(stringBuilder.toString());

        testDescriptor
                .getChildren()
                .forEach(t -> trace(t, indent + 2));
    }
}
