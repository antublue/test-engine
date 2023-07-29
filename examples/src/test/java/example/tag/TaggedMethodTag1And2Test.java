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

package example.tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.StringArgument;

/**
 * Example test
 *
 * <p>All test methods are executed due to the fact that the test engine system properties /
 * environment variables have to be defined during test discovery
 */
public class TaggedMethodTag1And2Test {

    @TestEngine.Argument protected StringArgument stringArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<StringArgument> arguments() {
        Collection<StringArgument> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int value = i * 3;
            collection.add(StringArgument.of(String.valueOf(value)));
        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    @TestEngine.Tag(tag = "/tag1/")
    public void test1() {
        System.out.println("test1(" + stringArgument + ")");
    }

    @TestEngine.Test
    @TestEngine.Tag(tag = "/tag2/")
    public void test2() {
        System.out.println("test2(" + stringArgument + ")");
    }

    @TestEngine.Test
    public void test3() {
        System.out.println("test3(" + stringArgument + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
