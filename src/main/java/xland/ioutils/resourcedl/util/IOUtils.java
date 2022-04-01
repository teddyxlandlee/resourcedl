package xland.ioutils.resourcedl.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

public class IOUtils {
    public static BufferedInputStream buffered(InputStream inputStream) {
        if (inputStream instanceof BufferedInputStream)
            return (BufferedInputStream) inputStream;
        return new BufferedInputStream(inputStream);
    }

    public static void download(URL url, Path path) throws IOException {
        if (Files.exists(path)) throw new FileAlreadyExistsException(path.toString());
        Files.copy(url.openStream(), path);
    }

    /* (null, null) -> null */
    public static OutputStream combine(OutputStream os1, OutputStream os2) {
        if (os1 == null) return os2;
        if (os2 == null) return os1;
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                os1.write(b);
                os2.write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                os1.write(b, off, len);
                os2.write(b, off, len);
            }

            @Override
            public void close() throws IOException {
                os1.close();
                os2.close();
            }

            @Override
            public void flush() throws IOException {
                os1.flush();
                os2.flush();
            }
        };
    }

    public static OutputStream nullOutputStream() {
        return new OutputStream() {
            private volatile boolean closed;

            private void ensureOpen() throws IOException {
                if (closed) {
                    throw new IOException("Stream closed");
                }
            }

            @Override
            public void write(int b) throws IOException {
                ensureOpen();
            }

            private void checkFromIndexSize(int fromIndex, int size, int length) {
                if ((length | fromIndex | size) < 0 || size > length - fromIndex)
                    throw new IndexOutOfBoundsException("len " + length + ", from " + fromIndex +
                            ", size " + size);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                checkFromIndexSize(off, len, b.length);
                ensureOpen();
            }

            @Override
            public void close() {
                closed = true;
            }
        };
    }

    @FunctionalInterface
    public interface IORunnable {
        void runIo() throws IOException;
    }

    @FunctionalInterface
    public interface IOFunction<T, R> {
        R applyIo(T t) throws IOException;
    }
}
