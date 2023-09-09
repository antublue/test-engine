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
import org.antublue.test.engine.api.extension.Extension;

/** Example Extension */
@SuppressWarnings("unchecked")
public class ParameterizedTest1Extension implements Extension {

    private static final List<String> EXPECTED = new ArrayList<>();

    static {
        EXPECTED.add("extension.postCreateTestInstance()");
        EXPECTED.add("prepare()");
        EXPECTED.add("extension.prepare()");
        for (int i = 0; i < 2; i++) {
            EXPECTED.add("beforeAll(StringArgument " + i + ")");
            EXPECTED.add("extension.beforeAll()");
            for (int j = 0; j < 2; j++) {
                EXPECTED.add("beforeEach(StringArgument " + i + ")");
                EXPECTED.add("extension.beforeEach()");
                EXPECTED.add("extension.beforeTest()");
                EXPECTED.add("test" + (j + 1) + "(StringArgument " + i + ")");
                EXPECTED.add("extension.afterTest()");
                EXPECTED.add("afterEach(StringArgument " + i + ")");
                EXPECTED.add("extension.afterEach()");
            }
            EXPECTED.add("afterAll(StringArgument " + i + ")");
            EXPECTED.add("extension.afterAll()");
        }
        EXPECTED.add("conclude()");
        EXPECTED.add("extension.conclude()");
    }

    @Override
    public void postCreateTestInstance(Object testInstance) {
        System.out.println(
                String.format(
                        "%s postCreateTestInstance(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((ParameterizedTest1) testInstance).ACTUAL.add("extension.postCreateTestInstance()");
    }

    @Override
    public void prepare(Object testInstance) {
        System.out.println(
                String.format(
                        "%s prepare(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((ParameterizedTest1) testInstance).ACTUAL.add("extension.prepare()");
    }

    @Override
    public void beforeAll(Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "%s beforeAll(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((ParameterizedTest1) testInstance).ACTUAL.add("extension.beforeAll()");
    }

    @Override
    public void beforeEach(Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "%s beforeEach(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((ParameterizedTest1) testInstance).ACTUAL.add("extension.beforeEach()");
    }

    @Override
    public void beforeTest(Object testInstance, Argument testArgument, Method testMethod) {
        System.out.println(
                String.format(
                        "%s beforeTest(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((ParameterizedTest1) testInstance).ACTUAL.add("extension.beforeTest()");
    }

    @Override
    public void afterTest(Object testInstance, Argument testArgument, Method testMethod) {
        System.out.println(
                String.format(
                        "%s afterTest(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((ParameterizedTest1) testInstance).ACTUAL.add("extension.afterTest()");
    }

    @Override
    public void afterEach(Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "%s afterEach(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((ParameterizedTest1) testInstance).ACTUAL.add("extension.afterEach()");
    }

    @Override
    public void afterAll(Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "%s afterAll(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((ParameterizedTest1) testInstance).ACTUAL.add("extension.afterAll()");
    }

    @Override
    public void conclude(Object testInstance) {
        System.out.println(
                String.format(
                        "%s conclude(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        List<String> ACTUAL = ((ParameterizedTest1) testInstance).ACTUAL;
        ACTUAL.add("extension.conclude()");
        assertThat(ACTUAL).isEqualTo(EXPECTED);
    }
}
