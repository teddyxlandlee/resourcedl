package xland.ioutils.resourcedl.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xland.ioutils.resourcedl.download.UriHashRule;
import xland.ioutils.resourcedl.hashing.Hasher;
import xland.ioutils.resourcedl.util.IOUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;

public final class ChecksumProcessorWrapper implements IOUtils.IORunnable {
    private final ArrayList<ChecksumProcessor> processors;

    public ChecksumProcessorWrapper(ArrayList<ChecksumProcessor> processors) {
        this.processors = processors;
    }

    @Override
    public void runIo() throws IOException {
        for (ChecksumProcessor cp : processors)
            cp.runIo();
    }

    public static IOUtils.IORunnable checksum(Properties properties, String[] s) {
        UriHashRule rule = UriHashRule.fromDesc(Objects.requireNonNull(properties.getProperty("rule")));
        String rs = properties.getProperty("root");
        Path[] paths = Arrays.stream(s).map(Paths::get).toArray(Path[]::new);
        Hasher hasher = Hasher.getHasher(properties.getProperty("hasher", "sha256"));

        if (rs == null && properties.containsKey("print")) {
            return new Printer(rule, paths, hasher);
        } else {
            ArrayList<ChecksumProcessor> processors = new ArrayList<>(paths.length);
            Path root = Paths.get(Objects.requireNonNull(rs, "root"));
            for (Path p : paths) {
                processors.add(new ChecksumProcessor(
                        root, hasher, rule, p));
            }
            return new ChecksumProcessorWrapper(processors);
        }
    }

    static final class Printer implements IOUtils.IORunnable {
        private final UriHashRule rule;
        private final Path[] paths;
        private final Hasher hasher;

        private static final Logger LOGGER = LoggerFactory.getLogger("ChecksumPrinter");
        private static final Path nulPath = Paths.get("");

        Printer(UriHashRule rule, Path[] paths, Hasher hasher) {
            this.rule = rule;
            this.paths = paths;
            this.hasher = hasher;
        }

        @Override
        public void runIo() throws IOException {
            StringBuilder sb = new StringBuilder("Hashing results:");
            for (Path path : paths) {
                String hash = hasher.hash(Files.newInputStream(path)).toString();
                Path resolved = rule.getFilePath(nulPath, hash);
                sb.append("\n - ").append(path).append("\t=> ").append(resolved);
            }
            LOGGER.info(sb.toString());
        }
    }
}
