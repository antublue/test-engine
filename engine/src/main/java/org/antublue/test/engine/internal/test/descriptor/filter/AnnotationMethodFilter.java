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

package org.antublue.test.engine.internal.test.descriptor.filter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class AnnotationMethodFilter implements Predicate<Method> {

    private final List<Class<? extends Annotation>> annotations;

    private AnnotationMethodFilter(List<Class<? extends Annotation>> annotations) {
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

    public static AnnotationMethodFilter of(Class<? extends Annotation> annotation) {
        return of(Collections.singletonList(annotation));
    }

    @SafeVarargs
    public static AnnotationMethodFilter of(Class<? extends Annotation>... annotations) {
        return new AnnotationMethodFilter(new ArrayList<>(Arrays.asList(annotations)));
    }

    public static AnnotationMethodFilter of(List<Class<? extends Annotation>> annotations) {
        return new AnnotationMethodFilter(annotations);
    }
}
