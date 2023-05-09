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

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Class to implement a Logger
 */
@SuppressWarnings("PMD.GodClass")
public class Logger {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd | HH:mm:ss.SSS", Locale.getDefault());

    private static final int OFF = 0;
    private static final int ERROR = 100;
    private static final int WARNING = 200;
    private static final int INFO = 300;
    private static final int DEBUG = 400;
    private static final int TRACE = 500;
    private static final int ALL = Integer.MAX_VALUE;

    private static final Map<String, Integer> LOG_LEVEL_MAP;

    static {
        LOG_LEVEL_MAP = new HashMap<>();
        LOG_LEVEL_MAP.put("OFF", OFF);
        LOG_LEVEL_MAP.put("ERROR", ERROR);
        LOG_LEVEL_MAP.put("WARNING", WARNING);
        LOG_LEVEL_MAP.put("INFO", INFO);
        LOG_LEVEL_MAP.put("DEBUG", DEBUG);
        LOG_LEVEL_MAP.put("TRACE", TRACE);
        LOG_LEVEL_MAP.put("ALL", ALL);
    }

    private final String name;
    private final AtomicReference<Level> level;

    /**
     * Constructor
     *
     * @param name name
     * @param level level
     */
    public Logger(String name, Level level) {
        this.name = name;
        this.level = new AtomicReference<>(level);
    }

    /**
     * Method to return if TRACE logging is enabled
     *
     * @return the return value
     */
    public boolean isTraceEnabled() {
        return level.get().toInt() >= Level.TRACE.toInt();
    }

    /**
     * Method to return if DEBUG logging is enabled
     *
     * @return the return value
     */
    public boolean isDebugEnabled() {
        return level.get().toInt() >= Level.DEBUG.toInt();
    }

    /**
     * Method to return if INFO logging is enabled
     *
     * @return the return value
     */
    public boolean isInfoEnabled() {
        return level.get().toInt() >= Level.INFO.toInt();
    }

    /**
     * Method to return if WARNING logging is enabled
     *
     * @return the return value
     */
    public boolean isWarnEnabled() {
        return level.get().toInt() >= Level.WARN.toInt();
    }

    /**
     * Method to return if ERROR logging is enabled
     *
     * @return the return value
     */
    public boolean isErrorEnabled() {
        return level.get().toInt() >= Level.ERROR.toInt();
    }

    /**
     * Method to log a TRACE message
     *
     * @param message message
     */
    public void trace(String message) {
        if (isTraceEnabled()) {
            log(System.out, createMessage(Level.TRACE, message));
        }
    }

    /**
     * Method to log a TRACE message
     *
     * @param format format
     * @param object object
     */
    public void trace(String format, Object object) {
        if (isTraceEnabled()) {
            trace(format, new Object[]{object});
        }
    }


    /**
     * Method to log a TRACE message
     *
     * @param format format
     * @param objects objects
     */
    public void trace(String format, Object... objects) {
        if (isTraceEnabled()) {
            Objects.requireNonNull(format);
            log(System.out, createMessage(Level.TRACE, String.format(format, objects)));
        }
    }

    /**
     * Method to log a TRACE message
     *
     * @param message message
     * @param throwable throwable
     */
    public void trace(String message, Throwable throwable) {
        if (isTraceEnabled()) {
            log(System.out, createMessage(Level.TRACE, createMessage(message, throwable)));
        }
    }

    /**
     * Method to log a DEBUG message
     *
     * @param message message
     */
    public void debug(String message) {
        if (isDebugEnabled()) {
            log(System.out, createMessage(Level.DEBUG, message));
        }
    }

    /**
     * Method to log a DEBUG message
     *
     * @param format format
     * @param object object
     */
    public void debug(String format, Object object) {
        if (isDebugEnabled()) {
            debug(format, new Object[]{object});
        }
    }

    /**
     * Method to log a DEBUG message
     *
     * @param format format
     * @param objects objects
     */
    public void debug(String format, Object... objects) {
        if (isDebugEnabled()) {
            Objects.requireNonNull(format);
            log(System.out, createMessage(Level.DEBUG, String.format(format, objects)));
        }
    }

    /**
     * Method to log a DEBUG message
     *
     * @param message message
     * @param throwable throwable
     */
    public void debug(String message, Throwable throwable) {
        if (isDebugEnabled()) {
            log(System.out, createMessage(Level.DEBUG, createMessage(message, throwable)));
        }
    }

    /**
     * Method to log an INFO message
     *
     * @param message message
     */
    public void info(String message) {
        if (isInfoEnabled()) {
            log(System.out, createMessage(Level.INFO, message));
        }
    }

    /**
     * Method to log an INFO message
     *
     * @param format format
     * @param object object
     */
    public void info(String format, Object object) {
        if (isInfoEnabled()) {
            info(format, new Object[]{object});
        }
    }

    /**
     * Method to log an INFO message
     *
     * @param format format
     * @param objects objects
     */
    public void info(String format, Object... objects) {
        if (isInfoEnabled()) {
            Objects.requireNonNull(format);
            log(System.out, createMessage(Level.INFO, String.format(format, objects)));
        }
    }

    /**
     * Method to log an INFO message
     *
     * @param message message
     * @param throwable throwable
     */
    public void info(String message, Throwable throwable) {
        if (isInfoEnabled()) {
            log(System.out, createMessage(Level.INFO, createMessage(message, throwable)));
        }
    }

    /**
     * Method to log a WARN message
     *
     * @param message message
     */
    public void warn(String message) {
        if (isWarnEnabled()) {
            log(System.out, createMessage(Level.WARN, message));
        }
    }

    /**
     * Method to log an WARN message
     *
     * @param format format
     * @param object object
     */
    public void warn(String format, Object object) {
        if (isWarnEnabled()) {
            warn(format, new Object[]{object});
        }
    }

    /**
     * Method to log an WARN message
     *
     * @param format format
     * @param objects objects
     */
    public void warn(String format, Object... objects) {
        if (isWarnEnabled()) {
            Objects.requireNonNull(format);
            log(System.out, createMessage(Level.WARN, String.format(format, objects)));
        }
    }

    /**
     * Method to log an WARN message
     *
     * @param message message
     * @param throwable throwable
     */
    public void warn(String message, Throwable throwable) {
        if (isWarnEnabled()) {
            log(System.out, createMessage(Level.WARN, createMessage(message, throwable)));
        }
    }

    /**
     * Method to log an ERROR message
     *
     * @param message message
     */
    public void error(String message) {
        if (isErrorEnabled()) {
            log(System.err, createMessage(Level.ERROR, message));
        }
    }

    /**
     * Method to log an ERROR message
     *
     * @param format format
     * @param object object
     */
    public void error(String format, Object object) {
        if (isErrorEnabled()) {
            error(format, new Object[]{object});
        }
    }

    /**
     * Method to log an ERROR message
     *
     * @param format format
     * @param objects objects
     */
    public void error(String format, Object... objects) {
        if (isErrorEnabled()) {
            Objects.requireNonNull(format);
            log(System.out, createMessage(Level.ERROR, String.format(format, objects)));
        }
    }

    /**
     * Method to log an ERROR message
     *
     * @param message message
     * @param throwable throwable
     */
    public void error(String message, Throwable throwable) {
        if (isErrorEnabled()) {
            log(System.out, createMessage(Level.ERROR, createMessage(message, throwable)));
        }
    }

    /**
     * Method to create a log message
     *
     * @apram level
     * @param message message
     * @return the return value
     */
    private String createMessage(Level level, String message) {
        String dateTime;

        synchronized (SIMPLE_DATE_FORMAT) {
            dateTime = SIMPLE_DATE_FORMAT.format(new Date());
        }

        return String.format("%s | %s | %s | %s | %s ",
                dateTime,
                Thread.currentThread().getName(),
                level.toString(),
                name,
                message);
    }

    /**
     * Method to create a log message
     *
     * @param message message
     * @param throwable throwable
     * @return the return value
     */
    private String createMessage(String message, Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
            printWriter.println(message);
            if (throwable != null) {
                throwable.printStackTrace(printWriter);
            }
        }
        return stringWriter.toString();
    }

    /**
     * Method to log to a PrintStream
     *
     * @param printStream printStream
     * @param message message
     */
    private void log(PrintStream printStream, String message) {
        printStream.println(message);
        printStream.flush();
    }
}
