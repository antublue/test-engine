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

package org.antublue.test.engine.api;

import org.junit.platform.commons.annotation.Testable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Interface that contains all TestEngine annotations
 */
public @interface TestEngine {

    @Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @interface ArgumentSupplier {

    }

    @Target({ ElementType.ANNOTATION_TYPE, ElementType.FIELD })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Argument {

    }

    @Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Prepare {

    }

    @Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @interface BeforeAll {

    }

    @Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @interface BeforeEach {

    }

    @Testable
    @Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Test {

    }

    @Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @interface AfterEach {

    }

    @Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @interface AfterAll {

    }

    @Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Conclude {

    }

    @Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Order {
        int value();
    }

    @Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Disabled {

    }

    @Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface BaseClass {

    }

    @Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Tag {
        String value();
    }

    @Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @interface DisplayName {
        String value();
    }

    enum LockMode {
        READ_WRITE,
        READ;
    }

    @Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(ResourceLock.List.class)
    @interface ResourceLock {
        String value();
        LockMode mode() default LockMode.READ_WRITE;

        @Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
        @Retention(RetentionPolicy.RUNTIME)
        @interface List {
            ResourceLock[] value();
        }
    }

    @Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(TestEngine.Lock.List.class)
    @interface Lock {
        String value();
        LockMode mode() default LockMode.READ_WRITE;

        @Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
        @Retention(RetentionPolicy.RUNTIME)
        @interface List {
            TestEngine.Lock[] value();
        }
    }

    @Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(Unlock.List.class)
    @interface Unlock {
        String value();
        LockMode mode() default LockMode.READ_WRITE;

        @Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
        @Retention(RetentionPolicy.RUNTIME)
        @interface List {
            Unlock[] value();
        }
    }
}
