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

import java.util.Optional;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Store;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.IntegerArgument;
import org.antublue.test.engine.extras.LockManager;

/** Example test */
public class LockManagerTest2 {

    public static final String PREFIX = "LockManagerTest";
    public static final String LOCK_NAME = PREFIX + ".lock";

    public static final String COUNTER_NAME = PREFIX + ".counter";

    @TestEngine.Argument public IntegerArgument integerArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<IntegerArgument> arguments() {
        return Stream.of(IntegerArgument.of(1), IntegerArgument.of(2), IntegerArgument.of(3));
    }

    @TestEngine.Prepare
    public void prepare() {
        System.out.println("prepare()");
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
    public void test1() {
        try {
            LockManager.getInstance().getLock(LOCK_NAME).writeLock().lock();

            System.out.println(
                    "enter test1() thread = ["
                            + Thread.currentThread().getName()
                            + "] instance = ["
                            + this
                            + "]");

            Optional<Object> optional =
                    Store.getInstance()
                            .putIfAbsent(COUNTER_NAME, s -> new example.locking.Counter());

            example.locking.Counter counter = (example.locking.Counter) optional.get();

            long count = counter.getCount();
            if (count != 0) {
                fail("expected count = 0");
            }

            count = counter.increment();
            if (count != 1) {
                fail("expected count = 1");
            }

            count = counter.decrement();
            if (count != 0) {
                fail("expected count = 0");
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // DO NOTHING
            }
        } finally {
            System.out.println(
                    "leave test1() thread = ["
                            + Thread.currentThread().getName()
                            + "] instance = ["
                            + this
                            + "]");

            LockManager.getInstance().getLock(LOCK_NAME).writeLock().unlock();
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
    public void conclude() {
        System.out.println("conclude()");
    }
}
