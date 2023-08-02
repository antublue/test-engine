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

package org.antublue.test.engine.internal;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

/** Class to store a TestDescriptor based on UniqueId */
public final class TestDescriptorStore {

    private static final TestDescriptorStore SINGLETON = new TestDescriptorStore();

    private final Map<UniqueId, TestDescriptor> testDescriptorMap;

    /** Constructor */
    private TestDescriptorStore() {
        testDescriptorMap = new LinkedHashMap<>();
    }

    /**
     * Method to get the singleton instance
     *
     * @return the singleton instance
     */
    public static TestDescriptorStore singleton() {
        return SINGLETON;
    }

    /**
     * Method to store the EngineDescriptor and all children
     *
     * @param engineDescriptor the engine descriptor
     */
    public void store(EngineDescriptor engineDescriptor) {
        recursivelyStore(engineDescriptor);
    }

    /**
     * Method to get a TestDescriptor by UniqueId
     *
     * @param uniqueId the unique id
     * @return an Optional that may contain a TestDescriptor
     */
    public Optional<TestDescriptor> get(UniqueId uniqueId) {
        return Optional.ofNullable(testDescriptorMap.get(uniqueId));
    }

    /**
     * Method to recursively store a TestDescriptor and it's children
     *
     * @param testDescriptor the test descriptor
     */
    private void recursivelyStore(TestDescriptor testDescriptor) {
        testDescriptorMap.put(testDescriptor.getUniqueId(), testDescriptor);
        testDescriptor.getChildren().forEach(this::recursivelyStore);
    }
}
