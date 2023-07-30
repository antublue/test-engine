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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

/** Class to implement a Directory class */
public class Directory implements AutoCloseable {

    private static final String JAVA_TMPDIR = System.getenv("java.io.tmpdir");

    /** Path type */
    public enum PathType {
        /** Relative path to "java.io.tmpdir" */
        RELATIVE,
        /** Absolute path */
        ABSOLUTE
    }

    private final String path;
    private final PathType pathType;
    private final File file;

    /**
     * Constructor
     *
     * @param path path
     */
    private Directory(String path, PathType pathType) throws IOException {
        if (pathType == PathType.ABSOLUTE) {
            this.file = new File(path).getAbsoluteFile();
        } else {
            this.file = new File(JAVA_TMPDIR + path).getAbsoluteFile();
        }

        Files.createDirectory(this.file.toPath());

        this.path = this.file.getAbsolutePath();
        this.pathType = pathType;
    }

    /**
     * Method to get a File
     *
     * @return a File
     */
    public File getFile() {
        return file;
    }

    /** Method to close (delete) the directory */
    public void close() {
        if (path != null) {
            try {
                delete(new File(path));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        return file.getAbsolutePath();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Directory directory = (Directory) o;
        return Objects.equals(path, directory.path)
                && pathType == directory.pathType
                && Objects.equals(file, directory.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, pathType, file);
    }

    private void delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                delete(c);
            }
        }

        if (!f.delete()) {
            throw new IOException(String.format("Failed to delete file [%s]", f));
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
