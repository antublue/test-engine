package org.antublue.test.engine.testing.inheritance.standard;

import org.antublue.test.engine.api.TestEngine;

public abstract class BaseTest {

    @TestEngine.Test
    public void testB1() {
        System.out.println("testB1()");
    }

    @TestEngine.Test
    public void testB2() {
        System.out.println("testB2()");
    }
}
