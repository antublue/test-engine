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

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Context;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.support.lock.LockManager;
import org.antublue.test.engine.api.support.lock.LockReference;
import org.antublue.test.engine.api.support.named.NamedString;

/** Example test */
public class LockManagerClassLockingTest2 {

    private static final String NAMESPACE = "ClassLockingTest";
    private static final String LOCK_MANAGER = "LockManager";
    private static final String LOCK_NAME = "Lock";

    private LockReference lockReference;

    @TestEngine.Context protected static Context context;

    @TestEngine.Argument protected NamedString argument;

    @TestEngine.Random.Integer protected Integer randomInteger;

    @TestEngine.ArgumentSupplier
    public static Stream<NamedString> arguments() {
        Collection<NamedString> collection = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            collection.add(NamedString.of("StringArgument " + i));
        }
        return collection.stream();
    }

    @TestEngine.Prepare
    public void prepare() {
        lockReference =
                context.getStore(NAMESPACE)
                        .computeIfAbsent(LOCK_MANAGER, o -> new LockManager())
                        .acquire(LOCK_NAME);
        System.out.println("prepare()");
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll(" + argument + ")");
        System.out.println("randomInteger = [" + randomInteger + "]");
        assertThat(argument).isNotNull();
        assertThat(randomInteger).isNotNull();
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach(" + argument + ")");
        assertThat(argument).isNotNull();
    }

    @TestEngine.Test
    public void test1() throws Throwable {
        System.out.println("test1(" + argument + ")");
        System.out.println("sleeping 1000");
        Thread.sleep(1000);
        assertThat(argument).isNotNull();
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + argument + ")");
        assertThat(argument).isNotNull();
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach(" + argument + ")");
        assertThat(argument).isNotNull();
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll(" + argument + ")");
        System.out.println("randomInteger = [" + randomInteger + "]");
        assertThat(argument).isNotNull();
        assertThat(randomInteger).isNotNull();
    }

    @TestEngine.Conclude
    public void conclude() {
        System.out.println("conclude()");
        lockReference.release();
    }
}
