package example;

import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.KeyValueStore;
import org.antublue.test.engine.api.ObjectArgument;
import org.antublue.test.engine.api.TestEngine;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example test
 */
public class KeyValueStoreTest {

    private static final String STRING = "string";
    private static final String VALUE = "This is a static string";

    private KeyValueStore keyValueStore;

    @TestEngine.Argument
    public ObjectArgument<Integer> objectArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<Argument> arguments() {
        return Stream.of(
                ObjectArgument.of(1),
                ObjectArgument.of(2));
    }

    @TestEngine.Prepare
    public void prepare() {
        keyValueStore = new KeyValueStore();
        keyValueStore.put(STRING, VALUE);
        keyValueStore.putIfAbsent(STRING, "Shouldn't be added");
        System.out.println(String.format("prepare() context [%s]", keyValueStore));
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println(String.format("beforeAll() context [%s] argument [%s]", keyValueStore, objectArgument.value()));
        System.out.println(String.format(STRING + " = [%s]", keyValueStore.get(STRING, String.class)));
        assertThat(keyValueStore.get(STRING, String.class)).isEqualTo(VALUE);
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println(String.format("beforeEach() context [%s] argument [%s]", keyValueStore, objectArgument.value()));
        System.out.println(String.format(STRING + " = [%s]", keyValueStore.get(STRING, String.class)));
        assertThat(keyValueStore.get(STRING, String.class)).isEqualTo(VALUE);
    }

    @TestEngine.Test
    public void test() {
        System.out.println(String.format("test() context [%s] argument [%s]", keyValueStore, objectArgument.value()));
        System.out.println(String.format(STRING + " = [%s]", keyValueStore.get(STRING, String.class)));
        assertThat(keyValueStore.get(STRING, String.class)).isEqualTo(VALUE);
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println(String.format("afterEach() context [%s] argument [%s]", keyValueStore, objectArgument.value()));
        System.out.println(String.format(STRING + " = [%s]", keyValueStore.get(STRING, String.class)));
        assertThat(keyValueStore.get(STRING, String.class)).isEqualTo(VALUE);
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println(String.format("afterAll() context [%s] argument [%s]", keyValueStore, objectArgument.value()));
        System.out.println(String.format(STRING + " = [%s]", keyValueStore.get(STRING, String.class)));
        assertThat(keyValueStore.get(STRING, String.class)).isEqualTo(VALUE);
    }

    @TestEngine.Conclude
    public void conclude() {
        System.out.println(String.format("conclude() context [%s]", keyValueStore));
        assertThat(keyValueStore.get(STRING, String.class)).isEqualTo(VALUE);

        keyValueStore.dispose();

        assertThat(keyValueStore.get(STRING, String.class)).isNull();
    }
}
