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

package org.antublue.test.engine.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/** Class to implement Conditions */
public class Conditions {

    private static final Map<Object, CountDownLatch> MAP = new ConcurrentHashMap<>();

    /**
     * Constructor
     */
    private Conditions() {
        // DO NOTHING
    }

    /**
     * Method to signal a Condition
     *
     * @param name name
     */
    public static void signal(Object name) {
        CountDownLatch countDownLatch =
                MAP.compute(
                        name,
                        (k, v) -> {
                            if (v == null) {
                                v = new CountDownLatch(1);
                            }
                            return v;
                        });

        if (countDownLatch.getCount() == 0) {
            throw new IllegalStateException("Condiition [%s] already signaled");
        }

        countDownLatch.countDown();
    }

    /**
     * Method to wait for a Condition
     *
     * @param name name
     */
    public static void await(Object name) {
        CountDownLatch countDownLatch =
                MAP.compute(
                        name,
                        (k, v) -> {
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
