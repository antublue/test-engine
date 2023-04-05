/*
 * Copyright 2022-2023 Douglas Hoard
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
import java.util.Locale;
import java.util.Map;

/**
 * Class to implement a LoggerFactory for Logback Classic
 */
public final class LoggerFactory {

    private static final Map<String, Level> LEVEL_MAP = new HashMap<>();
    private static final Map<String, Logger> LOGGER_MAP = new HashMap<>();
    private static final Level LEVEL;

    static {
        LEVEL_MAP.put("ERROR", Level.ERROR);
        LEVEL_MAP.put("WARN", Level.WARN);
        LEVEL_MAP.put("INFO", Level.INFO);
        LEVEL_MAP.put("DEBUG", Level.DEBUG);
        LEVEL_MAP.put("TRACE", Level.TRACE);
        LEVEL_MAP.put("ALL", Level.ALL);

        LEVEL = getLevel();
    }

    /**
     * Constructor
     */
    private LoggerFactory() {
        // DO NOTHING
    }

    /**
     * Method to get a Logger by Class name
     *
     * @param clazz
     * @return
     */
    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    /**
     * Method to get a Logger by name
     *
     * @param name
     * @return
     */
    public static Logger getLogger(String name) {
        Logger logger;

        synchronized (LOGGER_MAP) {
            logger = LOGGER_MAP.get(name);
            if (logger == null) {
                logger = new Logger(name, LEVEL);
                LOGGER_MAP.put(name, logger);
            }
        }

        return logger;
    }

    /**
     * Method to get the configured Level
     *
     * @return
     */
    private static Level getLevel() {
        String propertyName = "antublue.test.engine.log.level";
        String propertyValue = System.getProperty(propertyName);
        String environmentVariableValue =
                System.getenv(
                        propertyName.toUpperCase(Locale.ENGLISH).replace('.', '_'));

        if ((propertyValue != null) && !propertyValue.trim().isEmpty()) {
            return LEVEL_MAP.getOrDefault(propertyValue.trim().toUpperCase(Locale.ENGLISH), Level.INFO);
        } else if ((environmentVariableValue != null) && !environmentVariableValue.trim().isEmpty()) {
            return LEVEL_MAP.getOrDefault(environmentVariableValue.toUpperCase(Locale.ENGLISH), Level.INFO);
        }

        return Level.INFO;
    }
}
