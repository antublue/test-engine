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

package example.inheritance;

import java.util.stream.Stream;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.named.NamedInteger;

public abstract class EvenBaseTest extends BaseTest {

    @TestEngine.ArgumentSupplier
    protected static Stream<NamedInteger> arguments() {
        return BaseTest.arguments()
                .filter(
                        argument -> {
                            int value = argument.getPayload();
                            return (value % 2) == 0;
                        });
    }
}
