package org.antublue.test.engine.testing;

import org.antublue.test.engine.api.Namespace;
import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.support.NamedString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

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

        Namespace namespace = Namespace.of("1", "2", "3", 4);

        assertThat(namespace).isNotNull();
        assertThat(namespace.toString()).isEqualTo("/1/2/3/4/");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test1()");

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
