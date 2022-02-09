package xland.test.ioutils.resourcedl;

import org.apache.commons.codec.cli.Digest;

import java.io.IOException;

public class CommonsCLIHashingTest {
    public static void main(String[] args) throws IOException {
        run("ALL", "Hello");
    }

    private static void run(String... args) throws IOException {
        Digest.main(args);
    }
}
