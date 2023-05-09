/*
 * Copyright (C) 2022-2023 The AntuBLUE test-engine project authors
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

import org.antublue.test.engine.internal.util.Precondition;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Class to implement a LoggerFactory
 */
public final class LoggerFactory {

    private static final LoggerFactory INSTANCE = new LoggerFactory();
    private static final String ANTUBLUE_TEST_ENGINE_LOG_LEVEL = "antublue.test.engine.log.level";

    private final Map<String, Logger> loggerMap = new HashMap<>();
    private final Level level;

    /**
     * Constructor
     */
    private LoggerFactory() {
        Map<String, Level> levelMap = new HashMap<>();
        levelMap.put("ERROR", Level.ERROR);
        levelMap.put("WARN", Level.WARN);
        levelMap.put("INFO", Level.INFO);
        levelMap.put("DEBUG", Level.DEBUG);
        levelMap.put("TRACE", Level.TRACE);
        levelMap.put("ALL", Level.ALL);

        // Convert the system property to an environment variable and get the value
        String value =
                System.getenv(
                        ANTUBLUE_TEST_ENGINE_LOG_LEVEL.toUpperCase(Locale.ENGLISH).replace('.', '_'));

        Level level = null;

        if (value != null && !value.trim().isEmpty()) {
            value = value.trim().toUpperCase(Locale.ENGLISH);
            level = levelMap.get(value);
        }

        if (level == null) {
            value = System.getProperty(ANTUBLUE_TEST_ENGINE_LOG_LEVEL);
            if (value != null && !value.trim().isEmpty()) {
                value = value.trim().toUpperCase(Locale.ENGLISH);
                level = levelMap.get(value);
            }
        }

        if (level == null) {
            level = Level.INFO;
        }

        this.level = level;
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
                logger = new Logger(name, level);
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
        Precondition.notNull(clazz);

        return getLogger(clazz.getName());
    }

    /**
     * Method to get a Logger by name
     *
     * @param name name
     * @return the return value
     */
    public static Logger getLogger(String name) {
        Precondition.notNull(name);
        Precondition.notBlank(name);

        return INSTANCE.createLogger(name);
    }
}
