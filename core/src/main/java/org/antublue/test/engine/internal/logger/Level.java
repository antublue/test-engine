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

/**
 * Class to represent Logger levels
 */
public final class Level {

    public static final Level ERROR = new Level(100, "ERROR");
    public static final Level WARN = new Level(200, "WARN");
    public static final Level INFO = new Level(300, "INFO");
    public static final Level DEBUG = new Level(400, "DEBUG");
    public static final Level TRACE = new Level(500, "TRACE");
    public static final Level ALL = new Level(Integer.MAX_VALUE, "ALL");

    private final int level;
    private final String string;

    /**
     * Constructor
     *
     * @param level
     * @param string
     */
    private Level(int level, String string) {
        this.level = level;
        this.string = string;
    }

    /**
     * Method to get the Level as an int
     *
     * @return
     */
    public int toInt() {
        return level;
    }

    /**
     * Method to get the Level string
     *
     * @return
     */
    @Override
    public String toString() {
        return string;
    }
}
