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

public class Timed {

    public enum Units {
        SECONDS,
        MILLISECONDS,
        NANOSECONDS
    }

    private Timed() {
        // DO NOTHING
    }

    public static double execute(Executable executable) throws Throwable {
        return execute(executable, Units.MILLISECONDS);
    }

    public static double execute(Executable executable, Units units) throws Throwable {
        double t0 = 0;

        switch (units) {
            case SECONDS:
                {
                    t0 = System.currentTimeMillis() * 1000;
                    break;
                }
            case MILLISECONDS:
                {
                    t0 = System.currentTimeMillis();
                    break;
                }
            case NANOSECONDS:
                {
                    t0 = System.nanoTime();
                    break;
                }
        }

        executable.execute();

        double t1 = 0;

        switch (units) {
            case SECONDS:
                {
                    t1 = System.currentTimeMillis() * 1000;
                    break;
                }
            case MILLISECONDS:
                {
                    t1 = System.currentTimeMillis();
                    break;
                }
            case NANOSECONDS:
                {
                    t1 = System.nanoTime();
                    break;
                }
        }

        return t1 - t0;
    }
}
