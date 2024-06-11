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

package org.antublue.test.engine.testing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Extension;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.support.named.NamedString;
import org.antublue.test.engine.internal.util.StandardStreams;

/** Example test */
public class SimpleExtensionTest {

    @TestEngine.Random.Long private long randomLong;

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
    public static Stream<Extension> extensions() {
        Collection<Extension> collection = new ArrayList<>();
        collection.add(new SimpleExtension());
        return collection.stream();
    }

    @TestEngine.Prepare
    public void prepare() {
        System.out.println("-> prepare()");
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("-> beforeAll(" + argument + ")");
        System.out.println("-> randomLong = [" + randomLong + "]");
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("-> beforeEach(" + argument + ")");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("-> test1(" + argument + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("-> test2(" + argument + ")");
    }

    @TestEngine.Test
    public void test3() {
        System.out.println("-> test3(" + argument + ")");
    }

    @TestEngine.Test
    public void test4() {
        System.out.println("-> test4(" + argument + ")");
    }

    @TestEngine.Test
    public void test5() {
        System.out.println("-> test5(" + argument + ")");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("-> afterEach(" + argument + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("-> afterAll(" + argument + ")");
    }

    @TestEngine.Conclude
    public void conclude() {
        System.out.println("-> conclude()");
    }

    public static class SimpleExtension implements Extension {

        @Override
        public void postInstantiateCallback(Object instance) throws Throwable {
            StandardStreams.println(
                    "postInstantiateCallback() instance [%s]", instance.getClass().getName());
        }

        @Override
        public void preDestroyCallback(Class<?> clazz, Object testInstance) {
            StandardStreams.println(
                    "preDestroyCallback() class [%s] instance is not null [%s]",
                    clazz.getName(), testInstance != null);
        }
    }
}
