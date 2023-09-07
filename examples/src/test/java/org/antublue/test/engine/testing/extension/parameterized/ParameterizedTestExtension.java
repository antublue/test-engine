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
        EXPECTED.add("instantiatedCallback()");
        EXPECTED.add("prepare()");
        EXPECTED.add("prepareCallback()");

        for (int i = 0; i < 2; i++) {
            EXPECTED.add("beforeAll(StringArgument " + i + ")");
            EXPECTED.add("beforeAllCallback()");
            for (int j = 0; j < 2; j++) {
                EXPECTED.add("beforeEach(StringArgument " + i + ")");
                EXPECTED.add("beforeEachCallback()");
                EXPECTED.add("beforeTestCallback()");
                EXPECTED.add("test" + (j + 1) + "(StringArgument " + i + ")");
                EXPECTED.add("afterTestCallback()");
                EXPECTED.add("afterEach(StringArgument " + i + ")");
                EXPECTED.add("afterEachCallback()");
            }
            EXPECTED.add("afterAll(StringArgument " + i + ")");
            EXPECTED.add("afterAllCallback()");
        }

        EXPECTED.add("conclude()");
        EXPECTED.add("concludeCallback()");
    }

    @Override
    public void instantiatedCallback(Object testInstance) {
        System.out.println(
                String.format(
                        "%s instantiateCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((ArrayList<String>) Singleton.get("2468.lifecycle.list")).add("instantiatedCallback()");
    }

    @Override
    public void prepareCallback(Object testInstance) {
        System.out.println(
                String.format(
                        "%s prepareCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((ArrayList<String>) Singleton.get("2468.lifecycle.list")).add("prepareCallback()");
    }

    @Override
    public void beforeAllCallback(Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "%s beforeAllCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((ArrayList<String>) Singleton.get("2468.lifecycle.list")).add("beforeAllCallback()");
    }

    @Override
    public void beforeEachCallback(Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "%s beforeEachCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((ArrayList<String>) Singleton.get("2468.lifecycle.list")).add("beforeEachCallback()");
    }

    @Override
    public void beforeTestCallback(Object testInstance, Argument testArgument, Method testMethod) {
        System.out.println(
                String.format(
                        "%s beforeTestCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((ArrayList<String>) Singleton.get("2468.lifecycle.list")).add("beforeTestCallback()");
    }

    @Override
    public void afterTestCallback(Object testInstance, Argument testArgument, Method testMethod) {
        System.out.println(
                String.format(
                        "%s afterTestCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((ArrayList<String>) Singleton.get("2468.lifecycle.list")).add("afterTestCallback()");
    }

    @Override
    public void afterEachCallback(Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "%s afterEachCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((ArrayList<String>) Singleton.get("2468.lifecycle.list")).add("afterEachCallback()");
    }

    @Override
    public void afterAllCallback(Object testInstance, Argument testArgument) {
        System.out.println(
                String.format(
                        "%s afterAllCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((ArrayList<String>) Singleton.get("2468.lifecycle.list")).add("afterAllCallback()");
    }

    @Override
    public void concludeCallback(Object testInstance) {
        System.out.println(
                String.format(
                        "%s concludeCallback(class [%s])",
                        this.getClass().getSimpleName(), testInstance.getClass().getName()));
        ((ArrayList<String>) Singleton.get("2468.lifecycle.list")).add("concludeCallback()");
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
