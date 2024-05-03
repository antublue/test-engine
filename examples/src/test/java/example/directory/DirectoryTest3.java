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

package example.directory;

import static org.antublue.test.engine.extras.Directory.PathType.ABSOLUTE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Stream;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.StringArgument;
import org.antublue.test.engine.extras.Directory;

/** Example test */
public class DirectoryTest3 {

    @TestEngine.AutoClose.Conclude private Directory directory1;

    @TestEngine.AutoClose.AfterAll private Directory directory2;

    @TestEngine.Argument protected StringArgument stringArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<StringArgument> arguments() {
        Collection<StringArgument> collection = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            collection.add(StringArgument.of("StringArgument " + i));
        }
        return collection.stream();
    }

    @TestEngine.Prepare
    public void prepare() throws IOException {
        System.out.println("prepare()");
        directory1 = Directory.create("/tmp/directory-" + UUID.randomUUID(), ABSOLUTE);
        System.out.format(String.format("directory1 [%s]", directory1));
    }

    @TestEngine.BeforeAll
    public void beforeAll() throws IOException {
        System.out.println("beforeAll(" + stringArgument + ")");
        directory2 = Directory.create("/tmp/directory-" + UUID.randomUUID(), ABSOLUTE);
        System.out.println(String.format("directory2 [%s]", directory2));
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach(" + stringArgument + ")");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + stringArgument + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + stringArgument + ")");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach(" + stringArgument + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll(" + stringArgument + ")");
    }

    @TestEngine.Conclude
    public void conclude() {
        System.out.println("conclude()");
    }
}
