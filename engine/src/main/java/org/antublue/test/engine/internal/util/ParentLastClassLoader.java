/*
 * Copyright (C) 2024 The AntuBLUE test-engine project authors
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

package org.antublue.test.engine.internal.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Set;

/** Class to implement ParentLastClassLoader */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class ParentLastClassLoader extends ClassLoader {

    private final ClassLoader parent;
    private final Set<String> parentClassNames;

    /**
     * Constructor
     *
     * @param parent parent
     */
    public ParentLastClassLoader(ClassLoader parent) {
        this(parent, null);
    }

    /**
     * Constructor
     *
     * @param parent parent
     * @param pareClassNames parentClassNames
     */
    public ParentLastClassLoader(ClassLoader parent, Set<String> pareClassNames) {
        super(parent);
        this.parent = parent;
        this.parentClassNames = pareClassNames;
    }

    /**
     * Method to add a class that will be loaded by the parent class loader
     *
     * @param className className
     * @return this
     */
    public ParentLastClassLoader addParentClass(String className) {
        parentClassNames.add(className);
        return this;
    }

    /**
     * Method to add a collection of classes that will be loaded by the parent class loader
     *
     * @param classNames classNames
     * @return this
     */
    public ParentLastClassLoader addParentClass(Collection<String> classNames) {
        parentClassNames.addAll(classNames);
        return this;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (parentClassNames.contains(name)) {
            return parent.loadClass(name);
        }

        try {
            Class<?> loadedClass = findClass(name);
            if (loadedClass != null) {
                return loadedClass;
            }
        } catch (ClassNotFoundException e) {
            // DO NOTHING
        }

        return super.loadClass(name);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String classFileName = name.replace('.', '/') + ".class";
        InputStream classDataStream = getResourceAsStream(classFileName);

        if (classDataStream == null) {
            throw new ClassNotFoundException("Class not found: " + name);
        }

        try {
            byte[] classData = new byte[classDataStream.available()];
            classDataStream.read(classData);

            return defineClass(name, classData, 0, classData.length);
        } catch (IOException e) {
            throw new ClassNotFoundException("Failed to load class: " + name, e);
        }
    }

    @Override
    public URL getResource(String name) {
        URL url = findResource(name);
        if (url == null && parent != null) {
            url = parent.getResource(name);
        }
        return url;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> childResources = findResources(name);
        Enumeration<URL> parentResources = parent != null ? parent.getResources(name) : null;

        if (parentResources == null) {
            return childResources;
        }

        return new Enumeration<URL>() {
            @Override
            public boolean hasMoreElements() {
                return childResources.hasMoreElements() || parentResources.hasMoreElements();
            }

            @Override
            public URL nextElement() {
                if (childResources.hasMoreElements()) {
                    return childResources.nextElement();
                }
                return parentResources.nextElement();
            }
        };
    }
}
