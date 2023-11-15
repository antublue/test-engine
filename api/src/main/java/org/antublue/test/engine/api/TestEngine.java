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

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.platform.commons.annotation.Testable;

/** Interface that contains all TestEngine annotations */
public @interface TestEngine {

    /** Supplier meta-annotation */
    @Target(ElementType.ANNOTATION_TYPE)
    @interface Supplier {

        /** Argument annotation */
        @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
        @Retention(RetentionPolicy.RUNTIME)
        @interface Argument {}

        /** Extension annotation */
        @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
        @Retention(RetentionPolicy.RUNTIME)
        @interface Extension {}
    }

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

    /** AutoClose meta-annotation */
    @Target(ElementType.ANNOTATION_TYPE)
    @interface AutoClose {

        /** AfterEach annotation */
        @Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
        @Retention(RetentionPolicy.RUNTIME)
        @interface AfterEach {

            /**
             * Method value
             *
             * @return the method name
             */
            String method() default "";
        }

        /** AfterAll annotation */
        @Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
        @Retention(RetentionPolicy.RUNTIME)
        @interface AfterAll {

            /**
             * Method value
             *
             * @return the method name
             */
            String method() default "";
        }

        /** Conclude annotation */
        @Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
        @Retention(RetentionPolicy.RUNTIME)
        @interface Conclude {

            /**
             * Method value
             *
             * @return the method name
             */
            String method() default "";
        }
    }

    /** Random meta-annotation */
    @Target(ElementType.ANNOTATION_TYPE)
    @interface Random {

        /** Boolean annotation */
        @Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
        @Retention(RetentionPolicy.RUNTIME)
        @interface Boolean {}

        /** Byte annotation */
        @Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
        @Retention(RetentionPolicy.RUNTIME)
        @interface Byte {

            /**
             * Minimum value
             *
             * @return the minimum value
             */
            byte minimum() default java.lang.Byte.MIN_VALUE;

            /**
             * Maximum value
             *
             * @return the maximum value
             */
            byte maximum() default java.lang.Byte.MAX_VALUE;
        }

        /** Char annotation */
        @Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
        @Retention(RetentionPolicy.RUNTIME)
        @interface Character {

            /**
             * Minimum value
             *
             * @return the minimum value
             */
            char minimum() default java.lang.Character.MIN_VALUE;

            /**
             * Maximum value
             *
             * @return the maximum value
             */
            char maximum() default java.lang.Character.MAX_VALUE;
        }

        /** Short annotation */
        @Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
        @Retention(RetentionPolicy.RUNTIME)
        @interface Short {

            /**
             * Minimum value
             *
             * @return the minimum value
             */
            short minimum() default java.lang.Short.MIN_VALUE;

            /**
             * Maximum value
             *
             * @return the maximum value
             */
            short maximum() default java.lang.Short.MAX_VALUE;
        }

        /** Integer annotation */
        @Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
        @Retention(RetentionPolicy.RUNTIME)
        @interface Integer {

            /**
             * Minimum value
             *
             * @return the minimum value
             */
            int minimum() default java.lang.Integer.MIN_VALUE;

            /**
             * Maximum value
             *
             * @return the maximum value
             */
            int maximum() default java.lang.Integer.MAX_VALUE;
        }

        /** Long annotation */
        @Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
        @Retention(RetentionPolicy.RUNTIME)
        @interface Long {

            /**
             * Minimum value
             *
             * @return the minimum value
             */
            long minimum() default java.lang.Long.MIN_VALUE;

            /**
             * Maximum value
             *
             * @return the maximum value
             */
            long maximum() default java.lang.Long.MAX_VALUE;
        }

        /** Float annotation */
        @Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
        @Retention(RetentionPolicy.RUNTIME)
        @interface Float {

            /**
             * Minimum value
             *
             * @return the minimum value
             */
            float minimum() default -java.lang.Float.MAX_VALUE;

            /**
             * Maximum value
             *
             * @return the maximum value
             */
            float maximum() default java.lang.Float.MAX_VALUE;
        }

        /** Double annotation */
        @Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
        @Retention(RetentionPolicy.RUNTIME)
        @interface Double {

            /**
             * Minimum value
             *
             * @return the minimum value
             */
            double minimum() default -java.lang.Double.MAX_VALUE;

            /**
             * Maximum value
             *
             * @return the maximum value
             */
            double maximum() default java.lang.Double.MAX_VALUE;
        }

        /** BigInteger annotation */
        @Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
        @Retention(RetentionPolicy.RUNTIME)
        @interface BigInteger {

            /**
             * Minimum value
             *
             * @return the minimum value
             */
            String minimum();

            /**
             * Maximum value
             *
             * @return the maximum value
             */
            String maximum();
        }

        /** BigDecimal annotation */
        @Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
        @Retention(RetentionPolicy.RUNTIME)
        @interface BigDecimal {

            /**
             * Minimum value
             *
             * @return the minimum value
             */
            String minimum();

            /**
             * Maximum value
             *
             * @return the maximum value
             */
            String maximum();
        }

        /** UUID annotation */
        @Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
        @Retention(RetentionPolicy.RUNTIME)
        @interface UUID {}
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
        READ
    }
}
