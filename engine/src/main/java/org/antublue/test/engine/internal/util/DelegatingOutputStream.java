/*
 * Copyright 2022-2023 Douglas Hoard
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DelegatingOutputStream extends OutputStream {

    private final OutputStream outputStream;
    private final FileOutputStream fileOutputStream;

    public DelegatingOutputStream(OutputStream outputStream, FileOutputStream fileOutputStream) {
        this.outputStream = outputStream;
        this.fileOutputStream = fileOutputStream;
    }

    public void close() throws IOException {
        outputStream.close();
        synchronized (fileOutputStream) {
            fileOutputStream.close();
        }
    }

    public void flush() throws IOException {
        outputStream.flush();
        synchronized (fileOutputStream) {
            fileOutputStream.flush();
        }
    }

    @Override
    public void write(int b) throws IOException {
        outputStream.write(b);
        synchronized (fileOutputStream) {
            fileOutputStream.write(b);
            fileOutputStream.flush();
        }
    }
}
