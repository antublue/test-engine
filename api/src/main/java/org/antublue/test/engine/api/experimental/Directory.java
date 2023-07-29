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

package org.antublue.test.engine.api.experimental;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

/** Class to implement a TempDir class */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class Directory implements AutoCloseable {

    /** Path type */
    public enum PathType {
        RELATIVE,
        ABSOLUTE
    }

    private final String absolutePath;
    private final PathType pathType;

    /**
     * Constructor
     *
     * @param path path
     */
    private Directory(String path, PathType pathType) throws IOException {
        this.absolutePath = Files.createTempDirectory(path).toFile().getAbsolutePath();
        this.pathType = pathType;
    }

    /** Method to close (delete) the temporary directory */
    public void close() {
        System.out.println(String.format("close [%s]", absolutePath));

        if (absolutePath != null) {
            try {
                delete(new File(absolutePath));
            } catch (IOException e) {
                // DO NOTHING
            }
        }
    }

    @Override
    public String toString() {
        return absolutePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Directory directory = (Directory) o;
        return Objects.equals(absolutePath, directory.absolutePath)
                && pathType == directory.pathType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(absolutePath, pathType);
    }

    private void delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                delete(c);
            }
        }

        if (!f.delete()) {
            throw new FileNotFoundException(String.format("Failed to delete file [%s]", f));
        }
    }

    /**
     * Method to create a directory relative to "java.tmpdir"
     *
     * @param path path
     * @return a TempDir
     * @throws IOException if the temporary directory can't be created
     */
    public static Directory createRelative(String path) throws IOException {
        return create(path, PathType.RELATIVE);
    }

    /**
     * Method to create an absolute directory
     *
     * @param path path
     * @return a TempDir
     * @throws IOException if the temporary directory can't be created
     */
    public static Directory createAbsolution(String path) throws IOException {
        return create(path, PathType.ABSOLUTE);
    }

    /**
     * Method to create a temporary directory
     *
     * @param path path
     * @param pathType pathType
     * @return a TempDir
     * @throws IOException if the temporary directory can't be created
     */
    public static Directory create(String path, PathType pathType) throws IOException {
        return new Directory(path, pathType);
    }
}
