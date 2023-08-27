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

package org.antublue.test.engine.internal.descriptor;

import java.lang.reflect.Method;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.internal.TestClassConfigurationException;
import org.antublue.test.engine.internal.TestEngineUtils;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;
import org.antublue.test.engine.internal.util.Switch;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

/** Class to contain TestDescriptor utility methods */
public final class TestDescriptorUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestDescriptorUtils.class);

    /** Constructor */
    private TestDescriptorUtils() {
        // DO NOTHING
    }

    /**
     * Method to create a ClassTestDescriptor
     *
     * @param uniqueId uniqueId
     * @param testClass testClass
     * @return the return value
     */
    public static ClassTestDescriptor createClassTestDescriptor(
            UniqueId uniqueId, Class<?> testClass) {
        validateTestClass(testClass);

        return new ClassTestDescriptor(
                uniqueId, TestEngineUtils.singleton().getDisplayName(testClass), testClass);
    }

    /**
     * Method to create an ArgumentTestDescriptor
     *
     * @param uniqueId uniqueId
     * @param testClass testClass
     * @param argument argument
     * @return the return value
     */
    public static ArgumentTestDescriptor createArgumentTestDescriptor(
            UniqueId uniqueId, Class<?> testClass, Argument argument) {
        validateTestClass(testClass);

        return new ArgumentTestDescriptor(uniqueId, argument.name(), testClass, argument);
    }

    /**
     * Method to create a MethodTestDescriptor
     *
     * @param uniqueId uniqueId
     * @param testClass testClass
     * @param method method
     * @param argument argument
     * @return the return value
     */
    public static MethodTestDescriptor createMethodTestDescriptor(
            UniqueId uniqueId, Class<?> testClass, Method method, Argument argument) {
        validateTestClass(testClass);

        return new MethodTestDescriptor(
                uniqueId,
                TestEngineUtils.singleton().getDisplayName(method),
                testClass,
                method,
                argument);
    }

    /**
     * @param testDescriptor testDescriptor
     */
    public static void logTestDescriptorTree(TestDescriptor testDescriptor) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("--------------------");
            LOGGER.trace("Test descriptor tree");
            LOGGER.trace("--------------------");
            logTestDescriptorTree(testDescriptor, 0);
            LOGGER.trace("------------------------");
        }
    }

    /**
     * Method to log the test descriptor tree hierarchy
     *
     * @param testDescriptor testDescriptor
     * @param indent indent
     */
    private static void logTestDescriptorTree(TestDescriptor testDescriptor, int indent) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            stringBuilder.append(" ");
        }

        Switch.switchType(
                testDescriptor,
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

        testDescriptor.getChildren().forEach(t -> logTestDescriptorTree(t, indent + 2));
    }

    private static void validateTestClass(Class<?> testClass) {
        // Validate we have a @TestEngine.ArgumentSupplier method
        if (TestEngineUtils.singleton().getArgumentSupplierMethod(testClass) == null) {
            throw new TestClassConfigurationException(
                    String.format(
                            "Test class [%s] must declare a static @TestEngine.ArgumentSupplier"
                                    + " method",
                            testClass.getName()));
        }

        // Validate we have a @TestEngine.Test method
        if (TestEngineUtils.singleton().getTestMethods(testClass).isEmpty()) {
            throw new TestClassConfigurationException(
                    String.format(
                            "Test class [%s] must declare a @TestEngine.Test method",
                            testClass.getName()));
        }

        // Get other method optional annotated methods
        // which will check for duplicate @TestEngine.Order values
        TestEngineUtils.singleton().getPrepareMethods(testClass);
        TestEngineUtils.singleton().getBeforeAllMethods(testClass);
        TestEngineUtils.singleton().getBeforeEachMethods(testClass);
        TestEngineUtils.singleton().getAfterEachMethods(testClass);
        TestEngineUtils.singleton().getAfterAllMethods(testClass);
        TestEngineUtils.singleton().getConcludeMethods(testClass);
    }
}
