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

package example;

/** Example test */
public class SimpleTest3 {

    /*
    @TestEngine.Argument protected Named<FakeContainer> argument;

    @TestEngine.ArgumentSupplier
    public static Stream<Named<FakeContainer>> arguments() {
        Collection<Named<FakeContainer>> collection = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            collection.add(Named.of("FakeContainer " + i, new FakeContainer()));
        }
        return collection.stream();
    }

    @TestEngine.Prepare
    public void prepare() {
        System.out.println("prepare()");
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
        assertThat(argument.getPayload().isRunning()).isFalse();
        argument.getPayload().start();
        assertThat(argument.getPayload().isRunning()).isTrue();
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach(" + argument + ")");
        assertThat(argument.getPayload().isRunning()).isTrue();
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + argument + ")");
        assertThat(argument.getPayload().isRunning()).isTrue();
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + argument + ")");
        assertThat(argument.getPayload().isRunning()).isTrue();
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach(" + argument + ")");
        assertThat(argument.getPayload().isRunning()).isTrue();
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll(" + argument + ")");

        assertThat(argument.getPayload().isRunning()).isTrue();
        argument.getPayload().stop();
        assertThat(argument.getPayload().isRunning()).isFalse();
    }

    @TestEngine.Conclude
    public void conclude() {
        System.out.println("conclude()");
        assertThat(argument).isNull();
    }

    public static class FakeContainer {

        private boolean running;

        public void start() {
            running = true;
        }

        public void stop() {
            running = false;
        }

        public boolean isRunning() {
            return running;
        }
    }
    */
}
