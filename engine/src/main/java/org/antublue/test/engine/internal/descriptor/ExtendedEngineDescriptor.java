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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

/** Class to implement a extended EngineDescriptor */
@SuppressWarnings("unchecked")
public final class ExtendedEngineDescriptor extends EngineDescriptor {

    /***
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     */
    public ExtendedEngineDescriptor(UniqueId uniqueId, String displayName) {
        super(uniqueId, displayName);
    }

    /**
     * Method to get the TestSource
     *
     * @return the return value
     */
    @Override
    public Optional<TestSource> getSource() {
        return Optional.empty();
    }

    /**
     * Method to get the test descriptor Type
     *
     * @return the return value
     */
    @Override
    public Type getType() {
        return Type.CONTAINER;
    }

    /**
     * Method to return whether the test descriptor is a test
     *
     * @return the return value
     */
    @Override
    public boolean isTest() {
        return false;
    }

    /**
     * Method to return whether the test descriptor is a container
     *
     * @return the return value
     */
    @Override
    public boolean isContainer() {
        return true;
    }

    /**
     * Method to get a List of children cast as a specific class
     *
     * @param clazz clazz
     * @return the return value
     * @param <T> the return type
     */
    public <T> List<T> getChildren(Class<T> clazz) {
        final List<T> list = new ArrayList<>();
        getChildren().forEach(testDescriptor -> list.add((T) testDescriptor));
        return list;
    }
}
