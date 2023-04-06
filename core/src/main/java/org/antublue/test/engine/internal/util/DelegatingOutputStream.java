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
            fileOutputStream.flush();;
        }
    }
}
