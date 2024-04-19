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

import java.util.ArrayList;
import java.util.Collection;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.StringArgument;

@TestEngine.ResourceLock(name = "ClassResourceLockTest")
public class ClassResourceLockTestB {

    @TestEngine.Argument private Argument argument;

    @TestEngine.ArgumentSupplier
    public static Iterable<Argument> arguments() {
        Collection<Argument> collection = new ArrayList<>();
        collection.add(StringArgument.of("foo"));
        return collection;
    }

    @TestEngine.Test
    public void test() throws InterruptedException {
        System.out.println(
                String.format(
                        "test() class [%s] testing [%s]", getClass().getName(), argument.name()));
        Thread.sleep(10000);
    }
}