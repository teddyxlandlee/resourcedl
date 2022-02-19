package xland.ioutils.resourcedl.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    @FunctionalInterface
    public interface IORunnable {
        void runIo() throws IOException;
    }

    @FunctionalInterface
    public interface IOFunction<T, R> {
        R applyIo(T t) throws IOException;
    }

    public interface IOBiFunction<T, U, R> {
        R applyIo(T t, U u) throws IOException;
    }
}
