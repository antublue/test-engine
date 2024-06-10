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

package example.locking;

import static org.assertj.core.api.Fail.fail;

import java.util.stream.Stream;
import org.antublue.test.engine.api.Context;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.support.named.NamedInteger;

/** Example test */
public class LockModeTest1 {

    public static final String PREFIX = "LockModeTest";
    public static final String LOCK_NAME = PREFIX + ".lock";
    public static final String COUNTER_NAME = PREFIX + ".counter";

    @TestEngine.Context protected static Context context;

    @TestEngine.Argument public NamedInteger argument;

    @TestEngine.ArgumentSupplier
    public static Stream<NamedInteger> arguments() {
        return Stream.of(NamedInteger.of(1), NamedInteger.of(2), NamedInteger.of(3));
    }

    @TestEngine.Prepare
    public static void prepare() {
        System.out.println("prepare()");
        context.getStore().computeIfAbsent(COUNTER_NAME, k -> 0);
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach()");
    }

    @TestEngine.Test
    @TestEngine.Lock(name = LOCK_NAME, mode = TestEngine.LockMode.READ_WRITE)
    @TestEngine.Unlock(name = LOCK_NAME, mode = TestEngine.LockMode.READ_WRITE)
    public void test1() {
        System.out.println("test1()");

        int count = context.getStore().get(COUNTER_NAME);
        if (count != 0) {
            fail("expected count = 0");
        }

        count++;
        context.getStore().put(COUNTER_NAME, count);

        count = context.getStore().get(COUNTER_NAME);
        if (count != 1) {
            fail("expected count = 1");
        }

        count--;
        context.getStore().put(COUNTER_NAME, count);

        count = context.getStore().get(COUNTER_NAME);
        if (count != 0) {
            fail("expected count = 0");
        }
    }

    @TestEngine.Test
    @TestEngine.Lock(name = LOCK_NAME, mode = TestEngine.LockMode.READ)
    @TestEngine.Unlock(name = LOCK_NAME, mode = TestEngine.LockMode.READ)
    public void test2() {
        System.out.println("test2()");

        int count = context.getStore().get(COUNTER_NAME);
        if (count != 0) {
            fail("expected count = 0");
        }
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }

    @TestEngine.Conclude
    public static void conclude() {
        System.out.println("conclude()");
    }
}
