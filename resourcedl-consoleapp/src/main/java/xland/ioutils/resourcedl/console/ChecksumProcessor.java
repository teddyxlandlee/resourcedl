package xland.ioutils.resourcedl.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xland.ioutils.resourcedl.download.UriHashRule;
import xland.ioutils.resourcedl.hashing.Hasher;
import xland.ioutils.resourcedl.util.IOUtils;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class ChecksumProcessor implements IOUtils.IORunnable {
    /*@Nullable*/ private final Path root;
    private final Hasher hasher;
    private final UriHashRule uriHashRule;
    private final Path originFile;
    private final boolean interactive;

    private final Map<Path, String> hashCache;

    private static final Logger LOGGER = LoggerFactory.getLogger("ChecksumProcessor");

    public ChecksumProcessor(Path root, Hasher hasher, UriHashRule uriHashRule, Path originFile,
                             boolean interactive, Map<Path, String> hashCache) {
        this.root = root;
        this.hasher = hasher;
        this.uriHashRule = uriHashRule;
        this.originFile = originFile;

        this.interactive = interactive;
        this.hashCache = hashCache;
    }

    @Override
    public void runIo() throws IOException {
        String res = hashCache.getOrDefault(originFile, hasher.hash(Files.newInputStream(originFile))
                .toString());
        Path path = uriHashRule.getFilePath(root, res);

        Files.createDirectories(path.getParent());
        copy(path, res);
        if (uriHashRule.hasSubDirectory() && path.endsWith("file")) {
            Path path2 = path.resolveSibling(originFile.getFileName());
            copy(path2, res);
        }
    }

    private void copy(Path target, String res) throws IOException {
        LOGGER.info("Copying {} | {} to {}", originFile, res, target);
        if (Files.exists(target)) {
            boolean skip = this.interactive &&
                    InteractiveManaging.readYesOrNo("File `" + target + "` already exists.\n" +
                            " Skip? [y/N] ", false);
            if (skip) {
                LOGGER.info("Skipped existing file: " + target);
                return;
            } else throw new FileAlreadyExistsException(target.toString());
        }

        Files.copy(originFile, target);
        LOGGER.info("Successfully copied {}", originFile);
    }

    public Path root() { return root; }
    public UriHashRule uriHashRule() { return uriHashRule; }
    public Path originFile() { return originFile; }
    public Hasher hasher() { return hasher; }
}
