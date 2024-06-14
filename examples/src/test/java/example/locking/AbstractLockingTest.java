/*
 * Copyright (C) 2024 The AntuBLUE test-engine project authors
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
import org.antublue.test.engine.api.Lock;
import org.antublue.test.engine.api.Named;
import org.antublue.test.engine.api.Namespace;
import org.antublue.test.engine.api.TestEngine;

public abstract class AbstractLockingTest {

    private static final String NAMESPACE = "AbstractLockingTest";
    private static final String LOCK_NAME = "Lock";

    @TestEngine.Context public static Context context;

    @TestEngine.Argument public Named<String> argument;

    @TestEngine.ArgumentSupplier
    public static Stream<Named<String>> arguments() {
        Collection<Named<String>> collection = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            collection.add(Named.ofString("StringArgument " + i));
        }
        return collection.stream();
    }

    @TestEngine.Test
    public void test() throws Throwable {
        final String className = getClass().getName();

        Lock.execute(
                Namespace.of(NAMESPACE, LOCK_NAME),
                () -> {
                    System.out.println(className + ".test1(" + argument + ")");
                    System.out.println("sleeping 1000");
                    Thread.sleep(5000);
                    assertThat(argument).isNotNull();
                    System.out.println(className + ".continuing");
                });
    }
}
