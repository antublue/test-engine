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

package org.antublue.test.engine;

/**
 * Class to implement Test Engine configuration constants
 */
public final class TestEngineConstants {

    public static final String PREFIX = "antublue.test.engine";
    public static final String EXPERIMENTAL = ".experimental";
    
    public static final String CONSOLE_OUTPUT = PREFIX + ".console.output";
    public static final String THREAD_COUNT = PREFIX + ".thread.count";
    public static final String LOG_TEST_MESSAGES = PREFIX + EXPERIMENTAL + ".log.test.messages";
    public static final String LOG_PASS_MESSAGES = PREFIX + EXPERIMENTAL + ".log.pass.messages";

    public static final String TEST_CLASS_INCLUDE = PREFIX + ".test.class.include";
    public static final String TEST_CLASS_EXCLUDE = PREFIX + ".test.class.exclude";
    public static final String TEST_METHOD_INCLUDE = PREFIX + ".test.method.include";
    public static final String TEST_METHOD_EXCLUDE = PREFIX + ".test.method.exclude";
    public static final String TEST_CLASS_TAG_INCLUDE = PREFIX + ".test.class.tag.include";
    public static final String TEST_CLASS_TAG_EXCLUDE = PREFIX + ".test.class.tag.exclude";
    public static final String TEST_METHOD_TAG_INCLUDE = PREFIX + ".test.method.tag.include";
    public static final String TEST_METHOD_TAG_EXCLUDE = PREFIX + ".test.method.tag.exclude";

    /**
     * Constructor
     */
    private TestEngineConstants() {
        // DO NOTHING
    }
}
