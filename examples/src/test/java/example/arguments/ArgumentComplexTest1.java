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
public class ArgumentComplexTest1 {

    @TestEngine.Argument public Argument<ComplexArgument> argument;

    private ComplexArgument customArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<Argument<ComplexArgument>> arguments() {
        Collection<Argument<ComplexArgument>> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add(
                    Argument.of(
                            "ComplexArgument(" + i + ")",
                            ComplexArgument.of("FirstName" + i, "LastName" + i)));
        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
        customArgument = argument.getPayload();
    }

    @TestEngine.Test
    public void test1() {
        System.out.println(
                "test1("
                        + customArgument.getFirstName()
                        + " "
                        + customArgument.getLastName()
                        + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println(
                "test1("
                        + customArgument.getFirstName()
                        + " "
                        + customArgument.getLastName()
                        + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }

    public static class ComplexArgument {

        private final String firstName;
        private final String lastName;

        private ComplexArgument(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public static ComplexArgument of(String firstName, String lastName) {
            return new ComplexArgument(firstName, lastName);
        }
    }
}
