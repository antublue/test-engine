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
import org.antublue.test.engine.api.Named;
import org.antublue.test.engine.api.TestEngine;

/** Example test */
public class NamedComplexArgumentTest1 {

    @TestEngine.Argument @TestEngine.AutoClose.AfterAll
    protected Named<ComplexArgument> complexArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<Named<ComplexArgument>> arguments() {
        return Stream.of(
                Named.of("A", new ComplexArgument("http://foo.bar")),
                Named.of("B", new ComplexArgument("http://bar.foo")));
    }

    @TestEngine.Prepare
    public static void prepare() {
        System.out.println("prepare()");
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll(" + complexArgument.getName() + ")");
        complexArgument.getPayload().initialize();
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach(" + complexArgument.getName() + ")");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + complexArgument.getName() + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + complexArgument.getName() + ")");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach(" + complexArgument.getName() + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll(" + complexArgument.getName() + ")");
    }

    @TestEngine.Conclude
    public static void conclude() {
        System.out.println("conclude()");
    }

    public static class ComplexArgument implements AutoCloseable {

        private final String url;

        public ComplexArgument(String url) {
            this.url = url;
        }

        public void initialize() {
            System.out.println("initialize(" + url + ")");
        }

        public void close() {
            System.out.println("close(" + url + ")");
        }
    }
}
