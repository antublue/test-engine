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
import java.util.List;
import org.antublue.test.engine.api.Extension;
import org.antublue.test.engine.api.TestEngine;

/** Example test */
@SuppressWarnings("unchecked")
public class JUnit5LikeTest3 {

    public final List<String> ACTUAL = new ArrayList<>();

    @TestEngine.ExtensionSupplier
    public static Collection<Extension> extensions() {
        Collection<Extension> extensions = new ArrayList<>();
        extensions.add(new JUnit5LikeTest3Extension());
        return extensions;
    }

    @TestEngine.Prepare
    public void prepare() {
        System.out.println("prepare()");
        ACTUAL.add("prepare()");
    }

    @TestEngine.Prepare
    public void prepare2() {
        System.out.println("prepare2()");
        ACTUAL.add("prepare2()");
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach()");
        ACTUAL.add("beforeEach()");
    }

    @TestEngine.BeforeEach
    public void beforeEach2() {
        System.out.println("beforeEach2()");
        ACTUAL.add("beforeEach2()");
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

    @TestEngine.AfterEach
    public void afterEach2() {
        System.out.println("afterEach2()");
        ACTUAL.add("afterEach2()");
    }

    @TestEngine.Conclude
    public void conclude() {
        System.out.println("conclude()");
        ACTUAL.add("conclude()");
    }
}