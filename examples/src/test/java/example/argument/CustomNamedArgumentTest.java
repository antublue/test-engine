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

import org.antublue.test.engine.api.Named;
import org.antublue.test.engine.api.TestEngine;

import java.util.stream.Stream;

/** Example test */
public class CustomNamedArgumentTest {

    @TestEngine.Argument @TestEngine.AutoClose.AfterAll
    protected Named<ComplexArgument> argument;

    @TestEngine.ArgumentSupplier
    public static Stream<ComplexArgument> arguments() {
        return Stream.of(
                ComplexArgument.of("A", "http://foo.bar"),
                ComplexArgument.of("B", "http://bar.foo"));
    }

    @TestEngine.Prepare
    public void prepare() {
        System.out.println("prepare()");
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll(" + argument.getName() + ")");
        argument.getPayload().initialize();
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach(" + argument.getName() + ")");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + argument.getName() + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + argument.getName() + ")");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach(" + argument.getName() + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll(" + argument.getName() + ")");
    }

    @TestEngine.Conclude
    public void conclude() {
        System.out.println("conclude()");
    }

    public static class ComplexArgument implements Named<ComplexArgument>, AutoCloseable {

        private final String name;
        private final String url;

        public ComplexArgument(String name, String url) {
            this.name = name;
            this.url = url;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ComplexArgument getPayload() {
            return this;
        }

        public void initialize() {
            System.out.println("initialize(" + url + ")");
        }

        public void close() {
            System.out.println("close(" + url + ")");
        }

        public static ComplexArgument of(String name, String url) {
            return new ComplexArgument(name, url);
        }
    }
}
