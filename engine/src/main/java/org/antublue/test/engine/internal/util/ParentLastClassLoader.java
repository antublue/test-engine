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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;
import java.util.function.Predicate;

import static java.lang.String.format;

/** Class to implement ParentLastClassLoader */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class ParentLastClassLoader extends ClassLoader {

    private final ClassLoader parent;
    private final Set<String> parentClassNames;
    private final Predicate<String> parentClassNamesPredicate;

    /**
     * Constructor
     *
     * @param parent parent
     */
    public ParentLastClassLoader(ClassLoader parent) {
        super(parent);
        this.parent = parent;
        this.parentClassNames = null;
        this.parentClassNamesPredicate = null;
    }

    /**
     * Constructor
     *
     * @param parent parent
     * @param parentClassNames parentClassNames
     */
    public ParentLastClassLoader(ClassLoader parent, Set<String> parentClassNames) {
        super(parent);
        this.parent = parent;
        this.parentClassNames = parentClassNames;
        this.parentClassNamesPredicate = null;
    }

    /**
     * Constructor
     *
     * @param parent parent
     * @param parentClassNamesPredicate parentClassNamesPredicate
     */
    public ParentLastClassLoader(ClassLoader parent, Predicate<String> parentClassNamesPredicate) {
        super(parent);
        this.parent = parent;
        this.parentClassNames = null;
        this.parentClassNamesPredicate = parentClassNamesPredicate;
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        if (parentClassNames != null && parentClassNames.contains(className)) {
            return parent.loadClass(className);
        } else if (parentClassNamesPredicate != null && parentClassNamesPredicate.test(className)) {
            return parent.loadClass(className);
        }

        try {
            Class<?> loadedClass = findClass(className);
            if (loadedClass != null) {
                return loadedClass;
            }
        } catch (ClassNotFoundException e) {
            // DO NOTHING
        }

        return super.loadClass(className);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String classFileName = name.replace('.', '/') + ".class";
        InputStream inputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;

        try {
            inputStream = getResourceAsStream(classFileName);
            if (inputStream == null) {
                throw new ClassNotFoundException("Failed to load class: " + name);
            }

            byteArrayOutputStream = new ByteArrayOutputStream();

            byte[] bytes = new byte[1024 * 100];
            int bytesRead;

            while ((bytesRead = inputStream.read(bytes, 0, bytes.length)) != -1) {
                byteArrayOutputStream.write(bytes, 0, bytesRead);
            }

            byte[] classBytes = byteArrayOutputStream.toByteArray();
            
            return defineClass(name, classBytes, 0, classBytes.length);
        } catch (IOException e) {
            throw new ClassNotFoundException("Failed to load class: " + name, e);
        } finally {
            try {
                inputStream.close();
            } catch (Throwable t) {
                // DO NOTHING
            }
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
