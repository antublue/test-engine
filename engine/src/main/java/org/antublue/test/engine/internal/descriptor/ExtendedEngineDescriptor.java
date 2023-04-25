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

import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Class to implement a extended EngineDescriptor
 */
@SuppressWarnings("unchecked")
public final class ExtendedEngineDescriptor extends EngineDescriptor {

    /***
     * Constructor
     *
     * @param uniqueId
     * @param displayName
     */
    public ExtendedEngineDescriptor(UniqueId uniqueId, String displayName) {
        super(uniqueId, displayName);
    }

    /**
     * Method to get the TestSource
     *
     * @return
     */
    @Override
    public Optional<TestSource> getSource() {
        return Optional.ofNullable(null);
    }

    /**
     * Method to get the test descriptor Type
     *
     * @return
     */
    @Override
    public Type getType() {
        return Type.CONTAINER;
    }

    /**
     * Method to return whether the test descriptor is a test
     *
     * @return
     */
    @Override
    public boolean isTest() {
        return false;
    }

    /**
     * Method to return whether the test descriptor is a container
     *
     * @return
     */
    @Override
    public boolean isContainer() {
        return true;
    }

    /**
     * Method to get a List of children cast as a specific class
     *
     * @param clazz
     * @return
     * @param <T>
     */
    public <T> List<T> getChildren(Class<T> clazz) {
        final List<T> list = new ArrayList<>();
        getChildren().forEach(testDescriptor -> list.add((T) testDescriptor));
        return list;
    }
}
