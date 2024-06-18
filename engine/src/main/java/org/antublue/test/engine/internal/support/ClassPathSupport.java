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

package org.antublue.test.engine.internal.support;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/** Class to implement ClassPathURIUtils */
public class ClassPathSupport {

    private static final ReentrantLock LOCK = new ReentrantLock(true);
    private static List<URI> URIS;

    /** Constructor */
    private ClassPathSupport() {
        // DO NOTHING
    }

    /**
     * Method to get a Set of classpath URIs
     *
     * @return a List of classpath URIs
     */
    public static List<URI> getClasspathURIs() {
        try {
            LOCK.lock();

            if (URIS == null) {
                Set<URI> uriSet = new LinkedHashSet<>();

                String classpath = System.getProperty("java.class.path");

                String[] paths = classpath.split(File.pathSeparator);
                for (String path : paths) {
                    URI uri = new File(path).toURI();
                    uriSet.add(uri);
                }

                URIS = new ArrayList<>(uriSet);
            }

            return URIS;
        } finally {
            LOCK.unlock();
        }
    }
}
