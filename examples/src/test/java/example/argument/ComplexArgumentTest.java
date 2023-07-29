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

import java.util.stream.Stream;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.AbstractArgument;

/** Example test */
public class ComplexArgumentTest {

    @TestEngine.Argument
    @TestEngine.AutoClose(lifecycle = "@TestEngine.AfterAll")
    protected ComplexArgument complexArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<ComplexArgument> arguments() {
        return Stream.of(
                new ComplexArgument("A", "http://foo.bar"),
                new ComplexArgument("B", "http://bar.foo"));
    }

    @TestEngine.Prepare
    public void prepare() {
        System.out.println("prepare()");
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll(" + complexArgument.name() + ")");

        complexArgument.initialize();
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach(" + complexArgument.name() + ")");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + complexArgument.name() + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + complexArgument.name() + ")");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach(" + complexArgument.name() + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll(" + complexArgument.name() + ")");
    }

    @TestEngine.Conclude
    public void conclude() {
        System.out.println("conclude()");
    }

    private static class ComplexArgument extends AbstractArgument implements AutoCloseable {

        private final String name;
        private final String url;

        public ComplexArgument(String name, String url) {
            this.name = name;
            this.url = url;
        }

        public String name() {
            return name;
        }

        public void initialize() {
            System.out.println(name + " -> initialize(" + url + ")");
        }

        public void close() {
            System.out.println(name + " -> close(" + url + ")");
        }
    }
}
