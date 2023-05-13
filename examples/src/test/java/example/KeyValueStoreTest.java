package example;

import org.antublue.test.engine.api.KeyValueStore;
import org.antublue.test.engine.api.Parameter;
import org.antublue.test.engine.api.SimpleParameter;
import org.antublue.test.engine.api.TestEngine;

import java.util.stream.Stream;

/**
 * Example test
 */
public class KeyValueStoreTest {

    public static final String STRING = "string";

    private KeyValueStore keyValueStore;

    @TestEngine.Parameter
    public SimpleParameter<Integer> simpleParameter;

    @TestEngine.ParameterSupplier
    public static Stream<Parameter> parameters() {
        return Stream.of(
                SimpleParameter.of(1),
                SimpleParameter.of(2));
    }

    @TestEngine.Prepare
    public void prepare() {
        keyValueStore = new KeyValueStore();
        keyValueStore.put(STRING, "This is a static string");
        System.out.println(String.format("prepare() context [%s]", keyValueStore));
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println(String.format("beforeAll() context [%s] parameter [%s]", keyValueStore, simpleParameter.value()));
        System.out.println(String.format(STRING + " = [%s]", keyValueStore.get(STRING, String.class)));
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println(String.format("beforeEach() context [%s] parameter [%s]", keyValueStore, simpleParameter.value()));
        System.out.println(String.format(STRING + " = [%s]", keyValueStore.get(STRING, String.class)));
    }

    @TestEngine.Test
    public void test() {
        System.out.println(String.format("test() context [%s] parameter [%s]", keyValueStore, simpleParameter.value()));
        System.out.println(String.format(STRING + " = [%s]", keyValueStore.get(STRING, String.class)));
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println(String.format("afterEach() context [%s] parameter [%s]", keyValueStore, simpleParameter.value()));
        System.out.println(String.format(STRING + " = [%s]", keyValueStore.get(STRING, String.class)));
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println(String.format("afterAll() context [%s] parameter [%s]", keyValueStore, simpleParameter.value()));
        System.out.println(String.format(STRING + " = [%s]", keyValueStore.get(STRING, String.class)));
    }

    @TestEngine.Conclude
    public void conclude() {
        System.out.println(String.format("conclude() context [%s]", keyValueStore));
        keyValueStore.dispose();
        keyValueStore = null;
    }
}
