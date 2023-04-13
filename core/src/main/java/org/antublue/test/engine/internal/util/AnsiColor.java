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

package org.antublue.test.engine.internal.util;

// Based on https://www.w3schools.blog/ansi-colors-java
public class AnsiColor {

    // Reset
    public static final AnsiColor RESET = new AnsiColor("\033[0m");  // Text Reset

    // Regular Colors
    public static final AnsiColor BLACK = new AnsiColor("\033[0;30m");   // BLACK
    public static final AnsiColor RED = new AnsiColor("\033[0;31m");     // RED
    public static final AnsiColor GREEN = new AnsiColor("\033[0;32m");   // GREEN
    public static final AnsiColor YELLOW = new AnsiColor("\033[0;33m");  // YELLOW
    public static final AnsiColor BLUE = new AnsiColor("\033[0;34m");    // BLUE
    public static final AnsiColor PURPLE = new AnsiColor("\033[0;35m");  // PURPLE
    public static final AnsiColor CYAN = new AnsiColor("\033[0;36m");    // CYAN
    public static final AnsiColor WHITE = new AnsiColor("\033[0;37m");   // WHITE

    // Bold
    public static final AnsiColor BLACK_BOLD = new AnsiColor("\033[1;30m");  // BLACK
    public static final AnsiColor RED_BOLD = new AnsiColor("\033[1;31m");    // RED
    public static final AnsiColor GREEN_BOLD = new AnsiColor("\033[1;32m");  // GREEN
    public static final AnsiColor YELLOW_BOLD = new AnsiColor("\033[1;33m"); // YELLOW
    public static final AnsiColor BLUE_BOLD = new AnsiColor("\033[1;34m");   // BLUE
    public static final AnsiColor PURPLE_BOLD = new AnsiColor("\033[1;35m"); // PURPLE
    public static final AnsiColor CYAN_BOLD = new AnsiColor("\033[1;36m");   // CYAN
    public static final AnsiColor WHITE_BOLD = new AnsiColor("\033[1;37m");  // WHITE

    // Underline
    public static final AnsiColor BLACK_UNDERLINED = new AnsiColor("\033[4;30m");  // BLACK
    public static final AnsiColor RED_UNDERLINED = new AnsiColor("\033[4;31m");    // RED
    public static final AnsiColor GREEN_UNDERLINED = new AnsiColor("\033[4;32m");  // GREEN
    public static final AnsiColor YELLOW_UNDERLINED = new AnsiColor("\033[4;33m"); // YELLOW
    public static final AnsiColor BLUE_UNDERLINED = new AnsiColor("\033[4;34m");   // BLUE
    public static final AnsiColor PURPLE_UNDERLINED = new AnsiColor("\033[4;35m"); // PURPLE
    public static final AnsiColor CYAN_UNDERLINED = new AnsiColor("\033[4;36m");   // CYAN
    public static final AnsiColor WHITE_UNDERLINED = new AnsiColor("\033[4;37m");  // WHITE

    // Background
    public static final AnsiColor BLACK_BACKGROUND = new AnsiColor("\033[40m");  // BLACK
    public static final AnsiColor RED_BACKGROUND = new AnsiColor("\033[41m");    // RED
    public static final AnsiColor GREEN_BACKGROUND = new AnsiColor("\033[42m");  // GREEN
    public static final AnsiColor YELLOW_BACKGROUND = new AnsiColor("\033[43m"); // YELLOW
    public static final AnsiColor BLUE_BACKGROUND = new AnsiColor("\033[44m");   // BLUE
    public static final AnsiColor PURPLE_BACKGROUND = new AnsiColor("\033[45m"); // PURPLE
    public static final AnsiColor CYAN_BACKGROUND = new AnsiColor("\033[46m");   // CYAN
    public static final AnsiColor WHITE_BACKGROUND = new AnsiColor("\033[47m");  // WHITE

    // High Intensity
    public static final AnsiColor BLACK_BRIGHT = new AnsiColor("\033[0;90m");  // BLACK
    public static final AnsiColor RED_BRIGHT = new AnsiColor("\033[0;91m");    // RED
    public static final AnsiColor GREEN_BRIGHT = new AnsiColor("\033[0;92m");  // GREEN
    public static final AnsiColor YELLOW_BRIGHT = new AnsiColor("\033[0;93m"); // YELLOW
    public static final AnsiColor BLUE_BRIGHT = new AnsiColor("\033[0;94m");   // BLUE
    public static final AnsiColor PURPLE_BRIGHT = new AnsiColor("\033[0;95m"); // PURPLE
    public static final AnsiColor CYAN_BRIGHT = new AnsiColor("\033[0;96m");   // CYAN
    public static final AnsiColor WHITE_BRIGHT = new AnsiColor("\033[1;97m");  //"\033[38;2;255;255;255m");  // WHITE

    // Bold High Intensity
    public static final AnsiColor BLACK_BOLD_BRIGHT = new AnsiColor("\033[1;90m");  // BLACK
    public static final AnsiColor RED_BOLD_BRIGHT = new AnsiColor("\033[1;91m");    // RED
    public static final AnsiColor GREEN_BOLD_BRIGHT = new AnsiColor("\033[1;92m");  // GREEN
    public static final AnsiColor YELLOW_BOLD_BRIGHT = new AnsiColor("\033[1;93m"); // YELLOW
    public static final AnsiColor BLUE_BOLD_BRIGHT = new AnsiColor("\033[1;94m");   // BLUE
    public static final AnsiColor PURPLE_BOLD_BRIGHT = new AnsiColor("\033[1;95m"); // PURPLE
    public static final AnsiColor CYAN_BOLD_BRIGHT = new AnsiColor("\033[1;96m");   // CYAN
    public static final AnsiColor WHITE_BOLD_BRIGHT = new AnsiColor("\033[1;97m");  // WHITE

    // High Intensity backgrounds
    public static final AnsiColor BLACK_BACKGROUND_BRIGHT = new AnsiColor("\033[0;100m");  // BLACK
    public static final AnsiColor RED_BACKGROUND_BRIGHT = new AnsiColor("\033[0;101m");    // RED
    public static final AnsiColor GREEN_BACKGROUND_BRIGHT = new AnsiColor("\033[0;102m");  // GREEN
    public static final AnsiColor YELLOW_BACKGROUND_BRIGHT = new AnsiColor("\033[0;103m"); // YELLOW
    public static final AnsiColor BLUE_BACKGROUND_BRIGHT = new AnsiColor("\033[0;104m");   // BLUE
    public static final AnsiColor PURPLE_BACKGROUND_BRIGHT = new AnsiColor("\033[0;105m"); // PURPLE
    public static final AnsiColor CYAN_BACKGROUND_BRIGHT = new AnsiColor("\033[0;106m");   // CYAN
    public static final AnsiColor WHITE_BACKGROUND_BRIGHT = new AnsiColor("\033[0;107m");  // WHITE

    private static boolean ANSI_COLOR_SUPPORTED;

    static {
        if ((System.console() != null) || "true".equals(System.getenv("__ANTUBLUE_TEST_ENGINE_HAS_CONSOLE__"))) {
            ANSI_COLOR_SUPPORTED = true;
        } else {
            ANSI_COLOR_SUPPORTED = false;
        }
    }

    private final String escapeString;

    /**
     * Constructor
     *
     * @param escapeString
     */
    public AnsiColor(String escapeString) {
        this.escapeString = escapeString;
    }

    /**
     * Method to wrap a String with an ANSI color escape sequence
     *
     * @param object
     * @return
     */
    public String apply(Object object) {
        if (ANSI_COLOR_SUPPORTED) {
            return escapeString + object + AnsiColor.RESET;
        } else {
            return "" + object;
        }
    }

    /**
     * Method to get the ANSI color escape sequence String
     *
     * @return
     */
    @Override
    public String toString() {
        if (!ANSI_COLOR_SUPPORTED) {
            return "";
        }

        return escapeString;
    }

    /**
     * Method to indicate whether ANSI color sequences are supported
     *
     * @return
     */
    public static boolean isSupported() {
        return ANSI_COLOR_SUPPORTED;
    }
}