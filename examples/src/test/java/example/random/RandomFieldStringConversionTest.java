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

package example.random;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.StringArgument;

/** Example test */
public class RandomFieldStringConversionTest {

    @TestEngine.Argument protected StringArgument stringArgument;

    @TestEngine.Random.Boolean protected String randomBoolean;
    @TestEngine.Random.Integer protected String randomInteger;
    @TestEngine.Random.Long protected String randomLong;
    @TestEngine.Random.Float protected String randomFloat;
    @TestEngine.Random.Double protected String randomDouble;

    @TestEngine.Random.BigInteger(
            minimum = "-10000000000000000000",
            maximum = "10000000000000000000")
    protected String randomBigInteger;

    @TestEngine.Random.BigDecimal(
            minimum = "-10000000000000000000",
            maximum = "10000000000000000000")
    protected String randomBigDecimal;

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
    public void beforeAll() {
        System.out.println("beforeAll(" + stringArgument + ")");
        System.out.println("randomBoolean [" + randomBoolean + "]");
        System.out.println("randomInteger [" + randomInteger + "]");
        System.out.println("randomLong [" + randomLong + "]");
        System.out.println("randomFloat [" + randomFloat + "]");
        System.out.println("randomDouble [" + randomDouble + "]");
        System.out.println("randomBigInteger [" + randomBigInteger + "]");
        System.out.println("randomBigDecimal [" + randomBigDecimal + "]");
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach(" + stringArgument + ")");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + stringArgument + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + stringArgument + ")");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach(" + stringArgument + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll(" + stringArgument + ")");
    }

    @TestEngine.Conclude
    public void conclude() {
        System.out.println("conclude()");
    }
}