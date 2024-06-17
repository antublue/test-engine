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

package example;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.util.StandardStreams;

@TestEngine.Environment
public class TestEngineEnvironment2 {

    @TestEngine.Prepare
    public void prepare() throws Throwable {
        System.out.println(getClass().getName() + ".prepare()");
        StandardStreams.flush();
    }

    @TestEngine.Conclude
    public void conclude() throws Throwable {
        System.out.println(getClass().getName() + ".conclude()");
        StandardStreams.flush();
    }
}
