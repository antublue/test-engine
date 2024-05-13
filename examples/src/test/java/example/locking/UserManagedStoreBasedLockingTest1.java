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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Context;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.IntegerArgument;
import org.antublue.test.engine.internal.util.RandomGenerator;

/** Example test */
public class UserManagedStoreBasedLockingTest1 {

    public static final String NAMESPACE = "UserManagedStoreBasedLockingTest";
    public static final String LOCK_NAME = "lock";
    public static final String COUNTER_NAME = "counter";

    static {
        Context.getInstance().getStore(NAMESPACE).computeIfAbsent(COUNTER_NAME, k -> new Counter());
    }

    @TestEngine.Argument public IntegerArgument integerArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<IntegerArgument> arguments() {
        return Stream.of(IntegerArgument.of(1), IntegerArgument.of(2), IntegerArgument.of(3));
    }

    @TestEngine.Prepare
    public void prepare() {
        System.out.println("prepare()");

        assertThat(Context.getInstance().getStore(NAMESPACE) != Context.getInstance().getStore())
                .isTrue();
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
    public void lockingTest() throws InterruptedException {
        System.out.println("lockingTest()");

        ReentrantReadWriteLock reentrantReadWriteLock = null;

        try {
            reentrantReadWriteLock =
                    (ReentrantReadWriteLock)
                            Context.getInstance()
                                    .getStore(NAMESPACE)
                                    .computeIfAbsent(LOCK_NAME, o -> new ReentrantReadWriteLock());

            reentrantReadWriteLock.writeLock().lock();

            Counter counter = (Counter) Context.getInstance().getStore(NAMESPACE).get(COUNTER_NAME);

            long count = counter.increment();
            if (count != 1) {
                fail("expected count = 1");
            }

            count = counter.decrement();
            if (count != 0) {
                fail("expected count = 0");
            }

            Thread.sleep(RandomGenerator.getInstance().nextInteger(0, 1000));
        } finally {
            reentrantReadWriteLock.writeLock().unlock();
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
