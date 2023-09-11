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

package org.antublue.test.engine.maven.plugin.logger;

import org.apache.maven.plugin.logging.Log;

/** Class to implement MavenPluginLogger */
public class Logger {

    private final Log log;

    /**
     * Constructor
     *
     * @param log log
     */
    private Logger(Log log) {
        this.log = log;
    }

    public void debug(String message) {
        if (log.isDebugEnabled()) {
            log.debug(message);
        }
    }

    public void debug(String format, Object... objects) {
        if (log.isDebugEnabled()) {
            log.debug(String.format(format, objects));
        }
    }

    public void info(String message) {
        if (log.isInfoEnabled()) {
            log.info(message);
        }
    }

    public void info(String format, Object... objects) {
        if (log.isInfoEnabled()) {
            log.info(String.format(format, objects));
        }
    }

    public void warn(String message) {
        if (log.isWarnEnabled()) {
            log.warn(message);
        }
    }

    public void war(String format, Object... objects) {
        if (log.isWarnEnabled()) {
            log.warn(String.format(format, objects));
        }
    }

    public void error(String message) {
        if (log.isErrorEnabled()) {
            log.error(message);
        }
    }

    public void error(String format, Object... objects) {
        if (log.isErrorEnabled()) {
            log.error(String.format(format, objects));
        }
    }

    public static Logger from(Log log) {
        return new Logger(log);
    }
}
