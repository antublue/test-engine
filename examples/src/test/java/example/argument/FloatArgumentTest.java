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

package example.argument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.FloatArgument;

/** Example test */
public class FloatArgumentTest {

    @TestEngine.Argument protected FloatArgument floatArgument;

    @TestEngine.Supplier.Argument
    public static Stream<FloatArgument> arguments() {
        Collection<FloatArgument> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add(FloatArgument.of(i + 0.1f));
        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + floatArgument.value() + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + floatArgument.value() + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
