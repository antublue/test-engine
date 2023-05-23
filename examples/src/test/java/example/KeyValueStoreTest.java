package example;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.IntegerArgument;
import org.antublue.test.engine.api.support.KeyValueStore;

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
    public IntegerArgument integerArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<IntegerArgument> arguments() {
        return Stream.of(
                IntegerArgument.of(1),
                IntegerArgument.of(2));
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
        System.out.println(String.format("beforeAll() context [%s] argument [%s]", keyValueStore, integerArgument.value()));
        System.out.println(String.format(STRING + " = [%s]", keyValueStore.get(STRING, String.class)));
        assertThat(keyValueStore.get(STRING, String.class)).isEqualTo(VALUE);
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println(String.format("beforeEach() context [%s] argument [%s]", keyValueStore, integerArgument.value()));
        System.out.println(String.format(STRING + " = [%s]", keyValueStore.get(STRING, String.class)));
        assertThat(keyValueStore.get(STRING, String.class)).isEqualTo(VALUE);
    }

    @TestEngine.Test
    public void test() {
        System.out.println(String.format("test() context [%s] argument [%s]", keyValueStore, integerArgument.value()));
        System.out.println(String.format(STRING + " = [%s]", keyValueStore.get(STRING, String.class)));
        assertThat(keyValueStore.get(STRING, String.class)).isEqualTo(VALUE);
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println(String.format("afterEach() context [%s] argument [%s]", keyValueStore, integerArgument.value()));
        System.out.println(String.format(STRING + " = [%s]", keyValueStore.get(STRING, String.class)));
        assertThat(keyValueStore.get(STRING, String.class)).isEqualTo(VALUE);
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println(String.format("afterAll() context [%s] argument [%s]", keyValueStore, integerArgument.value()));
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
