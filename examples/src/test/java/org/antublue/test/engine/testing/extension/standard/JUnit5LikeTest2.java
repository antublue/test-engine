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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.antublue.test.engine.api.Extension;
import org.antublue.test.engine.api.TestEngine;

/** Example test */
@SuppressWarnings("unchecked")
public class JUnit5LikeTest2 {

    public final List<String> ACTUAL = new ArrayList<>();

    @TestEngine.ExtensionSupplier
    public static Collection<Extension> extensions() {
        Collection<Extension> extensions = new ArrayList<>();
        extensions.add(new RandomizeTestMethods());
        return extensions;
    }

    @TestEngine.Prepare
    public void prepare() {
        System.out.println("prepare()");
        ACTUAL.add("prepare()");
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach()");
        ACTUAL.add("beforeEach()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1()");
        ACTUAL.add("test1()");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2()");
        ACTUAL.add("test2()");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
        ACTUAL.add("afterEach()");
    }

    @TestEngine.Conclude
    public void conclude() {
        System.out.println("conclude()");
        ACTUAL.add("conclude()");
    }

    public static class RandomizeTestMethods implements Extension {

        @Override
        public void postTestMethodDiscovery(Class<?> testClass, List<Method> testMethods) {
            Collections.shuffle(testMethods);
        }
    }
}
