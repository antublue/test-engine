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

import java.util.ArrayList;
import java.util.Collection;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.extension.Extension;
import org.antublue.test.engine.util.Singleton;

/** Example test */
@SuppressWarnings("unchecked")
public class JUnit5LikeTest {

    static {
        Singleton.register("12345.lifecycle.list", s -> new ArrayList<>());
    }

    @TestEngine.ExtensionSupplier
    public static Collection<Extension> extensions() {
        Collection<Extension> extensions = new ArrayList<>();
        extensions.add(new JUnit5LikeTestExtension());
        return extensions;
    }

    @TestEngine.Prepare
    public void prepare() {
        System.out.println("prepare()");
        ((ArrayList<String>) Singleton.get("12345.lifecycle.list")).add("prepare()");
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach()");
        ((ArrayList<String>) Singleton.get("12345.lifecycle.list")).add("beforeEach()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1()");
        ((ArrayList<String>) Singleton.get("12345.lifecycle.list")).add("test1()");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2()");
        ((ArrayList<String>) Singleton.get("12345.lifecycle.list")).add("test2()");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
        ((ArrayList<String>) Singleton.get("12345.lifecycle.list")).add("afterEach()");
    }

    @TestEngine.Conclude
    public void conclude() {
        System.out.println("conclude()");
        ((ArrayList<String>) Singleton.get("12345.lifecycle.list")).add("conclude()");
    }
}
