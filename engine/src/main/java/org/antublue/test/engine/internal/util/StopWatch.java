/*
 * Copyright (C) 2023 The AntuBLUE test-engine project authors
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

package org.antublue.test.engine.internal.util;

import java.util.concurrent.TimeUnit;
import org.antublue.test.engine.internal.logger.Logger;
import org.antublue.test.engine.internal.logger.LoggerFactory;

/** Class to implement a stop watch */
public class StopWatch {

    private static final Logger LOGGER = LoggerFactory.getLogger(StopWatch.class);

    private long startNanoTime;
    private long stopNanoTime;

    /**
     * Method to start the stop watch
     *
     * @return this
     */
    public StopWatch start() {
        startNanoTime = System.nanoTime();
        LOGGER.trace("start [%d]", startNanoTime);
        return this;
    }

    /**
     * Method to stop the stop watch
     *
     * @return this
     */
    public StopWatch stop() {
        stopNanoTime = System.nanoTime();
        LOGGER.trace("stop [%d]", stopNanoTime);
        return this;
    }

    /**
     * Method to get the elapsed time in milliseconds
     *
     * @return the elapsed time in milliseconds
     */
    public long elapsedTime() {
        long elapsedTime = elapsedTime(TimeUnit.MILLISECONDS);
        LOGGER.trace("elapsedTime [%d]", elapsedTime);
        return elapsedTime;
    }

    /**
     * Method to get the elapsed time
     *
     * @param timeUnit timeUnit
     * @return the elapsed time based on the timeUnit
     */
    public long elapsedTime(TimeUnit timeUnit) {
        return timeUnit.convert(stopNanoTime - startNanoTime, TimeUnit.NANOSECONDS);
    }
}
