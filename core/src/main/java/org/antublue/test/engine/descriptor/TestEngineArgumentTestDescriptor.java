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

package org.antublue.test.engine.descriptor;

import org.antublue.test.engine.api.Argument;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.FilePosition;

import java.util.Optional;

public class TestEngineArgumentTestDescriptor extends TestEngineAbstractTestDescriptor {

    private final Class<?> testClass;
    private final Argument testArgument;

    public TestEngineArgumentTestDescriptor(UniqueId uniqueId, String displayName, Class<?> testClass, Argument testArgument) {
        super(uniqueId, testArgument.name());
        this.testClass = testClass;
        this.testArgument = testArgument;
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.empty();
    }

    @Override
    public Type getType() {
        return Type.CONTAINER_AND_TEST;
    }

    @Override
    public boolean isTest() {
        return true;
    }

    @Override
    public boolean isContainer() {
        return true;
    }

    public Class<?> getTestClass() {
        return testClass;
    }

    public Argument getTestArgument() {
        return testArgument;
    }
}
