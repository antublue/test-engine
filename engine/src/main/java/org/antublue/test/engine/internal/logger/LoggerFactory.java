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

package org.antublue.test.engine.internal.logger;

import java.util.HashMap;
import java.util.Map;
import org.antublue.test.engine.internal.Configuration;

/** Class to implement a LoggerFactory */
@SuppressWarnings("PMD.EmptyCatchBlock")
public final class LoggerFactory {

    private static LoggerFactory SINGLETON;

    private static final Configuration CONFIGURATION = Configuration.singleton();

    private final Map<String, Logger> loggerMap = new HashMap<>();

    /** Constructor */
    private LoggerFactory() {
        // DO NOTHING
    }

    /**
     * Method to create a Logger
     *
     * @param name name
     * @return the return value
     */
    private Logger createLogger(String name) {
        synchronized (this) {
            Logger logger = loggerMap.get(name);
            if (logger == null) {
                logger = new Logger(name);
                loggerMap.put(name, logger);
            }
            return logger;
        }
    }

    /**
     * Method to get a Logger by Class name
     *
     * @param clazz clazz
     * @return the return value
     */
    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    /**
     * Method to get a Logger by name
     *
     * @param name name
     * @return the return value
     */
    public static Logger getLogger(String name) {
        synchronized (CONFIGURATION) {
            if (SINGLETON == null) {
                SINGLETON = new LoggerFactory();
            }
        }

        return SINGLETON.createLogger(name);
    }
}
