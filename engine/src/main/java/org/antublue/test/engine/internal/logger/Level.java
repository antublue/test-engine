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

/** Class to represent Logger levels */
public final class Level {

    /** ERROR log level */
    public static final Level ERROR = new Level(100, "ERROR");

    /** WARN log level */
    public static final Level WARN = new Level(200, "WARN");

    /** INFO log level */
    public static final Level INFO = new Level(300, "INFO");

    /** DEBUG log level */
    public static final Level DEBUG = new Level(400, "DEBUG");

    /** TRACE log level */
    public static final Level TRACE = new Level(500, "TRACE");

    /** ALL log level */
    public static final Level ALL = new Level(Integer.MAX_VALUE, "ALL");

    private static Map<String, Level> LEVEL_MAP = new HashMap<>();

    static {
        LEVEL_MAP.put(ERROR.toString(), ERROR);
        LEVEL_MAP.put(WARN.toString(), WARN);
        LEVEL_MAP.put(INFO.toString(), INFO);
        LEVEL_MAP.put(DEBUG.toString(), DEBUG);
        LEVEL_MAP.put(TRACE.toString(), TRACE);
        LEVEL_MAP.put(ALL.toString(), ALL);
    }

    private final int level;
    private final String string;

    /**
     * Constructor
     *
     * @param level level
     * @param string string
     */
    private Level(int level, String string) {
        this.level = level;
        this.string = string;
    }

    /**
     * Method to get the Level as an int
     *
     * @return the return value
     */
    public int toInt() {
        return level;
    }

    /**
     * Method to get the Level string
     *
     * @return the return value
     */
    @Override
    public String toString() {
        return string;
    }

    /**
     * Method to map a Level string to a Level
     *
     * @param string string
     * @param defaultLevel defaultLevel
     * @return the return value
     */
    public static Level toLevel(String string, Level defaultLevel) {
        return LEVEL_MAP.getOrDefault(string, defaultLevel);
    }
}
