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

package org.antublue.test.engine.internal.predicate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/** Class to implement a predicate to match a test method */
public class AnnotationMethodPredicate implements Predicate<Method> {

    private final List<Class<? extends Annotation>> annotations;

    private AnnotationMethodPredicate(List<Class<? extends Annotation>> annotations) {
        this.annotations = annotations;
    }

    @Override
    public boolean test(Method method) {
        for (Class<? extends Annotation> annotation : annotations) {
            if (method.isAnnotationPresent(annotation)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method to get an AnnotationMethodPredicate for an Annotation
     *
     * @param annotation annotation
     * @return an AnnotationMethodPredicate for an Annotation
     */
    public static AnnotationMethodPredicate of(Class<? extends Annotation> annotation) {
        return of(Collections.singletonList(annotation));
    }

    /**
     * Method to get an AnnotationMethodPredicate for a list of Annotations
     *
     * @param annotations annotations
     * @return an AnnotationMethodPredicate for a list of Annotations
     */
    @SafeVarargs
    public static AnnotationMethodPredicate of(Class<? extends Annotation>... annotations) {
        return new AnnotationMethodPredicate(new ArrayList<>(Arrays.asList(annotations)));
    }

    /**
     * Method to get an AnnotationMethodPredicate for a list of Annotations
     *
     * @param annotations annotations
     * @return an AnnotationMethodPredicate for a list of Annotations
     */
    public static AnnotationMethodPredicate of(List<Class<? extends Annotation>> annotations) {
        return new AnnotationMethodPredicate(annotations);
    }
}
