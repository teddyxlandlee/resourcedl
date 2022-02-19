package xland.ioutils.resourcedl.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xland.ioutils.resourcedl.download.UriHashRule;
import xland.ioutils.resourcedl.hashing.Hasher;
import xland.ioutils.resourcedl.util.IOUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ChecksumProcessor implements IOUtils.IORunnable {
    /*@Nullable*/ private final Path root;
    private final Hasher hasher;
    private final UriHashRule uriHashRule;
    private final Path originFile;

    private static final Logger LOGGER = LoggerFactory.getLogger("ChecksumProcessor");

    public ChecksumProcessor(Path root, Hasher hasher, UriHashRule uriHashRule, Path originFile) {
        this.root = root;
        this.hasher = hasher;
        this.uriHashRule = uriHashRule;
        this.originFile = originFile;
    }

    @Override
    public void runIo() throws IOException {
        String res = hasher.hash(Files.newInputStream(originFile))
                .toString();
        Path path = uriHashRule.getFilePath(root, res);
        if (uriHashRule.hasSubDirectory()) {
            Path path2 = path.resolveSibling(originFile.getFileName());
            LOGGER.info("Copying {} | {} to {}", originFile, res, path2);
            Files.createFile(path2);
            Files.copy(originFile, path2);
            LOGGER.info("Successfully copied {}", originFile);
        }
        LOGGER.info("Copying {} | {} to {}", originFile, res, path);
        Files.copy(originFile, path);
        LOGGER.info("Successfully copied {}", originFile);
    }

    public Path root() { return root; }
    public UriHashRule uriHashRule() { return uriHashRule; }
    public Path originFile() { return originFile; }
    public Hasher hasher() { return hasher; }
}
