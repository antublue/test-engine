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

package example.signals;

import java.util.stream.Stream;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.extras.Signals;

@TestEngine.Order(order = 0)
public class SignalTest {

    @TestEngine.Argument public Argument<Boolean> argument;

    @TestEngine.ArgumentSupplier
    public static Stream<Argument<Boolean>> arguments() {
        return Stream.of(Argument.ofBoolean(true));
    }

    @TestEngine.Test
    public void test() throws Throwable {
        Thread.sleep(5000);
        Class<?> clazz = SignalTest.class;
        System.out.println("test(" + clazz + ")");
        Signals.signal(clazz);
    }
}
