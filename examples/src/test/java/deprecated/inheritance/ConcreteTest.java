package deprecated.example.inheritance;

import org.antublue.test.engine.api.TestEngine;

public class ConcreteTest extends BaseTest {

    @TestEngine.Test
    public void testB() {
        System.out.println("testB()");
    }
}
