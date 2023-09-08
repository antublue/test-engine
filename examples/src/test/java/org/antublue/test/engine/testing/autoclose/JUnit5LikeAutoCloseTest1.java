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

package org.antublue.test.engine.testing.autoclose;

import org.antublue.test.engine.api.TestEngine;

/** Example test */
public class JUnit5LikeAutoCloseTest1 {

    @TestEngine.Random.Integer private Integer randomInteger;

    @TestEngine.AutoClose.AfterEach private TestAutoCloseable afterEachAutoCloseable;

    @TestEngine.AutoClose.Conclude private TestAutoCloseable afterConcludeAutoCloseable;

    @TestEngine.Prepare
    public void prepare() {
        System.out.println("prepare()");
        afterConcludeAutoCloseable = new TestAutoCloseable("afterConclude");
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach(" + randomInteger + ")");
        afterEachAutoCloseable = new TestAutoCloseable("afterEach");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1()");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2()");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
    }

    @TestEngine.Conclude
    public void conclude() {
        System.out.println("conclude()");
    }

    private static class TestAutoCloseable implements AutoCloseable {

        private final String name;
        private boolean isClosed;

        public TestAutoCloseable(String name) {
            this.name = name;
        }

        public void close() {
            System.out.println(name + ".close()");
            isClosed = true;
        }

        public boolean isClosed() {
            return isClosed;
        }
    }
}
