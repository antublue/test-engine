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
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;

import java.util.Optional;

public final class ParameterTestDescriptor extends AbstractTestDescriptor {

    private final Class<?> testClass;
    private final Parameter testParameter;

    public ParameterTestDescriptor(UniqueId uniqueId, String displayName, Class<?> testClass, Parameter testParameter) {
        super(uniqueId, displayName);
        this.testClass = testClass;
        this.testParameter = testParameter;
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(ClassSource.from(testClass));
    }

    @Override
    public Type getType() {
        return Type.CONTAINER;
    }

    @Override
    public boolean isTest() {
        return false;
    }

    @Override
    public boolean isContainer() {
        return true;
    }

    public Class<?> getTestClass() {
        return testClass;
    }

    public Parameter getTestParameter() {
        return testParameter;
    }

    public static class Composite {

        private final Class<?> clazz;

        public Composite(Class<?> clazz) {
            this.clazz = clazz;
        }
    }
}
