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

package org.antublue.test.engine.internal;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Class to store TestDescriptors based on UniqueId
 */
public final class TestEngineTestDescriptorStore {

    private static final TestEngineTestDescriptorStore INSTANCE = new TestEngineTestDescriptorStore();

    private final Map<UniqueId, TestDescriptor> testDescriptorMap;

    /**
     * Method to get an instance of the TestEngineTestDescriptorStore
     * @return
     */
    public static TestEngineTestDescriptorStore getInstance() {
        return INSTANCE;
    }

    /**
     * Constructor
     */
    private TestEngineTestDescriptorStore() {
        testDescriptorMap = new LinkedHashMap<>();
    }

    /**
     * Method to store the EngineDescriptor and all children
     *
     * @param engineDescriptor
     */
    public void put(EngineDescriptor engineDescriptor) {
        store(engineDescriptor);
    }

    /**
     * Method to get a TestDescriptor based on UniqueId
     *
     * @param uniqueId
     * @return
     */
    public Optional<TestDescriptor> get(UniqueId uniqueId) {
        return Optional.ofNullable(testDescriptorMap.get(uniqueId));
    }

    /**
     * Method to recursively store TestDescriptors
     *
     * @param testDescriptor
     */
    private void store(TestDescriptor testDescriptor) {
        testDescriptorMap.put(testDescriptor.getUniqueId(), testDescriptor);
        testDescriptor.getChildren().forEach((Consumer<TestDescriptor>) testDescriptor1 -> store(testDescriptor1));
    }
}
