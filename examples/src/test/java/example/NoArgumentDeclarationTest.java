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

package example;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.StringArgument;

/** Example test */
public class NoArgumentDeclarationTest {

    @TestEngine.ArgumentSupplier
    public static Stream<StringArgument> arguments() {
        Collection<StringArgument> collection = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            collection.add(StringArgument.of("StringArgument " + i));
        }
        return collection.stream();
    }

    @TestEngine.Prepare
    public void prepare() {
        System.out.println("prepare()");
    }

    @TestEngine.BeforeAll
    public void beforeAll(StringArgument stringArgument) {
        System.out.println("beforeAll(" + stringArgument + ")");
    }

    @TestEngine.BeforeEach
    public void beforeEach(StringArgument stringArgument) {
        System.out.println("beforeEach(" + stringArgument + ")");
    }

    @TestEngine.Test
    public void test1(StringArgument stringArgument) {
        System.out.println("test1(" + stringArgument + ")");
    }

    @TestEngine.Test
    public void test2(StringArgument stringArgument) {
        System.out.println("test2(" + stringArgument + ")");
    }

    @TestEngine.AfterEach
    public void afterEach(StringArgument stringArgument) {
        System.out.println("afterEach(" + stringArgument + ")");
    }

    @TestEngine.AfterAll
    public void afterAll(StringArgument stringArgument) {
        System.out.println("afterAll(" + stringArgument + ")");
    }

    @TestEngine.Conclude
    public void conclude() {
        System.out.println("conclude()");
    }
}
