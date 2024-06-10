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

package org.antublue.test.engine.testing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Namespace;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.support.named.NamedString;

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
        System.out.println("test1(" + argument + ")");

        Namespace namespace = Namespace.of("1", "2", "3", 4);

        assertThat(namespace).isNotNull();
        assertThat(namespace.toString()).isEqualTo("/1/2/3/4/");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + argument + ")");

        Namespace namespace = Namespace.of("1", "2");

        assertThat(namespace).isNotNull();
        assertThat(namespace.toString()).isEqualTo("/1/2/");

        namespace = namespace.append(3, 4);

        assertThat(namespace).isNotNull();
        assertThat(namespace.toString()).isEqualTo("/1/2/3/4/");

        namespace = namespace.append(Namespace.of(5, 6));

        assertThat(namespace).isNotNull();
        assertThat(namespace.toString()).isEqualTo("/1/2/3/4/5/6/");
    }
}
