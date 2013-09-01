package com.bydavy.easy.network.test;

import junit.framework.TestCase;

public class TestExample extends TestCase{

    @Override
    protected void setUp() throws Exception {

    }

    @Override
    protected void tearDown() throws Exception {

    }

    @Override
    protected void runTest() throws Throwable {
        test1();
        test2();
    }

    public void test1(){
        assertTrue(true);
    }

    public void test2(){
        assertTrue(true);
    }
}
