package org.antublue.test.engine.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiFunction;

public class Conditions {

    private static final Map<Object, CountDownLatch> MAP = new ConcurrentHashMap<>();

    private Conditions() {
        // DO NOTHING
    }

    public static void signal(Object name) {
        CountDownLatch countDownLatch = MAP.compute(name, (k, v) -> {
            if (v == null) {
                v = new CountDownLatch(1);
            }
            return v;
        });
        countDownLatch.countDown();
    }

    public static void await(Object name) {
        CountDownLatch countDownLatch = MAP.compute(name, (k, v) -> {
            if (v == null) {
                v = new CountDownLatch(1);
            }

            return v;
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            // DO NOTHING
        }
    }
}
