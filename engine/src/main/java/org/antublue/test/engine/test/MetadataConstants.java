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

package org.antublue.test.engine.test;

public class MetadataConstants {

    public static String PASS = "PASS";

    public static String FAIL = "FAIL";

    public static String SKIP = "SKIP";

    public static final String TEST_CLASS = "testClass";

    public static final String TEST_ARGUMENT = "testArgument";

    public static final String TEST_METHOD = "testMethod";

    public static final String TEST_DESCRIPTOR_STATUS = "testDescriptorStatus";

    public static final String TEST_DESCRIPTOR_ELAPSED_TIME = "testDescriptorElapsedTime";

    private MetadataConstants() {
        // DO NOTHING
    }
}
