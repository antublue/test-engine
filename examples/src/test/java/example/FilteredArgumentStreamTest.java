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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Named;
import org.antublue.test.engine.api.TestEngine;

/** Example test */
public class FilteredArgumentStreamTest {

    @TestEngine.Argument public Named<String> argument;

    @TestEngine.ArgumentSupplier
    public static Stream<Named<String>> arguments() {
        return ArgumentSupplier.arguments(argument -> !argument.getPayload().contains("b"));
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + argument + ")");
        assertThat(argument.getPayload()).isNotEqualTo("b");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + argument + ")");
        assertThat(argument.getPayload()).isNotEqualTo("b");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }

    private static class ArgumentSupplier {

        private static final String[] VALUES = {"a", "b", "c"};

        private ArgumentSupplier() {
            // DO NOTHING
        }

        public static Stream<Named<String>> arguments() {
            Collection<Named<String>> arguments = new ArrayList<>();
            for (String value : VALUES) {
                arguments.add(Named.ofString(value));
            }
            return arguments.stream();
        }

        public static Stream<Named<String>> arguments(Predicate<Named<String>> predicate) {
            return predicate != null ? arguments().filter(predicate) : arguments();
        }
    }
}
