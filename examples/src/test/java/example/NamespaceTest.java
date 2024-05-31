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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Context;
import org.antublue.test.engine.api.Namespace;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.support.NamedString;

/** Example test */
public class NamespaceTest {

    @TestEngine.Argument protected NamedString argument;

    @TestEngine.ArgumentSupplier
    public static Stream<NamedString> arguments() {
        Collection<NamedString> collection = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            collection.add(NamedString.of("StringArgument " + i));
        }
        return collection.stream();
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1()");

        Namespace namespace = Namespace.of(NamespaceTest.class);
        System.out.println("namespace(" + namespace + ")");
        Context.getInstance().getStore(namespace).put("foo", "bar");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2()");

        Namespace namespace = Namespace.of(NamespaceTest.class);
        System.out.println("namespace(" + namespace + ")");
        String value = Context.getInstance().getStore(namespace).remove("foo");
        assertThat(value).isNotNull();
        assertThat(value).isNotEmpty();
        assertThat(value).isEqualTo("bar");
    }
}
