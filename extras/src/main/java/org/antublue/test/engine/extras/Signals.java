/*
 * Copyright (C) 2024 The AntuBLUE test-engine project authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.antublue.test.engine.extras;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Class to implement Signals */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class Signals {

    private static final Map<Object, CountDownLatch> MAP = new ConcurrentHashMap<>();

    /** Constructor */
    private Signals() {
        // DO NOTHING
    }

    /**
     * Method to signal
     *
     * @param key key
     */
    public static void signal(Object key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }

        MAP.compute(
                        key,
                        (k, o) -> {
                            if (o == null) {
                                o = new CountDownLatch(1);
                            }
                            return o;
                        })
                .countDown();
    }

    /**
     * Method to wait for a signal
     *
     * @param key key
     */
    public static void await(Object key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }

        while (true) {
            try {
                MAP.compute(
                                key,
                                (k, o) -> {
                                    if (o == null) {
                                        o = new CountDownLatch(1);
                                    }
                                    return o;
                                })
                        .await();

                return;
            } catch (InterruptedException e) {
                // DO NOTHING
            }
        }
    }

    /**
     * Method to wait for a signal
     *
     * @param key key
     * @param timeout timeout
     * @param unit unit
     */
    public static void await(Object key, long timeout, TimeUnit unit)
            throws TimeoutException, InterruptedException {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }

        if (timeout < 1) {
            throw new IllegalArgumentException("timeout [" + timeout + "] is less than 1");
        }

        if (unit == null) {
            throw new IllegalArgumentException("unit is null");
        }

        CountDownLatch countDownLatch =
                MAP.compute(
                        key,
                        (k, o) -> {
                            if (o == null) {
                                o = new CountDownLatch(1);
                            }
                            return o;
                        });

        countDownLatch.await(timeout, unit);

        if (countDownLatch.getCount() > 0) {
            throw new TimeoutException("signal [" + key + "] timed out");
        }
    }
}
