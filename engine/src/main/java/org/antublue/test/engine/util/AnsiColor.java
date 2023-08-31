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

package org.antublue.test.engine.util;

import org.antublue.test.engine.TestEngineConstants;

/** Class to implement ANSI colors */
public class AnsiColor {

    private static final String NO_COLOR = "NO_COLOR";
    private static final String EMPTY_STRING = "";
    private static final String ONE = "1";

    /** AnsiColor constant */
    public static final AnsiColor TEXT_RESET = new AnsiColor("\033[0m"); // Text Reset

    // Regular Colors

    /** AnsiColor constant */
    public static final AnsiColor BLACK = new AnsiColor("\033[0;30m"); // BLACK

    /** AnsiColor constant */
    public static final AnsiColor RED = new AnsiColor("\033[0;38;5;160m"); // RED

    /** AnsiColor constant */
    public static final AnsiColor GREEN = new AnsiColor("\033[0;32m"); // GREEN

    /** AnsiColor constant */
    public static final AnsiColor YELLOW = new AnsiColor("\033[0;33m"); // YELLOW

    /** AnsiColor constant */
    public static final AnsiColor BLUE = new AnsiColor("\033[0;34m"); // BLUE

    /** AnsiColor constant */
    public static final AnsiColor PURPLE = new AnsiColor("\033[0;35m"); // PURPLE

    /** AnsiColor constant */
    public static final AnsiColor CYAN = new AnsiColor("\033[0;36m"); // CYAN

    /** AnsiColor constant */
    public static final AnsiColor WHITE = new AnsiColor("\033[0;37m"); // WHITE

    // Bold

    /** AnsiColor constant */
    public static final AnsiColor BLACK_BOLD = new AnsiColor("\033[1;30m"); // BLACK

    /** AnsiColor constant */
    public static final AnsiColor RED_BOLD = new AnsiColor("\033[1;31m"); // RED

    /** AnsiColor constant */
    public static final AnsiColor GREEN_BOLD = new AnsiColor("\033[1;32m"); // GREEN

    /** AnsiColor constant */
    public static final AnsiColor YELLOW_BOLD = new AnsiColor("\033[1;33m"); // YELLOW

    /** AnsiColor constant */
    public static final AnsiColor BLUE_BOLD = new AnsiColor("\033[1;34m"); // BLUE

    /** AnsiColor constant */
    public static final AnsiColor PURPLE_BOLD = new AnsiColor("\033[1;35m"); // PURPLE

    /** AnsiColor constant */
    public static final AnsiColor CYAN_BOLD = new AnsiColor("\033[1;36m"); // CYAN

    /** AnsiColor constant */
    public static final AnsiColor WHITE_BOLD = new AnsiColor("\033[1;37m"); // WHITE

    // Underline

    /** AnsiColor constant */
    public static final AnsiColor BLACK_UNDERLINED = new AnsiColor("\033[4;30m"); // BLACK

    /** AnsiColor constant */
    public static final AnsiColor RED_UNDERLINED = new AnsiColor("\033[4;31m"); // RED

    /** AnsiColor constant */
    public static final AnsiColor GREEN_UNDERLINED = new AnsiColor("\033[4;32m"); // GREEN

    /** AnsiColor constant */
    public static final AnsiColor YELLOW_UNDERLINED = new AnsiColor("\033[4;33m"); // YELLOW

    /** AnsiColor constant */
    public static final AnsiColor BLUE_UNDERLINED = new AnsiColor("\033[4;34m"); // BLUE

    /** AnsiColor constant */
    public static final AnsiColor PURPLE_UNDERLINED = new AnsiColor("\033[4;35m"); // PURPLE

    /** AnsiColor constant */
    public static final AnsiColor CYAN_UNDERLINED = new AnsiColor("\033[4;36m"); // CYAN

    /** AnsiColor constant */
    public static final AnsiColor WHITE_UNDERLINED = new AnsiColor("\033[4;37m"); // WHITE

    // High Intensity

    /** AnsiColor constant */
    public static final AnsiColor BLACK_BRIGHT = new AnsiColor("\033[0;90m"); // BLACK

    /** AnsiColor constant */
    public static final AnsiColor RED_BRIGHT = new AnsiColor("\033[0;38;5;196m"); // RED

    /** AnsiColor constant */
    public static final AnsiColor GREEN_BRIGHT = new AnsiColor("\033[0;92m"); // GREEN

    /** AnsiColor constant */
    public static final AnsiColor YELLOW_BRIGHT = new AnsiColor("\033[0;93m"); // YELLOW

    /** AnsiColor constant */
    public static final AnsiColor BLUE_BRIGHT = new AnsiColor("\033[0;94m"); // BLUE

    /** AnsiColor constant */
    public static final AnsiColor PURPLE_BRIGHT = new AnsiColor("\033[0;95m"); // PURPLE

    /** AnsiColor constant */
    public static final AnsiColor CYAN_BRIGHT = new AnsiColor("\033[0;96m"); // CYAN

    /** AnsiColor constant */
    public static final AnsiColor WHITE_BRIGHT = new AnsiColor("\033[1;97m");

    // Bold High Intensity

    /** AnsiColor constant */
    public static final AnsiColor BLACK_BOLD_BRIGHT = new AnsiColor("\033[1;90m"); // BLACK

    /** AnsiColor constant */
    public static final AnsiColor RED_BOLD_BRIGHT = new AnsiColor("\033[1;38;5;160m"); // RED

    /** AnsiColor constant */
    public static final AnsiColor GREEN_BOLD_BRIGHT = new AnsiColor("\033[1;92m"); // GREEN

    /** AnsiColor constant */
    public static final AnsiColor YELLOW_BOLD_BRIGHT = new AnsiColor("\033[1;93m"); // YELLOW

    /** AnsiColor constant */
    public static final AnsiColor BLUE_BOLD_BRIGHT = new AnsiColor("\033[1;94m"); // BLUE

    /** AnsiColor constant */
    public static final AnsiColor PURPLE_BOLD_BRIGHT = new AnsiColor("\033[1;95m"); // PURPLE

    /** AnsiColor constant */
    public static final AnsiColor CYAN_BOLD_BRIGHT = new AnsiColor("\033[1;96m"); // CYAN

    /** AnsiColor constant */
    public static final AnsiColor WHITE_BOLD_BRIGHT = new AnsiColor("\033[1;97m"); // WHITE

    // Background

    /** AnsiColor constant */
    public static final AnsiColor BLACK_BACKGROUND = new AnsiColor("\033[40m"); // BLACK

    /** AnsiColor constant */
    public static final AnsiColor RED_BACKGROUND = new AnsiColor("\033[41m"); // RED

    /** AnsiColor constant */
    public static final AnsiColor GREEN_BACKGROUND = new AnsiColor("\033[42m"); // GREEN

    /** AnsiColor constant */
    public static final AnsiColor YELLOW_BACKGROUND = new AnsiColor("\033[43m"); // YELLOW

    /** AnsiColor constant */
    public static final AnsiColor BLUE_BACKGROUND = new AnsiColor("\033[44m"); // BLUE

    /** AnsiColor constant */
    public static final AnsiColor PURPLE_BACKGROUND = new AnsiColor("\033[45m"); // PURPLE

    /** AnsiColor constant */
    public static final AnsiColor CYAN_BACKGROUND = new AnsiColor("\033[46m"); // CYAN

    /** AnsiColor constant */
    public static final AnsiColor WHITE_BACKGROUND = new AnsiColor("\033[47m"); // WHITE

    // High Intensity backgrounds

    /** AnsiColor constant */
    public static final AnsiColor BLACK_BACKGROUND_BRIGHT = new AnsiColor("\033[0;100m"); // BLACK

    /** AnsiColor constant */
    public static final AnsiColor RED_BACKGROUND_BRIGHT = new AnsiColor("\033[0;101m"); // RED

    /** AnsiColor constant */
    public static final AnsiColor GREEN_BACKGROUND_BRIGHT = new AnsiColor("\033[0;102m"); // GREEN

    /** AnsiColor constant */
    public static final AnsiColor YELLOW_BACKGROUND_BRIGHT = new AnsiColor("\033[0;103m"); // YELLOW

    /** AnsiColor constant */
    public static final AnsiColor BLUE_BACKGROUND_BRIGHT = new AnsiColor("\033[0;104m"); // BLUE

    /** AnsiColor constant */
    public static final AnsiColor PURPLE_BACKGROUND_BRIGHT = new AnsiColor("\033[0;105m"); // PURPLE

    /** AnsiColor constant */
    public static final AnsiColor CYAN_BACKGROUND_BRIGHT = new AnsiColor("\033[0;106m"); // CYAN

    /** AnsiColor constant */
    public static final AnsiColor WHITE_BACKGROUND_BRIGHT = new AnsiColor("\033[0;107m"); // WHITE

    private static boolean ANSI_COLOR_SUPPORTED;

    static {
        ANSI_COLOR_SUPPORTED = System.console() != null;

        if (ONE.equals(System.getenv(NO_COLOR))) {
            ANSI_COLOR_SUPPORTED = false;
        }

        if (TestEngineConstants.MAVEN_PLUGIN_BATCH.equals(
                System.getenv(TestEngineConstants.MAVEN_PLUGIN_MODE))) {
            ANSI_COLOR_SUPPORTED = false;
        }
    }

    private final String escapeSequence;

    /**
     * Constructor
     *
     * @param escapeSequence sequence
     */
    private AnsiColor(String escapeSequence) {
        this.escapeSequence = escapeSequence;
    }

    /**
     * Method to wrap an Object's string representation (toString()) with an ANSI color escape
     * sequence
     *
     * @param object object
     * @return the return value
     */
    public String wrap(Object object) {
        if (ANSI_COLOR_SUPPORTED) {
            return escapeSequence + object + AnsiColor.TEXT_RESET;
        } else {
            return String.valueOf(object);
        }
    }

    /**
     * Method to get the ANSI color escape sequence String
     *
     * @return the ANSI color escape sequence if ANSI color is supported else an empty string
     */
    @Override
    public String toString() {
        if (ANSI_COLOR_SUPPORTED) {
            return escapeSequence;
        } else {
            return EMPTY_STRING;
        }
    }

    /**
     * Method to indicate whether ANSI color escape sequences are supported
     *
     * @return the return value
     */
    public static boolean isSupported() {
        return ANSI_COLOR_SUPPORTED;
    }

    /**
     * Method to set/force ANSI color escape sequences to be supported
     *
     * @param ansiColorSupported ansiColorSupported
     */
    public static void setSupported(boolean ansiColorSupported) {
        ANSI_COLOR_SUPPORTED = ansiColorSupported;
    }

    /**
     * Method to get an ANSI color for a custom ANSI color escape sequence
     *
     * @param escapeSequence escapeSequence
     * @return an AnsiColor
     */
    public static AnsiColor ofSequence(String escapeSequence) {
        return new AnsiColor(escapeSequence);
    }
}
