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

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/** Class to implement ExecutableSupport */
public class ExecutableSupport {

    /** Constructor */
    private ExecutableSupport() {
        // DO NOTHING
    }

    /**
     * Method execute an Executable
     *
     * @param executable executable
     * @return the Duration
     * @throws Throwable Throwable
     */
    public static Duration execute(Executable executable) throws Throwable {
        if (executable == null) {
            throw new IllegalArgumentException("executable is null");
        }

        long t0 = System.nanoTime();
        executable.execute();
        return Duration.of(System.nanoTime() - t0, ChronoUnit.NANOS);
    }

    /**
     * Method execute an Executable in a Lock
     *
     * @param key key
     * @param executable executable
     * @return the Duration
     * @throws Throwable Throwable
     */
    public static Duration execute(Object key, Executable executable) throws Throwable {
        long t0 = System.nanoTime();
        Locks.LockReference lockReference = Locks.getReference(key);

        try {
            lockReference.lock();
            executable.execute();
            return Duration.of(System.nanoTime() - t0, ChronoUnit.NANOS);
        } finally {
            lockReference.unlock();
        }
    }
}
