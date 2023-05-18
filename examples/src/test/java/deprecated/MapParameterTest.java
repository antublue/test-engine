package deprecated;

import org.antublue.test.engine.api.MapParameter;
import org.antublue.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example test
 */
public class MapParameterTest {

    @TestEngine.Parameter
    public MapParameter mapParameter;

    @TestEngine.ParameterSupplier
    public static Stream<MapParameter> parameters() {
        Collection<MapParameter> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int value = i * 3;
            collection.add(MapParameter.named(String.valueOf(i)).put("value", value));
        }
        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
        int value = mapParameter.get("value");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + mapParameter.get("value") + ")");
        assertThat(mapParameter.get("value").getClass().isAssignableFrom(Integer.class));
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + mapParameter.get("value")+ ")");
        assertThat(mapParameter.get("value").getClass().isAssignableFrom(Integer.class));
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
