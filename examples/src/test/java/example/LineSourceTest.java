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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Named;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.extras.source.LineSource;

/** Example test */
@TestEngine.Disabled
public class LineSourceTest {

    private static final String RESOURCE_NAME = "/sample.txt";

    @TestEngine.Argument public Named<String> argument;

    @TestEngine.ArgumentSupplier
    public static Stream<Named<String>> arguments() throws IOException {
        try (InputStream inputStream = LineSourceTest.class.getResourceAsStream(RESOURCE_NAME)) {
            return LineSource.of(inputStream, StandardCharsets.UTF_8);
        }
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + argument + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + argument + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
