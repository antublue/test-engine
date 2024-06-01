/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

/*
 * Copied from JUnit5 project and modified for the test engine API
 */

package org.antublue.test.engine.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Class to implement a Namespace */
public class Namespace {

    public static final String SEPARATOR = "/";

    private final List<Object> objects;

    /**
     * Constructor
     *
     * @param objects objects
     */
    private Namespace(List<Object> objects) {
        this.objects = objects;
    }

    /**
     * Method to append objects to an existing Namespace, returning a new Namespace
     *
     * @param objects objects
     * @return a new Namespace
     */
    public Namespace append(Object... objects) {
        checkObjects(objects);
        Namespace namespace = of(objects);
        List<Object> list = new ArrayList<>(this.objects.size() + namespace.objects.size());
        list.addAll(this.objects);
        Collections.addAll(list, objects);
        return new Namespace(list);
    }

    /**
     * Method to append to the Namespace, returning a new Namespace
     *
     * @param namespace namespace
     * @return a new Namespace
     */
    public Namespace append(Namespace namespace) {
        if (namespace == null) {
            throw new IllegalArgumentException("namespace is null");
        }
        List<Object> list = new ArrayList<>(this.objects.size() + namespace.objects.size());
        list.addAll(this.objects);
        Collections.addAll(list, objects);
        return new Namespace(list);
    }

    /**
     * Method to create a new Namespace
     *
     * @param objects object
     * @return a Namespace
     */
    public static Namespace of(Object... objects) {
        checkObjects(objects);
        return new Namespace(new ArrayList<>(Arrays.asList(objects)));
    }

    /**
     * Method to check objects parameters
     *
     * @param objects objects
     */
    private static void checkObjects(Object[] objects) {
        if (objects == null) {
            throw new IllegalArgumentException("objects is null");
        }

        if (objects.length == 0) {
            throw new IllegalArgumentException("objects is empty");
        }

        for (int i = 0; i < objects.length; i++) {
            if (objects[i] == null) {
                throw new IllegalArgumentException(
                        "objects contains a null object at index [" + i + "]");
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(SEPARATOR);
        for (Object object : objects) {
            stringBuilder.append(object.toString()).append(SEPARATOR);
        }
        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Namespace namespace = (Namespace) o;
        return Objects.equals(objects, namespace.objects);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(objects);
    }
}
