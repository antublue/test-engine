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

package org.antublue.test.engine.testing.extension.parameterized;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.Extension;

/** Example Extension */
@SuppressWarnings("unchecked")
public class ParameterizedTest2Extension implements Extension {

    private static final List<String> EXPECTED = new ArrayList<>();

    static {
        EXPECTED.add("extension.postInstantiateMethodsCallback()");
        EXPECTED.add("prepare()");
        EXPECTED.add("extension.postPrepareMethodsCallback()");
        for (int i = 0; i < 2; i++) {
            EXPECTED.add("beforeAll(StringArgument " + i + ")");
            EXPECTED.add("extension.postBeforeAllMethodsCallback()");
            for (int j = 0; j < 2; j++) {
                EXPECTED.add("beforeEach(StringArgument " + i + ")");
                EXPECTED.add("extension.postBeforeEachMethodsCallback()");
                EXPECTED.add("extension.preTestMethodsCallback()");
                EXPECTED.add("test" + (j + 1) + "(StringArgument " + i + ")");
                EXPECTED.add("extension.postTestMethodsCallback()");
                EXPECTED.add("afterEach(StringArgument " + i + ")");
                EXPECTED.add("extension.postAfterEachMethodsCallback()");
            }
            EXPECTED.add("afterAll(StringArgument " + i + ")");
            EXPECTED.add("extension.postAfterAllMethodsCallback()");
        }
        EXPECTED.add("conclude()");
        EXPECTED.add("extension.postConcludeMethodsCallback()");
    }

    @Override
    public void postInstantiateCallback(Object testInstance) {
        System.out.println(
                String.format(
                        "%s postInstantiateCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((ParameterizedTest2) testInstance)
                .ACTUAL.add("extension.postInstantiateMethodsCallback()");
    }

    @Override
    public void postPrepareMethodsCallback(Object testInstance) {
        System.out.println(
                String.format(
                        "%s postPrepareCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((ParameterizedTest2) testInstance).ACTUAL.add("extension.postPrepareMethodsCallback()");
    }

    @Override
    public void postBeforeAllMethodsCallback(Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "%s postBeforeAllCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((ParameterizedTest2) testInstance).ACTUAL.add("extension.postBeforeAllMethodsCallback()");
    }

    @Override
    public void postBeforeEachMethodsCallback(Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "%s postBeforeEachMethodsCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((ParameterizedTest2) testInstance).ACTUAL.add("extension.postBeforeEachMethodsCallback()");
    }

    @Override
    public void preTestMethodsCallback(Method method, Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "%s preTestCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((ParameterizedTest2) testInstance).ACTUAL.add("extension.preTestMethodsCallback()");
    }

    @Override
    public void postTestMethodsCallback(Method method, Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "%s postTestMethodsCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((ParameterizedTest2) testInstance).ACTUAL.add("extension.postTestMethodsCallback()");
    }

    @Override
    public void postAfterEachMethodsCallback(Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "%s postAfterEachCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((ParameterizedTest2) testInstance).ACTUAL.add("extension.postAfterEachMethodsCallback()");
    }

    @Override
    public void postAfterAllMethodsCallback(Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "%s postAfterAllCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((ParameterizedTest2) testInstance).ACTUAL.add("extension.postAfterAllMethodsCallback()");
    }

    @Override
    public void postConcludeMethodsCallback(Object testInstance) {
        System.out.println(
                String.format(
                        "%s postConcludeMethodsCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        List<String> ACTUAL = ((ParameterizedTest2) testInstance).ACTUAL;
        ACTUAL.add("extension.postConcludeMethodsCallback()");
        assertThat(ACTUAL).isEqualTo(EXPECTED);
    }
}
