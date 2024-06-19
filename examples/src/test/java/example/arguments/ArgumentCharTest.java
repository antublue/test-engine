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

package example.arguments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;

/** Example test */
public class ArgumentCharTest {

    @TestEngine.Argument public Argument<Character> argument;

    @TestEngine.ArgumentSupplier
    public static Stream<Argument<Character>> arguments() {
        char[] characters = new char[] {'a', 'b', 'c', 'd', 'e'};
        Collection<Argument<Character>> collection = new ArrayList<>();
        for (char value : characters) {
            collection.add(Argument.ofChar(value));
        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + argument.getPayload() + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + argument.getPayload() + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
