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
import org.antublue.test.engine.api.Extension;

/** Example Extension */
@SuppressWarnings("unchecked")
public class JUnit5LikeTest1Extension implements Extension {

    private static final List<String> EXPECTED = new ArrayList<>();

    static {
        EXPECTED.add("extension.postInstantiateMethodsCallback()");
        EXPECTED.add("extension.prePrepareMethodsCallback()");
        EXPECTED.add("prepare()");
        EXPECTED.add("extension.postPrepareMethodsCallback()");
        for (int i = 0; i < 2; i++) {
            EXPECTED.add("extension.preBeforeEachMethodsCallback()");
            EXPECTED.add("beforeEach()");
            EXPECTED.add("extension.postBeforeEachMethodsCallback()");
            EXPECTED.add("extension.preTestMethodsCallback()");
            EXPECTED.add("test" + (i + 1) + "()");
            EXPECTED.add("extension.postTestMethodsCallback()");
            EXPECTED.add("extension.preAfterEachMethodsCallback()");
            EXPECTED.add("afterEach()");
            EXPECTED.add("extension.postAfterEachMethodsCallback()");
        }
        EXPECTED.add("extension.preConcludeMethodsCallback()");
        EXPECTED.add("conclude()");
        EXPECTED.add("extension.postConcludeMethodsCallback()");
    }

    @Override
    public void postInstantiateCallback(Object testInstance) {
        System.out.println(
                String.format(
                        "%s postInstantiateCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((JUnit5LikeTest1) testInstance).ACTUAL.add("extension.postInstantiateMethodsCallback()");
    }

    @Override
    public void prePrepareMethodsCallback(Object testInstance) {
        System.out.println(
                String.format(
                        "%s prePrepareMethodsCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((JUnit5LikeTest1) testInstance).ACTUAL.add("extension.prePrepareMethodsCallback()");
    }

    @Override
    public void postPrepareMethodsCallback(Object testInstance) {
        System.out.println(
                String.format(
                        "%s postPrepareCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((JUnit5LikeTest1) testInstance).ACTUAL.add("extension.postPrepareMethodsCallback()");
    }

    @Override
    public void preBeforeAllMethodsCallback(Object testInstance, Argument testArgument) {
        throw new RuntimeException("Shouldn't run");
    }

    @Override
    public void postBeforeAllMethodsCallback(Object testInstance, Argument testArgument) {
        throw new RuntimeException("Shouldn't run");
    }

    @Override
    public void preBeforeEachMethodsCallback(Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "%s preBeforeEachMethodsCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((JUnit5LikeTest1) testInstance).ACTUAL.add("extension.preBeforeEachMethodsCallback()");
    }

    @Override
    public void postBeforeEachMethodsCallback(Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "%s postBeforeEachMethodsCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((JUnit5LikeTest1) testInstance).ACTUAL.add("extension.postBeforeEachMethodsCallback()");
    }

    @Override
    public void preTestMethodsCallback(Method method, Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "%s preTestCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((JUnit5LikeTest1) testInstance).ACTUAL.add("extension.preTestMethodsCallback()");
    }

    @Override
    public void postTestMethodsCallback(Method method, Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "%s postTestMethodsCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((JUnit5LikeTest1) testInstance).ACTUAL.add("extension.postTestMethodsCallback()");
    }

    @Override
    public void preAfterEachMethodsCallback(Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "%s preAfterEachMethodsCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((JUnit5LikeTest1) testInstance).ACTUAL.add("extension.preAfterEachMethodsCallback()");
    }

    @Override
    public void postAfterEachMethodsCallback(Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "%s postAfterEachCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((JUnit5LikeTest1) testInstance).ACTUAL.add("extension.postAfterEachMethodsCallback()");
    }

    @Override
    public void preAfterAllMethodsCallback(Object testInstance, Argument testArgument) {
        throw new RuntimeException("Shouldn't run");
    }

    @Override
    public void postAfterAllMethodsCallback(Object testInstance, Argument testArgument) {
        throw new RuntimeException("Shouldn't run");
    }

    @Override
    public void preConcludeMethodsCallback(Object testInstance) {
        System.out.println(
                String.format(
                        "%s preConcludeMethodsCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        List<String> ACTUAL = ((JUnit5LikeTest1) testInstance).ACTUAL;
        ACTUAL.add("extension.preConcludeMethodsCallback()");
    }

    @Override
    public void postConcludeMethodsCallback(Object testInstance) {
        System.out.println(
                String.format(
                        "%s postConcludeMethodsCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        List<String> ACTUAL = ((JUnit5LikeTest1) testInstance).ACTUAL;
        ACTUAL.add("extension.postConcludeMethodsCallback()");
        assertThat(ACTUAL).isEqualTo(EXPECTED);
    }
}
