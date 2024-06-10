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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Extension;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.support.named.NamedString;

/** Example test */
public class ParameterizedTest3 {

    public static final List<String> ACTUAL = new ArrayList<>();

    @TestEngine.Argument protected NamedString argument;

    @TestEngine.ArgumentSupplier
    public static Stream<NamedString> arguments() {
        Collection<NamedString> collection = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            collection.add(NamedString.of("StringArgument " + i));
        }
        return collection.stream();
    }

    @TestEngine.ExtensionSupplier
    public static Stream<Extension> extensionSupplier() {
        Collection<Extension> collection = new ArrayList<>();
        collection.add(new ShuffleTestMethodsExtension());
        return collection.stream();
    }

    @TestEngine.Prepare
    public static void prepare() {
        System.out.println("prepare()");
        ACTUAL.add("prepare()");
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll(" + argument + ")");
        ACTUAL.add("beforeAll(" + argument + ")");
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach(" + argument + ")");
        ACTUAL.add("beforeEach(" + argument + ")");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + argument + ")");
        ACTUAL.add("test1(" + argument + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + argument + ")");
        ACTUAL.add("test2(" + argument + ")");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach(" + argument + ")");
        ACTUAL.add("afterEach(" + argument + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll(" + argument + ")");
        ACTUAL.add("afterAll(" + argument + ")");
    }

    @TestEngine.Conclude
    public static void conclude() {
        System.out.println("conclude()");
        ACTUAL.add("conclude()");
    }

    public static class ShuffleTestMethodsExtension implements Extension {

        @Override
        public void postTestMethodDiscoveryCallback(Class<?> testClass, List<Method> testMethods) {
            Collections.shuffle(testMethods);
        }
    }
}
