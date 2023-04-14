package example.inheritance2;

import org.antublue.test.engine.api.TestEngine;

public class ConcreteTest extends BaseTest {

    @TestEngine.Test
    public void testB() {
        System.out.println("testB()");
    }
}
