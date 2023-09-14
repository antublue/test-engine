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

package org.antublue.test.engine.testing.order.method;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Extension;
import org.antublue.test.engine.api.TestEngine;

/** Example test */
public class StandardTestWithMethodOrderTest {

    @TestEngine.ExtensionSupplier
    public static Stream<Extension> extensions() {
        Collection<Extension> collection = new ArrayList<>();
        collection.add(new ShuffleTestMethods());
        return collection.stream();
    }

    @TestEngine.Prepare
    public void prepare() {
        System.out.println("prepare()");
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1()");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2()");
    }

    @TestEngine.Test
    public void test3() {
        System.out.println("test3()");
    }

    @TestEngine.Test
    public void test4() {
        System.out.println("test4()");
    }

    @TestEngine.Test
    public void test5() {
        System.out.println("test5()");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
    }

    @TestEngine.Conclude
    public void conclude() {
        System.out.println("conclude()");
    }

    private static class ShuffleTestMethods implements Extension {

        @Override
        public void postTestMethodDiscovery(Class<?> testClass, List<Method> testMethods) {
            Collections.shuffle(testMethods);
        }
    }
}
