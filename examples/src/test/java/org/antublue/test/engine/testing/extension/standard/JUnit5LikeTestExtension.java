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

package org.antublue.test.engine.testing.extension.standard;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.extension.Extension;

/** Example Extension */
@SuppressWarnings("unchecked")
public class JUnit5LikeTestExtension implements Extension {

    private static final List<String> EXPECTED = new ArrayList<>();

    static {
        EXPECTED.add("extension.postInstantiateCallback()");
        EXPECTED.add("prepare()");
        EXPECTED.add("extension.postPrepareCallback()");
        for (int i = 0; i < 2; i++) {
            EXPECTED.add("beforeEach()");
            EXPECTED.add("extension.postBeforeEachCallback()");
            EXPECTED.add("extension.preTestCallback()");
            EXPECTED.add("test" + (i + 1) + "()");
            EXPECTED.add("extension.postTestCallback()");
            EXPECTED.add("afterEach()");
            EXPECTED.add("extension.postAfterEachCallback()");
        }
        EXPECTED.add("conclude()");
        EXPECTED.add("extension.postConcludeCallback()");
    }

    @Override
    public void postInstantiateCallback(Object testInstance) {
        System.out.println(
                String.format(
                        "%s postInstantiateCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((JUnit5LikeTest) testInstance).ACTUAL.add("extension.postInstantiateCallback()");
    }

    @Override
    public void postPrepareCallback(Object testInstance) {
        System.out.println(
                String.format(
                        "%s postPrepareCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((JUnit5LikeTest) testInstance).ACTUAL.add("extension.postPrepareCallback()");
    }

    @Override
    public void postBeforeAllCallback(Object testInstance, Argument testArgument) {
        throw new RuntimeException("Shouldn't run");
    }

    @Override
    public void postBeforeEachCallback(Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "%s postBeforeEachCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((JUnit5LikeTest) testInstance).ACTUAL.add("extension.postBeforeEachCallback()");
    }

    @Override
    public void preTestCallback(Object testInstance, Argument testArgument, Method testMethod) {
        System.out.println(
                String.format(
                        "%s preTestCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((JUnit5LikeTest) testInstance).ACTUAL.add("extension.preTestCallback()");
    }

    @Override
    public void postTestCallback(Object testInstance, Argument testArgument, Method testMethod) {
        System.out.println(
                String.format(
                        "%s postTestCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((JUnit5LikeTest) testInstance).ACTUAL.add("extension.postTestCallback()");
    }

    @Override
    public void postAfterEachCallback(Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "%s postAfterEachCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((JUnit5LikeTest) testInstance).ACTUAL.add("extension.postAfterEachCallback()");
    }

    @Override
    public void postAfterAllCallback(Object testInstance, Argument testArgument) {
        throw new RuntimeException("Shouldn't run");
    }

    @Override
    public void postConcludeCallback(Object testInstance) {
        System.out.println(
                String.format(
                        "%s postConcludeCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        List<String> ACTUAL = ((JUnit5LikeTest) testInstance).ACTUAL;
        ACTUAL.add("extension.postConcludeCallback()");
        assertThat(ACTUAL).isEqualTo(EXPECTED);
    }
}