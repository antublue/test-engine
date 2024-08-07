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

package org.antublue.test.engine.testing;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.support.ClassPathSupport;
import org.antublue.test.engine.internal.support.UrlSupport;

/** Example test */
public class ClassPathSupportResourceTest {

    @TestEngine.Argument public Argument<String> argument;

    @TestEngine.Random.Integer public Integer randomInteger;

    @TestEngine.ArgumentSupplier
    public static Stream<Argument<String>> arguments() {
        Collection<Argument<String>> collection = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            collection.add(Argument.ofString("StringArgument " + i));
        }
        return collection.stream();
    }

    @TestEngine.Prepare
    public void prepare() {
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
    public void test() throws Throwable {
        System.out.println("test(" + argument + ")");
        assertThat(argument).isNotNull();

        for (URL url :
                ClassPathSupport.findResources("test-engine.properties").stream()
                        .filter(url -> url.getProtocol().equals("jar"))
                        .collect(Collectors.toList())) {

            BufferedReader bufferedReader = null;

            try {
                bufferedReader = UrlSupport.createBufferedReader(url);
                while (true) {
                    String line = bufferedReader.readLine();
                    if (line == null) {
                        break;
                    }
                    System.out.println("-> " + line);
                }
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Throwable t) {
                        // DO NOTHING
                    }
                }
            }
        }
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
    }
}
