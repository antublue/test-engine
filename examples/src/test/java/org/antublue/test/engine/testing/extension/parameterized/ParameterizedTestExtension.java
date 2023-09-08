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
import static org.assertj.core.api.Fail.fail;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.extension.Extension;
import org.antublue.test.engine.util.Singleton;

/** Example Extension */
@SuppressWarnings("unchecked")
public class ParameterizedTestExtension implements Extension {

    private static final List<String> EXPECTED = new ArrayList<>();

    static {
        EXPECTED.add("prepare()");
        EXPECTED.add("postPrepare()");
        for (int i = 0; i < 2; i++) {
            EXPECTED.add("beforeAll(StringArgument " + i + ")");
            EXPECTED.add("postBeforeAll()");
            for (int j = 0; j < 2; j++) {
                EXPECTED.add("beforeEach(StringArgument " + i + ")");
                EXPECTED.add("postBeforeEach()");
                EXPECTED.add("preTest()");
                EXPECTED.add("test" + (j + 1) + "(StringArgument " + i + ")");
                EXPECTED.add("postTest()");
                EXPECTED.add("afterEach(StringArgument " + i + ")");
                EXPECTED.add("postAfterEach()");
            }
            EXPECTED.add("afterAll(StringArgument " + i + ")");
            EXPECTED.add("postAfterAll()");
        }
        EXPECTED.add("conclude()");
        EXPECTED.add("postConclude()");
    }

    @Override
    public void postPrepare(Object testInstance) {
        System.out.println(
                String.format(
                        "%s postPrepare(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((ArrayList<String>) Singleton.get("2468.lifecycle.list")).add("postPrepare()");
    }

    @Override
    public void postBeforeAll(Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "%s postBeforeAll(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((ArrayList<String>) Singleton.get("2468.lifecycle.list")).add("postBeforeAll()");
    }

    @Override
    public void postBeforeEach(Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "%s postBeforeEach(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((ArrayList<String>) Singleton.get("2468.lifecycle.list")).add("postBeforeEach()");
    }

    @Override
    public void preTest(Object testInstance, Argument testArgument, Method testMethod) {
        System.out.println(
                String.format(
                        "%s preTest(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((ArrayList<String>) Singleton.get("2468.lifecycle.list")).add("preTest()");
    }

    @Override
    public void postTest(Object testInstance, Argument testArgument, Method testMethod) {
        System.out.println(
                String.format(
                        "%s postTest(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((ArrayList<String>) Singleton.get("2468.lifecycle.list")).add("postTest()");
    }

    @Override
    public void postAfterEach(Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "%s postAfterEach(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((ArrayList<String>) Singleton.get("2468.lifecycle.list")).add("postAfterEach()");
    }

    @Override
    public void postAfterAll(Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "%s postAfterAll(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((ArrayList<String>) Singleton.get("2468.lifecycle.list")).add("postAfterAll()");
    }

    @Override
    public void postConclude(Object testInstance) {
        System.out.println(
                String.format(
                        "%s postConclude(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((ArrayList<String>) Singleton.get("2468.lifecycle.list")).add("postConclude()");
        ArrayList<String> actual = Singleton.get("2468.lifecycle.list");
        assertThat(actual.size()).isEqualTo(EXPECTED.size());
        assertThat(actual).isEqualTo(EXPECTED);
        for (int i = 0; i < actual.size(); i++) {
            if (!actual.get(i).equals(EXPECTED.get(i))) {
                fail(
                        String.format(
                                "index [%d] actual [%s] expected [%s]",
                                i, actual.get(i), EXPECTED.get(i)));
            }
        }
    }
}
