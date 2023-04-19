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
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.engine.support.descriptor.UriSource;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

public final class MethodTestDescriptor extends AbstractTestDescriptor {

    private final Class<?> testClass;
    private final Parameter testParameter;
    private final Method testMethod;

    public MethodTestDescriptor(
            UniqueId uniqueId,
            String displayName,
            Class<?> testClass,
            Parameter testParameter,
            Method testMethod) {
        super(uniqueId, displayName);
        this.testClass = testClass;
        this.testParameter = testParameter;
        this.testMethod = testMethod;
        this.testMethod.setAccessible(true);
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(MethodSource.from(testMethod));
    }

    @Override
    public Type getType() {
        return Type.TEST;
    }

    @Override
    public boolean isTest() {
        return true;
    }

    @Override
    public boolean isContainer() {
        return false;
    }

    public Class<?> getTestClass() {
        return testClass;
    }

    public Parameter getTestParameter() {
        return testParameter;
    }

    public Method getTestMethod() {
        return testMethod;
    }

    public static class CustomUriSource implements UriSource {

        private final Class<?> testClass;
        private final Method testMethod;
        private final Parameter testParameter;

        private CustomUriSource(Class<?> testClass, Method testMethod, Parameter testParameter) {
            this.testClass = testClass;
            this.testMethod = testMethod;
            this.testParameter = testParameter;
        }

        public Class<?> getJavaClass() {
            return testClass;
        }

        public Method getJavaMethod() {
            return testMethod;
        }

        public String getMethodName() {
            return testMethod.getName();
        }

        public String getMethodParameterTypes() {
            return null;
        }

        @Override
        public URI getUri() {
            try {
                return new URI("urn:" + testMethod.getName() + "/" + testParameter.hashCode());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
