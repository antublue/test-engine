package org.antublue.test.engine.testing.inheritance.standard;

import org.antublue.test.engine.api.TestEngine;

public class SubClassTest extends BaseTest{

    @TestEngine.Test
    public void testSC1() {
        System.out.println("testSC1()");
    }

    @TestEngine.Test
    public void testSC2() {
        System.out.println("testSC2()");
    }
}
