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

package org.antublue.test.engine.internal.descriptor;

import org.antublue.test.engine.api.Parameter;
import org.junit.platform.engine.UniqueId;

import java.lang.reflect.Method;

/**
 * Class to create test descriptors
 */
public class TestDescriptorFactory {

    /**
     * Constructor
     */
    private TestDescriptorFactory() {
        // DO NOTHING
    }

    /**
     * Method to create a test class test descriptor
     *
     * @param uniqueId
     * @param clazz
     * @return
     */
    public static ClassTestDescriptor createTestEngineTestClassTestDescriptor(
            UniqueId uniqueId, Class<?> clazz) {
        return new ClassTestDescriptor(uniqueId, clazz.getName(), clazz);
    }

    /**
     * Method to create a test parameter test descriptor
     *
     * @param uniqueId
     * @param clazz
     * @param parameter
     * @return
     */
    public static ParameterTestDescriptor createTestEngineTestParameterTestDescriptor(
            UniqueId uniqueId, Class<?> clazz, Parameter parameter) {
        return new ParameterTestDescriptor(uniqueId, parameter.name(), clazz, parameter);
    }

    /**
     * Method to create a test method test descriptor
     *
     * @param uniqueId
     * @param clazz
     * @param parameter
     * @param method
     * @return
     */
    public static MethodTestDescriptor createTestEngineTestMethodTestDescriptor(
            UniqueId uniqueId, Class<?> clazz, Parameter parameter, Method method) {
        return new MethodTestDescriptor(uniqueId, method.getName(), clazz, parameter, method);
    }
}
