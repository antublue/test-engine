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

package org.antublue.test.engine.extras;

import static java.lang.String.format;

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

    /** Method to delete the directory */
    public void delete() {
        if (path != null) {
            try {
                delete(path);
            } catch (IOException e) {
                e.printStackTrace(System.out);
                System.out.flush();
            }
        }
    }

    /** Method to close (delete) the directory */
    @Override
    public void close() {
        delete();
    }

    /**
     * Method to create a directory relative to "java.tmpdir"
     *
     * @param path path
     * @return a TempDir
     * @throws IOException if the directory can't be created
     */
    public static Directory createRelative(String path) throws IOException {
        checkNotNullOrEmpty(path, "path is null", "path is empty");
        return create(path, PathType.RELATIVE);
    }

    /**
     * Method to create an absolute directory
     *
     * @param path path
     * @return a TempDir
     * @throws IOException if the directory can't be created
     */
    public static Directory createAbsolute(String path) throws IOException {
        checkNotNullOrEmpty(path, "path is null", "path is empty");
        return create(path, PathType.ABSOLUTE);
    }

    /**
     * Method to create a temporary directory
     *
     * @param path path
     * @param pathType pathType
     * @return a TempDir
     * @throws IOException if the directory can't be created
     */
    public static Directory create(String path, PathType pathType) throws IOException {
        checkNotNullOrEmpty(path, "path is null", "path is empty");
        return new Directory(path, pathType);
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
                && Objects.equals(file.getAbsolutePath(), directory.file.getAbsolutePath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, pathType, file);
    }

    /**
     * Method to delete a directory
     *
     * @param path path
     * @throws IOException if the directory can't be deleted
     */
    private void delete(String path) throws IOException {
        File f = new File(path);
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null) {
                for (File c : files) {
                    delete(c.getAbsolutePath());
                }
            }
        }

        if (!f.delete()) {
            throw new IOException(format("Failed to delete file [%s]", f));
        }
    }

    /**
     * Method to validate a value is not null and not empty
     *
     * @param string string
     * @param nullMessage nullMessage
     * @param emptyMessage emptyMessage
     */
    private static void checkNotNullOrEmpty(
            String string, String nullMessage, String emptyMessage) {
        if (string == null) {
            throw new IllegalArgumentException(nullMessage);
        }
        if (string.trim().equalsIgnoreCase("")) {
            throw new IllegalArgumentException(emptyMessage);
        }
    }
}
