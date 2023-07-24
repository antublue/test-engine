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

/** Interface that contains all TestEngine annotations */
public @interface TestEngine {

    /** ArgumentSupplier annotation */
    @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface ArgumentSupplier {}

    /** Argument annotation */
    @Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Argument {}

    /** Prepare annotation */
    @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Prepare {}

    /** BeforeAll annotation */
    @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface BeforeAll {}

    /** BeforeEach annotation */
    @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface BeforeEach {}

    /** Test annotation */
    @Testable
    @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Test {}

    /** AfterEach annotation */
    @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface AfterEach {}

    /** AfterAll annotation */
    @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface AfterAll {}

    /** Conclude annotation */
    @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Conclude {}

    /** Order annotation */
    @Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Order {

        /**
         * Order value
         *
         * @return the order value
         */
        int order();
    }

    /** Disabled annotation */
    @Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Disabled {}

    /** BaseClass annotation */
    @Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface BaseClass {}

    /** Tag annotation */
    @Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Tag {

        /**
         * Tag value
         *
         * @return the tag value
         */
        String tag();
    }

    /** DisplayName annotation */
    @Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface DisplayName {

        /**
         * DisplayName value
         *
         * @return the display name value
         */
        String name();
    }

    /** AutoClose annotation */
    @Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface AutoClose {

        /**
         * Lifecycle value
         *
         * @return the lifecycle value
         */
        String lifecycle();

        /**
         * Method value
         *
         * @return the method name
         */
        String method() default "";
    }

    /** Lock annotation */
    @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(ResourceLock.List.class)
    @interface ResourceLock {

        /**
         * Name value
         *
         * @return the lock name value
         */
        String name();

        /**
         * Mode value
         *
         * @return the lock mode value
         */
        LockMode mode() default LockMode.READ_WRITE;

        /** Lock.List annotation */
        @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
        @Retention(RetentionPolicy.RUNTIME)
        @interface List {

            /**
             * List values
             *
             * @return the list values
             */
            ResourceLock[] value();
        }
    }

    /** Lock annotation */
    @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(TestEngine.Lock.List.class)
    @interface Lock {

        /**
         * Name value
         *
         * @return the lock name value
         */
        String name();

        /**
         * Mode value
         *
         * @return the lock mode value
         */
        LockMode mode() default LockMode.READ_WRITE;

        /** Lock.List annotation */
        @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
        @Retention(RetentionPolicy.RUNTIME)
        @interface List {

            /**
             * List values
             *
             * @return the list values
             */
            TestEngine.Lock[] value();
        }
    }

    /** Unlock annotation */
    @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(Unlock.List.class)
    @interface Unlock {

        /**
         * Name value
         *
         * @return the lock name value
         */
        String name();

        /**
         * Mode value
         *
         * @return the lock mode value
         */
        LockMode mode() default LockMode.READ_WRITE;

        /** Unlock.List annotation */
        @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
        @Retention(RetentionPolicy.RUNTIME)
        @interface List {

            /**
             * List values
             *
             * @return the list values
             */
            Unlock[] value();
        }
    }

    /** Lock modes */
    enum LockMode {

        /** Read write mode */
        READ_WRITE,

        /** Read mode */
        READ;
    }
}
