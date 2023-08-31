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

package org.antublue.test.engine.configuration;

/** Class that contains Test Engine configuration constants */
public final class Constants {

    /** Configuration constant */
    public static final String TRUE = "true";

    /** Configuration constant */
    public static final String PREFIX = "antublue.test.engine";

    /** Configuration constant */
    public static final String EXPERIMENTAL = ".experimental";

    /** Configuration constant */
    public static final String THREAD_COUNT = PREFIX + ".thread.count";

    /** Configuration constant */
    public static final String EXTENSIONS = PREFIX + ".extensions";

    /** Configuration constant */
    public static final String STACK_TRACE_PRUNE = PREFIX + ".stack.trace.pruning";

    /** Configuration constant */
    public static final String LOGGER_REGEX = PREFIX + ".logger.regex";

    /** Configuration constant - DEPRECATED */
    public static final String LOG_LEVEL_REGEX = PREFIX + ".log.level.regex";

    /** Configuration constant */
    public static final String LOGGER_LEVEL = PREFIX + ".logger.level";

    /** Configuration constant - DEPRECATED */
    public static final String LOG_LEVEL = PREFIX + ".log.level";

    /** Configuration constant */
    public static final String CONSOLE_LOG = PREFIX + ".console.log";

    /** Configuration constant */
    public static final String CONSOLE_LOG_TIMING = CONSOLE_LOG + ".timing";

    /** Configuration constant */
    public static final String CONSOLE_LOG_TIMING_UNITS = CONSOLE_LOG_TIMING + ".units";

    /** Configuration constant */
    public static final String CONSOLE_LOG_TEST_MESSAGES = CONSOLE_LOG + ".test.messages";

    /** Configuration constant */
    public static final String CONSOLE_LOG_PASS_MESSAGES = CONSOLE_LOG + ".pass.messages";

    /** Configuration constant */
    public static final String CONSOLE_LOG_SKIP_MESSAGES = CONSOLE_LOG + ".skip.messages";

    /** Configuration constant */
    public static final String TEST_CLASS_SHUFFLE = PREFIX + ".test.class.shuffle";

    /** Configuration constant */
    public static final String TEST_CLASS_INCLUDE = PREFIX + ".test.class.include";

    /** Configuration constant */
    public static final String TEST_CLASS_EXCLUDE = PREFIX + ".test.class.exclude";

    /** Configuration constant */
    public static final String TEST_METHOD_INCLUDE = PREFIX + ".test.method.include";

    /** Configuration constant */
    public static final String TEST_METHOD_EXCLUDE = PREFIX + ".test.method.exclude";

    /** Configuration constant */
    public static final String TEST_CLASS_TAG_INCLUDE = PREFIX + ".test.class.tag.include";

    /** Configuration constant */
    public static final String TEST_CLASS_TAG_EXCLUDE = PREFIX + ".test.class.tag.exclude";

    /** Configuration constant */
    public static final String TEST_METHOD_TAG_INCLUDE = PREFIX + ".test.method.tag.include";

    /** Configuration constant */
    public static final String TEST_METHOD_TAG_EXCLUDE = PREFIX + ".test.method.tag.exclude";

    /** Configuration constant */
    public static final String MAVEN_PLUGIN = PREFIX + ".maven.plugin";

    /** Configuration constant */
    public static final String MAVEN_PLUGIN_MODE = PREFIX + ".maven.plugin.mode";

    /** Configuration constant */
    public static final String MAVEN_PLUGIN_INTERACTIVE = PREFIX + ".maven.plugin.interactive";

    /** Configuration constant */
    public static final String MAVEN_PLUGIN_BATCH = PREFIX + ".maven.plugin.batch";

    /** Constructor */
    private Constants() {
        // DO NOTHING
    }
}
