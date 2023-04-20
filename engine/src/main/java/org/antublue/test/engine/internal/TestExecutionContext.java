package org.antublue.test.engine.internal;

import org.junit.platform.engine.ExecutionRequest;

import java.util.concurrent.CountDownLatch;

public class TestExecutionContext {

    private final ExecutionRequest executionRequest;
    private final CountDownLatch countDownLatch;
    private Object testInstance;

    public TestExecutionContext(ExecutionRequest executionRequest, CountDownLatch countDownLatch) {
        this.executionRequest = executionRequest;
        this.countDownLatch = countDownLatch;
    }

    public ExecutionRequest getExecutionRequest() {
        return executionRequest;
    }

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    public void setTestInstance(Object testInstance) {
        this.testInstance = testInstance;
    }

    public Object getTestInstance() {
        return testInstance;
    }
}
