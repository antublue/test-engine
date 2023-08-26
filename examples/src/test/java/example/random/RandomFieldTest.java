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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.StringArgument;

/** Example test */
public class RandomFieldTest {

    @TestEngine.Argument protected StringArgument stringArgument;

    @TestEngine.RandomBoolean protected boolean randomBoolean;
    @TestEngine.RandomInteger protected int randomInteger;
    @TestEngine.RandomLong protected long randomLong;
    @TestEngine.RandomFloat protected float randomFloat;
    @TestEngine.RandomDouble protected double randomDouble;

    @TestEngine.RandomBigInteger(
            minimum = "-10000000000000000000",
            maximum = "10000000000000000000")
    protected BigInteger randomBigInteger;

    @TestEngine.RandomBigDecimal(
            minimum = "-10000000000000000000",
            maximum = "10000000000000000000")
    protected BigDecimal randomBigDecimal;

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
        System.out.println("randomBigDecimal [" + randomBigDecimal.toPlainString() + "]");
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
