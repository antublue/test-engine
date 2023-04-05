package org.antublue.test.engine.internal.predicate;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.internal.TestEngineException;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class TestMethodTagPredicate extends RegexPredicate<Method> {

    private TestMethodTagPredicate(String regex) {
        super(regex);
    }

    @Override
    public boolean test(Method method) {
        if (!method.isAnnotationPresent(TestEngine.Tag.class)) {
            return false;
        }

        try {
            Annotation annotation = method.getAnnotation(TestEngine.Tag.class);
            Class<? extends Annotation> type = annotation.annotationType();
            Method valueMethod = type.getDeclaredMethod("value", (Class<?>[]) null);
            String tag = valueMethod.invoke(annotation, (Object[]) null).toString();
            return matcher.reset(tag).find();
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new TestEngineException(String.format("Invalid @TestEngine.Tag configuration", e));
        }
    }

    public static TestMethodTagPredicate of(String regex) {
        return new TestMethodTagPredicate(regex);
    }
}
